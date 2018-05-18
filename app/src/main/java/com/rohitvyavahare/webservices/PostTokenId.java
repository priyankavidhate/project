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

import org.json.JSONObject;

/**
 * Created by rohitvyavahare on 8/6/17.
 */

public class PostTokenId extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "PostOrgItems";
    private Storage storage;
    private Context c;

    public PostTokenId(Context c, Storage storage) {
        progress = new ProgressDialog(c);
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

            Uri uri = new Uri.Builder()
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.firebase_token))
                    .appendPath(storage.getUid())
                    .build();

            JSONObject obj = new JSONObject();
            obj.put("registration_id", storage.getRefreshToken());
            obj.put("_id", storage.getUid());

            output = new Call("PUT", uri, storage.getUid(), obj.toString(), this.c).Run();
            onPostExecute();
            switch (output.getInt("response")) {
                case 201: {
                    output.putString("exception", "no_exception");
                    storage.setFirstToken("false");
                    break;
                }
                default: {
                    storage.setFirstToken("true");
                    break;
                }
            }
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            onPostExecute();
            output.putString("exception", e.getMessage());
            storage.setFirstToken("false");
            return output;
        }
    }

    protected void onPostExecute() {
        progress.dismiss();
    }
}