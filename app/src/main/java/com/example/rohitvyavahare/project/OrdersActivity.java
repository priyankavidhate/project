package com.example.rohitvyavahare.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "OrdersActivity";
    private List<ListData> mDataList = new ArrayList<>();
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private JSONArray arr;
    private HashMap<String, String> status = new HashMap<>();
    private String type = "inbox";
    private SharedPreferences prefs;
    SharedPreferences.Editor editor;
    int no=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
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



        mDrawableBuilder = TextDrawable.builder()
                .round();

        ListView listView = (ListView) findViewById(R.id.listView);
        final View empty = findViewById(R.id.empty);

        Bundle bundle = getIntent().getExtras();

        status.put("created", "Created");
        status.put("acknowledged", "Acknowledged");
        status.put("shipping", "Shipping");
        status.put("sender_completed", "Order delivered by seller");
        status.put("receiver_completed", "Order received by seller");
        status.put("cancelled", "Cancelled");

        try{

            if(bundle.get("orders").toString().equals("null")){
                empty.setVisibility(View.VISIBLE);

            }
            else {
                arr = new JSONArray(bundle.get("orders").toString());

                Log.d(TAG, "Array length :" + arr.length());
                if(arr.length() > 0 ){
                    Log.d(TAG, "empty INVISIBLE");
                    empty.setVisibility(View.INVISIBLE);
                }

                for(int i=0; i<arr.length(); i++){
                    Log.d(TAG, "Object :" + arr.getJSONObject(i).toString());
                    if(arr.getJSONObject(i).has("status")){
                        String stat = status.get(arr.getJSONObject(i).getString("status"));
                        Log.d(TAG, "status " + stat);
                        stat = stat.substring(0, 1).toUpperCase() +  stat.substring(1);
                        String item =  arr.getJSONObject(i).getString("item").substring(0, 1).toUpperCase() +  arr.getJSONObject(i).getString("item").substring(1);
                        String quant;
                        if(arr.getJSONObject(i).getString("quantity").matches("[0-9]+")){
                            quant = Integer.parseInt(arr.getJSONObject(i).getString("quantity")) == 1 ? arr.getJSONObject(i).getString("quantity") + " Item" : arr.getJSONObject(i).getString("quantity") + " Items";
                        }
                        else {
                            quant = arr.getJSONObject(i).getString("quantity");
                        }

                        no = prefs.getInt(arr.getJSONObject(i).getString("id") + getIntent().getExtras().get("type") +  getString(R.string.number_of_notification), 0);
                        mDataList.add(new ListData(item, "Status: " + stat, quant, no));
                    }
                }
            }

        }
        catch(JSONException | NullPointerException e){
            e.printStackTrace();
            listView.setAdapter(new SampleAdapter());
        }

        listView.setAdapter(new SampleAdapter());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Log.d(TAG, "Clicked at position :"+ position);

                if(arr.length() > 0) {
                    try {
                        JSONObject obj = arr.getJSONObject(position);
                        Bundle orderData = new Bundle();
                        orderData.putString("order_details", obj.toString());
                        orderData.putString("orders", getIntent().getExtras().get("orders").toString());
                        orderData.putString("type", getIntent().getExtras().get("type").toString());
                        Log.d(TAG, "clicked org_name : "+ getIntent().getExtras().get("org_name").toString());
                        Log.d(TAG, "clicked org_tag : "+ getIntent().getExtras().get("org_tag").toString());
                        orderData.putString("org_name", getIntent().getExtras().get("org_name").toString());
                        orderData.putString("org_tag", getIntent().getExtras().get("org_tag").toString());
                        orderData.putString("position", Integer.toString(position));
                        Intent intent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);
                        intent.putExtras(orderData);
                        for (String key: orderData.keySet())
                        {
                            Log.d (TAG, key + " is a key in the bundle");
                        }

                        if(obj.has("id")){
//                            int no2 = prefs.getInt(getIntent().getExtras().get("org_id").toString()+getIntent().getExtras().get("type").toString(), 0);
//                            no2--;
//                            editor.putInt(getIntent().getExtras().get("org_id").toString()+getIntent().getExtras().get("type").toString(), no2);
                            editor.remove(obj.getString("id") + getIntent().getExtras().get("type").toString() + getString(R.string.number_of_notification));
                            editor.commit();
                        }
                        startActivity(intent);

                    }
                    catch (JSONException | NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }
        });

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent;
        if(type.equals("inbox")){
            intent = new Intent(OrdersActivity.this, InboxActivity.class);
        }
        else {
            intent = new Intent(OrdersActivity.this, OutboxActivity.class);
        }
        startActivity(intent);
        finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Intent intent;
        if(type.equals("inbox")){
            intent = new Intent(OrdersActivity.this, InboxActivity.class);
        }
        else {
            intent = new Intent(OrdersActivity.this, OutboxActivity.class);
        }
        startActivity(intent);
        finish();
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
                convertView = View.inflate(OrdersActivity.this, R.layout.list_orders, null);
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
            holder.textView2.setText(item.data2);
            if(item.data3 > 0){
                holder.textView3.setText(""+item.data3);
            }
            else {
                holder.textView3.setVisibility(View.GONE);
            }


            return convertView;
        }

        private void updateCheckedState(ViewHolder holder, ListData item) {
            Log.d(TAG, "setting image view :" + item.data);;
            TextDrawable drawable = mDrawableBuilder.build(String.valueOf(item.data.charAt(0)), mColorGenerator.getColor(item.data));
            holder.imageView.setImageDrawable(drawable);
            holder.view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static class ViewHolder {

        private View view;

        private ImageView imageView;

        private TextView textView, textView1, textView2, textView3;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textView = (TextView) view.findViewById(R.id.textView);
            textView1 = (TextView) view.findViewById(R.id.textView1);
            textView2 = (TextView) view.findViewById(R.id.OrderQuantity);
            textView3 = (TextView) view.findViewById(R.id.textView2);
        }
    }

    private static class ListData {

        private String data, data1, data2;
        int data3;

        ListData(String data, String data1, String data2, int data3) {
            this.data = data;
            this.data1 = data1;
            this.data2 = data2;
            this.data3 = data3;
        }
    }

}
