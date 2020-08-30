package com.priyankavidhate.extensions.Contact;

import android.util.Log;

/**
 * Created by priyankavidhate on 6/25/17.
 */

public class ContactListData {

    private static final String TAG = "ContactListData";
    private String name, number, profile_pic;

    public ContactListData(String data, String data1, String profile_pic) {
        Log.d(TAG, data  + " " + data1 );

        this.name = data;
        this.number = data1;
        this.profile_pic = profile_pic;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

}
