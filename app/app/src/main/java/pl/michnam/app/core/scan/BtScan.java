package pl.michnam.app.core.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.analysis.AreaAnalysis;
import pl.michnam.app.core.service.MainService;
import pl.michnam.app.util.Tag;

public class BtScan {
    private static BluetoothAdapter btAdapter;
    private static BroadcastReceiver mReceiver;

    public static void startScan(Context context) {
        Log.i(Tag.BT, "BT - Starting scan");
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    //Log.d(Tag.BT, "Bt name: " + device.getName() + ", addr: " + device.getAddress() + ", rssi: " + rssi);
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        btScanLoop();
    }

    private static void btScanLoop() {
        btAdapter.startDiscovery();
        new Handler().postDelayed(() -> {
            btAdapter.cancelDiscovery();
            if (MainService.isWorking()) {
                new Handler().postDelayed(BtScan::btScanLoop, AppConfig.btScanWaitTime);
            }
        }, AppConfig.btScanTime);
    }
}
