package com.rohitvyavahare.webservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigital.rohitvyavahare.project.R;
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.webservices.REST.Call;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by rohitvyavahare on 11/5/17.
 */

public class AssociatedOrg extends AsyncTask<Bundle, Void, Bundle> {
    private ProgressDialog progress;
    private static final String TAG = "PostOrder";

    private Storage storage;
    private Context c;

    public AssociatedOrg(Context c, Storage storage) {
        progress = new ProgressDialog(c);
        this.c = c;
        this.storage = storage;
    }

    @Override
    protected void onPreExecute() {
        progress.setMessage("Loading");
        progress.show();
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {
        Bundle output = new Bundle();
        try {
            Bundle input = params[0];

            Log.d(TAG, "In background job");

            JSONObject newOrg = new JSONObject(input.getString("org"));


            Uri uri = new Uri.Builder()
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.org))
                    .build();

            output = new Call("POST", uri, storage.getUid(), newOrg.toString(), this.c).Run();
            onPostExecute();
            switch (output.getInt("response")) {
                case 200: {
                    output.putString("exception", "no_exception");
                    String associatedOrgs = storage.getAssociatedOrgs().toString();  //prefs.getString("orgs", "null");
                    Log.d(TAG, "orgs_string :" + associatedOrgs);

                    JSONObject result = new JSONObject(output.getString("output"));

                    if(!result.has("id")){
                        throw new org.json.JSONException("Result from server does not have id for org");
                    }

                    newOrg.put("id", result.getString("id"));

                    JSONArray orgs;

                    if (associatedOrgs.equals("null")) {
                        orgs = new JSONArray();
                    }
                    else {
                        orgs = new JSONArray(associatedOrgs);
                    }
                    Log.d(TAG, "Associated Orgs :" + orgs.toString());

                    for (int i = 0; i < orgs.length(); i++) {

                        JSONObject org = orgs.getJSONObject(i);

                        if (org.has("id") && org.getString("id").equals(newOrg.getString("id"))) {
                            Log.d(TAG, "Org is present");
                            throw new org.json.JSONException("Org already present");
                        }
                    }
                    orgs.put(newOrg);
                    storage.setAssociatedOrg(orgs);

                    return output;
                }
                default: {
                    output.putString("exception", output.getString("output"));
                    return output;
                }
            }
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
