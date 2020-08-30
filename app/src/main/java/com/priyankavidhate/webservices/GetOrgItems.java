package com.priyankavidhate.webservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigital.priyankavidhate.project.R;
import com.bigital.priyankavidhate.project.Utils;
import com.priyankavidhate.Data.Storage;
import com.priyankavidhate.webservices.REST.Call;

import org.json.JSONArray;

/**
 * Created by priyankavidhate on 8/5/17.
 */

public class GetOrgItems extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "GetOrgItems";
    private Utils util;
    private Context c;
    private Storage storage;
    private static final String _nullArr = "[{null}]";


    public GetOrgItems(Context c, Storage storage) {
        progress = new ProgressDialog(c);
        this.c = c;
        this.storage = storage;
        util = new Utils();
    }

    @Override
    protected void onPreExecute() {
        progress.setMessage("Loading");
        progress.show();
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {

        Bundle input = params[0];

        Log.d(TAG, "In background job");
        Bundle output = new Bundle();

        try {
            Uri uri = new Uri.Builder()
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.org))
                    .appendPath(input.getString("id"))
                    .appendPath(c.getString(R.string.org_itmes))
                    .build();

            output = new Call("GET", uri, storage.getUid(), "null", this.c).Run();
            onPostExecute();

            switch (output.getInt("response")) {
                case 200: {
                    try {
                        output.putString("exception", "no_exception");
                        JSONArray jArray = new JSONArray(output.getString("output"));
                        Log.d(TAG, "Setting org itmes for org :" + input.getString("tag"));
                        storage.setOrgItems(input.getString("tag"), jArray.toString());
                        storage.setLastSyncTime(input.getString("tag"));
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                        storage.setOrgItems(input.getString("tag"), _nullArr);
                        output.putString("exception", e.getMessage());
                        return output;
                    }
                    break;
                }
                case 404: {
                    output.putString("exception", "no_exception");
                    storage.setOrgItems(input.getString("tag"), _nullArr);
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
