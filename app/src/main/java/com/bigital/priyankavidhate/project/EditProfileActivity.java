package com.bigital.priyankavidhate.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.priyankavidhate.Data.Storage;
import com.priyankavidhate.webservices.PostTokenId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditProfileActivity extends AppCompatActivity
        implements View.OnClickListener {

    private ProgressDialog progress;
    private static final String TAG = "EditProfileActivity";
    private static final int SELECT_PICTURE = 1;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private UploadTask uploadTask;
    private Bitmap bitmap = null;
    private ProgressDialog mProgressDialog;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private EditText name;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.setTitle("Sign up");
        findViewById(R.id.EditProfilePic).setOnClickListener(this);
        findViewById(R.id.btn_submit).setOnClickListener(this);
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        storage = new Storage(this);
        String profile_pic = prefs.getString("profile_pic", "default");
        if (!profile_pic.equals("default")) {
            final Utils util = new Utils();
            final CircularImageView imageView = (CircularImageView) findViewById(R.id.EditProfilePic);
            imageView.setImageBitmap((util.StringToBitMap(profile_pic)));

        }
        name = (EditText) findViewById(R.id.EditTextFName);
        name.setText(prefs.getString("user_name", "default"));
    }

    boolean isProfileChanged() {

        String oldName = prefs.getString("user_name", "default");
        return oldName.equals(name.getText().toString());

    }

    boolean validateData() {

        if (name.getText().toString().trim().equals("")) {
            name.setError("Name is required!");
            return false;
        }
        return true;

    }

    @Override
    public void onClick(View v) {

        try {

            final Intent intent;

            int i = v.getId();
            if (i == R.id.EditProfilePic) {
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_PICTURE);
            } else if (i == R.id.btn_submit) {

                Log.d(TAG, bitmap.toString());

                if (!isProfileChanged() || bitmap == null) {

                    final Context c = this.getBaseContext();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(c, "No changes has been made to this profile", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                if (!validateData()) {
                    return;
                }

                String fullName = name.getText().toString().trim();
                final JSONObject account = new JSONObject();
                account.put("name", fullName);

                editor = prefs.edit();
                editor.putString("user_name", fullName);
                editor.apply();


                if (bitmap != null) {

                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setMessage("Loading");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.show();
                    final Context c = this.getApplicationContext();

                    StorageReference storageRef = firebaseStorage.getReferenceFromUrl(getString(R.string.firebase_storage));
                    StorageReference mountainsRef = storageRef.child(fullName);
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
                            Toast.makeText(EditProfileActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Handle successful uploads on complete
                            mProgressDialog.dismiss();
                            Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                            Log.d(TAG, "downloadUrl :" + downloadUrl);
                            try {
                                account.put("profile_pic", downloadUrl);
                                handleToken();
                                new PutClass(EditProfileActivity.this).execute(account);
                            } catch (org.json.JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(c, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    account.put("profile_pic", "default");
                    handleToken();
                    new PutClass(this).execute(account);
                }
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Opss Something went wrong please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleToken() {
        try {
            String token = prefs.getString(getString(R.string.refresh_token), "null");
            if (!token.equals("null")) {
                new PostTokenId(this, storage)
                        .execute().get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
                progress = new ProgressDialog(this);
                progress.setMessage("Loading");
                progress.show();
                Uri selectedImage = data.getData();
                Log.d(TAG, "selectedImageUri: " + selectedImage.toString());
                CircularImageView imageView = (CircularImageView) findViewById(R.id.EditProfilePic);
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                progress.dismiss();
                Log.d(TAG, bitmap.toString());
                imageView.setImageBitmap(bitmap);
            }
        } catch (java.io.FileNotFoundException | java.lang.NullPointerException e) {
            Log.e(TAG, e.toString());
        }
    }

    private class PutClass extends AsyncTask<JSONObject, Void, Void> {

        private Context context;

        PutClass(Context c) {
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

                String auth = prefs.getString("uid", "null");

                Log.d(TAG, "auth " + auth);
                if (auth.equals("null")) {
                    onPostExecute();
                    //@TODO add alert
                }

                Uri uri = new Uri.Builder()
                        .scheme(getString(R.string.http))
                        .encodedAuthority(getString(R.string.server_ur_templ))
                        .path(getString(R.string.update_account))
                        .appendPath(auth)
                        .build();

                URL url = new URL(uri.toString());
                Log.d(TAG, "url:" + url.toString());


                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", auth);

                Log.d(TAG, "params:" + params.toString());

                final JSONObject account = params[0];
                account.put("id", auth);

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(account.toString());
                dStream.flush();
                dStream.close();

                final int responseCode = connection.getResponseCode();

                Log.d(TAG, "Sending 'PUT' request to URL : :" + url);
                Log.d(TAG, "PUT parameters : " + account.toString());
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

                final JSONObject res = new JSONObject(sb.toString());

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();

                        try {

                            switch (response) {

                                case 200: {

                                    editor = prefs.edit();
                                    editor.putString("profile_pic", BitMapToString(bitmap));
                                    editor.apply();

                                    Intent intent = new Intent(EditProfileActivity.this, InboxActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                }
                                default: {
                                    throw new org.json.JSONException("" + responseCode + ", Body: " + res.toString());
                                }
                            }

                        } catch (org.json.JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            onPostExecute();
                        }
                    }
                });

            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        onPostExecute();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
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
