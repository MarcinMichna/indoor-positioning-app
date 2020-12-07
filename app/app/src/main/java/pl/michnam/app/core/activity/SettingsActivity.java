package pl.michnam.app.core.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import pl.michnam.app.R;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.http.RequestManager;
import pl.michnam.app.sql.DbManager;
import pl.michnam.app.util.Pref;
import pl.michnam.app.util.Tag;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat switchCompat;
    private EditText hotspotName;
    private EditText fittingThreshold;
    private EditText scanAge;

    private Boolean mode;
    private String hotspot;
    private String thresholdFitting;
    private String age;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
    }

    private void initView() {
        switchCompat = findViewById(R.id.activeModeSwitch);
        hotspotName = findViewById(R.id.hotspotName);
        fittingThreshold = findViewById(R.id.fittingThreshold);
        scanAge = findViewById(R.id.signalTime);

        SharedPreferences sharedPref = getSharedPreferences(Pref.prefFile, Context.MODE_PRIVATE);

        mode = sharedPref.getBoolean(Pref.activeMode, false);
        hotspot = sharedPref.getString(Pref.hotspotName, "");
        thresholdFitting = Integer.toString(sharedPref.getInt(Pref.fittingThreshold, -1));
        age = Integer.toString(sharedPref.getInt(Pref.scanAge,-1 ));

        switchCompat.setChecked(mode);
        hotspotName.setHint(hotspot);
        fittingThreshold.setHint(thresholdFitting);
        scanAge.setHint(age);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (sharedPref.getString(Pref.hotspotName, "").equals("") && hotspotName.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.active_no_hotspot), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public void onCancelPressed(View v) {
        onBackPressed();
    }

    public void onApplyPressed(View v) {
        SharedPreferences sharedPref = getSharedPreferences(Pref.prefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        boolean validPrefs = true;

        if (switchCompat.isChecked() != mode)
            editor.putBoolean(Pref.activeMode, switchCompat.isChecked());

        String newHotspotName = hotspotName.getText().toString();
        if (!newHotspotName.equals("") && !newHotspotName.equals(hotspot)) {
            editor.putString(Pref.hotspotName, hotspotName.getText().toString());
            new RequestManager(this).updateHotspotName(newHotspotName);
        }


        String newMarginString = fittingThreshold.getText().toString();
        if (!newMarginString.equals("") && !newMarginString.equals(thresholdFitting)) {
            try {
                int number = Integer.parseInt(newMarginString);
                editor.putInt(Pref.fittingThreshold, number);
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(),getString(R.string.settings_error), Toast.LENGTH_LONG).show();
                validPrefs = false;
            }
        }

        boolean changedAge = false;
        String newAge = scanAge.getText().toString();
        int number = AppConfig.maxScanAge;
        if (!newAge.equals("") && !newAge.equals(age)) {
            try {
                number = Integer.parseInt(newAge);
                editor.putInt(Pref.scanAge, number);
                changedAge = true;
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.settings_error), Toast.LENGTH_LONG).show();
                validPrefs = false;
            }
        }

        if (validPrefs) {
            editor.apply();
            RequestManager requestManager = new RequestManager(this);
            if (changedAge) requestManager.updateHotspotAge(number);
            onBackPressed();
        }
        else
            editor.clear();
    }

    public void onAddAreaPressed(View v) {
        startActivity(new Intent(this, AreaCreationActivity.class));
    }

    public void onDeleteAreaPressed(View v) {
        startActivity(new Intent(this, DeleteAreaActivity.class));
    }
}