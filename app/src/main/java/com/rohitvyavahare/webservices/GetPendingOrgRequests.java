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

/**
 * Created by rohitvyavahare on 11/11/17.
 */

public class GetPendingOrgRequests extends AsyncTask<Bundle, Void, Bundle> {

    private static final String TAG = "GetPendingOrgRequests";
    private Context c;
    private Storage storage;


    public GetPendingOrgRequests(Context c, Storage storage) {
        this.c = c;
        this.storage = storage;
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
                    .path(c.getString(R.string.get_pending_orgs))
                    .appendPath(input.getString("id"))
                    .build();

            output = new Call("GET", uri, storage.getUid(), "null", this.c).Run();

            switch (output.getInt("response")) {
                case 200: {
                    JSONArray orgRequests = new JSONArray(output.getString("output"));
                    storage.setIncomingPairOrgRequest(input.getString("tag"), orgRequests.toString());
                    storage.setHardResetPairedOrgs("false");
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
