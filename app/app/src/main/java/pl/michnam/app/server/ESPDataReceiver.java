package pl.michnam.app.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import pl.michnam.app.MainActivity;


public class ESPDataReceiver {
    private static void sendRequest(Context context) {
        String url = "http://michnam.pl:5000/checkGet";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(MainActivity.TAG, "Got JSON Response: " + response.toString());
                    }
                }, error -> Log.i(MainActivity.TAG, "Error while sending http request: " + error.getMessage()));
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HttpRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
}
