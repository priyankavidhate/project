package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PairOrgActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private static final String TAG = "PairOrgActivity";
    private SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private JSONObject d_org = new JSONObject();
    private JSONArray p_org;
    private ProgressDialog progress;

    // list of data items
    private List<ListData> mDataList = new ArrayList<>();
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Toolbar toolbar;

        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        editor = prefs.edit();

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl1);
        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.activity_pair_org, null, true);
        rl.addView(layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);

        View hView = navigationView.getHeaderView(0);
        TextView nav_user = (TextView) hView.findViewById(R.id.NavName);
        String username = prefs.getString("user_name", "null");

        Utils util = new Utils();
        if (!username.equals("null")) {
            nav_user.setText(util.capitalizeString(username));
        }


        CircularImageView usr_pic = (CircularImageView) hView.findViewById(R.id.ProfilePic);
        String profile_pic = prefs.getString("profile_pic", "null");
        if (!profile_pic.equals("null")) {

            if (InboxActivity.bitmap != null) {
                usr_pic.setImageBitmap(InboxActivity.bitmap);
            }
            else {
                InboxActivity.bitmap = util.StringToBitMap(profile_pic);
                if (InboxActivity.bitmap != null) {
                    usr_pic.setImageBitmap(InboxActivity.bitmap);
                }
            }
        }

        mDrawableBuilder = TextDrawable.builder()
                .round();

        String refreshToken = prefs.getString(getString(R.string.refresh_token), "null");

        if(refreshToken.equals("null")){
            editor.putString(getString(R.string.hard_reload_pair_org), "true");
            editor.commit();
        }

        String reload = prefs.getString(getString(R.string.hard_reload_pair_org), "null");
        if (reload.equals("true") || reload.equals("null")) {
            new GetClass(this).execute();
        }

        ListView listView = (ListView) findViewById(R.id.pairOrg);
        TextView empty = (TextView) findViewById(R.id.empty);


        try {

            String default_org = prefs.getString("default_org", "null");
            String pair_org_request = "null";
            d_org = new JSONObject(default_org);

            Log.d(TAG, "d_org: " + d_org.toString());


            if (!default_org.equals("null") && d_org.has("name")) {
                pair_org_request = prefs.getString(d_org.getString("name") + R.string.incoming_request, "null");
                Log.d(TAG, "pair_org_request :" + pair_org_request + " and default org name :" + d_org.getString("name"));
            }

            // init the list view and its adapter
            empty.setVisibility(View.INVISIBLE);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PairOrgActivity.this, SearchOrgActivity.class);
                    startActivity(intent);
                }
            });

            if (default_org.equals("null")) {

                empty.setVisibility(View.VISIBLE);
                empty.setText(getString(R.string.empty_msg_no_default_org_pair_org));
                fab.setVisibility(View.INVISIBLE);

            } else if (pair_org_request.equals("null")) {

                empty.setVisibility(View.VISIBLE);
                empty.setText(getString(R.string.pair_org_no_request));

            } else if (d_org.has("band") && Integer.parseInt(d_org.getString("band")) > 2) {

                empty.setVisibility(View.VISIBLE);
                empty.setText(getString(R.string.not_authorized_view));
                fab.setVisibility(View.INVISIBLE);

            } else {
                p_org = new JSONArray(pair_org_request);

                Log.d(TAG, "p_org length :" + p_org.length());

                HashMap<String , Boolean> map = new HashMap<>();

                for (int i = 0; i < p_org.length(); i++) {
                    JSONObject obj = new JSONObject(p_org.getString(i));
                    if (obj.has("name")) {
                        if(obj.has("id") && !map.containsKey(obj.getString("id"))) {
                            Log.d(TAG, "Org Name for request :" + obj.getString("name"));
                            mDataList.add(new ListData(obj.getString("name")));
                            map.put(obj.getString("id"), true);
                        }
                        else {
                            Log.d(TAG, "Duplicate entry :" + obj.getString("name"));
                        }
                    }
                }

                if (p_org.length() > 0) {
                    listView.setAdapter(new SampleAdapter());
                }
                else {
                    empty.setVisibility(View.VISIBLE);
                    empty.setText(getString(R.string.pair_org_no_request));
                }
            }

        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "Error :" + e.getLocalizedMessage());
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Log.d(TAG, "Clicked at position :"+ position);

                try {
                    JSONObject obj = p_org.getJSONObject(position);
                    Bundle orgData = new Bundle();
                    orgData.putString("org", obj.toString());
                    orgData.putString("type", "action");
                    orgData.putString("position", Integer.toString(position));
                    orgData.putString("second_org", d_org.toString());
                    orgData.putString("pair_org", p_org.toString());
                    Intent intent = new Intent(PairOrgActivity.this, PairOrgDetailsActivity.class);
                    intent.putExtras(orgData);
                    startActivity(intent);

                }
                catch (JSONException | NullPointerException e){
                    e.printStackTrace();
                }
            }
        });

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
                convertView = View.inflate(PairOrgActivity.this, R.layout.list_pair_org, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData item = getItem(position);

            // provide support for selected state
            updateCheckedState(holder, item);

            Log.d(TAG, "List Item: " + item.data);
            holder.textView.setText(item.data);

            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try{

                        Log.d(TAG, "Clicked at position : " +  position);
                        JSONObject obj = p_org.optJSONObject(position);
                        JSONObject body = new JSONObject();
                        body.put("action", "accept");
                        body.put("first_org", obj);
                        body.put("second_org", d_org);
                        body.put("position", position);
                        body.put("id", d_org.getString("id"));
                        new PostClass(PairOrgActivity.this).execute(body);

                    }
                    catch (JSONException | NullPointerException e ){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(PairOrgActivity.this, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });

            holder.ignore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "Clicked at position : " +  position);
                    try{

                        Log.d(TAG, "Clicked at position : " +  position);
                        JSONObject obj = new JSONObject(p_org.getString(position));
                        JSONObject body = new JSONObject();
                        body.put("action", "ignore");
                        body.put("first_org", obj);
                        body.put("second_org", d_org);
                        body.put("id", d_org.getString("id"));
                        new PostClass(PairOrgActivity.this).execute(body);

                    }
                    catch (JSONException | NullPointerException e ){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(PairOrgActivity.this, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });

            return convertView;
        }

        private void updateCheckedState(ViewHolder holder, ListData item) {

            TextDrawable drawable = mDrawableBuilder.build(String.valueOf(item.data.charAt(0)), mColorGenerator.getColor(item.data));
            holder.imageView.setImageDrawable(drawable);
            holder.view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static class ViewHolder {

        private View view;
        private ImageView imageView;
        private TextView textView;
        private Button accept, ignore;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.pairOrgImageView);
            textView = (TextView) view.findViewById(R.id.pairOrgText);
            accept = (Button) view.findViewById(R.id.btn_accept);
            ignore = (Button) view.findViewById(R.id.btn_reject);
        }
    }

    private static class ListData {

        private String data;

        public ListData(String data) {
            this.data = data;
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.side_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Intent intent;

        switch (item.getItemId()) {

            case R.id.nav_inbox: {

                intent = new Intent(PairOrgActivity.this, InboxActivity.class);
                startActivity(intent);
                break;

            }

            case R.id.nav_outbox: {

                intent = new Intent(PairOrgActivity.this, OutboxActivity.class);
                startActivity(intent);
                break;

            }

            case R.id.nav_add_employee: {

                intent = new Intent(PairOrgActivity.this, AddEmployeeActivity.class);
                startActivity(intent);
                break;

            }

            case R.id.nav_pair_prg: {

                intent = new Intent(PairOrgActivity.this, PairOrgActivity.class);
                startActivity(intent);
                break;

            }

            case R.id.nav_add_org: {

                intent = new Intent(PairOrgActivity.this, CreateOrgActivity.class);
                startActivity(intent);
                break;

            }

            case R.id.nav_settings: {

                intent = new Intent(PairOrgActivity.this, SettingActivity.class);
                startActivity(intent);
                break;

            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetClass extends AsyncTask<Bundle, Void, Void> {

        private final Context context;

        public GetClass(Context c) {
            context = c;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            try {

                Log.d(TAG, "In background job");
                final JSONObject obj = new JSONObject(prefs.getString("default_org", "null"));
                Log.d(TAG, "default org: " + obj.getString("id"));

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.get_pending_orgs))
                        .appendPath(obj.getString("id"))
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
                String auth = prefs.getString("uid", "null");

                Log.d(TAG, "auth " + auth);
                if (auth.equals("null")) {
                    onPostExecute();
                    //@TODO add alert
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", auth);

                final int responseCode = connection.getResponseCode();
                final int response = responseCode;

                Log.d(TAG, "Sending 'GET' request to URL : :" + url);
                Log.d(TAG, "Get parameters : " + prefs.getString("default_org", "null"));
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

                Log.d(TAG, "Response from GET :" + sb.toString());
                final ListView listView = (ListView) findViewById(R.id.pairOrg);
                final View empty = findViewById(R.id.empty);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        switch (response) {
                            case 200: {
                                try {
                                    JSONArray arr = new JSONArray(sb.toString());
                                    editor.putString(obj.getString("name") + R.string.incoming_request, arr.toString());
                                    editor.putString(getString(R.string.hard_reload_pair_org), "false");
                                    editor.commit();
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                    break;
                                } catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            case 404: {

                                Log.d(TAG, "404 response");
                                listView.setEmptyView(empty);
                                break;

                            }
                        }
                    }
                });

            } catch (IOException | JSONException | NullPointerException e) {
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

                final JSONObject body = params[0];

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.perform_action_on_org))
                        .appendPath(body.getString("id"))
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
                String auth = prefs.getString("uid", "null");

                Log.d(TAG, "auth " + auth);
                if (auth.equals("null")) {
                    onPostExecute();
                    //@TODO add alert
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", auth);

                Log.d(TAG, "params:" + params.toString());


                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(body.toString());
                dStream.flush();
                dStream.close();

                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'POST' request to URL : :" + url);
                Log.d(TAG, "Post parameters : " + body.toString());
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

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();

                        try {

                            switch (response) {

                                case 200: {

                                    SharedPreferences.Editor editor = prefs.edit();
                                    int position = Integer.parseInt(body.getString("position"));

                                    List<String> list = new ArrayList<>();
                                    for(int i = 0; i < p_org.length(); i++){
                                        list.add(p_org.getJSONObject(i).getString("name"));
                                    }
                                    list.remove(position);

                                    if(list.size() > 0){
                                        p_org = new JSONArray(Arrays.asList(list));
                                    }
                                    else {
                                        p_org = new JSONArray();
                                    }

                                    if(body.has("action") && body.getString("action").equals("accept") && d_org.has("name")){
                                        String paired_orgs = prefs.getString(d_org.getString("name") + R.string.paired_orgs, "null");
                                        JSONArray arr ;
                                        if(paired_orgs.equals("null")){
                                            arr = new JSONArray();
                                        }
                                        else {
                                            arr = new JSONArray(paired_orgs);

                                        }
                                        arr.put(body.get("first_org"));
                                        editor.putString(d_org.getString("name") + R.string.paired_orgs, arr.toString());
                                    }

                                    editor.putString(d_org.getString("name") + R.string.incoming_request, p_org.toString());

                                    editor.commit();

                                    String temp = prefs.getString(d_org.getString("name") + R.string.paired_orgs, "null");
                                    Log.d(TAG, "paired_orgs : " + temp);

                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                    break;
                                }
                                case 409: {
                                    Toast.makeText(context, "Request already exist", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                default: {
                                    throw new org.json.JSONException("409");
                                }
                            }

                        } catch (org.json.JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                            onPostExecute();
                        }

                    }
                });

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                    }
                });


            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }


}
