package com.bigital.priyankavidhate.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.priyankavidhate.Data.Storage;
import com.priyankavidhate.webservices.REST.Call;

import org.json.JSONObject;

/**
 * Created by priyankavidhate on 1/16/17.
 */

public class FirebaseToken extends FirebaseInstanceIdService {

    private static final String TAG = "FirebaseToken";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    public void onTokenRefresh() {

        try {
            //Getting registration token
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();

            prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);

            //Displaying token on logcat
            Log.d(TAG, "Refreshed token: " + refreshedToken);

            String auth = prefs.getString("uid", "null");
            Storage storage = new Storage(this);

            Log.d(TAG, "auth " + auth);
            editor = prefs.edit();
            if ( auth == null || auth.equals("null")) {
                editor.putString("refreshToken", refreshedToken);
                Log.d(TAG, "Setting first token to true");
                storage.setFirstToken("true");
            }
            else {
                editor.putString("refreshToken", refreshedToken);
                handleToken(refreshedToken, storage, this, auth);
            }
            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleToken(String token, Storage storage, Context c, String id) {
        try {

            Uri uri = new Uri.Builder()
                    .scheme(getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.firebase_token))
                    .appendPath(storage.getUid())
                    .build();

            JSONObject obj = new JSONObject();
            obj.put("registration_id", token);
            obj.put("_id", id);

            Bundle output = new Call("PUT", uri, storage.getUid(), obj.toString(), this).Run();
            output.putString("exception", "no_exception");
            switch (output.getInt("response")) {
                case 201: {
                    storage.setFirstToken("false");
                    break;
                }
                default: {
                    storage.setFirstToken("true");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            storage.setFirstToken("false");
        }

    }

    public String getToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    public Boolean verifyToken(String token) {
        return token.equals(FirebaseInstanceId.getInstance().getToken());
    }
}
