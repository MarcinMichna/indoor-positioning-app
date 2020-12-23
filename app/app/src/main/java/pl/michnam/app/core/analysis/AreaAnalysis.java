package pl.michnam.app.core.analysis;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.SystemClock;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.NotificationCompat;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.michnam.app.R;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.activity.MainActivity;
import pl.michnam.app.core.http.RequestManager;
import pl.michnam.app.core.http.model.HotspotResult;
import pl.michnam.app.core.service.ServiceCallbacks;
import pl.michnam.app.sql.entity.AreaData;
import pl.michnam.app.sql.entity.HotspotData;
import pl.michnam.app.util.MathCalc;
import pl.michnam.app.util.Pref;
import pl.michnam.app.util.Tag;

import static pl.michnam.app.App.CHANNEL_ID;

public class AreaAnalysis {
    private HashMap<String, ArrayList<AreaData>> areas = new HashMap<>();
    private HashMap<String, ArrayList<HotspotData>> areasHotspot = new HashMap<>();

    private final ArrayList<ScanResult> currentResWifi = new ArrayList<>();
    private final ArrayList<android.bluetooth.le.ScanResult> currentResBle = new ArrayList<>();

    private ArrayList<HotspotResult> hotspotData = new ArrayList<>();
    private ArrayList<String> excludedWifi = new ArrayList<>();
    private ArrayList<String> excludedBt = new ArrayList<>();
    private ArrayList<String> resultProbabilities = new ArrayList<>();

    private String currentArea = "";
    boolean noResults = false;

    public synchronized void updateAreas(HashMap<String, ArrayList<AreaData>> areaData) {
        this.areas = areaData;
        Log.i(Tag.CORE, "Updated areas list");
    }

    public synchronized void disable(Context context) {
        currentArea = "";
        updateNotificationArea(context.getString(R.string.defaultNotificationText), context);
    }

    public synchronized void addResBle(android.bluetooth.le.ScanResult res) {
        currentResBle.add(res);
    }

    public synchronized void addWifiResults(List<ScanResult> newResults) {
        currentResWifi.addAll(newResults);
    }

