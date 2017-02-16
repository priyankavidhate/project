package com.example.rohitvyavahare.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        type = getIntent().getExtras().get("type").toString();

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
                    String stat = status.get(arr.getJSONObject(i).getString("status"));
                    stat = stat.substring(0, 1).toUpperCase() +  stat.substring(1);
                    String item =  arr.getJSONObject(i).getString("item").substring(0, 1).toUpperCase() +  arr.getJSONObject(i).getString("item").substring(1);
                    mDataList.add(new ListData(item, "Status: " + stat, arr.getJSONObject(i).getString("quantity")));
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
                        orderData.putString("org_name", getIntent().getExtras().get("org_name").toString());
                        orderData.putString("position", Integer.toString(position));
                        Intent intent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);
                        intent.putExtras(orderData);
                        for (String key: orderData.keySet())
                        {
                            Log.d (TAG, key + " is a key in the bundle");
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

        private TextView textView, textView1, textView2;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textView = (TextView) view.findViewById(R.id.textView);
            textView1 = (TextView) view.findViewById(R.id.textView1);
            textView2 = (TextView) view.findViewById(R.id.textView2);
        }
    }

    private static class ListData {

        private String data, data1, data2;

        public ListData(String data, String data1, String data2) {
            this.data = data;
            this.data1 = data1;
            this.data2 = data2;
        }
    }

}
