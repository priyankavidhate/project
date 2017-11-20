package com.example.rohitvyavahare.project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.rohitvyavahare.webservices.GetOutbox;
import com.rohitvyavahare.webservices.GetPairedOrgs;
import com.rohitvyavahare.webservices.PostTokenId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.example.rohitvyavahare.project.InboxActivity.bitmap;

public class OutboxActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "OutboxActivity";

    // list of data items

    private List<ListData> outboxDataList = new ArrayList<>();
    ListView listView;
    TextView empty;
    Utils util;
    Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        util = new Utils();
        storage = new Storage(this);

        enableToolBar();
        enableLayout();
        enableAdView();
        enableNavigationView();
        enableFloatingActionButton();
        setupRefreshToken();
        setupDbPull();
    }

    private void setupRefreshToken() {
        try {
            FirebaseToken token = new FirebaseToken();
            String storedToken = storage.getRefreshToken();
            if(token.verifyToken(storedToken) && storage.getFirstToken().equals("false")) {
                Log.d(TAG, "Token present, no need to update");
                return;
            }
            storage.setRefreshToken(token.getToken());
            Log.d(TAG, "Got new token");

            new PostTokenId(this, storage)
                    .execute().get();
        } catch (Exception e) {
            showMessageOnUi(e.getMessage());
            e.printStackTrace();
        }
    }

    private Bundle preWork() {
        Bundle output = new Bundle();
        try {
            output = new GetOrgs(this, storage).execute().get();
            if (!output.getString("exception").equals("no_exception")) {
                return output;
            }

            output = new GetInbox(this, storage)
                    .execute().get();

            if (!output.getString("exception").equals("no_exception")) {
                return output;
            }

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

    private void setupDbPull() {

        try {
            if (storage.getAssociatedOrgs() == null || storage.getAssociatedOrgs().length() == 0) {
                action();
                return;
            }

            String hardReload = storage.getHardResetOutbox();
            if (hardReload.equals("null") || hardReload.equals("true")) {
                Log.d(TAG, "Hard Reload :" + hardReload);
                handleGetOutbox();
            }
            else {
                action();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
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
            if (bitmap != null) {
                usr_pic.setImageBitmap(bitmap);
            } else {
                bitmap = util.StringToBitMap(profile_pic);
                if (bitmap != null) {
                    usr_pic.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void enableLayout() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl1);
        LayoutInflater layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.content_side_bar, null, true);
        rl.addView(layout);

        listView = (ListView) findViewById(R.id.listView);
        empty = (TextView) findViewById(R.id.empty);
        empty.setText("Outbox is empty");
    }

    private void enableFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OutboxActivity.this, PlaceOrderActivity.class);
                startActivity(intent);
            }
        });
    }

    private void enableToolBar() {
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

    private void handleGetOutbox() {
        try {
            Bundle output;
            if(storage.getDefaultOrg() == null) {
                output = preWork();
                if (!output.getString("exception").equals("no_exception")) {
                    showMessageOnUi(output.getString("exception"));
                    return;
                }
            }
            Log.d(TAG, "Going to make get outbox call");
            output = new GetOutbox(this, storage)
                    .execute().get();

            if (!output.getString("exception").equals("no_exception")) {
                showMessageOnUi(output.getString("exception"));
                return;
            }

            if (output.getBoolean("empty_view")) {
                empty.setText(getString(R.string.no_outbox_order_msg));
                listView.setEmptyView(empty);
                return;
            }

            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private void action() {
        try {

            Log.d(TAG, "In Action");

            final TextView currentOrgName = (TextView) findViewById(R.id.currentOrg);

            // check any associated org is present
            if (storage.getAssociatedOrgs() == null || storage.getAssociatedOrgs().length() == 0) {
                empty.setText(getString(R.string.no_org_msg));
                listView.setEmptyView(empty);
                return;
            }

            String hardReload = storage.getHardResetOutbox();

            if(hardReload.equals("null") || hardReload.equals("true")) {
                Log.d(TAG, "hardReload true, going to make GET request");
                handleGetOutbox();
                return;
            }

            if (storage.getDefaultOrg() == null || !storage.getDefaultOrg().has("tag")) {
                Log.d(TAG, "No default org");
                empty.setText(getString(R.string.empty_msg_no_default_org_pair_org));
                return;
            }

            JSONObject defaultOrg = storage.getDefaultOrg();
            JSONArray pairedOrgs = storage.getPairedOrgs(defaultOrg.getString("tag"));

            if (pairedOrgs == null || pairedOrgs.length() == 0) {
                empty.setText(getString(R.string.no_paired_orgs));
                listView.setEmptyView(empty);
                return;
            }

            Log.d(TAG, "default org found: " + defaultOrg.getString("name"));
            Log.d(TAG, "Paired orgs :" + pairedOrgs.length());

            if (defaultOrg.has("name")) {
                StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                String text = "Current organization: " + defaultOrg.getString("name");
                SpannableStringBuilder sb = util.setTextWithSpan(text, defaultOrg.getString("name"), boldStyle);
                currentOrgName.setText(sb);
            }

            int no = 0;

            for (int i = 0; i < pairedOrgs.length(); i++) {

                JSONObject pairedOrg = pairedOrgs.getJSONObject(i);
                Log.d(TAG, "Paired org in consideration :" + pairedOrg.getString("name"));

                if (!pairedOrg.has("name") || !pairedOrg.has("id") || !pairedOrg.has("tag")) {
                    continue;
                }

                Log.d(TAG, "Number Of notification for org :"+ pairedOrg.getString("id") + "outbox");
                no = storage.getNumberOfNotifications(pairedOrg.getString("tag") + "outbox");
                JSONArray ordersToPairedOrg = storage.getOrdersTo(pairedOrg.getString("tag"));

                if(ordersToPairedOrg == null || ordersToPairedOrg.length() == 0) {
                    Log.d(TAG, "No Orders from :" + pairedOrg.getString("tag"));
                    continue;
                }

                Log.d(TAG, "orders :" + ordersToPairedOrg.toString());
                int count = util.countInPorgressOrders(ordersToPairedOrg);
                outboxDataList.add(new ListData(pairedOrg.getString("name"), pairedOrg.getString("tag"),
                        pairedOrg.getString("org_pic"), count, no));
            }

            listView.setAdapter(new Adapter(OutboxActivity.this, outboxDataList));
            empty.setVisibility(View.INVISIBLE);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    try {

                        ListData data = outboxDataList.get(position);
                        String orders = storage.getOrdersTo(data.getTag()).toString();

                        Bundle orderData = new Bundle();
                        orderData.putString("orders", orders);
                        orderData.putString("org_name", data.getData());
                        orderData.putString("org_tag", data.getTag());
                        orderData.putString("type", "outbox");

                        Intent intent = new Intent(OutboxActivity.this, OrdersActivityV2.class);
                        Log.d(TAG, "Setting 0 Number Of notification for org :"+ data.getData() + "outbox");
                        storage.setNumberOfNotifications(data.getData() + "outbox", 0);

                        intent.putExtras(orderData);
                        Log.d(TAG, "Clicked at position :" + position);
                        Log.d(TAG, "Data :" + data.getData());
                        Log.d(TAG, "Tag :" + data.getTag());
                        for (String key : orderData.keySet()) {
                            Log.d(TAG, key + " is a key in the bundle");
                        }
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                        showMessageOnUi(e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }

    }

    private void showMessageOnUi(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(OutboxActivity.this, message, Toast.LENGTH_SHORT).show();
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
                intent = new Intent(OutboxActivity.this, InboxActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }

            case R.id.nav_outbox: {
                intent = new Intent(OutboxActivity.this, OutboxActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }

            case R.id.nav_add_employee: {
                intent = new Intent(OutboxActivity.this, AddEmployeeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
            case R.id.nav_pair_prg: {
                intent = new Intent(OutboxActivity.this, PairOrgActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
            case R.id.nav_add_org: {
                intent = new Intent(OutboxActivity.this, CreateOrgActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {
                intent = new Intent(OutboxActivity.this, SettingActivity.class);
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