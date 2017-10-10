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

import org.json.JSONObject;

/**
 * Created by rohitvyavahare on 8/28/17.
 */

public class PostFeedback extends AsyncTask<Bundle, Void, Bundle> {
    private ProgressDialog progress;
    private static final String TAG = "PostFeedback";
    private String host;
    private String endpoint;
    private Storage storage;
    private Context c;


    public PostFeedback(Context c, Storage storage) {
        this.progress = new ProgressDialog(c);
        this.host = c.getString(R.string.server_ur_templ);
        this.endpoint = c.getString(R.string.feedback);
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

            Bundle input = params[0];

            JSONObject obj = new JSONObject();
            obj.put("message", input.getString("message"));

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .encodedAuthority(host)
                    .path(endpoint)
                    .appendPath(uid)
                    .build();

            output = new Call("POST", uri, uid, obj.toString(), this.c).Run();
            onPostExecute();

            switch (output.getInt("response")) {
                case 201: {
                    output.putString("exception", "no_exception");
                    Log.d(TAG, "Posted feedback");
                    break;
                }
                default: {
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


