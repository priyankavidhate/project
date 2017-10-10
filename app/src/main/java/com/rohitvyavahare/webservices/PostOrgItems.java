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

/**
 * Created by rohitvyavahare on 8/5/17.
 */

public class PostOrgItems extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "PostOrgItems";
    private Storage storage;
    private Context c;
    private static final String _nullArr = "[{null}]";

    public PostOrgItems(Context c, Storage storage) {
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
        Bundle input = params[0];

        Log.d(TAG, "In background job");
        Bundle output = new Bundle();

        try {

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.org))
                    .appendPath(input.getString("id"))
                    .appendPath(c.getString(R.string.org_itmes))
                    .build();

            output = new Call("POST", uri, storage.getUid(), input.getString("body"), this.c).Run();
            onPostExecute();
            switch (output.getInt("response")) {
                case 200: {
                    output.putString("exception", "no_exception");
                    storage.setOrgItems(input.getString("tag"), input.getString("body"));
                    break;
                }
                default: {
                    storage.setOrgItems(input.getString("tag"), _nullArr);
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
