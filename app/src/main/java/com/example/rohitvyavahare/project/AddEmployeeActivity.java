package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AddEmployeeActivity extends AppCompatActivity
        implements View.OnClickListener {

    private ProgressDialog progress;
    private static final String TAG = "AddEmployeeActivity";
    Button button;
    CheckBox ch1;
    ArrayList<CharSequence> numbers = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add an employee");
        setContentView(R.layout.activity_add_employee);

        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }


        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);

        findViewById(R.id.AddEmployee).setOnClickListener(AddEmployeeActivity.this);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(AddEmployeeActivity.this, InboxActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {

            progress = new ProgressDialog(this);
            progress.setMessage("Loading");
            progress.show();
            if (numbers.size() > 0) {
                numbers.clear();
            }


            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE},
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        Log.d(TAG, "Number:" + c.toString());
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        numbers.add(number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                        progress.dismiss();
                        handleData();
                    }
                }
            }
        }
    }

    private void handleData() {

        final CharSequence contact[] = numbers.toArray(new CharSequence[numbers.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent intent = new Intent(AddEmployeeActivity.this, InboxActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setTitle("Pick a number");
        builder.setItems(contact, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText employeeNumber = (EditText) findViewById(R.id.EditEmployeeId);
                String selectedNumber = contact[which].toString();
                if (selectedNumber.length() == 10) {
                    selectedNumber = "+91" + selectedNumber;
                    employeeNumber.setText(selectedNumber);
                } else {
                    employeeNumber.setText(selectedNumber);
                }

            }
        });
        builder.show();

    }

    @Override
    public void onClick(View v) {

        try {

            int i = v.getId();
            switch (i) {
                case R.id.AddEmployee: {
                    JSONObject account = new JSONObject();

                    ch1 = (CheckBox) findViewById(R.id.EmployeeBand);

                    if (ch1.isChecked()) {
                        account.put("band", "2");
                    } else {
                        account.put("band", "3");
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Add comment");
                    builder.setTitle("Error");
                    builder.setMessage(getString(R.string.empty_msg_no_default_org_pair_org));
                    builder.setCancelable(true);
                    builder.setNeutralButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });


                    boolean flag = true;

                    EditText editId = (EditText) findViewById(R.id.EditEmployeeId);

                    Log.d(TAG, "editId Length " + editId.getText().toString().length());
                    if (editId.getText().toString().trim().equals("") || editId.getText().toString().length() < 11) {
                        editId.setError("Contact Number is required!");
                        flag = false;
                    }

                    EditText editRole = (EditText) findViewById(R.id.EditEmployeeRole);
                    if (editRole.getText().toString().trim().equals("")) {
                        editRole.setError("Employee role is required!");
                        flag = false;
                    }
                    if (flag) {
                        StringBuilder stringBuilder = new StringBuilder(editId.getText().toString().trim());
                        stringBuilder.deleteCharAt(0);
                        account.put("phone_number", stringBuilder.toString());
                        Log.d(TAG, "phone_number: " + stringBuilder.toString());

                        String default_org = prefs.getString("default_org", "null");
                        if (!default_org.equals("null")) {
                            JSONObject d_org = new JSONObject(default_org);
                            if (d_org.has("id") && d_org.has("name") && d_org.has("tag")) {
                                account.put("org_id", d_org.getString("id"));
                                account.put("org_name", d_org.getString("name"));
                                account.put("org_tag", d_org.getString("tag"));
                            } else {
                                AlertDialog alert11 = builder.create();
                                alert11.show();
                            }

                        } else {
                            AlertDialog alert11 = builder.create();
                            alert11.show();
                        }

                        account.put("role", editRole.getText().toString().trim());
                        new PostClass(AddEmployeeActivity.this).execute(account);
                        ch1.setChecked(false);
                        editRole.setText("");
                        editId.setText("");
                    }


                }
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    private class PostClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        PostClass(Context c) {
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


                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.add_employee_endpoint))
                        .build();
                //@TODO add band as query parameter

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                String auth = prefs.getString("uid", "null");

                if (auth.equals("null")) {
                    onPostExecute();
                    //@TODO add alert
                }

                //TODO add id to from feild

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", auth);
                connection.setDoOutput(true);

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

                try {
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } catch (IOException ioe) {
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

                                Toast.makeText(context, "Employee already exist in your organization", Toast.LENGTH_SHORT).show();
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

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
                onPostExecute();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }
}
