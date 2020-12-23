package pl.michnam.app.core.scan;

import android.content.Context;

import java.util.TimerTask;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.analysis.AreaAnalysis;
import pl.michnam.app.core.http.RequestManager;
import pl.michnam.app.core.service.MainService;
import pl.michnam.app.core.service.ServiceCallbacks;

public class ScanManager {
    public static void startScanning(Context context, ServiceCallbacks serviceCallbacks) {
        WifiScan.startWifiScan(context);
        BleScan.startBleScan(context);
        apiLoop(context, serviceCallbacks);
    }

    private static void apiLoop(Context context, ServiceCallbacks serviceCallbacks) {

        RequestManager requestManager = new RequestManager(context);
        requestManager.handleExcludedDevices();
        requestManager.handleHotspotData();
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (MainService.isWorking()) {
                    AreaAnalysis.getInstance().updateLocation(context, serviceCallbacks);
                    apiLoop(context, serviceCallbacks);
                }
            }
        }, AppConfig.apiRequestWaitTime);
    }
}
