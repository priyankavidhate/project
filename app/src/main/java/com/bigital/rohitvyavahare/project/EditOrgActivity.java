package com.bigital.rohitvyavahare.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class EditOrgActivity extends AppCompatActivity
        implements View.OnClickListener{

    private ProgressDialog mProgressDialog, progress;
    private static final String TAG = "EditOrgActivity";
    EditText orgName, orgBranch, department, orgAddress, orgCity, orgZip, orgTag;
    TextView orgCountry;
    private static final int SELECT_PICTURE = 1;
    private Bitmap bitmap = null;
    private JSONObject current_org;
    private AutoCompleteTextView orgState;
    private String state = "";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private SharedPreferences prefs;
    private  SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_org);
        setTitle("Edit an Organization");
        final HashMap<String, JSONObject> nameToid;
        findViewById(R.id.btn_save).setOnClickListener(EditOrgActivity.this);
        findViewById(R.id.btn_cancel).setOnClickListener(EditOrgActivity.this);
        findViewById(R.id.EditProfilePic).setOnClickListener(EditOrgActivity.this);

        try{
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


            orgState = (AutoCompleteTextView) findViewById(R.id.AutoComepleteOrgState);
            final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.State));

            orgState.setAdapter(adapter);
            orgState.setThreshold(1);
            orgState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        state = adapter.getItem(position).toString();
                    } catch (java.lang.NullPointerException e) {
                        e.printStackTrace();
                    }

                }
            });

            prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
            String orgs_string = prefs.getString("orgs", "null");

            if(orgs_string.equals("null")){
                throw new Exception("No organization presents");
            }

            final JSONArray orgs = new JSONArray(orgs_string);

            ArrayList<String> orgs_arr = new ArrayList<>();
            nameToid = new HashMap<>();
            for(int i=0; i< orgs.length(); i++){

                JSONObject obj = orgs.getJSONObject(i);
                if(obj.has("name") && obj.has("tag") && obj.has("id")){
                    orgs_arr.add(obj.getString("name") + "-" + " @" + obj.getString("tag"));
                    nameToid.put(obj.getString("name") + "-" + " @" + obj.getString("tag"), obj);
                }

            }

            for(String key : nameToid.keySet()){
                Log.d(TAG, "nameToid key : " + key + " value :" + nameToid.get(key));
            }

            Set<String> hs = new HashSet<>();
            hs.addAll(orgs_arr);
            orgs_arr.clear();
            orgs_arr.addAll(hs);
            final int[] result = new int [1];

            final CharSequence org_names[] =  orgs_arr.toArray(new String[0]);
            final TextView currentOrg = (TextView) findViewById(R.id.currentOrg);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Intent intent = new Intent(EditOrgActivity.this, SettingActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setTitle("Pick an organization");
            builder.setItems(org_names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try{

                        result[0] = which;
                        String selected_org = org_names[which].toString();
                        currentOrg.setText("Edit Organization : " + selected_org);
                        orgName = (EditText) findViewById(R.id.EditTextOrgName);
                        orgTag = (EditText) findViewById(R.id.EditOrgTag);
                        orgBranch = (EditText) findViewById(R.id.EditOrgBranch);
                        department = (EditText) findViewById(R.id.EditDepartment);
                        orgAddress = (EditText) findViewById(R.id.EditOrgAddress);
                        orgCountry = (TextView) findViewById(R.id.EditOrgCountry);
                        orgCity = (EditText) findViewById(R.id.EditOrgCity);
                        orgZip = (EditText) findViewById(R.id.EditOrgZip);

                        current_org = nameToid.get(selected_org);

                        if (current_org.has("band") && Integer.parseInt(current_org.getString("band")) > 2) {
                            return;
                        }

                        ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher_4);
                        switcher.showNext();

                        if(current_org.has("name")){
                            orgName.setText(current_org.getString("name"));
                        }
                        if (current_org.has("tag")){
                            orgTag.setText(current_org.getString("tag"));
                        }
                        if (current_org.has("branch")){
                            orgBranch.setText(current_org.getString("branch"));
                        }
                        if (current_org.has("department")){
                            department.setText(current_org.getString("department"));
                        }
                        if (current_org.has("address")){
                            orgAddress.setText(current_org.getString("address"));
                        }
                        if (current_org.has("country")){
                            orgCountry.setText(current_org.getString("country"));
                        }
                        if (current_org.has("city")){
                            orgCity.setText(current_org.getString("city"));
                        }
                        if (current_org.has("zip")){
                            orgZip.setText(current_org.getString("zip"));
                        }
                        if(current_org.has("state")){
                            Log.d(TAG, "State Position:"+ adapter.getPosition(current_org.getString("state")));
                            Log.d(TAG, "Current State: "+ current_org.getString("state"));
                            orgState.setText(current_org.getString("state"));
                            state = current_org.getString("state");
                        }
                        if(current_org.has("org_pic") && !current_org.getString("org_pic").equals("default")){
                            Log.d(TAG, "Org pic :" + current_org.getString("org_pic"));
                            final Utils util = new Utils();
                            final CircularImageView imageView = (CircularImageView) findViewById(R.id.EditProfilePic);
                            String pic;
                            pic = prefs.getString(current_org.getString("tag") + "_pic", "null");

                            if(!pic.equals("null")){
                                imageView.setImageBitmap((util.StringToBitMap(pic)));

                            }
                            else {
                                Log.d(TAG, "org pic");
                                final String image = current_org.getString("org_pic");
                                final String tag = current_org.getString("tag");
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        final Bitmap bitMap = util.getBitmapFromURL(image, tag, EditOrgActivity.this);
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                imageView.setImageBitmap(bitMap);
                                            }
                                        });
                                    }
                                });

                            }
                        }

                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            });
            builder.show();
        }
        catch(java.lang.Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        final Intent intent;

        int i = v.getId();
        if (i == R.id.EditProfilePic) {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_PICTURE);
        }
        if (i == R.id.btn_save) {

            if(bitmap != null || isOrgChanged(current_org)){
                Button btn = (Button) findViewById(R.id.btn_save);
                btn.requestFocus();
                newOrg();
            }
            else{
                final Context c = this.getBaseContext();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(c, "No changes has been made to this organization", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
        if(i == R.id.btn_cancel){
            onBackPressed();
        }



    }

    boolean isOrgChanged(JSONObject currnetOrg){

        try{
            Log.d(TAG, "Department :" + department.getText().toString());
            Log.d(TAG, "Old Value department :" + currnetOrg.getString("department"));
            boolean isChanged = false;
            if(!orgName.getText().toString().equals(currnetOrg.getString("name")))
                isChanged = true;
            if(!orgTag.getText().toString().equals(currnetOrg.getString("tag")))
                isChanged = true;
            if(!orgBranch.getText().toString().equals(currnetOrg.getString("branch")))
                isChanged = true;
            if(!orgAddress.getText().toString().equals(currnetOrg.getString("address")))
                isChanged = true;
            if(!orgZip.getText().toString().equals(currnetOrg.getString("zip")))
                isChanged = true;
            if(!department.getText().toString().equals(currnetOrg.getString("department")))
                isChanged = true;
            if(!orgCity.getText().toString().equals(currnetOrg.getString("city")))
                isChanged = true;
            if(!orgState.getText().toString().equals(currnetOrg.getString("state")))
                isChanged = true;

            return isChanged;
        }
        catch (final JSONException e){
            e.printStackTrace();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(EditOrgActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }

    }

    boolean validateData(){

        boolean failFlag = false;
        if (orgName.getText().toString().trim().length() == 0) {
            failFlag = true;
            orgName.setError("A value is required");
        }

        if (!Character.isLetter(orgName.getText().toString().trim().charAt(0))) {
            failFlag = true;
            orgName.setError("Organization name should start with a letter");
        }
        if (orgTag.getText().toString().trim().length() == 0) {
            failFlag = true;
            orgTag.setError("A value is required");
        }
        if (orgTag.getText().toString().trim().length() < 3) {
            failFlag = true;
            orgTag.setError("tag should at least 3 character");
        }
        if (!Character.isLetter(orgTag.getText().toString().trim().charAt(0))) {
            failFlag = true;
            orgTag.setError("Tag should start with a letter");
        }
        if (orgAddress.getText().toString().trim().length() == 0) {
            failFlag = true;
            orgAddress.setError("A value is required");
        }
        if (orgCountry.getText().toString().trim().length() == 0) {
            failFlag = true;
            orgCountry.setError("A value is required");
        }

        Log.d(TAG, "State :" + state);
        Log.d(TAG, "State length:" + state.trim().length());
        if (state == null || state.trim().length() == 0 || !Arrays.asList(getResources().getStringArray(R.array.State)).contains(state)) {
            failFlag = true;
            orgState.setError("A value is required");
        }
        if (orgCity.getText().toString().trim().length() == 0) {
            failFlag = true;
            orgCity.setError("A value is required");
        }
        if (orgZip.getText().toString().trim().length() == 0) {
            failFlag = true;
            orgZip.setError("A value is required");
        }

        return !failFlag;
    }

    void newOrg(){
        try{

            if(validateData()){

                final JSONObject org = new JSONObject();
                org.put("name", orgName.getText().toString().trim());
                org.put("tag", orgTag.getText().toString().trim());
                if (orgBranch.getText().toString().trim().length() == 0) {
                    org.put("branch", "");
                } else {
                    org.put("branch", orgBranch.getText().toString().trim());
                }
                if (department.getText().toString().trim().length() == 0) {
                    org.put("department", "");
                } else {
                    org.put("department", department.getText().toString().trim());
                }

                org.put("address", orgAddress.getText().toString().trim());
                org.put("country", orgCountry.getText().toString().trim());
                org.put("state", orgState.getText().toString().trim());
                org.put("city", orgCity.getText().toString().trim());
                org.put("zip", orgZip.getText().toString().trim());
                org.put("id", current_org.getString("id"));

                if(bitmap != null){

                    UploadTask uploadTask;
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setMessage("Loading");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.show();
                    final Context c = this.getApplicationContext();


                    StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage));
                    StorageReference mountainsRef = storageRef.child(orgName.getText().toString().trim());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    uploadTask = mountainsRef.putBytes(data);


                    // Listen for state changes, errors, and completion of the upload.
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            mProgressDialog.setProgress((int) progress);
                            System.out.println("Upload is " + progress + "% done");
                        }
                    }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("Upload is paused");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            mProgressDialog.dismiss();
                            Toast.makeText(c, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Handle successful uploads on complete
                            mProgressDialog.dismiss();
                            Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                            Log.d(TAG, "downloadUrl :" + downloadUrl);
                            try {
                                org.put("org_pic", downloadUrl);
                                new PostClass(EditOrgActivity.this).execute(org);

                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(c, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
                else {
                    if(current_org.has("org_pic")){
                        org.put("org_pic", current_org.getString("org_pic"));
                    }

                    new PostClass(EditOrgActivity.this).execute(org);
                }

            }

        }
        catch (JSONException e){
            e.printStackTrace();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                Log.d(TAG, "selectedImageUri: " + selectedImage.toString());
                CircularImageView imageView = (CircularImageView) findViewById(R.id.EditProfilePic);
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                imageView.setImageBitmap(bitmap);
            }
        } catch (java.io.FileNotFoundException | java.lang.NullPointerException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(EditOrgActivity.this, SettingActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    private class PostClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        PostClass(Context c) {
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

                final JSONObject newOrg = params[0];

                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.http))
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.org))
                        .appendPath(newOrg.getString("id"))
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());

                prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
                String auth = prefs.getString("uid", "null");
                if (auth.equals("null")) {
                    onPostExecute();
                    //@TODO add alert
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", auth);
                connection.setDoOutput(true);

                Log.d(TAG, "params:" + params.toString());



                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(newOrg.toString());
                dStream.flush();
                dStream.close();

                int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'POST' request to URL : :" + url);
                Log.d(TAG, "Post parameters : " + newOrg.toString());
                Log.d(TAG, "Header parameter : " + auth);
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

                                    String orgs_string = prefs.getString("orgs", "null");
                                    Log.d(TAG, "orgs_string :" + orgs_string);

                                    JSONArray orgs;

                                    if (orgs_string.equals("null")) {
                                        orgs = new JSONArray();
                                    }
                                    else {
                                        orgs = new JSONArray(orgs_string);
                                    }
                                    Log.d(TAG, "orgs :" + orgs.toString());
                                    JSONArray newOrgs = new JSONArray();

                                    for (int i = 0; i < orgs.length(); i++) {

                                        JSONObject org = orgs.getJSONObject(i);

                                        if (org.has("id") && org.getString("id").equals(newOrg.getString("id"))) {
                                            Log.d(TAG, "Old Org :" + org.toString());
                                            newOrgs.put(newOrg);
                                            Log.d(TAG, "New Org :" + newOrg.toString());
                                        }
                                        else {
                                            newOrgs.put(org);
                                        }
                                    }
                                    editor = prefs.edit();
                                    editor.putString("orgs", newOrgs.toString());
                                    editor.apply();

                                    new AlertDialog.Builder(context)
                                            .setTitle("Success")
                                            .setMessage("Organization  " + newOrg.getString("name") + " updated successfully")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(EditOrgActivity.this, EditOrgActivity.class);
                                                    finish();
                                                    startActivity(intent);
                                                }
                                            })
                                            .setIcon(R.drawable.ic_done_black_24dp)
                                            .show();

                                    break;
                                }
                                default: {
                                    throw new org.json.JSONException("409");
                                }
                            }

                        } catch (org.json.JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            onPostExecute();
                        }

                    }
                });

            } catch (final IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
