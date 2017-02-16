package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog mProgressDialog;
    private static final String TAG = "LoginActivity";
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        String string = prefs.getString("uid", "null");
        if(!string.equals("null")) {
            Intent intent = new Intent(LoginActivity.this, InboxActivity.class);
            intent.putExtra("uid", getString(R.string.private_file));
            startActivity(intent);
            finish();
        }

        findViewById(R.id.btn_send_code).setOnClickListener(LoginActivity.this);

    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.btn_send_code) {

            EditText ed = (EditText) findViewById(R.id.EditCC);
            EditText ed2 = (EditText) findViewById(R.id.EditPhone);
            String cc = ed.getText().toString().trim();

            Log.d(TAG, "CC :"+ ed.getText().toString().trim());
            if (ed2.getText().toString().trim().equals("")) {
                ed2.setError("Phone Number is required!");
            } else {
                if (ed.getText().toString().trim().equals("")) {
                    cc = "1";
                }
                Bundle bundle = new Bundle();
                bundle.putString("phn", ed2.getText().toString().trim());
                bundle.putString("CC", cc);
                new GetClass(this).execute(bundle);
            }

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

                LoginActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        onPostExecute();
                        switch (responseCode) {
                            case 200: {

                                Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                                intent.putExtra("CC", bundle.getString("CC"));
                                intent.putExtra("phn", bundle.getString("phn"));
                                startActivity(intent);
                                break;
                            }

                            default: {
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                });

            } catch (IOException e) {
                onPostExecute();
                Log.d(TAG, "IOException");
                e.printStackTrace();
                return null;
            }
            return null;
        }

        protected void onPostExecute() {
            mProgressDialog.dismiss();
        }

    }
}
