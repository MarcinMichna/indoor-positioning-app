package pl.michnam.app.core.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import pl.michnam.app.R;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.view.AreaItemList;
import pl.michnam.app.sql.DbManager;
import pl.michnam.app.core.view.AreaListItemAdapter;
import pl.michnam.app.util.Tag;

public class AreaCreationActivity extends AppCompatActivity {
    private boolean areaScanActive;

    private String areaName;

    private ListView listView;
    private Button finishButton;

    private AreaListItemAdapter areaListAdapter;
    private ArrayList<AreaItemList> itemsToShow = new ArrayList<>();
    private HashMap<String, ArrayList<ScanResult>> allWifi = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_creation);
        initView();
        initList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        areaScanActive = true;
        setupWifiScan();
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
        finishButton = findViewById(R.id.finishButton);
        areaName = getIntent().getStringExtra("areaName");
    }

    private void initList() {
        areaListAdapter = new AreaListItemAdapter(this, R.layout.activity_area_creation, itemsToShow);
        areaListAdapter.addAll(itemsToShow);
        listView.setAdapter(areaListAdapter);
    }

    public void onFinishClicked(View v) {
        areaScanActive = false;

        ArrayList<AreaItemList> insertToDb = new ArrayList<>();
        for (AreaItemList item : itemsToShow) {
            if (item.isChecked()) insertToDb.add(item);
        }
        DbManager dbManager = new DbManager(this);
        dbManager.addNewArea(insertToDb, areaName);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
        addResultsToList(results);
        updateListOfItems();
        areaListAdapter.notifyDataSetChanged();

    }

    ////////////////////////
    ///// SCAN RESULTS /////
    ////////////////////////
    private void updateListOfItems() {
        for (String key : allWifi.keySet()) {
            boolean found = false;
            for (int i = 0; i < itemsToShow.size(); i++) {
                if (itemsToShow.get(i).getName() == key) found = true;
            }
            if (!found) itemsToShow.add(new AreaItemList(key));
        }
        //Log.d(Tag.AREA,"Size of item to show: " + itemsToShow.size());
        for (AreaItemList item : itemsToShow) {
            List<ScanResult> results = allWifi.get(item.getName());
            int min = results.get(0).level;
            int max = results.get(0).level;
            for (ScanResult result : results) {
                if (result.level > max) max = result.level;
                else if (result.level < min) min = result.level;
            }
            item.setMinRssi(min);
            item.setMaxRssi(max);
        }

    }

    private void addResultsToList(List<ScanResult> results) {
        for (ScanResult i : results) {
            if (allWifi.containsKey(i.SSID))
                allWifi.get(i.SSID).add(i);
            else
                allWifi.put(i.SSID, new ArrayList<>(Collections.singletonList(i)));
        }
    }

    ////////////////////
    ///// BLE SCAN /////
    ////////////////////

    /////////////////
    ///// UTILS /////
    /////////////////

}