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
 * Created by rohitvyavahare on 6/25/17.
 */

public class UpdateOrder extends AsyncTask<Bundle, Void, Bundle> {
    private static final String TAG = "UpdateOrder";
    private String host;
    private String endpoint;
    private Storage storage;
    private Context c;


    public UpdateOrder(Context c, Storage storage) {
        host =  c.getString(R.string.server_ur_templ);
        endpoint = c.getString(R.string.order);
        this.c = c;
        this.storage = storage;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {
        Log.d(TAG, "In background job");
        Bundle output = new Bundle();

        try {
            Bundle input = params[0];
            JSONObject body = new JSONObject(input.getString("body"));

            //TODO add to and from orgs in body

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .encodedAuthority(host)
                    .path(endpoint)
                    .appendPath(input.getString("id"))
                    .appendQueryParameter("message", input.getString("message"))
                    .appendQueryParameter("edit", input.getString("edit"))
                    .build();


            //@TODO add band as query parameter
            Log.d(TAG, "Updating order body :" + body.toString(4));

            output = new Call("PUT", uri, storage.getUid(), body.toString(), this.c).Run();

            switch (output.getInt("response")) {
                case 200: {
                    JSONObject selectedPairOrg = new JSONObject(input.getString("paired_org"));
                    output.putString("exception", "no_exception");
                    JSONArray orders;

                    if (input.getString("type").equals("inbox")) {
                        orders = storage.getOrdersFrom(selectedPairOrg.getString("tag"));
                    } else  {
                        orders = storage.getOrdersTo(selectedPairOrg.getString("tag"));
                    }
                    Log.d(TAG, "Updated order from server : "+ output.getString("output"));

                    JSONObject newOrder = new JSONObject(output.getString("output"));
                    JSONArray newOrders = new JSONArray();
                    for (int i = 0; i < orders.length(); i++) {
                        Log.d(TAG, "Matching :" + orders.getJSONObject(i).getString("id") + " with :" + newOrder.getString("id"));
                        if (orders.getJSONObject(i).has("id") && orders.getJSONObject(i).getString("id").equals(newOrder.getString("id"))) {
                            newOrders.put(newOrder);
                        } else {
                            newOrders.put(orders.getJSONObject(i));
                        }
                    }

                    Log.d(TAG, "Type :" + input.getString("type"));

                    if ( input.getString("type").equals("inbox")) {
                        storage.setOrdersFrom(selectedPairOrg.getString("tag"), newOrders);

                    } else {
                        storage.setOrdersTo(selectedPairOrg.getString("tag"), newOrders);
                    }


                    onPostExecute();
                    return output;
                }
                default: {
                    if (output.getString("exception") != null ) {
                        return output;
                    }
                    output.putString("exception", output.getString("output"));
                    onPostExecute();
                    return output;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onPostExecute();
            output.putString("exception", e.getMessage());
            onPostExecute();
            return output;
        }
    }

    private void onPostExecute() {
    }
}
