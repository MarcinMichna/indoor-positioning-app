package pl.michnam.app.core.http;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pl.michnam.app.core.http.model.HotspotData;
import pl.michnam.app.util.Tag;

public class RequestManager {
    private final String HOST = "http://michnam.pl:5000";
    private final String URL_HOTSPOT_NAME_POST = "http://michnam.pl:5000/hotspotName";
    private final String URL_CLEAR_HOTSPOT = "http://michnam.pl:5000/hotspotClear";
    private final String URL_HOTSPOT_AGE_POST = "http://michnam.pl:5000/hotspotAge";
    private final String URL_WATCHED_DEVICES_POST = "http://michnam.pl:5000/watchedDevices";
    private final String URL_HOTSPOT_GET = "http://michnam.pl:5000/hotspot";
    private final String URL_HOTSPOT_AREA_GET = "http://michnam.pl:5000/hotspotArea";
    private final String URL_EXCLUDED_DEVICES_GET = "http://michnam.pl:5000/excludedDevices";


    private RequestQueueHttp queue;

    private RequestManager() {
    }

    public RequestManager(Context context) {
        queue = RequestQueueHttp.getInstance(context);
    }

    public void updateHotspotName(String name) {
        String nameJsonString = "{\"name\":\"" + name + "\"}";
        Log.d(Tag.HTTP, "Sending request , url: " + URL_HOTSPOT_NAME_POST);
        try {
            JSONObject nameJsonObject = new JSONObject(nameJsonString);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_HOTSPOT_NAME_POST, nameJsonObject, response -> {
                try {
                    if (response.get("status").equals("OK"))
                        Log.d(Tag.HTTP, "Successful request , url: " + URL_HOTSPOT_NAME_POST);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(Tag.HTTP, "Error parsing json, url: " + URL_HOTSPOT_NAME_POST);
                }
            }, error -> {
                Log.d(Tag.HTTP, "Error request , url: " + URL_HOTSPOT_NAME_POST + ", error msg: " + error.getMessage());
            });
            queue.addToRequestQueue(request);
        } catch (Exception e) {
            Log.w(Tag.HTTP, "Error creating json: " + nameJsonString);
        }
    }

    public void updateWatchedDevices(ArrayList<String> wifi, ArrayList<String> bt) {
        JSONObject json = new JSONObject();
        JSONArray jsonWifi = new JSONArray(wifi);
        JSONArray jsonBt = new JSONArray(bt);

        try {
            json.put("wifi", jsonWifi);
            json.put("bt", jsonBt);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_WATCHED_DEVICES_POST, json, response -> {
                try {
                    if (response.get("status").equals("OK"))
                        Log.d(Tag.HTTP, "Successful request , url: " + URL_WATCHED_DEVICES_POST);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.w(Tag.HTTP, "Error parsing json, url: " + URL_WATCHED_DEVICES_POST);
                }
            }, error -> {
                Log.d(Tag.HTTP, "Error request , url: " + URL_WATCHED_DEVICES_POST + ", error msg: " + error.getMessage());
            });
            queue.addToRequestQueue(request);

        } catch (Exception e) {
            Log.w(Tag.HTTP, "Error creating json: " + jsonWifi + " " + jsonBt);
        }
    }

    public void updateHotspotAge(int maxAge) {
        String jsonString = "{\"age\":" + maxAge + "}";
        Log.d(Tag.HTTP, "Sending request , url: " + URL_HOTSPOT_AGE_POST);

        try {
            JSONObject nameJsonObject = new JSONObject(jsonString);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_HOTSPOT_AGE_POST, nameJsonObject, response -> {
                try {
                    if (response.get("status").equals("OK"))
                        Log.d(Tag.HTTP, "Successful request , url: " + URL_HOTSPOT_AGE_POST);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.w(Tag.HTTP, "Error parsing json, url: " + URL_HOTSPOT_AGE_POST);
                }
            }, error -> {
                Log.d(Tag.HTTP, "Error request , url: " + URL_HOTSPOT_AGE_POST + ", error msg: " + error.getMessage());
            });
            queue.addToRequestQueue(request);
        } catch (Exception e) {
            Log.w(Tag.HTTP, "Error creating json: " + jsonString);
        }
    }

    public void clearHotspotData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_CLEAR_HOTSPOT, null, response -> {
            try {
                if (response.get("status").equals("OK"))
                    Log.d(Tag.HTTP, "Successful request , url: " + URL_CLEAR_HOTSPOT);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(Tag.HTTP, "Error parsing json, url: " + URL_CLEAR_HOTSPOT);
            }
        }, error -> {
            Log.d(Tag.HTTP, "Error request , url: " + URL_CLEAR_HOTSPOT + ", error msg: " + error.getMessage());
        });
        queue.addToRequestQueue(request);
    }

    public void getHotspotData() {
        Gson gson = new Gson();
        ArrayList<HotspotData> hotspotData = new ArrayList<>();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_HOTSPOT_GET, null, response -> {
            try {
                JSONArray jsonArray = response.getJSONArray("data");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        hotspotData.add(gson.fromJson(jsonArray.getString(i), HotspotData.class));
                    }
                }
                Log.i(Tag.HTTP, hotspotData.toString());

                // TODO

            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(Tag.HTTP, "Error parsing json, url: " + URL_HOTSPOT_GET);
            }
        }, error -> {
            Log.d(Tag.HTTP, "Error request , url: " + URL_HOTSPOT_GET + ", error msg: " + error.getMessage());
        });
        queue.addToRequestQueue(request);
    }

    public void getHotspotDataArea() {
        Gson gson = new Gson();
        ArrayList<HotspotData> hotspotData = new ArrayList<>();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_HOTSPOT_AREA_GET, null, response -> {
            try {
                JSONArray jsonArray = response.getJSONArray("data");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        hotspotData.add(gson.fromJson(jsonArray.getString(i), HotspotData.class));
                    }
                }
                Log.i(Tag.HTTP, hotspotData.toString());

                // TODO

            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(Tag.HTTP, "Error parsing json, url: " + URL_HOTSPOT_GET);
            }
        }, error -> {
            Log.d(Tag.HTTP, "Error request , url: " + URL_HOTSPOT_AREA_GET + ", error msg: " + error.getMessage());
        });
        queue.addToRequestQueue(request);
    }

    public void getExcludedDevices() {
        ArrayList<String> excludedWifi = new ArrayList<>();
        ArrayList<String> excludedBt = new ArrayList<>();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_EXCLUDED_DEVICES_GET, null, response -> {
            try {
                JSONArray jsonWifi = response.getJSONArray("wifi");
                JSONArray jsonBt = response.getJSONArray("bt");
                if (jsonWifi != null) {
                    for (int i = 0; i < jsonWifi.length(); i++) {
                        excludedWifi.add(jsonWifi.getString(i));
                    }
                }
                Log.i(Tag.HTTP, "Excluded WIFI: " + excludedWifi.toString());

                if (jsonBt != null) {
                    for (int i = 0; i < jsonBt.length(); i++) {
                        excludedBt.add(jsonBt.getString(i));
                    }
                }
                Log.i(Tag.HTTP, "Excluded BT: " + excludedBt.toString());

                // TODO

            } catch (JSONException e) {
                e.printStackTrace();
                Log.w(Tag.HTTP, "Error parsing json, url: " + URL_HOTSPOT_GET);
            }
        }, error -> {
            Log.d(Tag.HTTP, "Error request , url: " + URL_EXCLUDED_DEVICES_GET + ", error msg: " + error.getMessage());
        });
        queue.addToRequestQueue(request);
    }


}
