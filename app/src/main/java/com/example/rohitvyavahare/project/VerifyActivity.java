package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VerifyActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog mProgressDialog;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        findViewById(R.id.btn_verify_code).setOnClickListener(VerifyActivity.this);
        findViewById(R.id.link_resend).setOnClickListener(VerifyActivity.this);
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.btn_verify_code) {

            Log.d(TAG, "verify click");
            Bundle bundle = getIntent().getExtras();
            EditText ed = (EditText) findViewById(R.id.EditCode);
            if (ed.getText().toString().trim().equals("")) {
                ed.setError("Code is required!");
            } else {
                bundle.putString("code", ed.getText().toString());
                new GetClass(this).execute(bundle);
            }
        }
        else if(i == R.id.link_resend) {

            new VerifyActivity.GetCodeClass(this).execute(getIntent().getExtras());

        }
    }

    private class GetClass extends AsyncTask<Bundle, Void, Void> {

        private final Context context;

        public GetClass(Context c) {
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
                final Bundle bundle = params[0];
                final String cc = bundle.getString("CC");
                final String phn = bundle.getString("phn");
                final String code = bundle.getString("code");

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.code))
                        .appendQueryParameter("phone_number", phn)
                        .appendQueryParameter("verification_code", code)
                        .appendQueryParameter("country_code", cc)
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + uri.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                final int responseCode = connection.getResponseCode();

                System.out.println("\nSending 'GET' request to URL : " + url);
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

                VerifyActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            onPostExecute();

                            onPostExecute();
                            switch (responseCode) {
                                case 200: {

                                    Log.d(TAG, "200 case");

                                    Intent intent = new Intent(VerifyActivity.this, SignUpForm.class);
                                    intent.putExtra(getString(R.string.phone_number), cc + phn);
                                    startActivity(intent);
                                    finish();
                                    break;
                                }

                                default: {
                                    JSONObject obj = new JSONObject(sb.toString());

                                    Log.d(TAG, "going to default");

                                    if(obj.has("message")){
                                        Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error " + e.getLocalizedMessage());
                            Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        protected void onPostExecute() {
            mProgressDialog.dismiss();
        }

    }

    private class GetCodeClass extends AsyncTask<Bundle, Void, Void> {

        private final Context context;

        public GetCodeClass(Context c) {
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
                final Bundle bundle = params[0];
                String cc = bundle.getString("CC");
                String phn = bundle.getString("phn");

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.authy))
                        .appendPath(cc)
                        .appendPath(phn)
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());


                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                final int responseCode = connection.getResponseCode();

                System.out.println("\nSending 'GET' request to URL : " + url);
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

                VerifyActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        onPostExecute();
                        switch (responseCode) {
                            case 200: {

                                Log.d(TAG, "200 from get code");

                                break;
                            }

                            default: {

                                Log.d(TAG, "going to default");
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        protected void onPostExecute() {
            mProgressDialog.dismiss();
        }

    }

}
