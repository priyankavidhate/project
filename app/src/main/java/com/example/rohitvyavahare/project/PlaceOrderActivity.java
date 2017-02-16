package com.example.rohitvyavahare.project;

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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PlaceOrderActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private static final String TAG = "PlaceOrderActivity";
    private SharedPreferences prefs;
    private String selected_org;
    private String[] stringArray;
    private JSONArray arr;
    private int selected_position;
    private JSONObject dorg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        setTitle("Place an order");
        final HashMap<String, Integer>nameToid;
        ViewSwitcher switcher;
        try {

            Toolbar toolbar;
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            try{

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);

            }
            catch (java.lang.NullPointerException e){
                e.printStackTrace();
            }

            prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
            String default_org = prefs.getString("default_org", "{type : null}");
            dorg = new JSONObject(default_org);
            String paired_orgs = prefs.getString(dorg.getString("name") + R.string.paired_orgs, "[{type : null}]");
            arr = new JSONArray(paired_orgs);

            Log.d(TAG, "Paired orgs :" + paired_orgs);

            if(!paired_orgs.equals("[{type : null}]") && arr.length() > 0 && !default_org.equals("{type : null}")){
                switcher = (ViewSwitcher) findViewById(R.id.my_switcher_3);
                switcher.showNext();

                ArrayList<String> orgs_arr = new ArrayList<>();
                nameToid = new HashMap<>();
                for(int i=0; i< arr.length(); i++){

                    JSONObject obj = arr.getJSONObject(i);
                    if(obj.has("name")){
                        orgs_arr.add(obj.getString("name"));
                        nameToid.put(obj.getString("name"), i);
                    }

                }

                for(String key : nameToid.keySet()){
                    Log.d(TAG, "nameToid key : " + key + " value :" + nameToid.get(key));
                }

                Set<String> hs = new HashSet<>();
                hs.addAll(orgs_arr);
                orgs_arr.clear();
                orgs_arr.addAll(hs);
                final int[] result = new int [1];

                final CharSequence org_names[] =  orgs_arr.toArray(new String[0]);
                final TextView orgName = (TextView) findViewById(R.id.OrgName);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Pick an organization");
                builder.setItems(org_names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result[0] = which;
                        String selected_org = org_names[which].toString();
                        orgName.setText("Order to Organization : " + selected_org);
                    }
                });
                builder.show();

                Button button;

                button = (Button) findViewById(R.id.PlaceOrder);
                button.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View arg0) {

                        try {

                            boolean flag = true;

                            if (orgName.getText().toString().trim().length() == 0 ) {
                                orgName.setError("Comapany name is required!");
                                flag = false;
                            }

                            EditText editItem = (EditText) findViewById(R.id.EditItem);
                            if (editItem.getText().toString().trim().equals("")) {
                                editItem.setError("Item name is required");
                                flag = false;
                            }
                            if (!Character.isLetter(editItem.getText().toString().trim().charAt(0))) {
                                editItem.setError("Item should start with letter");
                                flag = false;
                            }

                            EditText editQty = (EditText) findViewById(R.id.EditQuantity);
                            if (editQty.getText().toString().trim().equals("")) {
                                editQty.setError("Quantity is required");
                                flag = false;
                            }

                            EditText editMsg = (EditText) findViewById(R.id.EditMessage);

                            if (flag) {
                                JSONObject order_obj = new JSONObject();
                                JSONObject order_details = new JSONObject();
                                order_details.put("item", editItem.getText().toString().trim());
                                order_details.put("quantity", Double.parseDouble(editQty.getText().toString().trim()));
                                String a_id = prefs.getString("uid", "null");

                                order_details.put("status", "created");

                                Log.d(TAG, "Total paired orgs :" + arr.length() + " , org chosen :" + result[0]);

                                Log.d(TAG, "Selected org index :" + nameToid.get(orgName.getText().toString().split("Order to Organization : ")[1]));

                                JSONObject org = arr.getJSONObject(nameToid.get(orgName.getText().toString().split("Order to Organization : ")[1]));
                                if((dorg.has("id") || dorg.has("_id")) && (org.has("id") || org.has("_id"))){
                                    String to = org.has("id") ? org.getString("id") : org.getString("_id");
                                    order_details.put("to", to);
                                    String from = dorg.has("id") ? dorg.getString("id") : dorg.getString("_id");
                                    order_details.put("from", from);

                                    if(editMsg.getText().toString().trim().length() > 0 && !a_id.equals("null")){
                                        JSONArray messages =  new JSONArray();
                                        JSONObject msg = new JSONObject();
                                        msg.put("id", from);
                                        msg.put("text", editMsg.getText().toString().trim());
                                        msg.put("account_id", a_id);
                                        messages.put(msg);
                                        order_details.put("messages", messages);

                                    }

                                    Log.d(TAG, "Arr :" + arr.toString());

                                    Log.d(TAG, "Placing an order to :" + org.toString());
                                    order_obj.put("to", org);
                                    order_obj.put("from", dorg);
                                    order_obj.put("order", order_details);

                                    new PostClass(PlaceOrderActivity.this).execute(order_obj);
                                    editQty.setText("");
                                    editItem.setText("");
                                    editMsg.setText("");
                                }
                                else {

                                    Log.d(TAG, "dorg or org has not id");

                                }
                            }

                        } catch (org.json.JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(PlaceOrderActivity.this, InboxActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }


    private class PostClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        PostClass(Context c){
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

                JSONObject order = params[0];
                final JSONObject from = new JSONObject(order.getString("from"));
                final JSONObject to = new JSONObject(order.getString("to"));

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.org))
                        .appendPath(from.getString("id"))
                        .appendPath(getString(R.string.order))
                        .build();
                //@TODO add band as query parameter

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                Log.d(TAG, "url: " + url.toString());

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
                                    SharedPreferences.Editor editor = prefs.edit();
                                    String p_orgs = prefs.getString(from.getString("id")  + R.string.outbox, "null");

                                    Log.d(TAG, "paired orgs for outbox before :"+ p_orgs);


                                    String[] outbox = p_orgs.split(",");
                                    int i=0;
                                    for(;i < outbox.length; i++){
                                        if(outbox[i].equals(to.getString("name"))){
                                            break;
                                        }
                                    }
                                    if(i >= outbox.length){
                                        if(p_orgs.substring(p_orgs.length() - 1).equals(',')){
                                            p_orgs = p_orgs + to.getString("name");
                                        }
                                        else {
                                            p_orgs = p_orgs + "," + to.getString("name");
                                        }
                                    }

                                    Log.d(TAG, "paired orgs for outbox after :"+ p_orgs);

                                    editor.putString(from.getString("id") + R.string.outbox, p_orgs);

                                    String outbox_orders = prefs.getString(to.getString("name") + R.string.outbox, "null");
                                    JSONArray orders;
                                    if(outbox_orders.equals("null")){
                                        orders = new JSONArray();
                                    }
                                    else {
                                        orders = new JSONArray(outbox_orders);
                                    }

                                    JSONObject newOrder = new JSONObject(sb.toString());

                                    orders.put(newOrder);

                                    editor.putString(to.getString("name") + R.string.outbox, orders.toString());
                                    editor.commit(); //TODO research on apply

                                    JSONObject res = new JSONObject(sb.toString());
                                    Log.d(TAG, "response :" + res);
                                    builder.setTitle("Success");
                                    builder.setMessage("Order Id: " + res.getString("order_id"));
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


            } catch (IOException | org.json.JSONException e) {
                // TODO Auto-generated catch block
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
