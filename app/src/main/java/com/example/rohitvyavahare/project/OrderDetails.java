package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by rohitvyavahare on 10/8/16.
 */

public class OrderDetails extends Fragment {
    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;
    private ProgressDialog progress;
    private static final String TAG = "OrderFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_details, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateArticleView(args.getInt(ARG_POSITION));
        } else if (mCurrentPosition != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateArticleView(mCurrentPosition);
        }
    }

    public void updateArticleView(int position) {

        try {

            HashMap<String, String> possibleStatus = new HashMap<>();
            possibleStatus.put("created", "Mark acknowledged");
            possibleStatus.put("acknowledged", "Mark shipped");
            possibleStatus.put("shipping", "Mark order completion");
            possibleStatus.put("cancelled", "Cancel Order");
            Bundle b = getArguments();
            JSONObject obj = new JSONObject(b.getString("doc"));
            obj = obj.getJSONObject("value");
            TextView article = (TextView) getActivity().findViewById(R.id.ViewOrderFrom);
            article.setText("From: " + obj.getString("from"));
            article = (TextView) getActivity().findViewById(R.id.ViewOrderItem);
            article.setText("Item: " + obj.getString("item"));
            article = (TextView) getActivity().findViewById(R.id.ViewOrderQuantity);
            article.setText("Quantity: " + obj.getString("quantity"));
            article = (TextView) getActivity().findViewById(R.id.ViewOrderStatus);
            article.setText("Status: " + obj.getString("status"));

            DateFormat dffrom = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateFormat dfto = new SimpleDateFormat("EEE, dd MMM yyyy");
            Date today = dffrom.parse(obj.getString("created"));
            String s = dfto.format(today);

            article = (TextView) getActivity().findViewById(R.id.ViewOrderCreatedAt);
            article.setText("Created On: " + s);

            today = dffrom.parse(obj.getString(obj.getString("status")));
            s = dfto.format(today);

            article = (TextView) getActivity().findViewById(R.id.ViewOrderLastUpdatedAt);
            article.setText("Last Updated On: " + s);

            if (obj.getString("status").equals("sender_completed")) {
                article = (TextView) getActivity().findViewById(R.id.ViewReceiverCompletedStatus);
                article.setText(R.string.sender_completed);
            } else if (obj.getString("status").equals("receiver_completed")) {
                article = (TextView) getActivity().findViewById(R.id.ViewReceiverCompletedStatus);
                article.setText(R.string.receiver_completed);
            } else if (obj.getString("status").equals("cancelled")) {
                article = (TextView) getActivity().findViewById(R.id.ViewReceiverCompletedStatus);
                article.setText(R.string.cancelled);
                Button button = (Button) getActivity().findViewById(R.id.CancelOrder);
                button.setVisibility(View.GONE);

            }else {
                ViewSwitcher switcher = (ViewSwitcher) getActivity().findViewById(R.id.my_switcher_2);
                switcher.showNext();
                Button btn = (Button) getActivity().findViewById(R.id.ChangeStatus);
                btn.setText(possibleStatus.get(obj.getString("status")));
            }

            Log.d(TAG, "Position :" + position + " arg: " + b.getString("doc"));
            mCurrentPosition = position;

            addListenerOnButton(new JSONObject(b.getString("doc")));

        } catch (org.json.JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void addListenerOnButton(final JSONObject obj) {

        Button button = (Button) getActivity().findViewById(R.id.ChangeStatus);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new PutClass(getActivity()).execute(obj);
            }

        });

        button = (Button) getActivity().findViewById(R.id.CancelOrder);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try{
                    //obj.put("status", "cancelled");
                    obj.getJSONObject("value").put("status", "cancelled");
                    new PutClass(getActivity()).execute(obj);
                }catch (org.json.JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
                }

            }

        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }

    private class PutClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        public PutClass(Context c) {
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
                JSONObject b = params[0];
                String call = getString(R.string.server_url) + getString(R.string.order) + "/" + b.getString("_id");
                Log.d(TAG, "call: " +call);
                URL url = new URL(call);
                Log.d(TAG, "Sending 'PUT' request to URL : :" + url);
                Log.d(TAG, "PUT parameters : " + b.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                Log.d(TAG, "params:" + b.toString());

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(b.toString());
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Response Code : " + responseCode);

                final int response = responseCode;
                final StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader br;

                try{
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                }
                catch (IOException ioe) {
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPutExecute();

                        switch (response) {
                            case 200: {
                                Toast.makeText(context, "Successfully updated request", Toast.LENGTH_SHORT).show();
                                Fragment frg = null;
                                frg = getActivity().getSupportFragmentManager().findFragmentByTag("order_details");
                                frg.getArguments().putString("doc", sb.toString());
                                final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                ft.detach(frg);
                                ft.attach(frg);
                                ft.commit();
                                break;
                            }
                            case 400: {
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                break;

                            }
                            default: {
                                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }


                    }
                });


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPutExecute();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPutExecute();
            } catch (org.json.JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                onPutExecute();
            }
            return null;
        }

        protected void onPutExecute() {
            progress.dismiss();
        }

    }
}

