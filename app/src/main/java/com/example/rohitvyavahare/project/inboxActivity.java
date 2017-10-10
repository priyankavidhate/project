package com.example.rohitvyavahare.project;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.extensions.Inbox.Adapter;
import com.rohitvyavahare.extensions.Inbox.ListData;
import com.rohitvyavahare.webservices.GetInbox;
import com.rohitvyavahare.webservices.GetOrgs;
import com.rohitvyavahare.webservices.GetPairedOrgs;
import com.rohitvyavahare.webservices.PostTokenId;
import com.rohitvyavahare.webservices.UpdateActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "InboxActivity";
    protected DrawerLayout drawer;

    // list of data items
    private List<ListData> inboxDataList = new ArrayList<>();
    private Utils util;
    static Bitmap bitmap;
    private TextView currentOrgName;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        util = new Utils();
        storage = new Storage(this);

        enableLayout();
        enableAdView();
        enableToolBar();
        enableNavigationView();
        enableFloatingActionButton();

        setupDbPull();
        setupRefreshToken();
        setupActivityView();
    }

    private void setupActivityView() {
        Bundle bundle = getIntent().getExtras();

        if (bundle != null && bundle.getString("message") != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Warning");
            builder.setMessage(bundle.getString("message"));
            builder.setCancelable(true);
            builder.setNeutralButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            action();
                        }
                    });

            AlertDialog alert11 = builder.create();
            alert11.show();
        } else {
            action();
        }
    }

    private void setupRefreshToken() {
        try {
            FirebaseToken token = new FirebaseToken();
            String storedToken = storage.getRefreshToken();
            if(token.verifyToken(storedToken) && storage.getFirstToken().equals("false")) {
                Log.d(TAG, "Token present, no need to update");
                return;
            }
            Log.d(TAG, "Got new token");
            storage.setRefreshToken(token.getToken());
            new PostTokenId(this, storage)
                    .execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDbPull() {
        try {

            String last_active = storage.getLastActive();
            Long time = Long.parseLong(last_active);
            Long current_time = System.currentTimeMillis();
            Long timeDiff = current_time - time;

            Log.d(TAG, "time : " + time);
            Log.d(TAG, "current_time : " + current_time);
            Log.d(TAG, "time_diff : " + timeDiff);

            if (time < 2 || timeDiff > (86400000 * 7)) {
                Log.d(TAG, "No acivity since : " + time);
                new UpdateActivity(this, storage).execute().get();

                storage.setHardResetInbox("true");
                storage.setHardResetOutbox("true");
                storage.setHardResetPairedOrgs("true");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void enableNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);

        TextView nav_user = (TextView) hView.findViewById(R.id.NavName);
        String username = storage.getUserName();

        if (!username.equals("null")) {
            nav_user.setText(util.capitalizeString(username));
        }

        CircularImageView usr_pic = (CircularImageView) hView.findViewById(R.id.ProfilePic);
        String profile_pic = storage.getProfilePic();
        if (!profile_pic.equals("null")) {

            bitmap = util.StringToBitMap(profile_pic);
            if (bitmap != null) {
                usr_pic.setImageBitmap(bitmap);
            }
        }
    }

    private void enableLayout() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl1);
        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.content_side_bar, null, true);
        rl.addView(layout);
        currentOrgName = (TextView) findViewById(R.id.currentOrg);
    }

    private void enableFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InboxActivity.this, PlaceOrderActivity.class);
                startActivity(intent);
            }
        });
    }

    private void enableToolBar() {
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void enableAdView() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest;
        if (BuildConfig.DEBUG) {
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
            adRequest = new AdRequest.Builder()
                    .addTestDevice(deviceId)
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();

        }
        mAdView.loadAd(adRequest);
    }

    private String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (Byte j : messageDigest) {
                String h = Integer.toHexString(0xFF & j);
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

    private void action() {
        try {

            ListView listView = (ListView) findViewById(R.id.listView);
            final TextView empty = (TextView) findViewById(R.id.empty);

            // check any associated org is present

            String hardReload = storage.getHardResetInbox();
            Log.d(TAG, "Hard Reload Inbox value :" + hardReload);
            if(hardReload.equals("null") || hardReload.equals("true")) {
                Log.d(TAG, "hardReload true, going to make GET request");
                handleGetOrgs();
                return;
            }

            if (storage.getAssociatedOrgs() == null || storage.getAssociatedOrgs().length() == 0) {
                empty.setText(getString(R.string.no_org_msg));
                listView.setEmptyView(empty);
                return;
            }

            if (storage.getDefaultOrg() == null) {
                Log.d(TAG, "Empty Inbox, going to make GET request");
                handleGetOrgs();
                return;
            }

            JSONObject defaultOrg = storage.getDefaultOrg();
            JSONArray pairedOrgs = storage.getPairedOrgs(defaultOrg.getString("tag"));

            hardReload = storage.getHardResetPairedOrgs();
            Log.d(TAG, "Hard Reload Paired orgs value :" + hardReload);
            if(hardReload.equals("null") || hardReload.equals("true")) {
                Log.d(TAG, "hardReload true for Paired orgs, going to make GET request");
                handleGetPairedOrgs();
                pairedOrgs = storage.getPairedOrgs(defaultOrg.getString("tag"));
            }

            if (pairedOrgs == null || pairedOrgs.length() == 0) {
                empty.setText(getString(R.string.no_paired_orgs));
                listView.setEmptyView(empty);
                return;
            }

            if (defaultOrg.has("name")) {
                StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                String text = "Current organization: " + defaultOrg.getString("name");
                SpannableStringBuilder sb = util.setTextWithSpan(text, defaultOrg.getString("name"), boldStyle);
                this.currentOrgName.setText(sb);
            }

            Log.d(TAG, "default org found: " + defaultOrg.getString("name"));
            Log.d(TAG, "Paired orgs :" + pairedOrgs.length());

            int no = 0;

            for (int i = 0; i < pairedOrgs.length(); i++) {

                JSONObject pairedOrg = pairedOrgs.getJSONObject(i);
                Log.d(TAG, "Paired org in consideration :" + pairedOrg.getString("name"));

                if (!pairedOrg.has("name") || !pairedOrg.has("id") || !pairedOrg.has("tag")) {
                    continue;
                }
                no = storage.getNumberOfNotifications(pairedOrg.getString("tag") + "inbox");
                JSONArray ordersFromPairedOrg = storage.getOrdersFrom(pairedOrg.getString("tag"));

                if(ordersFromPairedOrg == null || ordersFromPairedOrg.length() == 0) {
                    Log.d(TAG, "No Orders from :" + pairedOrg.getString("tag"));
                    continue;
                }

                Log.d(TAG, "Orders :" + ordersFromPairedOrg.length());

                int count = util.countInPorgressOrders(ordersFromPairedOrg);
                inboxDataList.add(new ListData(pairedOrg.getString("name"), pairedOrg.getString("tag"),
                        pairedOrg.getString("org_pic"), count, no));
            }
            listView.setAdapter(new Adapter(InboxActivity.this, inboxDataList));
            empty.setVisibility(View.INVISIBLE);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    try {
                        ListData data = inboxDataList.get(position);
                        String orders = storage.getOrdersFrom(data.getTag()).toString();

                        Bundle orderData = new Bundle();
                        orderData.putString("orders", orders);
                        orderData.putString("org_name", data.getData());
                        orderData.putString("org_tag", data.getTag());
                        orderData.putString("type", "inbox");
                        Intent intent = new Intent(InboxActivity.this, OrdersActivityV2.class);
                        storage.setNumberOfNotifications(data.getTag() + "inbox", 0);


                        Log.d(TAG, "Clicked at position :" + position);
                        Log.d(TAG, "Data :" + data.getData());
                        Log.d(TAG, "clicked org_name : " + data.getData());
                        Log.d(TAG, "clicked org_tag : " + data.getTag());
                        for (String key : orderData.keySet()) {
                            Log.d(TAG, key + " is a key in the bundle");
                        }

                        intent.putExtras(orderData);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showMessageOnUi("Something went wrong while retrieving Inbox, Please try again");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi("Something went wrong while retrieving Inbox, Please try again");
        }
    }

    private void handleGetOrgs() {
        try {

            Log.d(TAG, "Starting Get Orgs");
            Bundle output;

            output = new GetOrgs(this, storage).execute().get();

            if (!output.getString("exception").equals("no_exception")) {
                showMessageOnUi(output.getString("exception"));
                return;
            }

            if (output.getBoolean("empty_view")) {
                TextView empty = (TextView) findViewById(R.id.empty);
                empty.setText(getString(R.string.no_org_msg));
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setEmptyView(empty);
                return;
            }

            Log.d(TAG, "Setting hard rest inbox false");

            storage.setHardResetInbox("false");

            currentOrgName.setText(output.getString("current_org_name"));
            handleGetInbox();
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi("Something went wrong while retrieving organizations, Please try again");
        }
    }

    private void handleGetInbox() {
        try {

            Log.d(TAG, "Starting Get Inbox");

            Bundle output = new GetInbox(this, storage)
                    .execute().get();

            if (!output.getString("exception").equals("no_exception")) {
                showMessageOnUi(output.getString("exception"));
                return;
            }

            View empty = findViewById(R.id.empty);
            ListView listView = (ListView) findViewById(R.id.listView);
            if (empty != null)
                listView.setEmptyView(empty);

            handleGetPairedOrgs();

            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi("Something went wrong while retrieving inbox, Please try again");
        }
    }

    private Bundle handleGetPairedOrgs() {

        Log.d(TAG, "Starting Get Paired Orgs");
        Bundle output = new Bundle();
        try {
            output = new GetPairedOrgs(this, storage)
                    .execute().get();

            if (!output.getString("exception").equals("no_exception")) {
                return output;
            }

        } catch (Exception e) {
            output.putString("exception", e.getMessage());
            return output;

        }
        return output;
    }

    private void showMessageOnUi(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(InboxActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Intent intent;

        switch (item.getItemId()) {

            case R.id.nav_inbox: {
                intent = new Intent(InboxActivity.this, InboxActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_outbox: {
                intent = new Intent(InboxActivity.this, OutboxActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_add_employee: {
                intent = new Intent(InboxActivity.this, AddEmployeeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_pair_prg: {
                intent = new Intent(InboxActivity.this, PairOrgActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_add_org: {
                intent = new Intent(InboxActivity.this, CreateOrgActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {

                intent = new Intent(InboxActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
