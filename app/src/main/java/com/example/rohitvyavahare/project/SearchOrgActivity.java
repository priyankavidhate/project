package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SearchOrgActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private static final String TAG = "SearchOrgActivity";
    private SharedPreferences prefs;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private List<ListData> mDataList = new ArrayList<>();
    private List<JSONObject> searchResult = new LinkedList<>();
    private Timer timer = new Timer();
    private final long DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_org);

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

        EditText edittext = (EditText) findViewById(R.id.SearchOrg);
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);

        mDrawableBuilder = TextDrawable.builder()
                .round();

        // init the list view and its adapter

        final TextView empty = (TextView)findViewById(R.id.empty);
        empty.setVisibility(View.INVISIBLE);
        final ListView listView = (ListView) findViewById(R.id.searchOrg);
        listView.setAdapter(new SampleAdapter());

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Log.d(TAG, "Clicked at position :" + position);
                Bundle orgData = new Bundle();
                Log.d(TAG, "Data :" + searchResult.get(position).toString());
                orgData.putString("org", searchResult.get(position).toString());
                orgData.putString("type", "info");
                orgData.putString("position", Integer.toString(position));
                Intent intent = new Intent(SearchOrgActivity.this, PairOrgDetailsActivity.class);
                intent.putExtras(orgData);
                startActivity(intent);
                // TODO Auto-generated method stub
                // Toast.makeText(SearchOrgActivity.this, listView[position], Toast.LENGTH_SHORT).show();
            }
        });

        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before,
                                      int count) {
                if (timer != null)
                    timer.cancel();
            }

            @Override
            public void afterTextChanged(final Editable s) {
                empty.setText("Loading...");
                //avoid triggering event when text is too short
                if (s.length() >= 3) {

                    Log.d(TAG, "calling timer");
                    empty.setVisibility(View.VISIBLE);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.d(TAG, "calling GetClass");
                            new GetClass(SearchOrgActivity.this).execute(s.toString());
                        }
                    }, DELAY);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(SearchOrgActivity.this, PairOrgActivity.class);
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
                convertView = View.inflate(SearchOrgActivity.this, R.layout.list_search_org, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData item = getItem(position);

            // provide support for selected state
            updateCheckedState(holder, item);
//            holder.imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // when the image is clicked, update the selected state
//                    ListData data = getItem(position);
//                    Log.d(TAG, "Clicked at position :"+ position);
//                    Bundle orgData = new Bundle();
//                    orgData.putString("org", searchResult.get(position).toString());
//                    Intent intent = new Intent();
//                    intent = new Intent(SearchOrgActivity.this, PairOrgDetailsActivity.class);
//                    startActivity(intent);
//
////                    data.setChecked(!data.isChecked);
////                    updateCheckedState(holder, data);
//                }
//            });

            Log.d(TAG, "List Item: " + item.data);
            holder.textView.setText(item.data);
            holder.textView1.setText(item.data1);
            holder.textView2.setText(item.data2);

            return convertView;
        }

        private void updateCheckedState(ViewHolder holder, ListData item) {

            TextDrawable drawable = mDrawableBuilder.build(String.valueOf(item.data.charAt(0)), mColorGenerator.getColor(item.data));
            holder.imageView.setImageDrawable(drawable);
            holder.view.setBackgroundColor(Color.TRANSPARENT);
            holder.checkIcon.setVisibility(View.GONE);

        }
    }

    private static class ViewHolder {

        private View view;

        private ImageView imageView;

        private TextView textView, textView1, textView2;

        private ImageView checkIcon;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textView = (TextView) view.findViewById(R.id.textView);
            textView1 = (TextView) view.findViewById(R.id.textView1);
            textView2 = (TextView) view.findViewById(R.id.textView2);
            checkIcon = (ImageView) view.findViewById(R.id.check_icon);
        }
    }

    private static class ListData {

        private String data, data1, data2;

        ListData(String data, String data1, String data2) {

            this.data = data;
            this.data1 = data1;
            this.data2 = data2;
        }
    }

    private class GetClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        GetClass(Context c) {
            this.context = c;
        }

