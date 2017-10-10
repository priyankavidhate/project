package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.rohitvyavahare.extensions.Contact.ContactAdapter;
import com.rohitvyavahare.extensions.Contact.ContactListData;
import com.rohitvyavahare.webservices.GetContacts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OrderDetailsActivity";
    private List<ListData> mDataList = new ArrayList<>();
    private List<ContactListData> contactDataList = new ArrayList<>();
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
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
    private HashMap<String, String> markedStatus = new HashMap<>();
    private String type = "inbox";
    private JSONObject d_org;
    private String auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        bundle = getIntent().getExtras();
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        auth = prefs.getString("uid", "null");

        orders = bundle.get("orders").toString();
        type = bundle.getString("type");

        mDrawableBuilder = TextDrawable.builder()
                .round();

        findViewById(R.id.back_btn).setOnClickListener(OrderDetailsActivity.this);
        findViewById(R.id.btn_cancel_order).setOnClickListener(OrderDetailsActivity.this);
        findViewById(R.id.btn_change_status).setOnClickListener(OrderDetailsActivity.this);
        findViewById(R.id.btn_single_status).setOnClickListener(OrderDetailsActivity.this);

//        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_app_id));
//
//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest;
//        if (BuildConfig.DEBUG) {
//            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
//            String deviceId = md5(android_id).toUpperCase();
//            adRequest = new AdRequest.Builder()
//                    .addTestDevice(deviceId)
//                    .build();
//        }
//        else {
//            adRequest = new AdRequest.Builder().build();
//
//        }
//        mAdView.loadAd(adRequest);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//        fab.setVisibility(View.INVISIBLE);

        try {

            for (String key : bundle.keySet()) {
                Log.d(TAG, key + " = \"" + bundle.get(key) + "\"");
            }

            possibleStatus.put("created", "Mark acknowledged");
            possibleStatus.put("acknowledged", "Mark shipped");
            possibleStatus.put("shipping", "Mark order completion");
            possibleStatus.put("cancelled", "Cancel Order");
            possibleStatus.put("sender_completed", "Mark order received");

            markedStatus.put("createdBy", "Created by");
            markedStatus.put("acknowledgedBy", "Acknowledged by");
            markedStatus.put("shippingBy", "Shipped by");
            markedStatus.put("sender_completedBy", "Order completed by");
            markedStatus.put("receiver_completedBy", "Received by");
            markedStatus.put("cancelledBy", "Cancelled by");

            LinkedList<String> markedStatusList = new LinkedList<>(Arrays.asList(
                    "createdBy",
                    "acknowledgedBy",
                    "shippingBy",
                    "sender_completedBy",
                    "receiver_completedBy",
                    "cancelledBy"
            ));

            outboxStatus.put("created", "Created");
            outboxStatus.put("acknowledged", "Acknowledged by seller");
            outboxStatus.put("shipping", "Order has been shipped");
            outboxStatus.put("sender_completed", "Order delivered by seller");

            LinkedList<CheckedTextView> textViews = new LinkedList<>(Arrays.asList(
                    (CheckedTextView) findViewById(R.id.ViewCreatedBy),
                    (CheckedTextView) findViewById(R.id.ViewAcknowledgedBy),
                    (CheckedTextView) findViewById(R.id.ViewShippedBy),
                    (CheckedTextView) findViewById(R.id.ViewCompleteddBy),
                    (CheckedTextView) findViewById(R.id.ViewReceivedBy)
            ));

            for (CheckedTextView v : textViews) {
                v.setOnClickListener(OrderDetailsActivity.this);
            }

            if (bundle.get("order_details").toString().equals("null")) {

                Toast.makeText(this.getApplicationContext(), "Order details are empty", Toast.LENGTH_SHORT).show();


            } else {
                JSONObject obj = new JSONObject(bundle.get("order_details").toString());

                Log.d(TAG, "Obj for order detail:" + obj.toString());

                TextView textView;
                ListView listView = (ListView) findViewById(R.id.listView);

                d_org = new JSONObject(prefs.getString("default_org", "null"));
                if (d_org.has("band") && Integer.parseInt(d_org.getString("band")) < 3) {
                    FrameLayout footerLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.fragment_order, null);
                    footerLayout.findViewById(R.id.btn_add_comment).setOnClickListener(OrderDetailsActivity.this);
                    listView.addHeaderView(footerLayout);
                } else {
                    Button btn = (Button) findViewById(R.id.btn_cancel_order);
                    btn.setVisibility(View.GONE);
                }

                if (obj.has("order_id")) {
                    textView = (TextView) findViewById(R.id.Item);
                    textView.setText(obj.getString("order_id"));
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

                if(obj.has("shipment")) {
                    handleShipmentTable(obj.getJSONArray("shipment"));
                }
                if(obj.has("item") && obj.has("quantity")) {
                    JSONArray arr = new JSONArray();
                    JSONObject itemQuantObj = new JSONObject();
                    itemQuantObj.put("item", obj.getString("item"));
                    itemQuantObj.put("quantity", obj.getString("quantity"));
                    arr.put(itemQuantObj);
                    handleShipmentTable(arr);

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

                boolean cancel = false, start = true;
                int i = 0;

                if (obj.getString("status").equals("cancelled")) {
                    Log.d(TAG, "Cancel is true");
                    cancel = true;
                }

                for (String by : markedStatusList) {
                    Log.d(TAG, "Marking status :" + by);
                    String st = by.split("By")[0];
                    if (obj.has(by)) {
                        Log.d(TAG, "By");
                        textViews.get(i).setText(markedStatus.get(by) + " " + obj.getString(by));
                        i++;
                    } else if (!cancel && start) {
                        textViews.get(i).setText(markedStatus.get(by));
                        i++;
                    } else if (!cancel && !by.equals("cancelledBy") && !start) {

                        Log.d(TAG, "Not cancel");

                        textViews.get(i).setText(markedStatus.get(by));
                        Drawable img = ContextCompat.getDrawable(this, R.drawable.ic_highlight_off_black_24dp);
//                        textViews.get(i).setCheckMarkDrawable(img);
                        textViews.get(i).setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        i++;

                    }
                    if (st.equals(obj.getString("status"))) {
                        start = false;
                    }
                }

                while (i < markedStatusList.size() - 1) {
                    Log.d(TAG, "Marking invisible");
                    textViews.get(i).setVisibility(View.INVISIBLE);
                    i++;
                }

                setStatus(obj, type);
            }

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void handleShipmentTable(JSONArray input) {
        try {

            TableLayout ll = (TableLayout) findViewById(R.id.ShipmentTable);
            ll.setColumnShrinkable(2,true);
            ll.setStretchAllColumns(true);
            TableRow.LayoutParams tlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

            TableRow row= new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView item = new TextView(this);
            item.setGravity(Gravity.CENTER);
            item.setText("Item");
            item.setTypeface(null, Typeface.BOLD);
            item.setBackgroundDrawable( getResources().getDrawable(R.drawable.cell_shape) );
            item.setPadding(5,2,5,2);
            item.setLayoutParams(tlp);

            TextView quantity = new TextView(this);
            quantity.setGravity(Gravity.CENTER);
            quantity.setText("Quantity");
            quantity.setTypeface(null, Typeface.BOLD);
            quantity.setBackgroundDrawable( getResources().getDrawable(R.drawable.cell_shape) );
            quantity.setPadding(5,2,5,2);
            quantity.setLayoutParams(tlp);

            row.setGravity(Gravity.CENTER);
            row.addView(item);
            row.addView(quantity);
            ll.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

            for(int i=0; i<input.length(); i++) {

                JSONObject obj = input.getJSONObject(i);

                if(!obj.has("item") && !obj.has("quantity")) {
                    continue;
                }

                row= new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                item = new TextView(this);
                item.setGravity(Gravity.CENTER);
                item.setText(obj.getString("item"));
                item.setBackgroundDrawable( getResources().getDrawable(R.drawable.cell_shape) );
                item.setPadding(5,2,5,2);
                item.setLayoutParams(tlp);

                quantity = new TextView(this);
                quantity.setText(obj.getString("quantity"));
                quantity.setGravity(Gravity.CENTER);
                quantity.setBackgroundDrawable( getResources().getDrawable(R.drawable.cell_shape) );
                quantity.setPadding(5,2,5,2);
                quantity.setLayoutParams(tlp);

                row.setGravity(Gravity.CENTER);
                row.addView(item);
                row.addView(quantity);
                ll.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void setStatus(JSONObject obj, String type) {

        try {
            ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher_2);
            TextView article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
            Button change_status = (Button) findViewById(R.id.btn_change_status);
            Button cancel = (Button) findViewById(R.id.btn_cancel_order);
            Button single_status = (Button) findViewById(R.id.btn_single_status);

            Log.d(TAG, "Status of order :" + obj.getString("status"));

            HashMap<String, Integer> order_type = new HashMap<String, Integer>() {{
                put("inbox", 0);
                put("outbox", 1);
            }};

            HashMap<String, Integer> status = new HashMap<String, Integer>() {{
                put("created", 0);
                put("acknowledged", 1);
                put("shipping", 2);
                put("cancelled", 3);
                put("sender_completed", 4);
                put("receiver_completed", 5);
            }};

            switch (order_type.get(type)) {
                case 0: {

                    Log.d(TAG, "Type inbox");

                    switch (status.get(obj.getString("status"))) {

                        case 0: {
                            Log.d(TAG, "Case 0");
                        }
                        case 1: {

                            Log.d(TAG, "Case 1");
                            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.StatusLinearLayout);
                            linearLayout.setVisibility(View.GONE);
                            if (d_org.has("band") && Integer.parseInt(d_org.getString("band")) > 2) {
                                change_status.setVisibility(View.GONE);
                                cancel.setVisibility(View.GONE);
                            } else {
                                change_status.setText(possibleStatus.get(obj.getString("status")));
                            }
                            break;
                        }
                        case 2: {

                            Log.d(TAG, "Case 2");
                            cancel.setVisibility(View.GONE);
                            switcher.showNext();
                            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.StatusLinearLayout);
                            linearLayout.setVisibility(View.GONE);
                            single_status.setText(possibleStatus.get(obj.getString("status")));
                            single_status.setBackgroundColor(getResources().getColor(R.color.primary_btn));
                            break;
                        }

                        case 3: {

                            Log.d(TAG, "Case 3");
                            article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                            article.setText(R.string.cancelled);
                            cancel.setVisibility(View.GONE);
                            change_status.setVisibility(View.GONE);
                            switcher.setVisibility(View.GONE);

                            break;
                        }

                        case 4: {

                            Log.d(TAG, "Case 4");
                            article.setText(R.string.sender_completed);
                            change_status.setVisibility(View.GONE);
                            cancel.setVisibility(View.GONE);
                            switcher.setVisibility(View.GONE);
                            break;
                        }
                        case 5: {

                            Log.d(TAG, "Case 5");
                            article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                            article.setText(R.string.receiver_completed);
                            change_status.setVisibility(View.GONE);
                            cancel.setVisibility(View.GONE);
                            switcher.setVisibility(View.GONE);
                            break;
                        }

                        default: {

                            Log.d(TAG, "Default Case");
                            break;
                        }
                    }


                    break;
                }
                case 1: {

                    switch (status.get(obj.getString("status"))) {

                        case 0: {
                            Log.d(TAG, "Case 0");
                        }
                        case 1: {
                            Log.d(TAG, "Case 1");
                        }
                        case 2: {

                            Log.d(TAG, "Case 2");
                            article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                            article.setText(outboxStatus.get(obj.getString("status")));
                            change_status.setVisibility(View.GONE);
                            switcher.showNext();
                            single_status.setText(getString(R.string.cancel_order));
                            single_status.setBackgroundColor(getResources().getColor(R.color.danger));

                            break;
                        }

                        case 3: {

                            Log.d(TAG, "Case 3");
                            article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                            article.setText(R.string.cancelled);
                            cancel.setVisibility(View.GONE);
                            change_status.setVisibility(View.GONE);

                            break;
                        }

                        case 4: {

                            Log.d(TAG, "Case 4");
                            switcher.showNext();
                            single_status.setText(possibleStatus.get(obj.getString("status")));
                            single_status.setBackgroundColor(getResources().getColor(R.color.primary_btn));

                            break;
                        }

                        case 5: {

                            Log.d(TAG, "Case 5");
                            article = (TextView) findViewById(R.id.ViewReceiverCompletedStatus);
                            article.setText(R.string.receiver_completed);
                            change_status.setVisibility(View.GONE);
                            cancel.setVisibility(View.GONE);
                            break;
                        }

                        default: {

                            Log.d(TAG, "Default Case");
                            break;
                        }

                    }

                    break;
                }
                default: {

                    Log.d(TAG, "Type Default Case");
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        Log.d(TAG, "something clicked");

        if (i == R.id.ViewCreatedBy || i == R.id.ViewAcknowledgedBy || i == R.id.ViewCompleteddBy || i == R.id.ViewReceivedBy
                || i == R.id.ViewReceiverCompletedStatus || i == R.id.ViewShippedBy) {
            handleStatusClick(i);
        } else {
            handleBtnClick(i);
        }

    }

    private void handleStatusClick(int i) {

        try {

            Log.d(TAG, "request button clicked");
            JSONObject body = new JSONObject();
            JSONArray orgArr = new JSONArray();

            String default_org_tag = d_org.getString("tag");

            Log.d(TAG, "default orf id :" + default_org_tag);
            String paired_orgs = prefs.getString(default_org_tag + getString(R.string.paired_orgs), "[{type : null}]");
            Log.d(TAG, "Paired orgs :" + paired_orgs);

            JSONArray arr = new JSONArray(paired_orgs);
            Log.d(TAG, "Paired orgs :" + arr.toString());

            JSONObject current_org = null;

            for (int k = 0; k < arr.length(); k++) {
                JSONObject obj = arr.getJSONObject(k);
                if (obj.has("tag") && obj.getString("tag").equals(bundle.getString("org_tag"))) {
                    current_org = obj;
                    break;
                }
            }

            for (int j = 1; j < 4; j++) {
                JSONObject org = new JSONObject();
                if (current_org.has("name") && current_org.has("id")) {
                    org.put("id", current_org.getString("id"));
                    org.put("name", current_org.getString("name"));
                    org.put("band", j);
                    orgArr.put(org);
                }
            }
            body.put("keys", orgArr);
            String[] name = ((CheckedTextView) findViewById(i)).getText().toString().split(" by ");
            if (name.length > 1) {
                body.put("name", name[1]);
                Bundle input = new Bundle();
                input.putString("uid", auth);
                input.putString("body", body.toString());

                Bundle output = new GetContacts(this).execute(input).get();
                output.putString("input", body.toString());
                handleGetContacts(output);
            }


        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void handleGetContacts(Bundle input) {
        try {
            if(!input.getString("exception").equals("no_exception")) {
                showMessageOnUi(input.getString("exception"));
                return;
            }
            int response = input.getInt("response");
            String outputBody = input.getString("output");
            JSONObject body = new JSONObject(input.getString("input"));
            switch (response) {
                case 200: {

                    JSONArray arr = new JSONArray(outputBody);
                    List<ListData> mDataList = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Log.d(TAG, "Matching Name :" + body.getString("name"));
                        Log.d(TAG, "Contact :" + obj.toString());
                        if (obj.has("name") && obj.has("phone_number") && obj.has(("profile_pic"))
                                && obj.getString("name").toLowerCase().equals(body.getString("name").toLowerCase())) {
                            Log.d(TAG, "Object :" + obj.toString());
                            contactDataList.add(new ContactListData(obj.getString("name"), obj.getString("phone_number"), obj.getString("profile_pic")));
                        }

                    }
                    if (contactDataList.size() > 0) {
                        Log.d(TAG, "Size :" + mDataList.size());
                        AlertDialog.Builder dialog = new AlertDialog.Builder(OrderDetailsActivity.this);
                        dialog.setTitle("Contacts");
                        LayoutInflater li = (LayoutInflater) OrderDetailsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View v = li.inflate(R.layout.dialog_listview, null, false);
                        ListView listView = (ListView) v.findViewById(R.id.listView);
                        listView.setAdapter(new ContactAdapter(OrderDetailsActivity.this, contactDataList));
                        dialog.setView(v);
                        dialog.show();
                    } else {
                        showMessageOnUi("No contacts found");
                    }
                    break;
                }
                default: {
                    showMessageOnUi("Something went wrong while retrieving contact, Please try again");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi("Something went wrong while retrieving contact, Please try again");
        }
    }

    private void showMessageOnUi(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(OrderDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void handleBtnClick(int i) {

        boolean single_btn_clicked = false;
        Button single_btn = (Button) findViewById(R.id.btn_single_status);
        String single_text = "";
        if (!single_btn.getText().toString().trim().equals("")) {
            single_btn_clicked = true;
            single_text = single_btn.getText().toString().trim().toLowerCase();
        }
        if (i == R.id.back_btn) {

            Log.d(TAG, "back btn clicked");

            Intent intent = new Intent(OrderDetailsActivity.this, OrdersActivityV2.class);
            Bundle bundle = new Bundle();

            Log.d(TAG, "Orders :" + orders);
            bundle.putString("orders", orders);
            bundle.putString("type", getIntent().getExtras().get("type").toString());
            bundle.putString("org_name", getIntent().getExtras().get("org_name").toString());
            bundle.putString("org_tag", getIntent().getExtras().get("org_tag").toString());
            intent.putExtras(bundle);
            startActivity(intent);

        } else if (i == R.id.btn_cancel_order || (i == R.id.btn_single_status && single_btn_clicked && single_text.equals("cancel order"))) {

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

        } else if (i == R.id.btn_change_status || (i == R.id.btn_single_status && single_btn_clicked)) {
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

    private String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.toString());
        }
        return "";
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

    private void handleEditOrder() {
        try{

            // @TODO call place order
            // @TODO check is it customer or owner

        } catch (Exception e) {

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

                Log.d(TAG, "default org for order :" + d_org.toString());

                String paired_orgs = prefs.getString(d_org.getString("tag") + getString(R.string.paired_orgs), "null");
                JSONObject to_org = new JSONObject();

                Log.d(TAG, "Put request for type " + type);
                Log.d(TAG, "paired_arr :" + paired_orgs);

                JSONArray paired_arr = new JSONArray(paired_orgs);

                Log.d(TAG, "org_name :" + getIntent().getExtras().get("org_name").toString());

                for (int i = 0; i < paired_arr.length(); i++) {
                    if (paired_arr.getJSONObject(i).has("name") && paired_arr.getJSONObject(i).getString("name").equals(getIntent().getExtras().get("org_name").toString())) {
                        to_org = paired_arr.getJSONObject(i);
                    }
                }

                if (!to_org.has("id")) {
                    throw new NullPointerException("to_org can't be null");
                }

                JSONObject body = new JSONObject();
                body.put("order", b);

                if (type.equals("inbox")) {
                    body.put("to", d_org);
                    body.put("from", to_org);
                    Log.d(TAG, "from org as type is inbox:" + to_org.toString());
                } else {
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
//                                    JSONArray arr = new JSONArray(prefs.getString(getIntent().getExtras().get("org_name").toString(), "null"));
                                    JSONArray arr = new JSONArray(prefs.getString(getIntent().getExtras().get("org_tag").toString(), "null"));

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

//                                    editor.putString(getIntent().getExtras().get("org_name").toString(), newArr.toString());
                                    editor.putString(getIntent().getExtras().get("org_tag").toString(), newArr.toString());
                                    editor.commit();
                                    Intent intent = new Intent(OrderDetailsActivity.this, OrderDetailsActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("order_details", obj.toString());
                                    b.putString("orders", getIntent().getExtras().get("orders").toString());
                                    b.putString("type", getIntent().getExtras().get("type").toString());
                                    b.putString("org_name", getIntent().getExtras().get("org_name").toString());
                                    b.putString("org_tag", getIntent().getExtras().get("org_tag").toString());
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
                                Toast.makeText(context, "Opss Something went wrong please try again later, Status code :400", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
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
