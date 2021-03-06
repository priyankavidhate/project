package com.priyankavidhate.webservices;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigital.priyankavidhate.project.R;
import com.priyankavidhate.Data.Storage;
import com.priyankavidhate.webservices.REST.Call;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by priyankavidhate on 7/10/17.
 */

public class PostOrder extends AsyncTask<Bundle, Void, Bundle> {

    private static final String TAG = "PostOrder";

    private Storage storage;
    private Context c;

    public PostOrder(Context c, Storage storage) {
        this.c = c;
        this.storage = storage;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {
        Bundle output = new Bundle();
        try {
            Bundle input = params[0];

            Log.d(TAG, "In background job");

            final JSONObject body = new JSONObject(input.getString("body"));
            JSONObject from = new JSONObject(body.getString("from"));


            final Uri uri = new Uri.Builder()
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.org))
                    .appendPath(from.getString("id"))
                    .appendPath(this.c.getString(R.string.order))
                    .build();

            output = new Call("POST", uri, storage.getUid(), body.toString(), c).Run();

            onPostExecute();
            switch (output.getInt("response")) {
                case 200: {
                    output.putString("exception", "no_exception");
                    JSONObject defaultOrg = storage.getDefaultOrg();
                    JSONArray pairedOrgs = storage.getPairedOrgs(defaultOrg.getString("tag"));

                    Log.d(TAG, "default org found: " + defaultOrg.getString("name"));
                    Log.d(TAG, "Paired orgs :" + pairedOrgs.length());

                    JSONArray ordersFromPairedOrg = storage.getOrdersTo(input.getString("tag"));
                    if (ordersFromPairedOrg == null) {
                        ordersFromPairedOrg = new JSONArray();
                    }
                    Log.d(TAG, "orders :" + ordersFromPairedOrg.toString());


                    JSONObject newOrder = new JSONObject(output.getString("output"));

                    JSONArray newArr = new JSONArray();
                    newArr.put(0, newOrder);
                    for (int j = 0; j < ordersFromPairedOrg.length(); j++) {
                        if (j > 100) {
                            break;
                        }
                        newArr.put(j + 1, ordersFromPairedOrg.getJSONObject(j));
                    }

                    ordersFromPairedOrg = newArr;
                    storage.setOrdersTo(input.getString("tag"), ordersFromPairedOrg);
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
            onPostExecute();
            output.putString("exception", e.getMessage());
            return output;
        }
    }

    protected void onPostExecute() {
    }
}
