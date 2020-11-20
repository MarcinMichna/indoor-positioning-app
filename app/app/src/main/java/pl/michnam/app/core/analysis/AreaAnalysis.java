package pl.michnam.app.core.analysis;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pl.michnam.app.R;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.activity.MainActivity;
import pl.michnam.app.core.service.ServiceCallbacks;
import pl.michnam.app.sql.entity.AreaData;
import pl.michnam.app.util.Tag;

import static pl.michnam.app.App.CHANNEL_ID;

public class AreaAnalysis {
    private HashMap<String, ArrayList<AreaData>> areas = new HashMap<>();
    private final ArrayList<ScanResult> currentRes = new ArrayList<>();
    private String currentArea = "";


    public void updateAreas(HashMap<String, ArrayList<AreaData>> areaData) {
        this.areas = areaData;
        Log.i(Tag.CORE, "Updated areas list");
    }

    public void disable(Context context) {
        currentArea = "";
        updateNotificationArea(context.getString(R.string.defaultNotificationText), context);
    }

    public void updateLocation(List<ScanResult> newResults, Context context, ServiceCallbacks callbacks) {
        HashMap<String, ArrayList<Integer>> strengthList = new HashMap<>();
        HashMap<String, Double> avgStrength = new HashMap<>();
        HashMap<String, Integer> matchesInAreas = new HashMap<>();

        // update list of locations to have only recent values
        Date date = new Date();
        long scanTimestamp;
        long actualTimestamp;
        currentRes.addAll(newResults);
        for (int i = currentRes.size() - 1; i > 0; i--) {
            scanTimestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + (currentRes.get(i).timestamp / 1000);
            actualTimestamp = date.getTime();
            if (actualTimestamp > scanTimestamp + AppConfig.maxScanAge) currentRes.remove(i);
        }

        if (AppConfig.minNumberOfSignalsToAnalyse < currentRes.size()) {
            // set matches in all areas to 0
            for (String key : areas.keySet()) {
                matchesInAreas.put(key, 0);
            }


            // calculate avg signal strength
            for (ScanResult i : currentRes) {
                if (strengthList.containsKey(i.SSID))
                    strengthList.get(i.SSID).add(i.level);
                else strengthList.put(i.SSID, new ArrayList<>(Collections.singletonList(i.level)));
            }

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

            //DEBUG
            if (callbacks != null) {
                StringBuilder info = new StringBuilder();
                for (String area : matchesInAreas.keySet()) {
                        info.append(area).append(": ");
                        info.append(matchesInAreas.get(area));
                        info.append("\n");
                        callbacks.setDebugMessage(info.toString());
                    //Log.d(Tag.ANALYZE, area + ": " + matchesInAreas.get(area));
                }
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
    private AreaAnalysis() {
    }

    private static class Holder {
        private static final AreaAnalysis instance = new AreaAnalysis();
    }

    public static AreaAnalysis getInstance() {
        return Holder.instance;
    }

}
