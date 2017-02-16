package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OrderDetailsActivity";
    private List<ListData> mDataList = new ArrayList<>();
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private ProgressDialog progress;
    private Bundle bundle;
    private int band = 0;
    private boolean noComment = false;
    private String message;
    private String orders;
    private HashMap<String, String> possibleStatus = new HashMap<>();
    private HashMap<String, String> outboxStatus = new HashMap<>();
    private HashMap<String, String> status = new HashMap<>();
    private String type = "inbox";
    private JSONObject d_org;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        bundle = getIntent().getExtras();
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);

        orders = bundle.get("orders").toString();
        type = bundle.getString("type");

        mDrawableBuilder = TextDrawable.builder()
                .round();

        findViewById(R.id.back_btn).setOnClickListener(OrderDetailsActivity.this);
        findViewById(R.id.btn_cancel_order).setOnClickListener(OrderDetailsActivity.this);
        findViewById(R.id.btn_change_status).setOnClickListener(OrderDetailsActivity.this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        fab.setVisibility(View.INVISIBLE);

        try {

            possibleStatus.put("created", "Mark acknowledged");
            possibleStatus.put("acknowledged", "Mark shipped");
            possibleStatus.put("shipping", "Mark order completion");
            possibleStatus.put("cancelled", "Cancel Order");
            possibleStatus.put("sender_completed", "Mark order received");


            outboxStatus.put("created", "Created");
            outboxStatus.put("acknowledged", "Acknowledged by seller");
            outboxStatus.put("shipping", "Order has been shipped");
            outboxStatus.put("sender_completed", "Order delivered by seller");


            status.put("created", "Created");
            status.put("acknowledged", "Acknowledged");
            status.put("shipping", "Shipping");
            status.put("sender_completed", "Order delivered by seller");
            status.put("receiver_completed", "Order received by seller");

            if (bundle.get("order_details").toString().equals("null")) {

                Toast.makeText(this.getApplicationContext(), "Order details are empty", Toast.LENGTH_SHORT).show();


            } else {
                JSONObject obj = new JSONObject(bundle.get("order_details").toString());

                Log.d(TAG, "Obj for order detail:" + obj.toString());

                TextView textView;
                ListView listView = (ListView) findViewById(R.id.listView);

                d_org = new JSONObject(prefs.getString("default_org", "null"));
                if (d_org.has("band") && Integer.parseInt(d_org.getString("band")) < 2) {
                    FrameLayout footerLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.fragment_order, null);
                    footerLayout.findViewById(R.id.btn_add_comment).setOnClickListener(OrderDetailsActivity.this);
                    listView.addHeaderView(footerLayout);

//                    listView.addFooterView(footerLayout);
                }
                else {
                    Button btn = (Button) findViewById(R.id.btn_cancel_order);
                    btn.setVisibility(View.GONE);
                }

                if (obj.has("item")) {
                    textView = (TextView) findViewById(R.id.Item);
                    String text = obj.getString("item").substring(0, 1).toUpperCase() + obj.getString("item").substring(1);
                    textView.setText(text);
                }
                if (obj.has("created")) {
                    textView = (TextView) findViewById(R.id.Line1);
                    String str;
                    if (obj.has("org_name")) {
                        str = "Created by" + obj.getString("org_name") + "on ";
                    } else {
                        str = "Created on ";
                    }
                    DateFormat dffrom = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
                    DateFormat dfto = new SimpleDateFormat("MMMM dd, yyyy", Locale.UK);
                    Date today = dffrom.parse(obj.getString("created"));
                    str = str + dfto.format(today);
                    textView.setText(str);
                }
                if (obj.has("last_update")) {
                    textView = (TextView) findViewById(R.id.Line2);
                    String str = "Last updated on ";
                    DateFormat dffrom = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
                    DateFormat dfto = new SimpleDateFormat("MMMM dd, yyyy", Locale.UK);
                    Date today = dffrom.parse(obj.getString("last_update"));
                    str = str + dfto.format(today);
                    textView.setText(str);
                }
                if (obj.has("quantity")) {
                    textView = (TextView) findViewById(R.id.Qunatity);
                    textView.setText("Quantity " + obj.getString("quantity"));
                }

                if (obj.has("messages")) {

                    Log.d(TAG, "Order has messages");
                    JSONArray arr = new JSONArray(obj.getString("messages"));
                    String default_org = prefs.getString("default_org", "null");
                    if (!default_org.equals("null")) {

                        for (int i = 0; i < arr.length(); i++) {
                            if (d_org.has("band") && Integer.parseInt(d_org.getString("band")) > 2) {
                                Log.d(TAG, "Order has messages but band is > 2");
                                break;
                            }
                            if (d_org.has("id") && d_org.has("name") && arr.getJSONObject(i).has("id") && d_org.getString("id").equals(arr.getJSONObject(i).getString("id")) && arr.getJSONObject(i).has("text")) {
                                Log.d(TAG, "Adding message");
                                mDataList.add(new ListData(d_org.getString("name"), arr.getJSONObject(i).getString("text")));
                            } else if (getIntent().getExtras().get("org_name") != null && arr.getJSONObject(i).has("text")) {
                                Log.d(TAG, "Adding message 2 :" + getIntent().getExtras().get("org_name").toString());
                                mDataList.add(new ListData(getIntent().getExtras().get("org_name").toString(), arr.getJSONObject(i).getString("text")));
                            }
                        }

                    }
                    listView.setAdapter(new SampleAdapter());

                } else {
                    mDataList.add(new ListData("No comments yet.", ""));
                    noComment = true;
                    listView.setAdapter(new SampleAdapter());
                }

                setStatus(obj, type);
            }

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void setStatus(JSONObject obj, String type) {

        try {
            TextView article;
            ViewSwitcher switcher;
            Button btn;

            Log.d("Status of order :", obj.getString("status"));

            if (obj.getString("status").equals("sender_completed")) {

                article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);

                if (type.equals("outbox")) {
                    switcher = (ViewSwitcher) findViewById(R.id.my_switcher_2);
                    switcher.showNext();
                    btn = (Button) findViewById(R.id.btn_change_status);
                    btn.setText(possibleStatus.get(obj.getString("status")));
                    article.setText(R.string.sender_completed);
                } else {
                    article.setText(R.string.sender_completed);
                }

            } else if (obj.getString("status").equals("receiver_completed")) {

                article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                article.setText(R.string.receiver_completed);

            } else if (obj.getString("status").equals("cancelled")) {

                article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                article.setText(R.string.cancelled);
                btn = (Button) findViewById(R.id.btn_cancel_order);
                btn.setVisibility(View.GONE);

            } else {

                if (type.equals("outbox")) {

                    article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                    article.setText(outboxStatus.get(obj.getString("status")));

                } else {

                    switcher = (ViewSwitcher) findViewById(R.id.my_switcher_2);
                    switcher.showNext();
                    btn = (Button) findViewById(R.id.btn_change_status);
                    if((obj.getString("status").equals("created") || obj.getString("status").equals("acknowledged")) && d_org.has("band") && Integer.parseInt(d_org.getString("band")) > 2){
                        btn.setVisibility(View.GONE);
                    }
                    else {
                        btn.setText(possibleStatus.get(obj.getString("status")));
                    }

                }
            }
            if(obj.getString("status").equals("shipping") || obj.getString("status").equals("sender_completed") || obj.getString("status").equals("receiver_completed")){
                btn = (Button) findViewById(R.id.btn_cancel_order);
                btn.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        Log.d(TAG, "something clicked");
        if (i == R.id.back_btn) {

            Log.d(TAG, "back btn clicked");

            Intent intent = new Intent(OrderDetailsActivity.this, OrdersActivity.class);
            Bundle bundle = new Bundle();

            Log.d(TAG, "Orders :" + orders);
            bundle.putString("orders", orders);
            bundle.putString("type", getIntent().getExtras().get("type").toString());
            bundle.putString("org_name", getIntent().getExtras().get("org_name").toString());
            intent.putExtras(bundle);
            startActivity(intent);

        } else if (i == R.id.btn_cancel_order) {

            Log.d(TAG, "Cancel order btn clicked");

            try {
                JSONObject obj = new JSONObject(getIntent().getExtras().get("order_details").toString());
                obj.put("status", "cancelled");
                Bundle bundle = new Bundle();
                bundle.putString("obj", obj.toString());
                bundle.putString("message", "false");
                new PutClass(OrderDetailsActivity.this).execute(bundle);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
                Toast.makeText(OrderDetailsActivity.this, "Something went wrong while retrieving Order Details, Please try again", Toast.LENGTH_SHORT).show();
            }

        } else if (i == R.id.btn_change_status) {
            Log.d(TAG, "Change status btn clicked");
            Bundle bundle = new Bundle();
            bundle.putString("obj", getIntent().getExtras().get("order_details").toString());
            bundle.putString("message", "false");
            new PutClass(OrderDetailsActivity.this).execute(bundle);
        } else if (i == R.id.btn_add_comment) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add comment");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setMaxLines(2);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        message = input.getText().toString();
                        JSONObject obj = new JSONObject(bundle.get("order_details").toString());
                        JSONArray arr;
                        if (obj.has("messages")) {
                            arr = new JSONArray(obj.getString("messages"));

                        } else {
                            arr = new JSONArray();
                        }

                        JSONObject msg = new JSONObject();
                        JSONObject d_org = new JSONObject(prefs.getString("default_org", "null"));
                        Log.d(TAG, "default org :" + d_org.toString());
                        if (d_org.has("id") && d_org.has("name")) {
                            msg.put("id", d_org.getString("id"));
                            msg.put("text", input.getText().toString().trim());
                            prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
                            String auth = prefs.getString("uid", "null");
                            if (!auth.equals("null")) {
                                msg.put("account_id", auth);
                                arr.put(msg);
                                obj.put("messages", arr);
                                Log.d(TAG, "order_details after message add :" + obj.toString());
                                Bundle bundle = new Bundle();
                                bundle.putString("obj", obj.toString());
                                bundle.putString("message", "true");
                                new PutClass(OrderDetailsActivity.this).execute(bundle);

                            }

                        } else {

                            Log.d(TAG, "Default org is null");
                            Toast.makeText(OrderDetailsActivity.this, "Please select default org", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(OrderDetailsActivity.this, OrdersActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    private class SampleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public ListData getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(OrderDetailsActivity.this, R.layout.list_order_commets, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData item = getItem(position);

            // provide support for selected state
            updateCheckedState(holder, item);
            holder.textView.setText(item.data);
            holder.textView1.setText(item.data1);

            return convertView;
        }

        private void updateCheckedState(ViewHolder holder, ListData item) {

            Log.d(TAG, "setting image view :" + item.data);
            TextDrawable drawable = mDrawableBuilder.build(String.valueOf(item.data.charAt(0)), mColorGenerator.getColor(item.data));
            if (item.data.equals("No comments yet.")) {
                holder.imageView.setImageResource(R.drawable.ic_mail_black_24dp);
            } else {
                holder.imageView.setImageDrawable(drawable);
            }
            holder.view.setBackgroundColor(Color.TRANSPARENT);


        }
    }

    private static class ViewHolder {

        private View view;

        private ImageView imageView;

        private TextView textView, textView1;


        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textView = (TextView) view.findViewById(R.id.textView);
            textView1 = (TextView) view.findViewById(R.id.textView1);
        }
    }

    private static class ListData {

        private String data, data1;

        ListData(String data, String data1) {
            this.data = data;
            this.data1 = data1;
        }
    }

    @Override
    public void onBackPressed() {

        this.onClick(findViewById(R.id.back_btn));
    }

    private class PutClass extends AsyncTask<Bundle, Void, Void> {

        private Context context;

        PutClass(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            try {
                final Bundle bundle = params[0];
                JSONObject b = new JSONObject(bundle.getString("obj"));

                //TODO add to and from orgs in body

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.order))
                        .appendPath(b.getString("id"))
                        .appendQueryParameter("message", bundle.getString("message"))
                        .build();


                //@TODO add band as query parameter

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
                String auth = prefs.getString("uid", "null");

                Log.d(TAG, "auth " + auth);
                if (auth.equals("null")) {
                    onPutExecute();
                    //@TODO add alert
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", auth);
                connection.setDoOutput(true);

                Log.d(TAG, "default org for order :"+ d_org.toString());

                String paired_orgs = prefs.getString(d_org.getString("name") + R.string.paired_orgs, "null");
                JSONObject to_org = new JSONObject();

                Log.d(TAG, "Put request for type " + type);

                Log.d(TAG, "paired_arr :" + paired_orgs);

                JSONArray paired_arr = new JSONArray(paired_orgs);

                Log.d(TAG, "org_name :"  + getIntent().getExtras().get("org_name").toString());

                for(int i=0; i<paired_arr.length(); i++){
                    if(paired_arr.getJSONObject(i).has("name") && paired_arr.getJSONObject(i).getString("name").equals(getIntent().getExtras().get("org_name").toString())){
                        to_org = paired_arr.getJSONObject(i);
                    }
                }

                if(!to_org.has("id")){
                    throw new NullPointerException("to_org can't be null");
                }

                JSONObject body = new JSONObject();
                body.put("order", b);

                if(type.equals("inbox")){
                    body.put("to", d_org);
                    body.put("from", to_org);
                    Log.d(TAG, "from org as type is inbox:" + to_org.toString());
                }
                else {
                    body.put("to", to_org);
                    body.put("from", d_org);
                    Log.d(TAG, "to org as type is outbox:" + to_org.toString());
                }

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(body.toString());
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                Log.d(TAG, "params:" + body.toString());

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
                    sb.append(line);
                    sb.append("\n");
                }
                br.close();

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        switch (response) {
                            case 200: {

                                try {
                                    Log.d(TAG, "response from put :" + sb.toString());
                                    JSONObject obj = new JSONObject(sb.toString());
                                    if (bundle.getString("message").equals("true")) {
                                        if (noComment)
                                            mDataList.remove(0);
                                        JSONObject d_org = new JSONObject(prefs.getString("default_org", "null"));
                                        Log.d(TAG, "default org :" + d_org.toString());
                                        if (d_org.has("id") && d_org.has("name")) {
                                            mDataList.add(new ListData(d_org.getString("name"), message));
                                        }
                                    }
                                    editor = prefs.edit();
                                    JSONArray arr = new JSONArray(prefs.getString(getIntent().getExtras().get("org_name").toString(), "null"));

                                    int position = Integer.parseInt(getIntent().getExtras().get("position").toString());
                                    JSONArray newArr = new JSONArray();
                                    for (int i = 0; i < arr.length(); i++) {
                                        if (i == position) {
                                            newArr.put(obj);
                                        } else {
                                            newArr.put(arr.getJSONObject(i));
                                        }
                                    }

                                    Log.d(TAG, "orders before response from server: " + orders);

                                    orders = newArr.toString();

                                    Log.d(TAG, "orders after response from server: " + orders);

                                    editor.putString(getIntent().getExtras().get("org_name").toString(), newArr.toString());
                                    editor.commit();
                                    Intent intent = new Intent(OrderDetailsActivity.this, OrderDetailsActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("order_details", obj.toString());
                                    b.putString("orders", getIntent().getExtras().get("orders").toString());
                                    b.putString("type", getIntent().getExtras().get("type").toString());
                                    b.putString("org_name", getIntent().getExtras().get("org_name").toString());
                                    b.putString("position", Integer.toString(position));
                                    onPutExecute();
                                    intent.putExtras(b);
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException | NullPointerException e) {
                                    e.printStackTrace();
                                }

                                Toast.makeText(context, "Successfully updated order", Toast.LENGTH_SHORT).show();

                                break;
                            }
                            case 400: {
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                onPutExecute();
                                break;

                            }
                            default: {
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                onPutExecute();
                                break;
                            }
                        }
                    }
                });


            } catch (IOException | org.json.JSONException | NullPointerException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPutExecute();
                        Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        void onPutExecute() {
            progress.dismiss();
        }

    }
}
