package com.example.rohitvyavahare.project;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by rohitvyavahare on 1/16/17.
 */

public class FirebaseToken extends FirebaseInstanceIdService {

    private static final String TAG = "FirebaseToken";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        String auth = prefs.getString("uid", "null");

        Log.d(TAG, "auth " + auth);
        editor = prefs.edit();
        if (!auth.equals("null")) {
            editor.putString("refreshToken", refreshedToken);
            editor.putString("first_token", "false");
        }
        else {

            editor.putString("refreshToken", refreshedToken);
            editor.putString("first_token", "true");
            editor.commit();

        }

    }
}
