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
import pl.michnam.app.core.model.AreaItemList;
import pl.michnam.app.util.AreaListItemAdapter;
import pl.michnam.app.util.Tag;

public class AreaCreationActivity extends AppCompatActivity {
    private boolean areaScanActive;

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
    }

    private void initList() {
        areaListAdapter = new AreaListItemAdapter(this, R.layout.activity_area_creation, itemsToShow);
        areaListAdapter.addAll(itemsToShow); // TODO add list of wifi objects
        listView.setAdapter(areaListAdapter);
    }

    public void onFinishClicked(View v) {

//        areaScanActive = false;
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
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
                    handleScanResults(results);
                } else Log.w(Tag.WIFI, "WIFI AREA - error while receiving scan results");
                if (areaScanActive) scanLoop();
                else {
                    Log.i(Tag.WIFI, "WIFI AREA - Stopping Scan");
                    unregisterReceiver(this);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
        Log.i(Tag.WIFI, "WIFI AREA - Starting scan");
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
                            Log.i(Tag.WIFI, "WIFI AREA - Scan while starting scanning");
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

    private void updateListOfItems() {
        for (String key : allWifi.keySet()) {
            boolean found = false;
            for (int i = 0; i < itemsToShow.size(); i++) {
                if (itemsToShow.get(i).getName() == key) found = true;
            }
            if (!found) itemsToShow.add(new AreaItemList(key));
        }
        //Log.d(Tag.WIFI,"Size of item to show: " + itemsToShow.size());
    }

    private void addResultsToList(List<ScanResult> results) {
        for (ScanResult i : results) {
            if (allWifi.containsKey(i.SSID))
                allWifi.get(i.SSID).add(i);
            else
                allWifi.put(i.SSID,new ArrayList<>(Collections.singletonList(i)));
        }
        int counter = 0;
        for (int i = 0; i < itemsToShow.size(); i++) {
            if (itemsToShow.get(i).isChecked()) counter++;
        }
        //Log.d(Tag.WIFI, "Selected items: " + counter);
    }
}