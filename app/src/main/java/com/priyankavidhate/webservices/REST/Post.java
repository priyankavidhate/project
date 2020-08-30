package com.priyankavidhate.webservices.REST;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigital.priyankavidhate.project.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by priyankavidhate on 7/10/17.
 */

public class Post extends AsyncTask<Bundle, Void, Bundle> {

    private ProgressDialog progress;
    private static final String TAG = "GetContacts";
    private String host;

    public Post(Context c) {
        progress = new ProgressDialog(c);
        host = c.getString(R.string.server_ur_templ);
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

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .encodedAuthority(host)
                    .path(bundle.getString("endpoint"))
                    .build();
            //@TODO add band as query parameter

            URL url = new URL(uri.toString());
            Log.d(TAG, "url:" + url.toString());

            String auth = bundle.getString("uid");

            Log.d(TAG, "auth " + auth);
            if (auth.equals("null")) {
                onPostExecute();
                //@TODO add alert
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", auth);

            JSONObject body = new JSONObject(bundle.getString("body"));

            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(body.toString());
            dStream.flush();
            dStream.close();

            int response = connection.getResponseCode();

            Log.d(TAG, "Sending 'POST' request to URL : :" + url);
            Log.d(TAG, "POST parameters : " + body.toString());
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
            Log.d(TAG, "Response from GetContacts :" + sb.toString());
            output.putInt("response", response);
            output.putString("output", sb.toString());
            output.putString("exception", "no_exception");
            return output;

        } catch (IOException | JSONException | NullPointerException e) {
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
