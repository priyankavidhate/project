package com.bigital.rohitvyavahare.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by rohitvyavahare on 1/18/17.
 */

public class Utils {

    private static final String TAG = "Utils";

    public Utils() {
    }

//    void updateActivity(Context c) {
//
//        try {
//
//            SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
//            SharedPreferences.Editor editor;
//
//            String auth = prefs.getString("uid", "null");
//
//            Log.d(TAG, "auth " + auth);
//            if (auth.equals("null")) {
//                throw new IOException("Auth is not defined");
//                //@TODO add alert
//            }
//
//            Uri uri = new Uri.Builder()
//                    .scheme("http")
//                    .encodedAuthority(c.getString(R.string.server_ur_templ))
//                    .path(c.getString(R.string.record_activity))
//                    .appendPath(auth)
//                    .build();
//
//            URL url = new URL(uri.toString());
//            Log.d(TAG, "url:" + url.toString());
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("PUT");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("Authorization", auth);
//            connection.setDoOutput(true);
//
//
//            int responseCode = connection.getResponseCode();
//
//            Log.d(TAG, "Sending 'Put' request to URL : :" + url);
//            Log.d(TAG, "Put parameters : " + auth);
//            Log.d(TAG, "Response Code : " + responseCode);
//
//
//            final StringBuilder sb = new StringBuilder();
//            String line;
//            BufferedReader br;
//
//            try {
//                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            } catch (IOException ioe) {
//                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//            }
//
//            while ((line = br.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//            br.close();
//
//            switch (responseCode) {
//
//                case 201: {
//                    Log.d(TAG, "Updated acitivity");
//                    editor = prefs.edit();
//                    editor.putString(c.getString(R.string.last_active), ""+System.currentTimeMillis());
//                    editor.commit();
//                    break;
//                }
//                default: {
//                    break;
//
//                }
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    void getPairedOrgs(Context c, String id, String tag) {
//
//        try {
//
//            SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
//            SharedPreferences.Editor editor;
//
//            String auth = prefs.getString("uid", "null");
//
//            Log.d(TAG, "auth " + auth);
//            if (auth.equals("null")) {
//                throw new IOException("Auth is not defined");
//                //@TODO add alert
//            }
//
//            Uri uri = new Uri.Builder()
//                    .scheme("http")
//                    .encodedAuthority(c.getString(R.string.server_ur_templ))
//                    .path(c.getString(R.string.get_paired_orgs))
//                    .appendPath(id)
//                    .build();
//
//            URL url = new URL(uri.toString());
//            Log.d(TAG, "url:" + url.toString());
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("Authorization", auth);
//
//            int responseCode = connection.getResponseCode();
//
//            Log.d(TAG, "Sending 'Post' request to URL : :" + url);
//            Log.d(TAG, "Get params : " + id);
//            Log.d(TAG, "Response Code : " + responseCode);
//
//
//            final StringBuilder sb = new StringBuilder();
//            String line;
//            BufferedReader br;
//
//            try {
//                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            } catch (IOException ioe) {
//                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//            }
//
//            while ((line = br.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//            br.close();
//
//            Log.d(TAG, "Response for get org pairs :" + sb.toString());
//
//            switch (responseCode) {
//
//                case 200: {
//                    Log.d(TAG, "got orgs");
//                    JSONArray arr = new JSONArray(sb.toString());
//                    if(arr.length() > 0){
//                        Log.d(TAG, "adding " + arr.length() + " paired orgs for " + tag );
//                        editor = prefs.edit();
//                        editor.putString(tag + c.getString(R.string.paired_orgs), arr.toString());
//                        editor.apply();
//                    }
//
//                    break;
//                }
//                default: {
//                    break;
//
//                }
//            }
//
//
//        } catch (IOException | JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

//    void sendFeedback(Context c, String message) {
//
//        try {
//
//            SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
//
//            String auth = prefs.getString("uid", "null");
//
//            Log.d(TAG, "auth " + auth);
//            if (auth.equals("null")) {
//                throw new IOException("Auth is not defined");
//                //@TODO add alert
//            }
//
//            Uri uri = new Uri.Builder()
//                    .scheme("http")
//                    .encodedAuthority(c.getString(R.string.server_ur_templ))
//                    .path(c.getString(R.string.feedback))
//                    .build();
//
//            URL url = new URL(uri.toString());
//            Log.d(TAG, "url:" + url.toString());
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("Authorization", auth);
//            connection.setDoOutput(true);
//
//            JSONObject obj = new JSONObject();
//            obj.put("message", message);
//
//            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
//            dStream.writeBytes(obj.toString());
//            dStream.flush();
//            dStream.close();
//
//
//            int responseCode = connection.getResponseCode();
//
//            Log.d(TAG, "Sending 'Post' request to URL : :" + url);
//            Log.d(TAG, "Post body : " + obj.toString());
//            Log.d(TAG, "Response Code : " + responseCode);
//
//
//            final StringBuilder sb = new StringBuilder();
//            String line;
//            BufferedReader br;
//
//            try {
//                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            } catch (IOException ioe) {
//                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//            }
//
//            while ((line = br.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//            br.close();
//
//            switch (responseCode) {
//
//                case 201: {
//                    Log.d(TAG, "Posted feedback");
//                    break;
//                }
//                default: {
//                    break;
//
//                }
//            }
//
//
//        } catch (IOException | JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception | java.lang.OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public Bitmap getBitmapFromURL(String src, String tag, Context c) {
        try {
            SharedPreferences prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
            SharedPreferences.Editor editor;
            editor = prefs.edit();
            Log.d(TAG, "Src :"+ src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.d(TAG, "Bitmap returned");
            editor.putString(tag + "_pic", BitMapToString(myBitmap));
            editor.apply();
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

    public SpannableStringBuilder setTextWithSpan(String text, String spanText, StyleSpan style) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        int start = text.indexOf(spanText);
        int end = start + spanText.length();
        Log.d(TAG, "start :" + start + " end :" + end);
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    public String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    int countInPorgressOrders(JSONArray orders){

        int count=0;

        try{

            Log.d(TAG, "Total orders :" + orders.length());

            for(int i=0; i<orders.length(); i++){
                JSONObject obj = orders.getJSONObject(i);
                if(obj.has("status") && !obj.getString("status").equals("receiver_complete") && !obj.getString("status").equals("cancelled") ){
                    count++;

                }
            }

            Log.d(TAG, "In process orders :" + count);

            return count;

        }
        catch (JSONException e){
            e.printStackTrace();
            Log.d(TAG, "In process orders :" + count);
            return  count;
        }
        

    }

    public JSONArray sortJsonArray(JSONArray jsonArr){
        JSONArray sortedJsonArray = new JSONArray();

        try{


            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }
            Collections.sort( jsonValues, new Comparator<JSONObject>() {
                //You can change "Name" with "ID" if you want to sort by ID
                private static final String KEY_NAME = "created";

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();

                    try {
                        valA = (String) a.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                    }
                    catch (JSONException e) {
                        //do something
                    }

                    return -valA.compareTo(valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
            });

            for (int i = 0; i < jsonArr.length(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }
            return sortedJsonArray;

        }
        catch (Exception e){
            e.printStackTrace();
            return sortedJsonArray;
        }

    }
}
