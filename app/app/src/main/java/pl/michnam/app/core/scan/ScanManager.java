package pl.michnam.app.core.scan;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.TimerTask;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.analysis.AreaAnalysis;
import pl.michnam.app.http.RequestManager;
import pl.michnam.app.core.service.MainService;
import pl.michnam.app.core.service.ServiceCallbacks;
import pl.michnam.app.util.Pref;

public class ScanManager {
    public static void startScanning(Context context, ServiceCallbacks serviceCallbacks) {
        WifiScan.startWifiScan(context);
        BleScan.startBleScan(context);
        analyzeLoop(context, serviceCallbacks);
    }

    private static void analyzeLoop(Context context, ServiceCallbacks serviceCallbacks) {
        SharedPreferences sharedPref = context.getSharedPreferences(Pref.prefFile, Context.MODE_PRIVATE);
        boolean activeMode = sharedPref.getBoolean(Pref.activeMode, false);
        if (activeMode) {
            RequestManager requestManager = new RequestManager(context);
            requestManager.handleExcludedDevices();
            requestManager.handleHotspotData();
        }
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (MainService.isWorking()) {
                    AreaAnalysis.getInstance().updateLocation(context, serviceCallbacks);
                    analyzeLoop(context, serviceCallbacks);
                }
            }
        }, AppConfig.apiRequestWaitTime);
    }
}
