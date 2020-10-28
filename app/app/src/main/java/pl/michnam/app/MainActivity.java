package pl.michnam.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import pl.michnam.app.scan.WifiScanner;
import pl.michnam.app.server.HttpRequestQueue;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainProcessing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        WifiScanner.wifiScan(this);
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        }
        else {
            // You can directly ask for the permission.
            requestPermissions(
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    1);
        }
    }
}