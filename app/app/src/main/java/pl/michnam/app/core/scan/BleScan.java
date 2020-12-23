package pl.michnam.app.core.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.analysis.AreaAnalysis;
import pl.michnam.app.core.service.MainService;
import pl.michnam.app.util.Tag;

public class BleScan {
    private static BluetoothLeScanner bleScanner;

    private static final ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //Log.d(Tag.BLE, "Ble name: " + result.getDevice().getName());
//            Log.d(Tag.BLE, "Ble device: " + result.toString());
            AreaAnalysis.getInstance().addResBle(result);
        }
    };

    public static void startBleScan(Context context) {
        BluetoothManager bleManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bleAdapter = bleManager.getAdapter();
        bleScanner = bleAdapter.getBluetoothLeScanner();

        bleScanLoop();
    }

    private static void bleScanLoop() {
        bleScanner.startScan(bleCallback);
        new Handler().postDelayed(() -> {
            bleScanner.stopScan(bleCallback);
            if (MainService.isWorking()) {
                new Handler().postDelayed(BleScan::bleScanLoop, AppConfig.bleScanWaitTime);
            }
        }, AppConfig.bleScanTime);
    }
}
