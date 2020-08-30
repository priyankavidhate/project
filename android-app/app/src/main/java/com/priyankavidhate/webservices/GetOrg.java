package com.priyankavidhate.webservices;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigital.priyankavidhate.project.R;
import com.priyankavidhate.Data.Storage;
import com.priyankavidhate.webservices.REST.Call;

/**
 * Created by priyankavidhate on 11/5/17.
 */

public class GetOrg  extends AsyncTask<Bundle, Void, Bundle> {
    private static final String TAG = "GetOrgItems";
    private Context c;
    private Storage storage;


    public GetOrg(Context c, Storage storage) {
        this.c = c;
        this.storage = storage;
    }


    @Override
    protected Bundle doInBackground(Bundle... params) {

        Bundle input = params[0];

        Log.d(TAG, "In background job");
        Bundle output = new Bundle();

        try {

            String tag = input.getString("tag");
            Uri uri = new Uri.Builder()
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.tag))
                    .appendPath(tag)
                    .build();

            output = new Call("GET", uri, storage.getUid(), "null", this.c).Run();

            switch (output.getInt("response")) {
                case 200: {
                    return output;
                }
                default: {
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
