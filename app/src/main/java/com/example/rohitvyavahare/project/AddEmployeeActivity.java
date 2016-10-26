package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.rohitvyavahare.project.R.string.account;

public class AddEmployeeActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private static final String TAG = "AddEmployeeActivity";
    Button button;
    CheckBox ch1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setTitle("Add an employee");
            setContentView(R.layout.activity_add_employee);
            final JSONObject act = new JSONObject(getIntent().getStringExtra("account"));

            button = (Button) findViewById(R.id.AddEmployee);
            button.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View arg0) {

                    try {
                        JSONObject account = new JSONObject();

                        ch1 = (CheckBox) findViewById(R.id.EmployeeBand);

                        if (ch1.isChecked()) {
                            account.put("band", "2");
                        } else {
                            account.put("band", "3");
                        }

                        boolean flag = true;

                        EditText editId = (EditText) findViewById(R.id.EditEmployeeId);
                        if (editId.getText().toString().trim().equals("")) {
                            editId.setError("Email id is required!");
                            flag = false;
                        }

                        if (!editId.getText().toString().toLowerCase().contains("gmail")) {
                            editId.setError("Right now we support only gmail accounts");
                            Toast.makeText(AddEmployeeActivity.this, "Sorry, Right now we support only gmail accounts", Toast.LENGTH_SHORT).show();
                            flag = false;
                        }

                        EditText editRole = (EditText) findViewById(R.id.EditEmployeeRole);
                        if (editRole.getText().toString().trim().equals("")) {
                            editRole.setError("Employee role is required!");
                            flag = false;
                        }
                        if(flag){
                            account.put("email", editId.getText().toString().trim());
                            Log.d(TAG, "org: " + act.getString("org"));
                            account.put("org", act.getString("org"));
                            account.put("first_name", "First Name");
                            account.put("last_name", "Last Name");
                            account.put("role", editRole.getText().toString().trim());
                            account.put("confirm", "false");
                            new PostClass(AddEmployeeActivity.this).execute(account);
                            ch1.setChecked(false);
                            editRole.setText("");
                            editId.setText("");
                        }

                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    private class PostClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        public PostClass(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            try {

                String call = getString(R.string.server_url) + getString(account);
                Log.d(TAG, "url:" + call);
                URL url = new URL(call);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                Log.d(TAG, "params:" + params.toString());

                final JSONObject account = params[0];

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(account.toString());
                dStream.flush();
                dStream.close();

                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'POST' request to URL : :" + url);
                Log.d(TAG, "Post parameters : " + account.toString());
                Log.d(TAG, "Response Code : " + responseCode);

                final int response = responseCode;

                final StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader br;

                try{
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                }
                catch (IOException ioe) {
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();


                AddEmployeeActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();

                        switch (response) {

                            case 200: {
                                Intent intent = new Intent(AddEmployeeActivity.this, AddEmployeeActivity.class);
                                //startActivity(intent);
                                Toast.makeText(context, "Successfully added employee", Toast.LENGTH_SHORT).show();
                                break;
                            }

                            case 409: {
                                if (sb.toString().toLowerCase().contains("org")) {
                                    Toast.makeText(context, "Employee already exist in your organization", Toast.LENGTH_SHORT).show();
                                } else {
                                    String msg = "Opsss Employee already exist with another org, Right now we don't support for person belonging to multiple org";
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }

                            case 500: {
                                Log.d(TAG, "500 code:");
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                });

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            } catch (IOException e) {
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }
}
