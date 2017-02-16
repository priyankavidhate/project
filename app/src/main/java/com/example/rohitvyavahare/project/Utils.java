package com.example.rohitvyavahare.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by rohitvyavahare on 1/18/17.
 */

public class Utils {

    private static final String TAG = "Utils";

    public void sendTokentoServer(String refreshToken, String id, Context c) {

        try {

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.firebase_token))
                    .appendPath(id)
                    .build();

            URL url = new URL(uri.toString());
            Log.d(TAG, "url:" + url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", id);
            connection.setDoOutput(true);

            JSONObject obj = new JSONObject();
            obj.put("registration_id", refreshToken);
            obj.put("_id", id);

            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(obj.toString());
            dStream.flush();
            dStream.close();

            Log.d(TAG, "params:" + refreshToken);


            int responseCode = connection.getResponseCode();

            Log.d(TAG, "Sending 'Put' request to URL : :" + url);
            Log.d(TAG, "Put parameters : " + obj.toString());
            Log.d(TAG, "Response Code : " + responseCode);


            final StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br;

            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException ioe) {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            switch (responseCode) {

                case 201: {
                    Log.d(TAG, "Updated refresh token");
                    break;
                }
                default: {
                    break;

                }
            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateActivity(Context c) {

        try {

            SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
            SharedPreferences.Editor editor;

            String auth = prefs.getString("uid", "null");

            Log.d(TAG, "auth " + auth);
            if (auth.equals("null")) {
                throw new IOException("Auth is not defined");
                //@TODO add alert
            }

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.record_activity))
                    .appendPath(auth)
                    .build();

            URL url = new URL(uri.toString());
            Log.d(TAG, "url:" + url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", auth);
            connection.setDoOutput(true);


            int responseCode = connection.getResponseCode();

            Log.d(TAG, "Sending 'Put' request to URL : :" + url);
            Log.d(TAG, "Put parameters : " + auth);
            Log.d(TAG, "Response Code : " + responseCode);


            final StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br;

            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException ioe) {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            switch (responseCode) {

                case 201: {
                    Log.d(TAG, "Updated acitivity");
                    editor = prefs.edit();
                    editor.putString(c.getString(R.string.last_active), ""+System.currentTimeMillis());
                    editor.commit();
                    break;
                }
                default: {
                    break;

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getPairedOrgs(Context c, String id, String name) {

        try {

            SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
            SharedPreferences.Editor editor;

            String auth = prefs.getString("uid", "null");

            Log.d(TAG, "auth " + auth);
            if (auth.equals("null")) {
                throw new IOException("Auth is not defined");
                //@TODO add alert
            }

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.get_paired_orgs))
                    .appendPath(id)
                    .build();

            URL url = new URL(uri.toString());
            Log.d(TAG, "url:" + url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", auth);

            int responseCode = connection.getResponseCode();

            Log.d(TAG, "Sending 'Post' request to URL : :" + url);
            Log.d(TAG, "Get params : " + id);
            Log.d(TAG, "Response Code : " + responseCode);


            final StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br;

            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException ioe) {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            Log.d(TAG, "Response for get org pairs :" + sb.toString());

            switch (responseCode) {

                case 200: {
                    Log.d(TAG, "got orgs");
                    JSONArray arr = new JSONArray(sb.toString());
                    if(arr.length() > 0){
                        Log.d(TAG, "adding " + arr.length() + " paired orgs for " + name );
                        editor = prefs.edit();
                        editor.putString(name + R.string.paired_orgs, arr.toString());
                        editor.apply();
                    }

                    break;
                }
                default: {
                    break;

                }
            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    public void sendFeedback(Context c, String message) {

        try {

            SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);

            String auth = prefs.getString("uid", "null");

            Log.d(TAG, "auth " + auth);
            if (auth.equals("null")) {
                throw new IOException("Auth is not defined");
                //@TODO add alert
            }

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.feedback))
                    .build();

            URL url = new URL(uri.toString());
            Log.d(TAG, "url:" + url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", auth);
            connection.setDoOutput(true);

            JSONObject obj = new JSONObject();
            obj.put("message", message);

            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(obj.toString());
            dStream.flush();
            dStream.close();


            int responseCode = connection.getResponseCode();

            Log.d(TAG, "Sending 'Post' request to URL : :" + url);
            Log.d(TAG, "Post body : " + obj.toString());
            Log.d(TAG, "Response Code : " + responseCode);


            final StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br;

            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException ioe) {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            switch (responseCode) {

                case 201: {
                    Log.d(TAG, "Posted feedback");
                    break;
                }
                default: {
                    break;

                }
            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception | java.lang.OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src", src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap", "returned");
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }

    String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    void setTextWithSpan(TextView textView, String text, String spanText, StyleSpan style) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        int start = text.indexOf(spanText);
        int end = start + spanText.length();
        Log.d(TAG, "start :" + start + " end :" + end);
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(sb);
    }
}
