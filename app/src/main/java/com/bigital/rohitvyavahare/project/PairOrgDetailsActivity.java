package com.bigital.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PairOrgDetailsActivity extends AppCompatActivity  implements View.OnClickListener {

    private static final String TAG = "PairOrgDetailsActivity";
    private String tag = null;
    private ProgressDialog mProgressDialog;
    private SharedPreferences prefs;
    private JSONObject d_org = new JSONObject();
    private JSONArray p_org;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_org_details);

        findViewById(R.id.btn_send_request).setOnClickListener(PairOrgDetailsActivity.this);
        findViewById(R.id.btn_reject).setOnClickListener(PairOrgDetailsActivity.this);
        findViewById(R.id.btn_accept).setOnClickListener(PairOrgDetailsActivity.this);

        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        Bundle bundle = getIntent().getExtras();
        ViewSwitcher switcher;
        TextView textView;

        Log.d(TAG, "type :" + bundle.getString("type"));

        try{

            if(bundle.getString("type").equals("action")){

                switcher = (ViewSwitcher) findViewById(R.id.my_switcher_3);
                switcher.showNext();

                p_org = new JSONArray(bundle.getString("pair_org"));
                d_org = new JSONObject(bundle.getString("second_org"));

            }

            Log.d(TAG, "Org " + getIntent().getStringExtra("org"));
            Log.d(TAG, "org: "+ bundle.getString("org"));

            JSONObject org = new JSONObject(bundle.getString("org"));
            setTitle(org.getString("name"));
            int margin = 0;

            if(org.has("name")){
                textView = (TextView) findViewById(R.id.OrgName);
                textView.setText(org.getString("name"));
            }
            else {
                margin +=50;
            }

            if(org.has("tag")){
                textView = (TextView) findViewById(R.id.Tag);
                textView.setText(org.getString("tag"));
                tag = org.getString("tag");
            }
            else {
                margin +=50;
            }

            if(org.has("branch")){
                textView = (TextView) findViewById(R.id.OrgBranch);
                textView.setText(org.getString("branch"));
            }
            else {
                margin +=50;
            }

            if(org.has("department")){
                textView = (TextView) findViewById(R.id.OrgDepartment);
                textView.setText(org.getString("department"));
            }
            else {
                margin +=50;
            }

            if(org.has("address")){
                textView = (TextView) findViewById(R.id.OrgAddress);
                textView.setText(org.getString("address"));
            }
            else {
                margin +=50;
            }

            if(org.has("country")){
                textView = (TextView) findViewById(R.id.OrgCountry);
                textView.setText(org.getString("country"));
            }
            else {
                margin +=50;
            }

            if(org.has("state")){
                textView = (TextView) findViewById(R.id.OrgState);
                textView.setText(org.getString("state"));
            }
            else {
                margin +=50;
            }

            if(org.has("city")){
                textView = (TextView) findViewById(R.id.OrgCity);
                textView.setText(org.getString("city"));
            }
            else {
                margin +=50;
            }

            if(org.has("zip")){
                textView = (TextView) findViewById(R.id.OrgZip);
                textView.setText(org.getString("zip"));
            }
            else {
                margin +=50;
            }

            if(margin > 0) {

                LinearLayout lt;

                if(bundle.getString("type").equals("action")){
                    lt = (LinearLayout) findViewById(R.id.actionLayout);
                }
                else {
                    lt = (LinearLayout) findViewById(R.id.send_request_btn_layout);

                }

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                margin = 600 - margin;
                params.setMargins(0, margin, 0, 0);
                lt.setLayoutParams(params);

            }

        }
        catch (JSONException e){
            e.printStackTrace();

        }

    }

    @Override
    public void onClick(View v) {

        try{

            Log.d(TAG, "Something clicked");

            int i = v.getId();
            if (i == R.id.btn_send_request) {

                Log.d(TAG, "request button clicked");

                Bundle bundle = new Bundle();
                bundle.putString("tag", tag);
                new PostClass(this).execute(bundle);

            }
            if (i == R.id.btn_accept) {

                Log.d(TAG, "Accept clicked");

                JSONObject body = new JSONObject();
                int position = Integer.parseInt(getIntent().getExtras().getString("position"));
                JSONObject obj = p_org.getJSONObject(position);
                body.put("action", "accept");
                body.put("first_org", obj);
                body.put("second_org", d_org);
                body.put("position", position);
                body.put("id", d_org.getString("id"));
                new ActionClass(this).execute(body);

            }
            if (i == R.id.btn_reject) {

                Log.d(TAG, "Reject clicked");

                JSONObject body = new JSONObject();
                int position = Integer.parseInt(getIntent().getExtras().getString("position"));
                JSONObject obj = p_org.getJSONObject(position);
                body.put("action", "ignore");
                body.put("first_org", obj);
                body.put("second_org", d_org);
                body.put("position", position);
                body.put("id", d_org.getString("id"));
                new ActionClass(this).execute(body);

            }

        }
        catch (JSONException | NullPointerException e){
            e.printStackTrace();

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(PairOrgDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private class PostClass extends AsyncTask<Bundle, Void, Void> {

        private final Context context;

        PostClass(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(this.context);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            try {

                JSONObject body = new JSONObject();
                JSONObject first_org = new JSONObject(getIntent().getStringExtra("org"));
                body.put("first_org", first_org);

                String default_org = prefs.getString("default_org", "null");
                if(default_org.equals("null")){
                    throw new JSONException(getString(R.string.no_org_msg));
                }
                d_org = new JSONObject(default_org);
                body.put("second_org", d_org);

                if(!d_org.has("id")){

                    throw new JSONException("default org need an id");

                }

                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.http))
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.pair_org_request))
                        .appendPath(d_org.getString("id"))
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                String auth = prefs.getString("uid", "null");
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

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(body.toString());
                dStream.flush();
                dStream.close();

                final int responseCode = connection.getResponseCode();

                System.out.println("\nSending 'POST' request to URL : " + url);
                Log.d(TAG, "Post parameters : " + body.toString());
                System.out.println("Response Code : " + responseCode);


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

                PairOrgDetailsActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        onPostExecute();
                        switch (responseCode) {
                            case 200: {

                                new AlertDialog.Builder(context)
                                        .setTitle("Success")
                                        .setMessage("Request sent successfully")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(PairOrgDetailsActivity.this, PairOrgActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                break;
                            }
                            case 409:{

                                new AlertDialog.Builder(context)
                                        .setTitle("Error")
                                        .setMessage("Request already pending")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(PairOrgDetailsActivity.this, PairOrgActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                break;

                            }

                            default: {
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                });

            } catch (IOException |JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        protected void onPostExecute() {
            mProgressDialog.dismiss();
        }

    }

    private class ActionClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        ActionClass(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(this.context);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            try {

                final JSONObject body = params[0];

                if(!body.has("action")){
                    throw new JSONException("body needs to have action");
                }

                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.http))
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.perform_action_on_org))
                        .appendPath(body.getString("id"))
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
                String auth = prefs.getString("uid", "null");

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

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(body.toString());
                dStream.flush();
                dStream.close();

                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'POST' request to URL : :" + url);
                Log.d(TAG, "Post parameters : " + body.toString());
                Log.d(TAG, "Response Code : " + responseCode);

                final int response = responseCode;

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

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();

                        try {

                            switch (response) {

                                case 200: {

                                    SharedPreferences.Editor editor = prefs.edit();
                                    int position = Integer.parseInt(body.getString("position"));

                                    List<String> list = new ArrayList<>();
                                    for(int i = 0; i < p_org.length(); i++){
                                        list.add(p_org.getJSONObject(i).getString("name"));
                                    }
                                    list.remove(position);

                                    if(list.size()>0){

                                        p_org = new JSONArray(Arrays.asList(list));

                                    }
                                    else {
                                        p_org = new JSONArray();
                                    }

                                    if(body.has("action") && body.getString("action").equals("accept") && d_org.has("name")){
//                                        String paired_orgs = prefs.getString(d_org.getString("name") + R.string.paired_orgs, "null");
                                        String paired_orgs = prefs.getString(d_org.getString("tag") + getString(R.string.paired_orgs), "null");
                                        JSONArray arr ;
                                        if(paired_orgs.equals("null")){
                                            arr = new JSONArray();
                                        }
                                        else {
                                            arr = new JSONArray(paired_orgs);

                                        }
                                        arr.put(body.get("first_org"));
//                                        editor.putString(d_org.getString("name") + R.string.paired_orgs, arr.toString());
                                        editor.putString(d_org.getString("tag") + getString(R.string.paired_orgs), arr.toString());

                                    }

//                                    editor.putString(d_org.getString("name") + R.string.incoming_request, p_org.toString());
                                    editor.putString(d_org.getString("tag") + getString(R.string.incoming_request), p_org.toString());

                                    editor.commit();

                                    Intent intent = new Intent(PairOrgDetailsActivity.this, PairOrgActivity.class);
                                    finish();
                                    startActivity(intent);
                                    break;
                                }
                                case 409: {
                                    Toast.makeText(context, "Request already exist", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                default: {
                                    throw new org.json.JSONException("409");
                                }
                            }

                        } catch (org.json.JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            onPostExecute();
                        }

                    }
                });

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
            return null;
        }

        protected void onPostExecute() {
            mProgressDialog.dismiss();
        }

    }
}
