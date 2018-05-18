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

/**
 * Created by rohitvyavahare on 8/28/17.
 */

public class UpdateActivity extends AsyncTask<Bundle, Void, Bundle> {
    private ProgressDialog progress;
    private static final String TAG = "UpdateActivity";
    private String host;
    private String endpoint;
    private Storage storage;
    private Context c;


    public UpdateActivity(Context c, Storage storage) {
        this.progress = new ProgressDialog(c);
        this.host = c.getString(R.string.server_ur_templ);
        this.endpoint = c.getString(R.string.record_activity);
        this.storage = storage;
        this.c = c;
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
            //get uid, if it is not present in storage then get it from input

            String uid = storage.getUid();

            if(params.length > 0 && params[0] != null) {
                Bundle input = params[0];
                if (uid == null || uid.length() == 0) {
                    uid = input.getString("uid");
                }
            }

            Uri uri = new Uri.Builder()
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(host)
                    .path(endpoint)
                    .appendPath(uid)
                    .build();

            output = new Call("PUT", uri, uid, "null", this.c).Run();
            onPostExecute();

            Log.d(TAG, "Response from server :" + output.getInt("response"));

            switch (output.getInt("response")) {
                case 201: {
                    output.putString("exception", "no_exception");
                    storage.setLastActive(""+System.currentTimeMillis());
                    break;
                }
                default: {
                    output.putString("exception", "Response from server: " + output.getInt("response"));
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

    private void onPostExecute() {
        progress.dismiss();
    }
}

