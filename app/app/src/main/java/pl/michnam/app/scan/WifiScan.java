package pl.michnam.app.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.service.MainService;
import pl.michnam.app.service.ServiceCallbacks;
import pl.michnam.app.util.Tag;

public class WifiScan {

    public static void setupWifiScan(Context context, ServiceCallbacks serviceCallbacks) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean noError = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (noError) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    handleScanResults(results, serviceCallbacks);
                } else Log.w(Tag.WIFI, "WIFI - error while receiving scan results");
                if (MainService.isWorking()) scanLoop(context);
                else {
                    Log.i(Tag.WIFI, "WIFI - Stopping Scan");
                    context.unregisterReceiver(this);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        Log.i(Tag.WIFI, "WIFI - Strating scan");
        if (MainService.isWorking()) scanLoop(context);
    }

    private static void scanLoop(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        boolean successfulScan = wifiManager.startScan(); // wifiScanReceiver.onReceive after scan
                        if (!successfulScan) Log.i(Tag.WIFI, "WIFI - Scan while starting scanning");
                    }
                },
                AppConfig.wifiScanWaitTime
        );

    }

    private static void handleScanResults(List<ScanResult> results, ServiceCallbacks serviceCallbacks) {
        StringBuilder info = new StringBuilder();
        for (ScanResult i : results) {
            info.append("SSID: ").append(i.SSID).append(", RSSI: ").append(i.level).append("\n");
            //Log.v(Tag.WIFI, "SSID: " + i.SSID + ", RSSI: " + i.level);
        }
        if (serviceCallbacks != null) serviceCallbacks.setDebugMessage(info.toString());
        Log.v(Tag.WIFI, "WIFI - Scan successful, got " + results.size() + " results");
    }
}
