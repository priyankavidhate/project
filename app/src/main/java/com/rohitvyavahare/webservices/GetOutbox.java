package com.rohitvyavahare.webservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.rohitvyavahare.project.R;
import com.example.rohitvyavahare.project.Utils;
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.webservices.REST.Call;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by rohitvyavahare on 7/29/17.
 */

public class GetOutbox extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "GetOutbox";
    private JSONObject defaultOrg;
    private Utils util;
    private Context c;
    private Storage storage;


    public GetOutbox(Context c, Storage storage) {
        progress = new ProgressDialog(c);
        this.c = c;
        this.storage = storage;
        this.defaultOrg = storage.getDefaultOrg();
        util = new Utils();
    }

    @Override
    protected void onPreExecute() {
        progress.setMessage("Loading");
        progress.show();
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {
        Log.d(TAG, "In background job");
        Bundle output = new Bundle();

        try {

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.org))
                    .appendPath(defaultOrg.getString("id"))
                    .appendPath(c.getString(R.string.outbox))
                    .build();

            output = new Call("GET", uri, storage.getUid(), "null", this.c).Run();
            onPostExecute();

            switch (output.getInt("response")) {
                case 200: {
                    output.putString("exception", "no_exception");
                    storage.setHardResetOutbox("false");
                    JSONArray jArray = new JSONArray(output.getString("output"));
                    HashMap<String, JSONArray> map = new HashMap<>();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < jArray.length(); ++i) {
                        JSONObject rec = jArray.getJSONObject(i);
                        rec = rec.getJSONObject("doc");
                        JSONObject value = rec.getJSONObject("value");
                        value.put("id", rec.getString("_id"));

                        if (defaultOrg.getString("band").trim().equals("3")) {
                            if (value.getString("status").trim().equals("acknowledged")) {
                                if (map.containsKey(value.getString("to"))) {
                                    map.get(value.getString("to")).put(value);
                                } else {
                                    sb.append(value.getString("to")).append(",");
                                    JSONArray arr = new JSONArray();
                                    arr.put(value);
                                    map.put(value.getString("to"), arr);
                                }
                            }
                        } else {
                            if (map.containsKey(value.getString("to"))) {
                                map.get(value.getString("to")).put(value);
                            } else {
                                sb.append(value.getString("to")).append(",");
                                JSONArray arr = new JSONArray();
                                arr.put(value);
                                map.put(value.getString("to"), arr);
                            }
                        }
                    }

                    Log.d(TAG, "Caching orgs :" + map.keySet());
                    for (String s : map.keySet()) {
                        storage.setOrdersTo(s, util.sortJsonArray(map.get(s)));
                    }
                    storage.setHardResetOutbox("false");
                    break;
                }
                case 404: {
                    output.putString("exception", "no_exception");
                    storage.setHardResetOutbox("fale");
                    output.putBoolean("empty_view", true);
                    break;
                }
                default: {
                    try {
                        Log.d(TAG, "Checking if exception exist");
                        String temp = output.getString("exception");
                        if (!temp.equals("no_exception")) {
                            output.putString("exception", temp);
                            Log.d(TAG, "Exception exist");
                            return output;
                        }

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Adding exception");
                    output.putString("exception", "Response from server: " + output.getInt("response"));
                    return output;
                }
            }
            return output;

        } catch (Exception e) {
            e.printStackTrace();
            onPostExecute();
            output.putString("exception", e.getMessage());
            return output;
        }
    }

    protected void onPostExecute() {
        progress.dismiss();
    }
}
