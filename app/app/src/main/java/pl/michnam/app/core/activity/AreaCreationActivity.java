package pl.michnam.app.core.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import pl.michnam.app.R;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.http.RequestManager;
import pl.michnam.app.core.view.AreaItem;
import pl.michnam.app.sql.DbManager;
import pl.michnam.app.core.view.AreaListItemAdapter;
import pl.michnam.app.util.Tag;

public class AreaCreationActivity extends AppCompatActivity {
    private boolean areaScanActive;

    private EditText areaNameView;
    private ListView listView;


    private AreaListItemAdapter areaListAdapter;
    private ArrayList<AreaItem> itemsToShow = new ArrayList<>();

    private HashMap<String, ArrayList<ScanResult>> allWifi = new HashMap<>();
    private HashMap<String, ArrayList<android.bluetooth.le.ScanResult>> allBle = new HashMap<>();

    private ArrayList<AreaItem> dbItems = new ArrayList<>();
    private HashMap<String, ArrayList<Integer>> wifiRssiPerDevice = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> btRssiPerDevice = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_creation);

        RequestManager requestManager = new RequestManager(this);
        requestManager.clearHotspotData();

        initView();
        initList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        areaScanActive = true;
        setupWifiScan();
        startBleScan(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        areaScanActive = false;
    }


    ///////////////////////////
    ///// VIEW CONTROLLER /////
    ///////////////////////////
    private void initView() {
        listView = findViewById(R.id.listView);
        areaNameView = findViewById(R.id.areaName);
    }

    private void initList() {
        areaListAdapter = new AreaListItemAdapter(this, R.layout.activity_area_creation, itemsToShow);
        areaListAdapter.addAll(itemsToShow);
        listView.setAdapter(areaListAdapter);
    }

    public synchronized void onFinishClicked(View v) {
        String areaName = areaNameView.getText().toString().trim();
        ArrayList<String> areasList = new DbManager(this).getAreasList();
        if (areaName.equals("")) {
            Toast.makeText(this, getString(R.string.set_area_name),
                    Toast.LENGTH_LONG).show();
        } else if (areasList.contains(areaName)) {
            Toast.makeText(this, getString(R.string.area_exists),
                    Toast.LENGTH_LONG).show();
        } else {
            areaScanActive = false;
            ArrayList<AreaItem> insertToDb = new ArrayList<>();

            AreaItem item;
            for (int i = 0; i < areaListAdapter.getCount(); i++) {
                item = areaListAdapter.getItem(i);
                if (item.isChecked()) insertToDb.add(item);
            }

            DbManager dbManager = new DbManager(this);
            dbManager.addNewArea(insertToDb, areaName);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void onCancelClicked(View v) {
        areaScanActive = false;
        onBackPressed();
    }

    /////////////////////
    ///// WIFI SCAN /////
    /////////////////////
    public void setupWifiScan() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean noError = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (noError) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    if (areaScanActive) handleScanResults(results);
                } else Log.w(Tag.AREA, "WIFI AREA - error while receiving scan results");
                if (areaScanActive) scanLoop();
                else {
                    Log.i(Tag.AREA, "WIFI AREA - Stopping Scan");
                    unregisterReceiver(this);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
        Log.i(Tag.AREA, "WIFI AREA - Starting scan");
        if (areaScanActive) scanLoop();
    }

    private void scanLoop() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        boolean successfulScan = wifiManager.startScan(); // wifiScanReceiver.onReceive after scan
                        if (!successfulScan)
                            Log.i(Tag.AREA, "WIFI AREA - Scan while starting scanning");
                    }
                },
                AppConfig.wifiAreaScanWaitTime
        );

    }

    private void handleScanResults(List<ScanResult> results) {
        addWifiResultsToList(results);
        updateListOfItems();
        areaListAdapter.notifyDataSetChanged();
    }

    ////////////////////////
    ///// SCAN RESULTS /////
    ////////////////////////
    private synchronized void updateListOfItems() {
        // list of wifi devices
        for (String key : allWifi.keySet()) {
            boolean found = false;
            for (int i = 0; i < itemsToShow.size(); i++) {
                if (itemsToShow.get(i).getName() == key) found = true;
            }
            if (!found) {
                itemsToShow.add(new AreaItem(key, false));
            }
        }

        // list of ble devices
        for (String key : allBle.keySet()) {
            boolean found = false;
            for (int i = 0; i < itemsToShow.size(); i++) {
                if (itemsToShow.get(i).getName() == key) found = true;
            }
            if (!found) {
                itemsToShow.add(new AreaItem(key, true));
            }
        }


        // update listview ranges wifi
        for (AreaItem item : itemsToShow) {
            if (!item.isBt()) {
                List<ScanResult> results = allWifi.get(item.getName());
                int min = results.get(0).level;
                int max = results.get(0).level;
                for (ScanResult result : results) {
                    if (result.level > max) max = result.level;
                    else if (result.level < min) min = result.level;

                    if (wifiRssiPerDevice.containsKey(item.getName()))
                        wifiRssiPerDevice.get(item.getName()).add(result.level);
                    else
                        wifiRssiPerDevice.put(item.getName(), new ArrayList<>(Collections.singletonList(result.level)));
                }
                item.setMinRssi(min);
                item.setMaxRssi(max);
            }
        }

        // update listview ranges ble
        for (AreaItem item : itemsToShow) {
            if (item.isBt()) {
                List<android.bluetooth.le.ScanResult> results = allBle.get(item.getName());
                int min = results.get(0).getRssi();
                int max = results.get(0).getRssi();
                for (android.bluetooth.le.ScanResult result : results) {
                    if (result.getRssi() > max) max = result.getRssi();
                    else if (result.getRssi() < min) min = result.getRssi();

                    if (btRssiPerDevice.containsKey(item.getName()))
                        btRssiPerDevice.get(item.getName()).add(result.getRssi());
                    else
                        btRssiPerDevice.put(item.getName(), new ArrayList<>(Collections.singletonList(result.getRssi())));
                }
                item.setMinRssi(min);
                item.setMaxRssi(max);
            }
        }

        for (AreaItem item : itemsToShow) {
            if (!item.isBt()) {
                Log.i(Tag.AREA, item.getName());
                ArrayList<Integer> deviceData = wifiRssiPerDevice.get(item.getName());
                double avg = averageList(deviceData);
                double sd = sdFromList(deviceData, avg);
                item.setAvg(avg);
                item.setSd(sd);
            }
        }

        for (AreaItem item : itemsToShow) {
            if (item.isBt()) {
                Log.i(Tag.AREA, item.getName());
                ArrayList<Integer> deviceData = btRssiPerDevice.get(item.getName());
                double avg = averageList(deviceData);
                double sd = sdFromList(deviceData, avg);
                item.setAvg(avg);
                item.setSd(sd);
            }
        }

    }

    public static double sdFromList(ArrayList<Integer> data, double avg) {
        double tmp = 0;
        for (int i = 0; i < data.size(); i++) {
            int val = data.get(i);
            double sqrtDiff = Math.pow(val - avg, 2);
            tmp += sqrtDiff;
        }
        double avgOfDiffs = tmp / (double) (data.size());

        return Math.sqrt(avgOfDiffs);
    }

    public static double averageList(List<Integer> data) {
        return data.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }


    private synchronized void addWifiResultsToList(List<ScanResult> results) {
        for (ScanResult i : results) {
            if (allWifi.containsKey(i.SSID))
                allWifi.get(i.SSID).add(i);
            else
                allWifi.put(i.SSID, new ArrayList<>(Collections.singletonList(i)));
        }
    }

    ///////////////
    ///// BLE /////
    ///////////////
    private BluetoothLeScanner bleScanner;

    private final ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
            super.onScanResult(callbackType, result);
            addBleResultToList(result);
        }
    };

    private synchronized void addBleResultToList(android.bluetooth.le.ScanResult result) {
        String id;
        if (result.getDevice().getName() != null) id = result.getDevice().getName();
        else id = result.getDevice().getAddress();

        if (allBle.containsKey(id)) allBle.get(id).add(result);
        else allBle.put(id, new ArrayList<>(Collections.singletonList(result)));
    }

    public void startBleScan(Context context) {
        BluetoothManager bleManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bleAdapter = bleManager.getAdapter();
        bleScanner = bleAdapter.getBluetoothLeScanner();

        bleScanLoop();
    }

    private void bleScanLoop() {
        bleScanner.startScan(bleCallback);
        Log.d(Tag.BLE, "BLE - Started scan");
        new Handler().postDelayed(() -> {
            bleScanner.stopScan(bleCallback);
            Log.d(Tag.BLE, "BLE - Stopped scan");
            if (areaScanActive) {
                new Handler().postDelayed(this::bleScanLoop, AppConfig.bleScanWaitTime);
            }
        }, AppConfig.bleScanTime);
    }

}