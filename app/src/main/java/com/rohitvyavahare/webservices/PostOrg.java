package com.rohitvyavahare.webservices;

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
 * Created by rohitvyavahare on 11/15/17.
 */

public class PostOrg extends AsyncTask<Bundle, Void, Bundle> {

    private static final String TAG = "PostOrg";

    private Storage storage;
    private Context c;

    public PostOrg(Context c, Storage storage) {
        this.c = c;
        this.storage = storage;
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {
        Bundle output = new Bundle();
        try {
            Bundle input = params[0];

            Log.d(TAG, "In background job");

            final JSONObject newOrg = new JSONObject(input.getString("org"));

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.org))
                    .build();

            output = new Call("POST", uri, storage.getUid(), newOrg.toString(), c).Run();

            switch (output.getInt("response")) {
                case 200: {
                    output.putString("exception", "no_exception");
                    String associatedOrgs = storage.getAssociatedOrgs().toString();
                    Log.d(TAG, "Associated Orgs :" + storage.getAssociatedOrgs().toString(4));

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
                    Log.d(TAG, "Associated Orgs :" + orgs.toString(4));

                    for (int i = 0; i < orgs.length(); i++) {

                        JSONObject org = orgs.getJSONObject(i);

                        if (org.has("id") && org.getString("id").equals(newOrg.getString("id"))) {
                            Log.d(TAG, "Org is present");
                            throw new org.json.JSONException("Org already present");
                        }
                    }
                    orgs.put(newOrg);
                    storage.setAssociatedOrg(orgs);
                    storage.setDefaultOrg(newOrg.toString());
                    return output;
                }
                default: {
                    if (output.getString("exception") != null ) {
                        return output;
                    }
                    output.putString("exception", output.getString("output"));
                    return output;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            output.putString("exception", e.getMessage());
            return output;
        }
    }
}
