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
 * Created by rohitvyavahare on 8/11/17.
 */

public class GetPairedOrgs extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "GetContacts";
    private Context c;
    private Storage storage;


    public GetPairedOrgs(Context c, Storage storage) {
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
        Log.d(TAG, "In background job");
        Bundle output = new Bundle();

        try {
            Log.d(TAG, "Default org for Paired org :" + storage.getDefaultOrg().toString());
            String defaultOrg = storage.getDefaultOrg().getString("id");

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.get_paired_orgs))
                    .appendPath(defaultOrg)
                    .build();


            output = new Call("GET", uri, storage.getUid(), "null", this.c).Run();
            onPostExecute();

            switch (output.getInt("response")) {
                case 200: {
                    output.putString("exception", "no_exception");
                    JSONArray jArray = new JSONArray(output.getString("output"));

                    Log.d(TAG, "Paired orgs response :" + jArray.toString());

                    if(storage.getDefaultOrg() == null) {
                        output.putString("exception", "Default org is not present");
                        return output;
                    }

                    String defaultOrgTag = storage.getDefaultOrg().getString("tag");

                    storage.setPairedOrgs(defaultOrgTag, jArray);

                    for(int i=0; i<jArray.length(); i++) {
                        JSONObject org = jArray.getJSONObject(i);

                        storage.setLastSyncTime(org.getString("tag"));

                        if(!org.has("items")) {
                            continue;
                        }

                        storage.setOrgItems(org.getString("tag") + c.getString(R.string.paired_orgs), org.getString("items"));
                    }
                    storage.setHardResetPairedOrgs("false");
                    break;
                }
                default: {
                    output.putString("exception", "Response from server" + output.getInt("response"));
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
