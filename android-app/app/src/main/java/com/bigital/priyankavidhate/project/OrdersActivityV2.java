package com.bigital.priyankavidhate.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrdersActivityV2 extends AppCompatActivity {

    private static final String TAG = "OrdersActivityV2";
    private List<Order> orders;
    private HashMap<String, String> status = new HashMap<>();
    private String type = "inbox";
    private SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private JSONArray arr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycle_view);

        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        type = getIntent().getExtras().get("type").toString();
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        editor = prefs.edit();

        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest;
        if (BuildConfig.DEBUG) {
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
            adRequest = new AdRequest.Builder()
                    .addTestDevice(deviceId)
                    .build();
        }
        else {
            adRequest = new AdRequest.Builder().build();

        }
        mAdView.loadAd(adRequest);

        try{

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
        catch (java.lang.NullPointerException e){
            e.printStackTrace();
        }
        try{

            Bundle bundle = getIntent().getExtras();

            status.put("created", "Created");
            status.put("acknowledged", "Acknowledged");
            status.put("shipping", "Shipping");
            status.put("sender_completed", "Order delivered by seller");
            status.put("receiver_completed", "Order received by seller");
            status.put("cancelled", "Cancelled");

            if(bundle.get("orders").toString().equals("null")){
                //TODO handle empty

            }
            else {
                arr = new JSONArray(bundle.get("orders").toString());
            }

            initializeData();
            RecyclerView rv = (RecyclerView)findViewById(R.id.rv);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            rv.setLayoutManager(llm);
            RVAdapter adapter = new RVAdapter(orders);
            rv.setAdapter(adapter);

        }
        catch (Exception e){
            e.printStackTrace();
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
            Log.e(TAG,e.toString());
        }
        return "";
    }

    private class Order {
        String title;
        String status;
        int notification_number;
        ArrayList<String> qunatity;
        ArrayList<String> item;
        JSONObject order;

        Order(String title, ArrayList<String> item, ArrayList<String> qunatity, String status, int notification_number, JSONObject order) {
            this.title = title;
            this.item = item;
            this.qunatity = qunatity;
            this.notification_number = notification_number;
            this.status = status;
            this.order = order;
        }
    }

    // This method creates an ArrayList that has three Person objects
    // Checkout the project associated with this tutorial on Github if
    // you want to use the same images.
    private void initializeData(){

        try{

            Log.d(TAG, "Number of Orders : " + arr.length());

            orders = new ArrayList<>();
            for(int i=0; i<arr.length(); i++){
                JSONObject order = arr.getJSONObject(i);
                Log.d(TAG, "Order :" + order.toString());

                String stat = status.get(arr.getJSONObject(i).getString("status"));
                Log.d(TAG, "status " + stat);
                stat = stat.substring(0, 1).toUpperCase() +  stat.substring(1);
                int no =0;
                no = prefs.getInt(arr.getJSONObject(i).getString("id") + getIntent().getExtras().get("type") +  getString(R.string.number_of_notification), 0);

                ArrayList<String> items = new ArrayList<>();
                ArrayList<String> qunatities = new ArrayList<>();

                if(order.has("shipment")){

                    Log.d(TAG, "Shipment order");
                    JSONArray shipment = order.getJSONArray("shipment");

                    for(int j=0; j<shipment.length(); j++){
                        JSONObject couple = shipment.getJSONObject(j);
                        if(!couple.has("item") && !couple.has("quantity"))
                            continue;
                        items.add(couple.getString("item"));
                        qunatities.add(couple.getString("quantity"));

                    }
                    Log.d(TAG, "Number of items :" + items.size());
                    Log.d(TAG, "Number of quantities :" + qunatities.size());

                    if(order.has("order_id"))
                        orders.add(new Order(order.getString("order_id"), items, qunatities, "Status: " + stat, no, order));
                    else {
                        orders.add(new Order("Order id not available", items, qunatities, "Status: " + stat, no, order));
                    }
                }
                else if(order.has("item") && order.has("quantity")) {

                    Log.d(TAG, "V1 order");
                    items.add(order.getString("item"));
                    qunatities.add(order.getString("quantity"));

                    if(order.has("order_id"))
                        orders.add(new Order(order.getString("order_id"), items, qunatities, "Status: " + stat, no, order));
                    else {
                        orders.add(new Order("Order id not available", items, qunatities, "Status: " + stat, no, order));
                    }
                }
            }
            Log.d(TAG, "Orders size :" + orders.size());

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{

        class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            CardView cv;
            TextView title, status, notification;
            ArrayList<TextView> items = new ArrayList<>();
            ArrayList<TextView> qunatities = new ArrayList<>();

            PersonViewHolder(View itemView) {
                super(itemView);
                Log.d(TAG, "PersonViewHolder");

                cv = (CardView)itemView.findViewById(R.id.cv);
                title = (TextView)itemView.findViewById(R.id.title);
                status = (TextView)itemView.findViewById(R.id.textView1);
                notification = (TextView)itemView.findViewById(R.id.textView2);
                TypedArray t = itemView.getResources().obtainTypedArray(R.array.orderIds);
                TypedArray t2 = itemView.getResources().obtainTypedArray(R.array.orderQunatityIds);

                for(int i=0; i<t.length(); i++){
                    items.add(i,(TextView) itemView.findViewById(t.getResourceId(i, 0)));
                    qunatities.add(i,(TextView) itemView.findViewById(t2.getResourceId(i, 0)));
                }
                t.recycle();
                t2.recycle();

                Log.d(TAG, "Items size : " + items.size());
                Log.d(TAG, "Quantity size :" + qunatities.size());

            }

            @Override
            public void onClick(View v) {
                Log.d(TAG, "CLICK!");
            }
        }
        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Log.d(TAG, "onCreateViewHolder :" + i);
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_orders_v2, viewGroup, false);
            MobileAds.initialize(getApplicationContext(), getString(R.string.banner_app_id));
            return new PersonViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        List<Order> ordersHolder;

        RVAdapter(List<Order> orders){
            Log.d(TAG, "RVAdapter Orders size :" + orders.size());
            this.ordersHolder = orders;
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
            Log.d(TAG, "onBindViewHolder");
            ArrayList<String> items = orders.get(i).item;
            ArrayList<String> quantities = orders.get(i).qunatity;
            int j=0;

            for(; j<items.size(); j++){
                Log.d(TAG, "orderArr.get(j) : " + items.get(j));
                String quant;
                if(quantities.get(j).matches("[0-9]+")){
                    quant = Integer.parseInt(quantities.get(j)) == 1 ? quantities.get(j) + " Item" :quantities.get(j) + " Items";
                }
                else {
                    quant = quantities.get(j);
                }

                if(quant.length() > 15){
                    quant = quant.substring(0,12) + "...";
                }
                String item = items.get(j);
                if(item.length() > 25){
                    item = item.substring(0, 22) + "...";
                }
                personViewHolder.qunatities.get(j).setText(quant);
                personViewHolder.items.get(j).setText(item);
            }

            Log.d(TAG, "Before J :" + j);

            for(; j < personViewHolder.items.size(); j++ ){
                personViewHolder.items.get(j).setVisibility(View.GONE);
                personViewHolder.qunatities.get(j).setVisibility(View.GONE);
            }

            personViewHolder.status.setText(orders.get(i).status);
            if(orders.get(i).notification_number > 0) {
                personViewHolder.notification.setText(""+orders.get(i).notification_number);
            }
            else {
                personViewHolder.notification.setVisibility(View.INVISIBLE);
            }

            Log.d(TAG, "Title  :" + orders.get(i).title);
            Log.d(TAG, "Status  :" + orders.get(i).status);

            personViewHolder.title.setText("Order Id - " + orders.get(i).title);

            final int position = i;

            personViewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    try {
                        Log.d(TAG, "Me clicked");

                        Bundle orderData = new Bundle();
                        orderData.putString("order_details", orders.get(position).order.toString());
                        orderData.putString("orders", getIntent().getExtras().get("orders").toString());
                        orderData.putString("type", getIntent().getExtras().get("type").toString());
                        Log.d(TAG, "clicked org_name : " + getIntent().getExtras().get("org_name").toString());
                        Log.d(TAG, "clicked org_tag : " + getIntent().getExtras().get("org_tag").toString());
                        orderData.putString("org_name", getIntent().getExtras().get("org_name").toString());
                        orderData.putString("org_tag", getIntent().getExtras().get("org_tag").toString());
                        orderData.putString("position", Integer.toString(position));
                        Intent intent = new Intent(OrdersActivityV2.this, OrderDetailsActivity.class);
                        intent.putExtras(orderData);
                        for (String key : orderData.keySet()) {
                            Log.d(TAG, key + " is a key in the bundle");
                        }

                        if (orders.get(position).order.has("id")) {
                            editor.remove(orders.get(position).order.getString("id") + getIntent().getExtras().get("type").toString() + getString(R.string.number_of_notification));
                            editor.commit();
                        }
                        startActivity(intent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent;
        if(type.equals("inbox")){
            intent = new Intent(OrdersActivityV2.this, InboxActivity.class);
        }
        else {
            intent = new Intent(OrdersActivityV2.this, OutboxActivity.class);
        }
        startActivity(intent);
        finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Intent intent;
        if(type.equals("inbox")){
            intent = new Intent(OrdersActivityV2.this, InboxActivity.class);
        }
        else {
            intent = new Intent(OrdersActivityV2.this, OutboxActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
