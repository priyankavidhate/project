package com.rohitvyavahare.webservices;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.rohitvyavahare.project.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by rohitvyavahare on 6/25/17.
 */

public class UpdateOrder extends AsyncTask<Bundle, Void, Bundle> {
    private ProgressDialog progress;
    private static final String TAG = "UpdateOrder";
    private String host;
    private String endpoint;


    UpdateOrder(Context c) {
        progress = new ProgressDialog(c);
        host = c.getString(R.string.server_ur_templ);
        endpoint = c.getString(R.string.order);
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
            Bundle bundle = params[0];
            JSONObject b = new JSONObject(bundle.getString("obj"));

            //TODO add to and from orgs in body

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .encodedAuthority(host)
                    .path(endpoint)
                    .appendPath(b.getString("id"))
                    .appendQueryParameter("message", bundle.getString("message"))
                    .build();


            //@TODO add band as query parameter

            URL url = new URL(uri.toString());
            Log.d(TAG, "url:" + url.toString());

            String auth = bundle.getString("uid");

            Log.d(TAG, "Auth " + auth);
            if (auth.equals("null")) {
                onPostExecute();
                //@TODO add alert
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", auth);
            connection.setDoOutput(true);

            JSONObject default_org = new JSONObject(bundle.getString("default_org"));
            Log.d(TAG, "Default org for order :" + default_org.toString());

            String paired_orgs = bundle.getString("paired_orgs");
            JSONObject to_org = new JSONObject();

            String type = bundle.getString("type");
            Log.d(TAG, "Request for type " + type);

            Log.d(TAG, "Paired Orgs :" + paired_orgs);
            JSONArray paired_arr = new JSONArray(paired_orgs);

            String org_name = bundle.getString("org_name");
            Log.d(TAG, "Organization name :" + org_name);

            for (int i = 0; i < paired_arr.length(); i++) {
                if (!paired_arr.getJSONObject(i).has("name")) {
                    continue;
                }
                String paired_org_name =  paired_arr.getJSONObject(i).getString("name");

                if (paired_org_name.equals(org_name)) {
                    to_org = paired_arr.getJSONObject(i);
                }
            }

            if (!to_org.has("id")) {
                throw new NullPointerException("to_org can't be null");
            }

            JSONObject body = new JSONObject();
            body.put("order", b);

            if (type.equals("inbox")) {
                body.put("to", default_org);
                body.put("from", to_org);
                Log.d(TAG, "From org as type is inbox:" + to_org.toString());
            } else {
                body.put("to", to_org);
                body.put("from", default_org);
                Log.d(TAG, "To org as type is outbox:" + to_org.toString());
            }

            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(body.toString());
            dStream.flush();
            dStream.close();
            int response = connection.getResponseCode();

            Log.d(TAG, "Sending 'PUT' request to URL : :" + url);
            Log.d(TAG, "PUT parameters : " + body.toString());
            Log.d(TAG, "Response Code : " + response);

            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br;

            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException ioe) {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();

            onPostExecute();

            Log.d(TAG, "Response :" + sb.toString());

            output.putInt("response", response);
            output.putString("output", sb.toString());
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
            output.putString("exception", "no_exception");
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
