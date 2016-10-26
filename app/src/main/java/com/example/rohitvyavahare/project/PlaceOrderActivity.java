package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class PlaceOrderActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private static final String TAG = "PlaceOrderActivity";
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        setTitle("Place an order");
        try {
            final JSONObject act = new JSONObject(getIntent().getStringExtra("account"));

            button = (Button) findViewById(R.id.PlaceOrder);
            button.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View arg0) {

                    try {
                        JSONObject account = new JSONObject();

                        boolean flag = true;

                        EditText editToOrg = (EditText) findViewById(R.id.EditToOrg);
                        if (editToOrg.getText().toString().trim().equals("")) {
                            editToOrg.setError("Comapany name is required!");
                            flag = false;
                        }

                        EditText editItem = (EditText) findViewById(R.id.EditItem);
                        if (editItem.getText().toString().trim().equals("")) {
                            editItem.setError("Item name is required");
                            flag = false;
                        }

                        EditText editQty = (EditText) findViewById(R.id.EditQuantity);
                        if (editQty.getText().toString().trim().equals("")) {
                            editQty.setError("Quantity is required");
                            flag = false;
                        }

                        EditText editMsg = (EditText) findViewById(R.id.EditMessage);

                        if (flag) {
                            account.put("to", editToOrg.getText().toString().trim());
                            account.put("item", editItem.getText().toString().trim());
                            account.put("quantity", Double.parseDouble(editQty.getText().toString().trim()));
                            account.put("message", editMsg.getText().toString().trim());
                            account.put("status", "created");
                            account.put("from", act.getString("org"));
                            new PostClass(PlaceOrderActivity.this).execute(account);
                            editQty.setText("");
                            editToOrg.setText("");
                            editItem.setText("");
                            editMsg.setText("");
                        }

                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    private class PostClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        public PostClass(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            try {

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                Log.d(TAG, "params:" +  params.toString());
                JSONObject order = params[0];

                String call = getString(R.string.server_url) + getString(R.string.org) + "/" + order.getString("from") + getString(R.string.order);
                Log.d(TAG, "url:" + call);
                URL url = new URL(call);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(order.toString());
                dStream.flush();
                dStream.close();

                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'POST' request to URL : :" +  url);
                Log.d(TAG, "Post parameters : " + order.toString());
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

                PlaceOrderActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        onPostExecute();
                        switch (response) {
                            case 200: {

                                try{

                                    JSONObject res = new JSONObject(sb.toString());
                                    Log.d(TAG, "response :" + res);
                                    builder.setTitle("Success");
                                    builder.setMessage("Order Id: " + res.getString("id"));
                                    builder.setCancelable(true);
                                    builder.setNeutralButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alert11 = builder.create();
                                    alert11.show();
                                    break;

                                }
                                catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Order Placed Successfully, Something went wrong while retrieving order id ", Toast.LENGTH_SHORT).show();
                                    onPostExecute();
                                    break;
                                }

                            }
                            case 403: {

                                try{

                                    JSONObject res = new JSONObject(sb.toString());
                                    Log.d(TAG, "response :" + res);
                                    builder.setTitle("Error");
                                    builder.setMessage(res.getString("status"));
                                    builder.setCancelable(true);
                                    builder.setNeutralButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alert11 = builder.create();
                                    alert11.show();
                                    break;

                                }
                                catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                    onPostExecute();
                                    break;
                                }

                            }

                            case 500: {

                                try{

                                    JSONObject res = new JSONObject(sb.toString());
                                    Log.d(TAG, "response :" + res);
                                    builder.setTitle("Error");
                                    builder.setMessage(res.getString("status"));
                                    builder.setCancelable(true);
                                    builder.setNeutralButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alert11 = builder.create();
                                    alert11.show();
                                    break;

                                }
                                catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                    onPostExecute();
                                    break;
                                }

                            }
                        }
                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            } catch (org.json.JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }
}
