package com.example.rohitvyavahare.project;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rohitvyavahare on 1/21/17.
 */

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    HashMap<String, Integer> notify = new HashMap<String, Integer>() {{
        put("incoming_pair_org_request", 0);
        put("outgoing_pair_org_request", 1);
        put("pair_org_update", 2);
        put("order_detail_update", 3);
        put("order_creation", 4);
        put("add_employee", 5);
        put("paired_orgs", 6);
    }};

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Data: " + remoteMessage.getData());
//        Log.d(TAG, "Notification Message Title: " + remoteMessage.getNotification().getTitle());
//        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        //Calling method to generate notification
        sendNotification(remoteMessage);
    }


    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(RemoteMessage notification) {

        Intent intent = new Intent(this, InboxActivity.class);
        SharedPreferences prefs;
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);

        try {

            JSONObject obj = new JSONObject(notification.getData());
            SharedPreferences.Editor editor = prefs.edit();

            switch (notify.get(obj.getString("switch"))) {

                case 0: {

                    Log.d(TAG, " switch case 0 ");

                    if (obj.has("org_name") && obj.has("value")) {

                        intent = new Intent(this, PairOrgActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        JSONArray arr;

                        String pair_org_request = prefs.getString(obj.getString("org_name") + R.string.incoming_request, "null");

                        // @TODO find logic for pair org pic;
                        Log.d(TAG, "pair_org_request :" + pair_org_request);

                        JSONObject object = new JSONObject(obj.getString("value"));;

                        if (pair_org_request.equals("null")) {
                            arr = new JSONArray();
                            arr.put(object);
                            editor.putString(obj.getString("org_name") + R.string.incoming_request, arr.toString());

                        } else {
                            arr = new JSONArray(pair_org_request);
                            arr.put(object);
                            editor.putString(obj.getString("org_name") + R.string.incoming_request, arr.toString());
                        }

                        editor.commit();
                    }

                    break;

                }
                case 1: {

                    Log.d(TAG, " switch case 1 ");

                    if (obj.has("org_name") && obj.has("value")) {

                        intent = new Intent(this, PairOrgActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        JSONArray arr;

                        String pair_org_request = prefs.getString(obj.getString("org_name") + R.string.outgoing_request, "null");

                        // @TODO find logic for pair org pic;
                        Log.d(TAG, "pair_org_request :" + pair_org_request);

                        JSONObject object = new JSONObject(obj.getString("value"));

                        if (pair_org_request.equals("null")) {
                            arr = new JSONArray();
                            arr.put(object);
                            editor.putString(obj.getString("org_name") + R.string.outgoing_request, arr.toString());

                        } else {
                            arr = new JSONArray(pair_org_request);
                            arr.put(object);
                            editor.putString(obj.getString("org_name") + R.string.outgoing_request, arr.toString());
                        }

                        editor.commit();
                    }

                    break;

                }
                case 2: {

                    String pair_org_request = prefs.getString(obj.getString("org_name") + R.string.incoming_request, "null");
                    JSONObject incoming_org = new JSONObject(obj.getString("value"));

                    if(!pair_org_request.equals("null")){
                        JSONArray arr = new JSONArray(pair_org_request);

                        for(int i=0; i<arr.length(); i++){
                            JSONObject temp_org = arr.getJSONObject(i);

                            if(temp_org.has("id") && incoming_org.has("id") && temp_org.getString("id").equals(incoming_org.getString("id"))){

                                List<JSONObject> list = new ArrayList<>();
                                for(int j= 0; j < arr.length(); j++){
                                    list.add(arr.getJSONObject(j));
                                }
                                list.remove(i);

                                if(list.size() > 0){
                                    arr = new JSONArray(Arrays.asList(list));
                                }
                                else {
                                    arr = new JSONArray();
                                }

                                editor.putString(obj.getString("org_name") + R.string.incoming_request, arr.toString());
                                break;
                            }
                        }

                        editor.commit();

                    }

                    break;

                }
                case 3:
                case 4: {

                    String type = "null";
                    JSONArray orders = new JSONArray();
                    int position = 0;

                    if(!obj.has("first_org") && !obj.has("second_org")) {
                        break;
                    }

                    JSONObject first_org = new JSONObject(obj.getString("first_org"));
                    JSONObject second_org = new JSONObject(obj.getString("second_org"));

                    Log.d(TAG, "First Org :" + first_org.toString());
                    Log.d(TAG, "Second Org :" + second_org.toString());

                    if(!first_org.has("id") &&!first_org.has("name") && !second_org.has("id") && !second_org.has("name")){
                        break;
                    }

                    String orgs_string = prefs.getString("orgs", "null");

                    Log.d(TAG, "orgs_string :"+ orgs_string);

                    if(orgs_string.equals("null")){
                        Log.d(TAG, "orgs_string null");
                        break;
                    }

                    JSONArray orgs = new JSONArray(orgs_string);

                    Log.d(TAG, "orgs :"+ orgs.toString());

                    Log.d(TAG, "Type :" + obj.getString("type"));

                    if(obj.has("type") && obj.getString("type").equals("inbox") && obj.has("value")){

                        boolean org_present = false;

                        Log.d(TAG, "Inbox");

                        for(int i=0; i<orgs.length(); i++){

                            JSONObject org = orgs.getJSONObject(i);

                            if(org.has("id") && first_org.has("id") && org.getString("id").equals(first_org.getString("id"))){
                                org_present = true;
                                Log.d(TAG, "Org is present");
                                break;
                            }
                        }

                        if(!org_present){
                            Log.d(TAG, "Org is not present");
                            break;
                        }

                        String inbox_orgs = prefs.getString(first_org.getString("id"), "null");
                        type = "inbox";

                        if(inbox_orgs.equals("null")){
                            break;
                        }

                        String[] all_orgs = inbox_orgs.split(",");
                        String inbox_org = "null";
                        for(String org : all_orgs){
                            if(org.equals(second_org.getString("name"))){
                                inbox_org = org;
                                Log.d(TAG, "inbox org: " + inbox_org);
                            }
                        }

                        if(inbox_org.equals("null")){
                            Log.d(TAG, "inbox org is not present");
                            break;
                        }

                        String inbox_orders = prefs.getString(second_org.getString("name"), "null");

                        Log.d(TAG, inbox_org + " orders :" + inbox_orders);

                        JSONObject object = new JSONObject(obj.getString("value"));
                        int i=0;
                        if(inbox_orders.equals("null")){
                            orders = new JSONArray();
                        }
                        else {
                            orders = new JSONArray(inbox_orders);
                            for(; i<orders.length(); i++){

                                try{
                                    JSONObject order = orders.getJSONObject(i);
                                    if(order.has("id") && object.has("id") && object.getString("id").equals(order.getString("id"))){
                                        Log.d(TAG, "Found Order, updating exisiting order");
                                        orders.put(i, object);
                                        break;
                                    }
                                }
                                catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }

                        }
                        //TODO check all order , and same id presnet then update current
                        Log.d(TAG, "Before total number of orders from org " + inbox_org + " :" + orders.length());

                        if(i >= orders.length()){

                            JSONArray newArr = new JSONArray();
                            newArr.put(object);
                            for(int j=0; j<orders.length(); j++){
                                if(j > 100){
                                    break;
                                }
                                newArr.put(j+1, orders.getJSONObject(j));
                            }

                            orders = newArr;
                        }

                        Log.d(TAG, "After total number of orders from org " + inbox_org + " :" + orders.length());

                        editor.putString(second_org.getString("name") , orders.toString());
                        editor.commit();

                        position = orders.length() - 1;


                    }
                    else if(obj.has("type") && obj.getString("type").equals("outbox") && obj.has("value")){

                        boolean org_present = false;

                        for(int i=0; i<orgs.length(); i++){

                            JSONObject org = orgs.getJSONObject(i);

                            if(org.has("id") && second_org.has("id") && org.getString("id").equals(second_org.getString("id"))){
                                org_present = true;
                                Log.d(TAG, "Org is present");
                                break;
                            }
                        }

                        if(!org_present){
                            Log.d(TAG, "Org is not present");
                            break;
                        }

                        type = "outbox";
                        String outbox_orgs = prefs.getString(first_org.getString("id") + R.string.outbox, "null");

                        if(outbox_orgs.equals("null")){

                            outbox_orgs = first_org.getString("name");
                            editor.putString(outbox_orgs + R.string.outbox, outbox_orgs);
                        }

                        String[] all_orgs = outbox_orgs.split(",");
                        String outbox_org = "null";
                        for(String org : all_orgs){
                            if(org.equals(first_org.getString("name"))){
                                outbox_org = org;
                                Log.d(TAG, "outbox org: " + outbox_org);

                            }
                        }

                        if(outbox_org.equals("null")){

                            Log.d(TAG, "outbox org not present, creating new one");

                            outbox_orgs = outbox_orgs + "," + first_org.getString("name");
                            editor.putString(outbox_orgs + R.string.outbox, outbox_orgs);

                        }

                        String outbox_orders = prefs.getString(first_org.getString("name") + R.string.outbox, "null");

                        JSONObject object = new JSONObject(obj.getString("value"));

                        int i=0;
                        if(outbox_orders.equals("null")){
                            orders = new JSONArray();
                        }
                        else {
                            orders = new JSONArray(outbox_orders);
                            for(; i<orders.length(); i++){

                                try{
                                    JSONObject order = orders.getJSONObject(i);
                                    if(order.has("id") && object.has("id") && object.getString("id").equals(order.getString("id"))){
                                        orders.put(i, object);
                                        break;
                                    }
                                }
                                catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        //TODO check all order , and same id presnet then update current

                        Log.d(TAG, "Before total number of orders from org " + outbox_org + " :" + orders.length());

                        if(i >= orders.length()){

                            JSONArray newArr = new JSONArray();
                            newArr.put(object);
                            for(int j=0; j<orders.length(); j++){
                                if(j > 100){
                                    break;
                                }
                                newArr.put(j+1, orders.getJSONObject(j));
                            }

                            orders = newArr;
                        }


                        Log.d(TAG, "After total number of orders from org " + outbox_org + " :" + orders.length());

                        editor.putString(first_org.getString("name") + R.string.outbox , orders.toString());
                        editor.commit();

                        position = orders.length() - 1;

                    }

                    if(!type.equals("null")){

                        intent = new Intent(this, OrderDetailsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        String default_org = prefs.getString("default_org", null);
                        JSONObject dorg = null;
                        if(default_org == null) {
                            break;
                        }
                        dorg = new JSONObject(prefs.getString("default_org", "{type : null}"));

                        if(dorg.has("type") && dorg.getString("type").equals("null")){
                            break;

                        }

                        Bundle orderData = new Bundle();

                        if(type.equals("inbox") && first_org.has("name") && dorg.has("name") && !first_org.getString("name").equals(dorg.getString("name"))){
                            String msg = getString(R.string.differnet_org_notification_msg);
                            msg += " " + first_org.getString("name") +  " from settings";
                            orderData.putString("message", msg);
                            intent = new Intent(this, InboxActivity.class);
                            intent.putExtras(orderData);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            break;

                        }
                        else if(type.equals("outbox") && second_org.has("name") && dorg.has("name") && !second_org.getString("name").equals(dorg.getString("name"))){
                            String msg = getString(R.string.differnet_org_notification_msg);
                            msg += " " + second_org.getString("name") +  " from settings";
                            orderData.putString("message", msg);
                            intent = new Intent(this, InboxActivity.class);
                            intent.putExtras(orderData);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            break;

                        }
                        orderData.putString("order_details", obj.getString("value"));
                        orderData.putString("orders", orders.toString());
                        orderData.putString("type", type);
                        if(type.equals("inbox")){
                            Log.d(TAG, "As type is inbox adding first_org to org_name");
                            orderData.putString("org_name", first_org.getString("name"));
                        }
                        else {
                            Log.d(TAG, "As type is outbox adding second_org to org_name");
                            orderData.putString("org_name", second_org.getString("name"));
                        }

                        orderData.putString("position", Integer.toString(position));
                        intent.putExtras(orderData);
                        obj.put("org_name", first_org.getString("name"));
                    }
                    break;

                }

                case 5: {

                    // @TODO change intent to settings
                    intent = new Intent(this, InboxActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    String orgs = prefs.getString("orgs", "null");
                    JSONObject incoming_org = new JSONObject(obj.getString("value"));

                    if(!orgs.equals("null")){

                        JSONArray org_docs = new JSONArray(orgs);
                        int i=0;
                        for(; i< org_docs.length(); i++){

                            JSONObject temp_org = org_docs.getJSONObject(i);

                            if(temp_org.has("id") &&  incoming_org.has("id") && temp_org.getString("id").equals(incoming_org.getString("id"))){
                                Log.d(TAG, "Org already exist");
                                break;
                            }
                        }

                        if(i >= org_docs.length()){
                            org_docs.put(incoming_org);
                            editor.putString("orgs" , org_docs.toString());
                            editor.commit();
                        }

                    }

                    break;

                }

                case 6: {

                    Log.d(TAG, "paired orgs");

                    intent = new Intent(this, InboxActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    String paired_orgs = prefs.getString(obj.getString("org_name") + R.string.paired_orgs, "null");
                    JSONArray arr ;
                    if(paired_orgs.equals("null")){
                        Log.d(TAG, "Creating new array for paired org");
                        arr = new JSONArray();
                    }
                    else
                        Log.d(TAG, "Existing array for paired org");{
                        arr = new JSONArray(paired_orgs);

                    }

                    Log.d(TAG, "Adding paired org  :" + obj.getString("value"));
                    JSONObject object = new JSONObject(obj.getString("value"));
                    arr.put(object);
                    editor.putString(obj.getString("org_name") + R.string.paired_orgs, arr.toString());
                    editor.commit();
                }

                default: {

//                    Log.d(TAG, "Unknown body :" + notification.getNotification().getBody());
                    break;

                }


            }


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            String body = "";
            if(obj.has("notification_body")) {
                JSONObject bodyJson = new JSONObject(obj.getString("notification_body"));
                if(bodyJson.has("body")){
                    body = bodyJson.getString("body");
                }
            }

            NotificationCompat.Builder notificationBuilder;

            if(body.length() > 50){

                notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Project")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(body))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

            }
            else {

                notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Project")
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(body))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

            }


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}