//        protected void onPreExecute() {
//            mProgressDialog = new ProgressDialog(this.context);
//            mProgressDialog.setMessage("Loading");
//            mProgressDialog.show();
//        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                final String query = params[0];
                final ListView listView = (ListView) findViewById(R.id.searchOrg);
                final TextView empty = (TextView) findViewById(R.id.empty);

                String type = "name";
                if(query.length() > 0){

                    if (query.charAt(0) == '@') {
                        type = "tag";
                    }

                }


                if (mDataList.size() > 0) {
                    mDataList.clear();
                    searchResult.clear();
                }

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.search_org))
                        .appendPath(query)
                        .appendQueryParameter("type", type)
                        .build();

                final URL url = new URL(uri.toString());
                Log.d(TAG, "Sending 'GET' request to URL :" + uri.toString());

                String auth = prefs.getString("uid", "null");
                Log.d(TAG, "auth " + auth);
                if (auth.equals("null")) {
//                    onPostExecute();
                    //@TODO add alert
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", auth);

                final int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'GET' request to URL : :" + url);
                Log.d(TAG, "Get parameters : " + prefs.getString("uid", "null"));
                Log.d(TAG, "Response Code : " + responseCode);

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

                final ArrayList<String> orgs_arr = new ArrayList<>();
                String default_org = prefs.getString("default_org", "null");


                Log.d(TAG, "Default org :" + default_org);

                if(!default_org.equals("null")){
                    JSONObject org = new JSONObject(default_org);
                    if(org.has("name")){
                        Log.d(TAG, "Adding org to backlist :"+ org.getString("name") );
                        orgs_arr.add(org.getString("name"));

//                        String paired_orgs = prefs.getString(org.getString("name") + R.string.paired_orgs, "[{type : null}]");
                        String paired_orgs = prefs.getString(org.getString("tag") + getString(R.string.paired_orgs), "[{type : null}]");
                        Log.d(TAG, "paired_Orgs :" + paired_orgs);
                        JSONArray arr = new JSONArray(paired_orgs);

                        if(!paired_orgs.equals("[{type : null}]") && arr.length() > 0) {

                            for (int i = 0; i < arr.length(); i++) {

                                JSONObject obj = arr.getJSONObject(i);
                                if (obj.has("name")) {

                                    Log.d(TAG, "Adding org to backlist :"+ obj.getString("name") );
                                    orgs_arr.add(obj.getString("name"));
                                }

                            }
                        }
                    }


                }

                Log.d(TAG, "backlisted orgs from search :" + orgs_arr.toString());


                Log.d(TAG, "sb :" + sb.toString());
                SearchOrgActivity.this.runOnUiThread(new Runnable() {


                    public void run() {
                        try {
                            Log.d(TAG, "sb in runOiThread :" + sb.toString());
                            switch (responseCode) {
                                case 200: {

                                    JSONArray jArray = new JSONArray(sb.toString());
                                    if (mDataList.size() > 0) {
                                        mDataList.clear();
                                        searchResult.clear();
                                    }
                                    for (int i = 0; i < jArray.length(); ++i) {
                                        JSONObject obj = jArray.getJSONObject(i);
                                        if(orgs_arr.contains(obj.getString("name"))){
                                            continue;
                                        }
                                        searchResult.add(obj);
                                        mDataList.add(new ListData(obj.getString("name"), "@" + obj.getString("tag"), obj.getString("city") + ", " + obj.getString("state")));
                                    }

                                    Log.d(TAG, "Length of the array :" + mDataList.size());


                                    if (jArray.length() > 0) {
                                        listView.setAdapter(new SampleAdapter());
                                        empty.setVisibility(View.INVISIBLE);

                                    } else {
                                        empty.setText("No organization present with given tag or name, Please confirm your search");
                                        listView.setEmptyView(empty);
                                    }
                                    break;

                                }

                                default: {
                                    Log.d(TAG, "default " + responseCode);
                                    Toast.makeText(SearchOrgActivity.this.getApplicationContext(), "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SearchOrgActivity.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //Do your UI operations like dialog opening or Toast here
                    }
                });


            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

//        protected void onPostExecute() {
//            mProgressDialog.dismiss();
//        }

    }
}
