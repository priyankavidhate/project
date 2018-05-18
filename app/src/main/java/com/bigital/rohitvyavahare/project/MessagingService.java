package com.bigital.rohitvyavahare.project;

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
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.Notification.Order;
import com.rohitvyavahare.Notification.PairedOrg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

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
        put("org_update", 7);
        put("pair_org_update", 8);
        put("org_items_update", 9);
    }};

    private Storage storage;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Data: " + remoteMessage.getData());
        storage = new Storage(this);

        //Calling method to generate notification
        sendNotification(remoteMessage);
    }


    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(RemoteMessage notification) {

        Intent intent = new Intent(this, InboxActivity.class);
        SharedPreferences prefs;
        prefs = getSharedPreferences(getString(R.string.private_file), MODE_PRIVATE);
        boolean showNotification = true;
        Utils util = new Utils();

        try {

            JSONObject obj = new JSONObject(notification.getData());
            SharedPreferences.Editor editor = prefs.edit();
            JSONObject dorg = new JSONObject(prefs.getString("default_org", "{type : null}"));
            JSONObject defaultOrg = storage.getDefaultOrg();

            Log.d(TAG, "Notification Data :" + obj.toString(4));

            switch (notify.get(obj.getString("switch"))) {

                case 0: {

                    Log.d(TAG, " switch case 0 ");

                    PairedOrg pOrg = new PairedOrg(this, storage);
                    pOrg.getRequest(obj);

                    //TODO find out need of this line
                    if (defaultOrg.has("type") && defaultOrg.getString("type").equals("null")) {
                        break;
                    }

                    if (obj.has("org_tag") && dorg.has("tag") && !obj.getString("org_tag").equals(dorg.getString("tag"))) {
                        String msg = getString(R.string.differnet_org_notification_msg);
                        msg += " " + obj.getString("org_name") + " from settings";
                        Bundle data = new Bundle();
                        data.putString("message", msg);
                        intent = new Intent(this, InboxActivity.class);
                        intent.putExtras(data);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }

                    break;

                }
                case 1: {

                    Log.d(TAG, " switch case 1 ");

                    intent = new Intent(this, PairOrgActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    PairedOrg pOrg = new PairedOrg(this, storage);
                    pOrg.sendRequest(obj);

                    if (defaultOrg.has("type") && defaultOrg.getString("type").equals("null")) {
                        break;

                    }

                    if (obj.has("org_tag") && defaultOrg.has("tag") && !obj.getString("org_tag").equals(defaultOrg.getString("tag"))) {
                        String msg = getString(R.string.differnet_org_notification_msg);
                        msg += " " + obj.getString("org_name") + " from settings";
                        Bundle data = new Bundle();
                        data.putString("message", msg);
                        intent = new Intent(this, InboxActivity.class);
                        intent.putExtras(data);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }

                    break;

                }
                case 2: {

                    PairedOrg pOrg = new PairedOrg(this, storage);
                    pOrg.remove(obj);

                    if (dorg.has("type") && dorg.getString("type").equals("null")) {
                        break;

                    }

                    if (obj.has("org_tag") && dorg.has("tag") && !obj.getString("org_tag").equals(dorg.getString("tag"))) {
                        String msg = getString(R.string.differnet_org_notification_msg);
                        msg += " " + obj.getString("org_name") + " from settings";
                        Bundle data = new Bundle();
                        data.putString("message", msg);
                        intent = new Intent(this, InboxActivity.class);
                        intent.putExtras(data);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }

                    break;

                }
                case 3:
                case 4: {

                    String type = "null";
                    JSONArray orders = new JSONArray();
                    Bundle orderData = new Bundle();

                    if (!obj.has("first_org") && !obj.has("second_org")) {
                        break;
                    }

                    JSONObject first_org = new JSONObject(obj.getString("first_org"));
                    JSONObject second_org = new JSONObject(obj.getString("second_org"));

                    Log.d(TAG, "First Org :" + first_org.toString());
                    Log.d(TAG, "Second Org :" + second_org.toString());

                    if (!first_org.has("id") && !first_org.has("name") && !second_org.has("id") && !second_org.has("name")) {
                        break;
                    }

                    if (!obj.has("value")) {
                        break;
                    }

                    JSONObject object = new JSONObject(obj.getString("value"));
                    if (object.has("id")) {
                        int no = prefs.getInt(object.getString("id") + obj.getString("type") + getString(R.string.number_of_notification), 0);
                        no++;
                        editor.putInt(object.getString("id") + obj.getString("type") + getString(R.string.number_of_notification), no);
                    }

//                    String orgs_string = prefs.getString("orgs", "null");
//                    Log.d(TAG, "orgs_string :" + orgs_string);
//
//                    if (orgs_string.equals("null")) {
//                        Log.d(TAG, "orgs_string null");
//                        break;
//                    }
//
//                    JSONArray orgs = new JSONArray(orgs_string);
//                    Log.d(TAG, "orgs :" + orgs.toString());
                    Log.d(TAG, "Type :" + obj.getString("type"));
                    Order order = new Order(this, storage);

                    if (obj.has("type") && obj.getString("type").equals("inbox") && obj.has("value")) {

                        type = "inbox";
                        orderData = order.addToInbox(obj);
                        if(!orderData.equals("no_exception")) {
                            break;
                        }


                    } else if (obj.has("type") && obj.getString("type").equals("outbox") && obj.has("value")) {
                        type = "outbox";
                        orderData = order.addToOutbox(obj);
                        if(!orderData.equals("no_exception")) {
                            break;
                        }
                    }

                    if (!type.equals("null")) {

                        intent = new Intent(this, OrderDetailsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        String default_org = prefs.getString("default_org", null);
                        if (default_org == null) {
                            break;
                        }
                        dorg = new JSONObject(prefs.getString("default_org", "{type : null}"));

                        if (dorg.has("type") && dorg.getString("type").equals("null")) {
                            break;

                        }

                        if (type.equals("inbox") && first_org.has("tag") && dorg.has("tag") && !first_org.getString("tag").equals(dorg.getString("tag"))) {
                            String msg = getString(R.string.differnet_org_notification_msg);
                            msg += " " + first_org.getString("name") + " from settings";
                            orderData.putString("message", msg);
                            intent = new Intent(this, InboxActivity.class);
                            intent.putExtras(orderData);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Log.d(TAG, "Adding warning message");
                            break;

                        } else if (type.equals("outbox") && second_org.has("tag") && dorg.has("tag") && !second_org.getString("tag").equals(dorg.getString("tag"))) {
                            String msg = getString(R.string.differnet_org_notification_msg);
                            msg += " " + second_org.getString("name") + " from settings";
                            orderData.putString("message", msg);
                            intent = new Intent(this, InboxActivity.class);
                            intent.putExtras(orderData);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Log.d(TAG, "Adding warning message");
                            break;
                        }

                        intent.putExtras(orderData);
                        obj.put("org_name", first_org.getString("name"));
                    }
                    break;

                }

                case 5: {

                    intent = new Intent(this, SettingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    String orgs = prefs.getString("orgs", "null");
                    JSONObject incoming_org = new JSONObject(obj.getString("value"));

                    if (!orgs.equals("null")) {

                        JSONArray org_docs = new JSONArray(orgs);
                        int i = 0;
                        for (; i < org_docs.length(); i++) {

                            JSONObject temp_org = org_docs.getJSONObject(i);

                            if (temp_org.has("id") && incoming_org.has("id") && temp_org.getString("id").equals(incoming_org.getString("id"))) {
                                Log.d(TAG, "Org already exist");
                                break;
                            }
                        }

                        if (i >= org_docs.length()) {
                            org_docs.put(incoming_org);
                            editor.putString("orgs", org_docs.toString());
                            editor.commit();
                        }

                    }
                    break;
                }

                case 6: {

                    try {
                        Log.d(TAG, "paired orgs");

                        intent = new Intent(this, InboxActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        JSONObject newOrg = new JSONObject(obj.getString("value"));
                        JSONArray pairedOrgs = storage.getPairedOrgs(defaultOrg.getString("tag"));
                        int i = 0;
                        if(pairedOrgs == null){
                            Log.d(TAG, "Creating new array for paired org");
                            pairedOrgs = new JSONArray();
                            Log.d(TAG, "Number of Paired Orgs before:" + pairedOrgs.length());
                        } else {
                            Log.d(TAG, "Number of Paired Orgs before:" + pairedOrgs.length());
                            Log.d(TAG, "Existing array for paired org");
                            for (; i < pairedOrgs.length(); i++) {
                                JSONObject currentPairOrgInConsideration = pairedOrgs.getJSONObject(i);

                                if (!(newOrg.has("id") && currentPairOrgInConsideration.has("id") &&
                                        newOrg.getString("id").equals(currentPairOrgInConsideration.getString("id")))) {
                                    continue;
                                }

                                break;
                            }
                        }

                        if ( i == pairedOrgs.length() )
                            pairedOrgs.put(newOrg);
                        Log.d(TAG, "Number of Paired Orgs after:" + pairedOrgs.length());
                        Log.d(TAG, "Newly Added Paird org :" + newOrg.toString(4));
                        storage.setPairedOrgs(defaultOrg.getString("tag"), pairedOrgs);

                        if (dorg.has("type") && dorg.getString("type").equals("null")) {
                            break;

                        }

                        if (obj.has("org_tag") && dorg.has("tag") && !obj.getString("org_tag").equals(dorg.getString("tag"))) {
                            String msg = getString(R.string.differnet_org_notification_msg);
                            msg += " " + obj.getString("org_name") + " from settings";
                            Bundle data = new Bundle();
                            data.putString("message", msg);
                            intent = new Intent(this, PairOrgActivity.class);
                            intent.putExtras(data);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

                case 7: {
                    String orgs_string = prefs.getString("orgs", "null");
                    showNotification = false;
                    if (orgs_string.equals("null")) {
                        Log.d(TAG, "No orgs present");
                        break;
                    }

                    JSONArray orgs = new JSONArray(orgs_string);
                    JSONArray newOrgs = new JSONArray();
                    for (int i = 0; i < orgs.length(); i++) {
                        JSONObject org = orgs.getJSONObject(i);
                        if (obj.getString("org_tag").equals(org.getString("tag"))) {
                            Log.d(TAG, "Updating org :" + org.getString("tag"));
                            JSONObject newOrg = new JSONObject(obj.getString("value"));
                            newOrgs.put(newOrg);
                            if (newOrg.has("org_pic") && !newOrg.getString("org_pic").equals("default")) {
                                util.getBitmapFromURL(newOrg.getString("org_pic"), newOrg.getString("tag"), MessagingService.this);
                            }


                        } else {
                            newOrgs.put(org);
                        }
                    }

                    Log.d(TAG, "Case 7 Previous number of orgs :" + orgs.length());
                    Log.d(TAG, "Case 7 New number of orgs :" + newOrgs.length());

                    editor.putString("orgs", newOrgs.toString());
                    editor.commit();

                }

                case 8: {
                    showNotification = false;
                    PairedOrg pOrg = new PairedOrg(this, storage);
                    pOrg.update(obj);
                    break;
                }
                case 9: {
                    break;
                }

                default: {
                    showNotification = false;
                    break;
                }
            }


            if (showNotification) {

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                String body = "";
                if (obj.has("notification_body")) {
                    JSONObject bodyJson = new JSONObject(obj.getString("notification_body"));
                    if (bodyJson.has("body")) {
                        body = bodyJson.getString("body");
                    }
                }

                NotificationCompat.Builder notificationBuilder;

                if (body.length() > 50) {

                    notificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(body))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

                } else {

                    notificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(body)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(body))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
                }


                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                int UniqueIntegerNumber = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                Log.d(TAG, "Notifying with Number :" + UniqueIntegerNumber);
                notificationManager.notify(UniqueIntegerNumber, notificationBuilder.build());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}