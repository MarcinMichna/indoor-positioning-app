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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pl.michnam.app.R;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.activity.MainActivity;
import pl.michnam.app.core.service.ServiceCallbacks;
import pl.michnam.app.sql.entity.AreaData;
import pl.michnam.app.util.Pref;
import pl.michnam.app.util.Tag;

import static pl.michnam.app.App.CHANNEL_ID;

public class AreaAnalysis {
    private HashMap<String, ArrayList<AreaData>> areas = new HashMap<>();
    private final ArrayList<ScanResult> currentResWifi = new ArrayList<>();
    private final ArrayList<android.bluetooth.le.ScanResult> currentResBle = new ArrayList<>();

    private ArrayList<String> excludedWifi = new ArrayList<>();
    private ArrayList<String> excludedBt = new ArrayList<>();
    private String currentArea = "";


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

    public synchronized void updateLocation(List<ScanResult> newResults, Context context, ServiceCallbacks callbacks) {
        HashMap<String, ArrayList<Integer>> strengthList = new HashMap<>();
        HashMap<String, Double> avgStrength = new HashMap<>();
        HashMap<String, Integer> matchesInAreas = new HashMap<>();

        // update list of results to have only recent values
        Date date = new Date();
        long scanTimestamp;
        long actualTimestamp;
        currentResWifi.addAll(newResults);

        SharedPreferences sharedPref = context.getSharedPreferences(Pref.prefFile, Context.MODE_PRIVATE);
        int maxScanAge = sharedPref.getInt(Pref.scanAge, AppConfig.maxScanAge);

        //wifi
        for (int i = currentResWifi.size() - 1; i > 0; i--) {
            scanTimestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + (currentResWifi.get(i).timestamp / 1000);
            actualTimestamp = date.getTime();
            if (actualTimestamp > scanTimestamp + maxScanAge) currentResWifi.remove(i);
        }

        //ble
        for (int i = currentResBle.size() - 1; i > 0; i--) {
            scanTimestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + (currentResBle.get(i).getTimestampNanos() / 1000000);
            actualTimestamp = date.getTime();
            if (actualTimestamp > scanTimestamp + maxScanAge) currentResBle.remove(i);
        }

        // analyze if there is enough data
        if (AppConfig.minNumberOfSignalsToAnalyse < currentResWifi.size() + currentResBle.size()) {
            // set matches in all areas to 0
            for (String key : areas.keySet()) {
                matchesInAreas.put(key, 0);
            }


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




            // set number of matching ranges
            for (String area : areas.keySet()) {
                ArrayList<AreaData> areaInfo = areas.get(area);
                for (int i = 0; i < areaInfo.size(); i++) {
                    AreaData singleInfo = areaInfo.get(i);
                    if (avgStrength.containsKey(singleInfo.getName())) {
                        double avgStr = avgStrength.get(singleInfo.getName());
                        if (avgStr > singleInfo.getMinRssi() && avgStr < singleInfo.getMaxRssi()) {
                            matchesInAreas.put(area, matchesInAreas.get(area) + 1);
                        }
                    }
                }
            }



            // find best matching area
            int max = 0;
            String bestAreaMatch = "";
            for (String key : matchesInAreas.keySet()) {
                if (matchesInAreas.get(key) > max) {
                    max = matchesInAreas.get(key);
                    bestAreaMatch = key;
                }
            }


            // if area changed, update notification
            if (!currentArea.equals(bestAreaMatch)) {
                currentArea = bestAreaMatch;
                Log.i(Tag.ANALYZE, "Entered new area: " + bestAreaMatch);
                updateNotificationArea(context.getString(R.string.notif_in_area) + bestAreaMatch, context);
            }

            if (callbacks != null) {

                ArrayList<Pair<String, Integer>> resUnordered = new ArrayList<>();
                for(String area: matchesInAreas.keySet())
                    resUnordered.add(new Pair<>(area, matchesInAreas.get(area)));

                resUnordered.sort(Comparator.comparing(p -> -p.second));

                ArrayList<String> res = new ArrayList<>();
                for (Pair<String, Integer> item : resUnordered)
                    res.add(item.first + ": " + item.second);

                callbacks.setResults(res);
                callbacks.setCurrentArea(bestAreaMatch);
                callbacks.setExcludedDevices(new ArrayList<>());
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

    /////////////////////
    ///// SINGLETON /////
    /////////////////////
    private AreaAnalysis() { }

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
}
