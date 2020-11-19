package pl.michnam.app.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.analysis.AreaAnalysis;
import pl.michnam.app.core.service.MainService;
import pl.michnam.app.core.service.ServiceCallbacks;
import pl.michnam.app.util.Tag;

public class WifiScan {
    public static void startWifiScan(Context context, ServiceCallbacks serviceCallbacks) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean noError = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (noError) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    handleScanResults(results, serviceCallbacks, context);
                } else Log.w(Tag.WIFI, "WIFI - error while receiving scan results");
                if (MainService.isWorking()) scanLoop(context);
                else {
                    Log.i(Tag.WIFI, "WIFI - Stopping Scan");
                    context.unregisterReceiver(this);
                    AreaAnalysis.getInstance().disable(context);
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
                        if (!successfulScan) Log.i(Tag.WIFI, "WIFI - Error while starting scanning");
                    }
                },
                AppConfig.wifiScanWaitTime
        );

    }

    private static void handleScanResults(List<ScanResult> results, ServiceCallbacks serviceCallbacks, Context context) {
        AreaAnalysis.getInstance().updateLocation(results, context, serviceCallbacks);
        //if (serviceCallbacks != null) serviceCallbacks.setDebugMessage("");
    }

    public static String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }
}
