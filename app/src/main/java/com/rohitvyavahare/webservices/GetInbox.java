package com.rohitvyavahare.webservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigital.rohitvyavahare.project.R;
import com.bigital.rohitvyavahare.project.Utils;
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.webservices.REST.Call;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by rohitvyavahare on 7/29/17.
 */

public class GetInbox extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "GetInbox";
    private JSONObject defaultOrg;
    private Utils util;
    private Context c;
    private Storage storage;
    private String skip = "0";


    public GetInbox(Context c, Storage storage, int skip) {
        progress = new ProgressDialog(c);
        this.c = c;
        this.storage = storage;
        this.defaultOrg = storage.getDefaultOrg();
        util = new Utils();
        this.skip = "" + skip;
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
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.org))
                    .appendQueryParameter("skip", this.skip)
                    .appendPath(defaultOrg.getString("id"))
                    .appendPath(c.getString(R.string.inbox))
                    .build();

            output = new Call("GET", uri, storage.getUid(), "null", this.c).Run();
            onPostExecute();

            switch (output.getInt("response")) {
                case 200: {
                    try {
                        output.putString("exception", "no_exception");
                        JSONArray jArray = new JSONArray(output.getString("output"));
                        HashMap<String, JSONArray> map = new HashMap<>();

                        for (int i = 0; i < jArray.length(); ++i) {
                            JSONObject rec = jArray.getJSONObject(i);
                            rec = rec.getJSONObject("doc");

                            JSONObject value = rec.getJSONObject("value");
                            value.put("id", rec.getString("_id"));

                            Log.d(TAG, "Value :" + value.toString());

                            if (defaultOrg.getString("band").trim().equals("3")) {
                                if (value.getString("status").trim().equals("acknowledged")) {
                                    if (map.containsKey(value.getString("from"))) {
                                        map.get(value.getString("from")).put(value);
                                    } else {
                                        JSONArray arr = new JSONArray();
                                        arr.put(value);
                                        map.put(value.getString("from"), arr);
                                    }
                                }
                            } else {
                                if (map.containsKey(value.getString("from"))) {
                                    map.get(value.getString("from")).put(value);
                                } else {
                                    JSONArray arr = new JSONArray();
                                    arr.put(value);
                                    map.put(value.getString("from"), arr);
                                }
                            }
                        }

                        Log.d(TAG, "Caching orgs :" + map.keySet());
                        for (String s : map.keySet()) {

                            Log.d(TAG, "Key :" + s);
                            Log.d(TAG, "Value for " + s + " :" + map.get(s).toString());
                            JSONArray orders = util.sortJsonArray(map.get(s));
                            storage.setOrdersFrom(s,orders);
                        }
                        storage.setHardResetInbox("false");
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                        output.putString("exception", e.getMessage());
                        return output;
                    }
                    break;
                }
                case 404: {
                    output.putString("exception", "no_exception");
                    storage.setHardResetInbox("false");
                    output.putString("exception", "Response from server" + output.getInt("response"));
                    break;
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
