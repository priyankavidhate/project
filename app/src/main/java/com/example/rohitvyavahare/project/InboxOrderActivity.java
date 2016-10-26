package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class InboxOrderActivity extends AppCompatActivity
        implements OrderFragment.OnHeadlineSelectedListener {

    private static final String TAG = "InboxOrderActivity";
    private ProgressDialog progress;
    private ArrayList<String> list;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.e(TAG, "on create of InboxOrder Activity");
        setTitle("Inbox");

        super.onCreate(savedInstanceState);
        Log.e(TAG, "after super on create of InboxOrder Activity");
//        setContentView(R.layout.fragment_order);
        new GetClass(InboxOrderActivity.this, savedInstanceState).execute(getIntent().getExtras());
        Log.e(TAG, "after setContentView on create of InboxOrder Activity");

        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        Log.e(TAG, "view id :" + findViewById(R.id.fragment_container));
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            Log.e(TAG, "savedInstance is not null");

            // Create an instance of ExampleFragment
            OrderFragment firstFragment = new OrderFragment();

            Bundle bundle = getIntent().getExtras();

            Log.e(TAG, "setting bundle");

            if (bundle != null) {
                Set<String> keys = bundle.keySet();
                Iterator<String> it = keys.iterator();
                Log.e(TAG, "Dumping Intent start");
                while (it.hasNext()) {
                    String key = it.next();
                    Log.e(TAG, "[" + key + "=" + bundle.get(key) + "]");
                }
                Log.e(TAG, "Dumping Intent end");
            }

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            Log.e(TAG, "begining transaction");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }

    }

    public void onArticleSelected(int position) {

        Log.e(TAG, "In article selected");
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        OrderDetails articleFrag = (OrderDetails)
                getSupportFragmentManager().findFragmentById(R.id.article_fragment);

        if (articleFrag != null) {

            Log.d(TAG, "if part of onArticleSelected");
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            articleFrag.updateArticleView(position);

        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            try{

                Log.d(TAG, "else part of onArticleSelected");
                Log.d(TAG, "Position: "+ position);

                JSONObject obj = new JSONObject(list.get(position));
                obj = obj.getJSONObject("value");
                setTitle(obj.getString("id"));

                // Create fragment and give it an argument for the selected article

                OrderDetails newFragment = new OrderDetails();
                Bundle args = new Bundle();
                args.putInt(OrderDetails.ARG_POSITION, position);
                args.putString("doc", list.get(position));
                newFragment.setArguments(args);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragment_container, newFragment, "order_details");
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();


            }
            catch (org.json.JSONException e) {
                e.printStackTrace();
                Toast.makeText(InboxOrderActivity.this, "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
            }


        }
    }

    private class GetClass extends AsyncTask<Bundle, Void, Void> {

        private final Context context;
        private final Bundle savedInstanceState;

        public GetClass(Context c, Bundle b) {
            context = c;
            savedInstanceState = b;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            try {

                Log.e(TAG, "In background job");
                Bundle bundle = params[0];
                if (bundle != null) {
                    Set<String> keys = bundle.keySet();
                    Iterator<String> it = keys.iterator();
                    Log.e(TAG, "Dumping Intent start");
                    while (it.hasNext()) {
                        String key = it.next();
                        Log.e(TAG, "[" + key + "=" + bundle.get(key) + "]");
                    }
                    Log.e(TAG, "Dumping Intent end");
                }

                JSONObject account = new JSONObject(bundle.getString("account"));

                String call = getString(R.string.server_url) + getString(R.string.org) + "/" + account.getString("org") + getString(R.string.orders);
                Log.d(TAG, "url:" + call);
                URL url = new URL(call);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                final int responseCode = connection.getResponseCode();
                final int response = responseCode;

                Log.d(TAG, "Sending 'GET' request to URL : :" + url);
                Log.d(TAG, "Get parameters : " + account.getString("org"));
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

//                System.out.println("\nSending 'GET' request to URL : " + url);
//                System.out.println("Response Code : " + responseCode);
//

//
//                if (responseCode == 200) {
//                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    StringBuilder sb = new StringBuilder();
//                    String line;
//                    while ((line = br.readLine()) != null) {
//                        sb.append(line + "\n");
//                    }
//                    br.close();
//                    JSONObject jObject = new JSONObject(sb.toString());
//                    Iterator<?> keys = jObject.keys();
//                    System.out.println("Response : " + sb.toString());
//                    while (keys.hasNext()) {
//                        String key = (String) keys.next();
//                        String value = jObject.getString(key);
//                        map.put(key, value);
//                    }
//
//                    arr = new ArrayList<>();
//                    Log.e(TAG, "adding to array :" + map.get("to"));
//                    arr.add(map.get("to"));
//                }

                InboxOrderActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        switch (response) {
                            case 200: {
                                try {

                                    setContentView(R.layout.orders);
                                    list = new ArrayList<>();
                                    JSONArray jArray = new JSONArray(sb.toString());
                                    for (int i = 0; i < jArray.length(); ++i) {
                                        JSONObject rec = jArray.getJSONObject(i);
                                        rec = rec.getJSONObject("doc");
                                        list.add(rec.toString());
                                    }

                                    if (savedInstanceState == null) {
                                        // During initial setup, plug in the details fragment.
                                        OrderFragment newFragment = new OrderFragment();
                                        Bundle bundle = getIntent().getExtras();
                                        bundle.putStringArrayList("list", list);
                                        newFragment.setArguments(bundle);
                                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                                        // Replace whatever is in the fragment_container view with this fragment,
                                        // and add the transaction to the back stack so the user can navigate back
                                        transaction.replace(R.id.fragment_container, newFragment);
                                        transaction.addToBackStack(null);

                                        // Commit the transaction
                                        transaction.commit();
                                    }
                                    break;
                                } catch (org.json.JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
                                    onPostExecute();
                                    break;
                                }
                            }
                        }

                    }
                });

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPostExecute();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }

}
