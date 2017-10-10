package com.example.rohitvyavahare.project;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.webservices.PostFeedback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.rohitvyavahare.project.InboxActivity.bitmap;

public class SettingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Utils util = new Utils();
    private static final String TAG = "SettingActivity";
    private List<ListData> mDataList = new ArrayList<>();
    Storage storage;
    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl1);
        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.activity_setting, null, true);
        rl.addView(layout);
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        editor = prefs.edit();
        c = this;
        storage = new Storage(this);

        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);
        TextView nav_user = (TextView) hView.findViewById(R.id.NavName);
        String username = prefs.getString("user_name", "null");

        if (!username.equals("null")) {
            nav_user.setText(util.capitalizeString(username));
        }


        CircularImageView usr_pic = (CircularImageView) hView.findViewById(R.id.ProfilePic);
        String profile_pic = prefs.getString("profile_pic", "null");
        if (!profile_pic.equals("null")) {

            if (bitmap != null) {
                usr_pic.setImageBitmap(bitmap);
            }
            else {
                bitmap = util.StringToBitMap(profile_pic);
                if (bitmap != null) {
                    usr_pic.setImageBitmap(bitmap);
                }
            }
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new SampleAdapter());

        mDataList.add(new ListData("Change Organization"));
        mDataList.add(new ListData("Edit Organization"));
        mDataList.add(new ListData("View Organization"));
        mDataList.add(new ListData("Add Items"));
        mDataList.add(new ListData("Edit Profile"));
        mDataList.add(new ListData("Payment"));
        mDataList.add(new ListData("Feedback"));
        mDataList.add(new ListData("Hard Reset"));


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                switch (position) {

                    case 0: {

                        try{

                            final JSONArray arr = storage.getAssociatedOrgs();

                            final ArrayList<String> orgs_arr = new ArrayList<>();
                            for(int i=0; i< arr.length(); i++){

                                JSONObject obj = arr.getJSONObject(i);
                                if(obj.has("name")){
                                    orgs_arr.add(obj.getString("name"));
                                }

                            }

                            Log.d(TAG, "Number of orgs in org_arr :" + orgs_arr.size());


                            Set<String> hs = new HashSet<>();
                            hs.addAll(orgs_arr);
                            orgs_arr.clear();
                            orgs_arr.addAll(hs);

                            Log.d(TAG, "Number of orgs in org_arr after hashset :" + orgs_arr.size());

                            final CharSequence org_names[] =  orgs_arr.toArray(new String[0]);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                            builder.setTitle("Pick an organization");
                            builder.setItems(org_names, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    try{

                                        Log.d(TAG, "Selected org position :"+ which);
                                        String orgName = orgs_arr.get(which);
                                        Log.d(TAG, "Selected org name :" + orgName);

                                        for(int i=0; i<arr.length(); i++){
                                            JSONObject obj = arr.getJSONObject(i);
                                            if(obj.has("name") && obj.getString("name").equals(orgName) && obj.has("id")){
                                                if(storage.getPairedOrgs(obj.getString("id")) == null || storage.getDefaultOrg().getString("id").length() == 0){
                                                    storage.setHardResetPairedOrgs("true");
                                                }
                                                storage.setDefaultOrg(obj.toString());
                                            }

                                        }

                                    }
                                    catch(Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });
                            builder.show();

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        break;
                    }

                    case 1: {

                        Intent intent = new Intent(SettingActivity.this, EditOrgActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 2: {

                        Intent intent = new Intent(SettingActivity.this, ViewOrgDetailsActivity.class);
                        startActivity(intent);
                        break;
                    }

                    case 3: {

                        Intent intent = new Intent(SettingActivity.this, OrgItemsActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 4: {

                        Intent intent = new Intent(SettingActivity.this, EditProfileActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 5: {

                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle("Payment details")
                                .setMessage("Coming soon")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();

                        break;

                    }
                    case 6: {

                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                        builder.setTitle("Add comment");

                        // Set up the input
                        final EditText input = new EditText(SettingActivity.this);
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        input.setMaxLines(5);
                        builder.setView(input);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    final String msg = input.getText().toString();
                                    Bundle input = new Bundle();
                                    input.putString("message", msg);
                                    new PostFeedback(c, storage).execute(input).get();

                                } catch (Exception e) {
                                    e.printStackTrace();

                                }}

                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                        break;
                    }
                    case 7: {

                        storage.setHardResetOutbox("true");
                        storage.setHardResetPairedOrgs("true");
                        storage.setHardResetInbox("true");

                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle("Success")
                                .setMessage("Hard reset applied successfully")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setIcon(R.drawable.ic_done_black_24dp)
                                .show();

                        break;

                    }

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
                    convertView = View.inflate(SettingActivity.this, R.layout.list_setting_items, null);
                    holder = new ViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                ListData item = getItem(position);

                // provide support for selected state
                holder.textView.setText(item.data);

                return convertView;
            }
        }

        private static class ViewHolder {

            private View view;

            private TextView textView;

            private ViewHolder(View view) {
                this.view = view;
                textView = (TextView) view.findViewById(R.id.textView);
            }
        }

        private static class ListData {

            private String data;

            ListData(String data) {
                this.data = data;
            }

            public String getData() {
                return data;
            }

        }

        @Override
        public void onBackPressed () {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.side_bar, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.

            return super.onOptionsItemSelected(item);
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected (MenuItem item){
            // Handle navigation view item clicks here.
            Intent intent;

            switch (item.getItemId()) {

                case R.id.nav_inbox: {
                    intent = new Intent(SettingActivity.this, InboxActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;

                }

                case R.id.nav_outbox: {

                    intent = new Intent(SettingActivity.this, OutboxActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;

                }

                case R.id.nav_add_employee: {

                    intent = new Intent(SettingActivity.this, AddEmployeeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;

                }

                case R.id.nav_pair_prg: {

                    intent = new Intent(SettingActivity.this, PairOrgActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;

                }

                case R.id.nav_add_org: {

                    intent = new Intent(SettingActivity.this, CreateOrgActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;

                }

                case R.id.nav_settings: {

                    intent = new Intent(SettingActivity.this, SettingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;

                }

            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

    }
