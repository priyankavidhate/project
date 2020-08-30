package com.priyankavidhate.extensions.Inbox;

/**
 * Created by priyankavidhate on 7/28/17.
 */

public class ListData {
    public String getProfile_pic() {
        return profile_pic;
    }

    public int getData1() {
        return data1;
    }

    public int getData2() {
        return data2;
    }

    private String data, data3, profile_pic;
    private int data1, data2;

    public ListData(String data, String data3, String profile_pic, int data1, int data2) {

        this.data = data;
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
        this.profile_pic = profile_pic;
    }

    public String getData() {
        return data;
    }
    public String getTag() { return data3; }
}
