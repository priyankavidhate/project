package com.example.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.rohitvyavahare.project.R.string.account;

public class SignUpForm extends AppCompatActivity {

    private ProgressDialog progress ;

    private static final String TAG = "SignUpFromActivity";
    Button button;
    EditText edit;
    TextView text;
    Bundle extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);
        extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.getString("fname") != null) {
                edit = (EditText) findViewById(R.id.EditTextFName);
                edit.setText(extras.getString("fname"));
            }
            if(extras.getString("lname") != null) {
                edit = (EditText) findViewById(R.id.EditTextLName);
                edit.setText(extras.getString("lname"));
            }
            if(extras.getString("email") != null) {
                text = (TextView) findViewById(R.id.EmailText);
                text.setText(extras.getString("email"));
            }
            if (extras.getString("org") != null) {
                edit = (EditText) findViewById(R.id.EditTextOrgName);
                edit.setText(extras.getString("org"));
                edit.setInputType(InputType.TYPE_NULL);
            }
            if(extras.getString("org") == null && edit.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
            //The key argument here must match that used in the other activity
        }
        String imageUrl = extras.getString("photo_url").toString();
        Log.d(TAG, "url:" + imageUrl);
        addListenerOnButton(extras);

    }

    public void addListenerOnButton(final Bundle bundle) {

        button = (Button) findViewById(R.id.SignUpSubmit);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                edit = (EditText) findViewById(R.id.EditTextOrgName);
                if( edit.getText().toString().trim().equals("")){
                    edit.setError( "Organization name is required!" );
                }
                if(extras.getString("org") != null) {
                    new PutClass(SignUpForm.this).execute(bundle);
                }
                else {
                    bundle.putString("org", edit.getText().toString().trim());
                    bundle.putString("role", "owner");
                    new PostClass(SignUpForm.this).execute(bundle);
                }

            }

        });

    }

    @Override
    protected void onDestroy() {
        try {
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private class PostClass extends AsyncTask<Bundle, Void, Void> {

        private Context context;

        public PostClass(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            try {

                //final TextView outputView = (TextView) findViewById(R.id.showOutput);
                URL url = new URL("http://10.0.2.2:5000/account");

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                Log.d(TAG, "params:" +  params.toString());
                Bundle b = params[0];
                Log.d(TAG, "b:" +  b.getString("fname"));
                JSONObject account   = new JSONObject();
                account.put("first_name", b.getString("fname"));
                account.put("last_name", b.getString("lname"));
                account.put("email", b.getString("email"));
                account.put("org", b.getString("org"));
                account.put("confirm", "true");

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(account.toString());
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'POST' request to URL : :" +  url);
                Log.d(TAG, "Post parameters : " + account.toString());
                Log.d(TAG, "Response Code : " + responseCode);

                final int response = responseCode;

                SignUpForm.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //outputView.setText(output);
                        if(response == 200){

                            Intent intent = new Intent(SignUpForm.this, HomeActivity.class);
                            startActivity(intent);
                            progress.dismiss();


                        }

                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }

    private class PutClass extends AsyncTask<Bundle, Void, Void> {

        private Context context;

        public PutClass(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            try {
                Bundle b = params[0];
                String call = getString(R.string.server_url) + getString(account) + "/" + b.getString("email");
                URL url = new URL(call);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                Log.d(TAG, "params:" +  params.toString());
                Log.d(TAG, "b:" +  b.getString("fname"));
                JSONObject account   = new JSONObject();
                account.put("first_name", b.getString("fname"));
                account.put("last_name", b.getString("lname"));
                account.put("email", b.getString("email"));
                account.put("org", b.getString("org"));
                account.put("confirm", "true");

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(account.toString());
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'PUT' request to URL : :" +  url);
                Log.d(TAG, "PUT parameters : " + account.toString());
                Log.d(TAG, "Response Code : " + responseCode);

                final int response = responseCode;

                SignUpForm.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //outputView.setText(output);
                        if(response == 201 || response == 200){

                            Intent intent = new Intent(SignUpForm.this, HomeActivity.class);
                            startActivity(intent);
                            progress.dismiss();


                        }

                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }

}

