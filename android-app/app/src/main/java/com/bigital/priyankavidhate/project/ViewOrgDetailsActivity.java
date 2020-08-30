package com.bigital.priyankavidhate.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bigital.priyankavidhate.project.R.string.paired_orgs;

public class ViewOrgDetailsActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "ViewOrgDetailsActivity";
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private List<ListData> mDataList = new ArrayList<>();
    static Bitmap bitmap;
    private String tag = null;
    private ProgressDialog mProgressDialog;
    private SharedPreferences prefs;
    private JSONObject d_org = new JSONObject();
    private JSONArray p_org;
    private JSONObject current_org;
    private Utils util = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_org_details);
        final HashMap<String, JSONObject> nameToid;

        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        mDrawableBuilder = TextDrawable.builder()
                .round();
        findViewById(R.id.btn_send_request).setOnClickListener(ViewOrgDetailsActivity.this);


        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }


        try {
            String orgs_string = prefs.getString("orgs", "null");

            if (orgs_string.equals("null")) {
                throw new Exception("No organization presents");
            }

            final JSONArray orgs = new JSONArray(orgs_string);

            ArrayList<String> orgs_arr = new ArrayList<>();
            nameToid = new HashMap<>();
            ArrayList<String> tags = new ArrayList<>();
            ArrayList<JSONArray> arr = new ArrayList<>();
            for (int i = 0; i < orgs.length(); i++) {

                JSONObject obj = orgs.getJSONObject(i);
                if (obj.has("name") && obj.has("tag") && obj.has("id")) {
                    orgs_arr.add(obj.getString("name") + "-" + " @" + obj.getString("tag"));
                    nameToid.put(obj.getString("name") + "-" + " @" + obj.getString("tag"), obj);
                    String porgs = prefs.getString(obj.getString("tag") + getString(paired_orgs), "null");
                    tags.add(obj.getString("tag"));

                    if (!porgs.equals("null")) {
                        arr.add(new JSONArray(porgs));
                    }
                }

            }

            for (int i = 0; i < arr.size(); i++) {
                JSONArray paired_orgs = arr.get(i);
                for (int j = 0; j < paired_orgs.length(); j++) {

                    JSONObject obj = paired_orgs.getJSONObject(j);
                    if (!tags.contains(obj.getString("tag"))) {
                        orgs_arr.add(obj.getString("name") + "-" + " @" + obj.getString("tag"));
                        nameToid.put(obj.getString("name") + "-" + " @" + obj.getString("tag"), obj);
                    }

                }

            }

            for (String key : nameToid.keySet()) {
                Log.d(TAG, "nameToid key : " + key + " value :" + nameToid.get(key));
            }

            Set<String> hs = new HashSet<>();
            hs.addAll(orgs_arr);
            orgs_arr.clear();
            orgs_arr.addAll(hs);
            final int[] result = new int[1];

            final CharSequence org_names[] = orgs_arr.toArray(new String[0]);
            final TextView currentOrg = (TextView) findViewById(R.id.currentOrg);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.d(TAG, "Cancle called");
                    Intent intent = new Intent(ViewOrgDetailsActivity.this, SettingActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setTitle("Pick an organization");
            builder.setItems(org_names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        result[0] = which;
                        String selected_org = org_names[which].toString();
                        currentOrg.setText("Edit Organization : " + selected_org);
                        current_org = nameToid.get(selected_org);
                        TextView textView;

                        setTitle(current_org.getString("name"));
                        int margin = 0;

                        if (current_org.has("name")) {
                            textView = (TextView) findViewById(R.id.OrgName);
                            textView.setText(current_org.getString("name"));
                        }

                        if (current_org.has("tag")) {
                            textView = (TextView) findViewById(R.id.Tag);
                            textView.setText(current_org.getString("tag"));
                            tag = current_org.getString("tag");
                        }
                        if (current_org.has("branch")) {
                            textView = (TextView) findViewById(R.id.OrgBranch);
                            textView.setText(current_org.getString("branch"));
                        }

                        if (current_org.has("department")) {
                            textView = (TextView) findViewById(R.id.OrgDepartment);
                            textView.setText(current_org.getString("department"));
                        }

                        if (current_org.has("address")) {
                            textView = (TextView) findViewById(R.id.OrgAddress);
                            textView.setText(current_org.getString("address"));
                        }

                        if (current_org.has("country")) {
                            textView = (TextView) findViewById(R.id.OrgCountry);
                            textView.setText(current_org.getString("country"));
                        }

                        if (current_org.has("state")) {
                            textView = (TextView) findViewById(R.id.OrgState);
                            textView.setText(current_org.getString("state"));
                        }

                        if (current_org.has("city")) {
                            textView = (TextView) findViewById(R.id.OrgCity);
                            textView.setText(current_org.getString("city"));
                        }

                        if (current_org.has("zip")) {
                            textView = (TextView) findViewById(R.id.OrgZip);
                            textView.setText(current_org.getString("zip"));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                }

                ;
            });
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                convertView = View.inflate(ViewOrgDetailsActivity.this, R.layout.list_contacts, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListData item = getItem(position);

            // provide support for selected state
            updateCheckedState(holder, item);

            return convertView;
        }

        private void updateCheckedState(ViewHolder holder, ListData item) {

            Log.d(TAG, "setting image view");
            Character c;
            Log.d(TAG, "item.data " + item.data);
            if (Character.isLetter(item.data.charAt(0))) {
                c = Character.toUpperCase(item.data.charAt(0));
            } else {
                c = 'Y';
            }
            TextDrawable drawable = mDrawableBuilder.build(String.valueOf(c), mColorGenerator.getColor(item.data));

            Log.d(TAG, "item.profile_pic " + item.profile_pic);

            Log.d(TAG, "no profile pic");
            holder.imageView.setImageDrawable(drawable);

//            String pic = "null";
//            if (!item.profile_pic.equals("default")) {
//                pic = prefs.getString(item.getNumber() + "_pic", "null");
//            }
//
//            if (!pic.equals("null")) {
//                holder.switcher.showNext();
//                holder.circularImage.setImageBitmap(util.StringToBitMap(pic));
//
//            } else if (!item.profile_pic.equals("default")) {
//                Log.d(TAG, "profile pic");
//                final String image = item.profile_pic;
//                final String tag = item.getNumber();
//                final ViewHolder vHolder = holder;
//                final Context context = ViewOrgDetailsActivity.this;
//                AsyncTask.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        final Bitmap bitMap = util.getBitmapFromURL(image, tag, context);
//                        runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                vHolder.switcher.showNext();
//                                vHolder.circularImage.setImageBitmap(bitMap);
//                            }
//                        });
//                    }
//                });
//
//            } else {
//                Log.d(TAG, "no profile pic");
//                holder.imageView.setImageDrawable(drawable);
//            }

            holder.textView.setText(item.data);
            holder.textView1.setText(item.data1);
            holder.view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private static class ViewHolder {

        private View view;

        private ImageView imageView;
        private CircularImageView circularImage;
        private ViewSwitcher switcher;

        private TextView textView, textView1;

        private ViewHolder(View view) {
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.imageView);
            textView = (TextView) view.findViewById(R.id.textView);
            textView1 = (TextView) view.findViewById(R.id.textView1);
            switcher = (ViewSwitcher) view.findViewById(R.id.image_switcher);
            circularImage = (CircularImageView) view.findViewById(R.id.circularImageView);
        }
    }

    private static class ListData {

        private String data, data1, profile_pic;

        ListData(String data, String data1, String profile_pic) {
            Log.d(TAG, data  + " " + data1 );

            this.data = data;
            this.data1 = data1;
            this.profile_pic = profile_pic;
        }

        String getData() {
            return data;
        }

        String getNumber() {
            return data1;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(ViewOrgDetailsActivity.this, SettingActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        try {


            Log.d(TAG, "Something clicked");

            int i = v.getId();
            if (i == R.id.btn_send_request) {

                Log.d(TAG, "request button clicked");
                JSONObject body = new JSONObject();
                JSONArray orgArr = new JSONArray();

                for (int j = 1; j < 4; j++) {
                    JSONObject org = new JSONObject();
                    if (current_org.has("name") &&  current_org.has("id")) {
                        org.put("id", current_org.getString("id"));
                        org.put("name", current_org.getString("name"));
                        org.put("band", j);
                        orgArr.put(org);
                    }
                }
                body.put("keys", orgArr);
                new GetClass(this).execute(body);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class GetClass extends AsyncTask<JSONObject, Void, Void> {

        private final Context context;

        GetClass(Context c) {
            context = c;
        }

        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(this.context);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(JSONObject... params) {
            try {

                Log.d(TAG, "In background job");

                final JSONObject obj = new JSONObject(prefs.getString("default_org", "null"));

                Log.d(TAG, "default org: " + obj.toString());


                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.http))
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.get_contacts))
                        .build();
                //@TODO add band as query parameter

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

                JSONObject body = params[0];

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(body.toString());
                dStream.flush();
                dStream.close();

                final int response = connection.getResponseCode();
                ;

                Log.d(TAG, "Sending 'POST' request to URL : :" + url);
                Log.d(TAG, "POST parameters : " + body.toString());
                Log.d(TAG, "Response Code : " + response);

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

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        switch (response) {
                            case 200: {
                                onPostExecute();
                                try {

                                    JSONArray arr = new JSONArray(sb.toString());
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject obj = arr.getJSONObject(i);
                                        Log.d(TAG, "Contact :" + obj.toString());
                                        if (obj.has("name") && obj.has("phone_number") && obj.has(("profile_pic"))) {
                                            Log.d(TAG, "Object :" + obj.toString());
                                            mDataList.add(new ListData(obj.getString("name"), obj.getString("phone_number"), obj.getString("profile_pic")));
                                        }

                                    }
                                    if(mDataList.size()>0){
                                        Log.d(TAG, "Size :" + mDataList.size());
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                                        dialog.setTitle("Contacts");
                                        LayoutInflater li =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                        View v = li.inflate(R.layout.dialog_listview, null, false);
                                        ListView listView = (ListView) v.findViewById(R.id.listView);
                                        listView.setAdapter(new SampleAdapter());
                                        dialog.setView(v);
                                        dialog.show();
                                    }

                                } catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
                                    break;
                                }
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
            mProgressDialog.dismiss();
        }

    }
}
