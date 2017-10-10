package com.rohitvyavahare.webservices.REST;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by rohitvyavahare on 7/10/17.
 */

public class Call {

    private Uri uri;
    private String auth, verb, body;
    private static final String TAG = "Call";
    private Context c;

    public Call(String verb, Uri uri, String auth, String body, Context c) {
        this.verb = verb;
        this.uri = uri;
        this.auth = auth;
        this.body = body;
        this.c = c;
    }

    public Bundle Run() throws Exception {

        Bundle output = new Bundle();

        if(!isUserOnline()) {
            Log.d(TAG, "User is not online");
            output.putInt("response", 504);
            output.putString("exception", "Not connected to internet, please try again later");
            return output;
        }

        Log.d(TAG, "User is online");

        URL url = new URL(this.uri.toString());
        Log.d(TAG, "Sending request to URL : :" + url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(this.verb);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", this.auth);

        if(!this.body.equals("null")) {
            Log.d(TAG, "Body :" + body);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(this.body);
            dStream.flush();
            dStream.close();
        }

        final int response = connection.getResponseCode();

        Log.d(TAG, "Response Code : " + response);

        final StringBuilder sb = new StringBuilder();
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

        Log.d(TAG, "Response :" + sb.toString());

        output.putInt("response", response);
        output.putString("output", sb.toString());
        return output;
    }

    private Boolean isUserOnline() {
        final ConnectivityManager conMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // notify user you are online
            return true;
        } else {
            // notify user you are not online
            return false;
        }
    }


}
