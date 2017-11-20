package com.rohitvyavahare.webservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.rohitvyavahare.project.R;
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.webservices.REST.Call;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by rohitvyavahare on 7/29/17.
 */

public class GetOrgs extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "GetOrgs";
    private JSONObject defaultOrg;
    private Context c;
    private Storage storage;


    public GetOrgs(Context c, Storage storage) {
        progress = new ProgressDialog(c);
        this.c = c;
        this.storage = storage;
        this.defaultOrg = storage.getDefaultOrg();
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
                    .scheme("http")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.account))
                    .appendPath(storage.getUid())
                    .appendPath(c.getString(R.string.orgs))
                    .build();


            output = new Call("GET", uri, storage.getUid(), "null", this.c).Run();
            onPostExecute();
            switch (output.getInt("response")) {
                case 200: {
                    /*
                    Get associated orgs to account and set default org
                     */
                    output.putString("exception", "no_exception");
                    JSONArray jArray = new JSONArray(output.getString("output"));
                    JSONArray orgs = new JSONArray();
                    storage.setAssociatedOrg(jArray);
                    for (int i = 0; i < jArray.length(); ++i) {
                        JSONObject rec = jArray.getJSONObject(i);
                        orgs.put(rec);
                    }

                    if (orgs.length() > 0) {
                        Log.d(TAG, "Caching orgs :" + orgs.toString());
                        int i = 0;
                        String orgName = "";
                        this.defaultOrg = storage.getDefaultOrg();
                        if (this.defaultOrg != null) {
                            Log.d(TAG, "Default org is not null");
                            for (; i < orgs.length(); i++) {
                                if (this.defaultOrg.has("id") & this.defaultOrg.getString("id").equals(orgs.getJSONObject(i).getString("id"))) {
                                    this.storage.setDefaultOrg(orgs.getJSONObject(i).toString());
                                    if (orgs.getJSONObject(i).has("name")) {
                                        orgName = orgs.getJSONObject(i).getString("name");
                                    }
                                    break;
                                }
                            }
                        }
                        if (i >= orgs.length() || i == 0) {
                            if (orgs.getJSONObject(0).has("name")) {
                                orgName = orgs.getJSONObject(0).getString("name");
                            }
                            this.storage.setDefaultOrg(orgs.getJSONObject(0).toString());
                            this.storage.setLastSyncTime(orgs.getJSONObject(0).getString("tag"));
                        }
                        output.putString("current_org_name", orgName);
                        output.putBoolean("empty_view", false);
                    } else {
                        output.putBoolean("empty_view", true);
                    }

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