    public synchronized void updateLocation(Context context, ServiceCallbacks callbacks) {
        HashMap<String, ArrayList<Integer>> strengthList = new HashMap<>();
        HashMap<String, Double> avgStrength = new HashMap<>();

        boolean nullCallback = false;
        if (callbacks == null) nullCallback = true;


        // update list of results to have only recent values
        Date date = new Date();
        long scanTimestamp;
        long actualTimestamp;


        SharedPreferences sharedPref = context.getSharedPreferences(Pref.prefFile, Context.MODE_PRIVATE);
        int maxScanAge = sharedPref.getInt(Pref.scanAge, AppConfig.maxScanAge);
        boolean activeMode = sharedPref.getBoolean(Pref.activeMode, false);
        int thresholdFitting = sharedPref.getInt(Pref.fittingThreshold, -1);

        //wifi age filtering
        for (int i = currentResWifi.size() - 1; i >= 0; i--) {
            scanTimestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + (currentResWifi.get(i).timestamp / 1000);
            actualTimestamp = date.getTime();
            if (actualTimestamp > scanTimestamp + maxScanAge) currentResWifi.remove(i);
        }

        //ble age filtering
        for (int i = currentResBle.size() - 1; i >= 0; i--) {
            scanTimestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + (currentResBle.get(i).getTimestampNanos() / 1000000);
            actualTimestamp = date.getTime();
            if (actualTimestamp > scanTimestamp + maxScanAge) currentResBle.remove(i);
        }

        Log.d(Tag.ANALYZE, "Starting analyze with hotspotData size: " + hotspotData.size() +", wifi results size: " + currentResWifi.size() + ", ble result size: " + currentResBle.size() + ", excludedDevices: " + excludedWifi.toString() + excludedBt.toString() + ", callback: " + !nullCallback + ", active: " + activeMode);

        noResults = false;
        if (hotspotData.size() == 0 && currentResWifi.size() == 0 && currentResBle.size() == 0) {
            if (!noResults) updateNotificationArea(context.getString(R.string.no_scan_results), context);
            noResults = true;
        }
        else {
            noResults = false;

            // create list of rssi for wifi
            for (ScanResult i : currentResWifi) {
                if (strengthList.containsKey(i.SSID))
                    strengthList.get(i.SSID).add(i.level);
                else strengthList.put(i.SSID, new ArrayList<>(Collections.singletonList(i.level)));
            }

            // create list of rssi for ble
            for (android.bluetooth.le.ScanResult i : currentResBle) {
                String id;
                if (i.getDevice().getName() != null) id = i.getDevice().getName();
                else id = i.getDevice().getAddress();

                if (strengthList.containsKey(id))
                    strengthList.get(id).add(i.getRssi());
                else strengthList.put(id, new ArrayList<>(Collections.singletonList(i.getRssi())));
            }

            // calculate avg signal strength
            for (String key : strengthList.keySet()) {
                avgStrength.put(key, calculateAverage(strengthList.get(key)));
            }


            HashMap<String, ArrayList<Integer>> hotspotRssiPerEsp = new HashMap<>();
            HashMap<String, Double> hotspotAvgRssiPerEsp = new HashMap<>();

            if (activeMode) {
                for (HotspotResult result : hotspotData) {
                    if (hotspotRssiPerEsp.containsKey(result.getEsp()))
                        hotspotRssiPerEsp.get(result.getEsp()).add(result.getRssi());
                    else
                        hotspotRssiPerEsp.put(result.getEsp(), new ArrayList<>(Collections.singletonList(result.getRssi())));
                }

                for (Map.Entry<String, ArrayList<Integer>> entry : hotspotRssiPerEsp.entrySet()) {
                    hotspotAvgRssiPerEsp.put(entry.getKey(), calculateAverage(entry.getValue()));
                }
            }


            HashMap<String, Double> probabilityPerArea = new HashMap<>();


            for (Map.Entry<String, ArrayList<AreaData>> area : areas.entrySet()) {
                Log.v(Tag.ANALYZE, "Area " + area.getKey());
                ArrayList<Double> distributions = new ArrayList<>();
                NormalDistribution dist;
                for (AreaData device : area.getValue()) {
                    if (avgStrength.containsKey(device.getName())) {
                        if (!excludedWifi.contains(device.getName()) && !excludedBt.contains(device.getName())) {
                            dist = new NormalDistribution(device.getAvg(), device.getSd());
                            double currentSignal = avgStrength.get(device.getName());
                            distributions.add(dist.cumulativeProbability(currentSignal));
                        }
                    }
                }

                if (activeMode) {
                    if (areasHotspot.containsKey(area.getKey())) {

                        for (HotspotData device: areasHotspot.get(area.getKey())) {
                            if (hotspotAvgRssiPerEsp.containsKey(device.getEsp())) {
                                dist = new NormalDistribution(device.getAvg(), device.getSd());
                                double currentSignal = hotspotAvgRssiPerEsp.get(device.getEsp());
                                distributions.add(dist.cumulativeProbability(currentSignal));
                            }
                        }

                    }
                }



                ArrayList<Double> diffToCenter = new ArrayList<>();
                for (double distribution : distributions)
                    diffToCenter.add(Math.abs(distribution - 0.5));

                double avgDiffToCenter = MathCalc.averageListDouble(diffToCenter);

                //double avgDistribution = MathCalc.averageListDouble(distributions);
                probabilityPerArea.put(area.getKey(), avgDiffToCenter);

                //Log.i(Tag.ANALYZE, "Distributions in " + area.getKey() + ": " + distributions.toString());
            }
            //Log.i(Tag.ANALYZE, "Avg probability: " + probabilityPerArea.toString());


            double min = 1;  // all values are < 0
            String bestAreaMatch = "";
            for (Map.Entry<String, Double> area : probabilityPerArea.entrySet()) {
                if (area.getValue() < min) {
                    min = area.getValue();
                    bestAreaMatch = area.getKey();
                }
            }

            double normalizeResult = Math.round(100 - (min * 100) * 2);
            if (normalizeResult < thresholdFitting) {
                bestAreaMatch = "";
            }

            if (!currentArea.equals(bestAreaMatch)) {
                currentArea = bestAreaMatch;
                Log.i(Tag.ANALYZE, "Entered new area: " + bestAreaMatch);
                updateNotificationArea(context.getString(R.string.notif_in_area) + bestAreaMatch, context);
            }

            ArrayList<Pair<String, Double>> resUnordered = new ArrayList<>();

            for (Map.Entry<String, Double> area : probabilityPerArea.entrySet()) {
                resUnordered.add(new Pair<>(area.getKey(), area.getValue()));
            }

            resUnordered.sort(Comparator.comparing(p -> p.second));

            resultProbabilities = new ArrayList<>();
            for (Pair<String, Double> item : resUnordered)
                resultProbabilities.add(item.first + ": " + Math.round(100 - (item.second * 100) * 2)  + "%");
            Log.d(Tag.ANALYZE, "Analyze result probabilites: " + resultProbabilities);

            if (callbacks != null) {


                callbacks.setResults(resultProbabilities);
                callbacks.setCurrentArea(bestAreaMatch);
                ArrayList<String> excludedAll = new ArrayList<>();
                excludedAll.addAll(excludedWifi);
                excludedAll.addAll(excludedBt);
                callbacks.setExcludedDevices(excludedAll);
            }
        }


    }

    private void updateNotificationArea(String msg, Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(msg)
                .setSmallIcon(R.drawable.flag)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(AppConfig.mainNotificationId, notification);
    }

    private double calculateAverage(List<Integer> marks) {
        Integer sum = 0;
        if (!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    public void setHotspotData(ArrayList<HotspotResult> hotspotData) {
        this.hotspotData = hotspotData;
    }





    /////////////////////
    ///// SINGLETON /////
    /////////////////////
    private AreaAnalysis() {
    }

    private static class Holder {
        private static final AreaAnalysis instance = new AreaAnalysis();
    }

    public static AreaAnalysis getInstance() {
        return Holder.instance;
    }


    public ArrayList<String> getExcludedWifi() {
        return excludedWifi;
    }

    public synchronized void setExcludedWifi(ArrayList<String> excludedWifi) {
        this.excludedWifi = excludedWifi;
    }

    public ArrayList<String> getExcludedBt() {
        return excludedBt;
    }

    public synchronized void setExcludedBt(ArrayList<String> excludedBt) {
        this.excludedBt = excludedBt;
    }

    public HashMap<String, ArrayList<HotspotData>> getAreasHotspot() {
        return areasHotspot;
    }

    public synchronized void setAreasHotspot(HashMap<String, ArrayList<HotspotData>> areasHotspot) {
        this.areasHotspot = areasHotspot;
    }

    public String getCurrentArea() {
        return currentArea;
    }

    public ArrayList<String> getResultProbabilities() {
        return resultProbabilities;
    }
}
