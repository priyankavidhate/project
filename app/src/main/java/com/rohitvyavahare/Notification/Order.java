package com.rohitvyavahare.Notification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.rohitvyavahare.Data.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rohitvyavahare on 8/23/17.
 */

public class Order {

    private static final String TAG = "Order";
    private Context c;
    private Storage storage;
    private static final String _nullStr = "null";

    public Order(Context c, Storage storage) {
        this.c = c;
        this.storage = storage;
    }

    public Bundle addToInbox(JSONObject input) {
        Bundle output = new Bundle();

        try {

            String _type = input.getString("type");

            JSONArray orgs = storage.getAssociatedOrgs();
            Log.d(TAG, "orgs :" + orgs.toString());
            Log.d(TAG, "Type :" + input.getString("type"));

            if (orgs == null || orgs.length() == 0) {
                Log.d(TAG, "orgs_string null");
                output.putString("exception", " Associated Orgs null");
                return output;
            }

            JSONObject associatedOrg = new JSONObject(input.getString("first_org"));
            JSONObject pairedOrg = new JSONObject(input.getString("second_org"));
            JSONObject newOrder = new JSONObject(input.getString("value"));

            Log.d(TAG, "First Org :" + associatedOrg.toString());
            Log.d(TAG, "Second Org :" + pairedOrg.toString());

            boolean isOrgPresent = false;

            Log.d(TAG, "Inbox");

            for (int i = 0; i < orgs.length(); i++) {

                JSONObject org = orgs.getJSONObject(i);

                if (org.has("id") && associatedOrg.has("id") && org.getString("id").equals(associatedOrg.getString("id"))) {
                    isOrgPresent = true;
                    Log.d(TAG, "Org is present");
                    break;
                }
            }

            if (pairedOrg.has("id")) {
                int no = storage.getNumberOfNotifications(pairedOrg.getString("id") + _type);
                no++;
                Log.d(TAG, "Org notification :" + pairedOrg.getString("id"));
                Log.d(TAG, "Putting value :" + no);
                Log.d(TAG, "Type : inbox");
                storage.setNumberOfNotifications(pairedOrg.getString("id") + _type, no);
            }

            if (!isOrgPresent) {
                Log.d(TAG, "Org is not present");
                output.putString("exception", "Org is not present");
                return output;
            }

            JSONArray orders = storage.getOrdersFrom(pairedOrg.getString("tag"));

            if (orders != null) {
                Log.d(TAG, "Stored orders " + orders.toString());
            }

            int i = 0;
            if (orders == null || orders.length() == 0) {
                orders = new JSONArray();
            } else {
                for (; i < orders.length(); i++) {

                    try {
                        JSONObject orderInConsideration = orders.getJSONObject(i);
                        if (orderInConsideration.has("id") && newOrder.has("id") &&
                                newOrder.getString("id").equals(orderInConsideration.getString("id"))) {
                            Log.d(TAG, "Found Order, updating exisiting order");
                            orders.put(i, newOrder);
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, "Before total number of orders from org " + pairedOrg.getString("tag") + " :" + orders.length());

            if (i >= orders.length()) {

                JSONArray newOrdersArr = new JSONArray();
                Log.d(TAG, "Adding new order at 0 : " + newOrder.getString("order_id"));
                newOrdersArr.put(0, newOrder);
                for (int j = 0; j < orders.length(); j++) {
                    if (j > 100) {
                        break;
                    }
                    int tempPosition = j + 1;
                    Log.d(TAG, "Adding old order " + orders.getJSONObject(j).getString("order_id") + " at position " + tempPosition);
                    newOrdersArr.put(j + 1, orders.getJSONObject(j));
                }

                orders = newOrdersArr;
            }

            Log.d(TAG, "After total number of orders from org " + pairedOrg.getString("tag") + " :" + orders.length());

            storage.setOrdersFrom(pairedOrg.getString("tag"), orders);

            output.putString("exception", "no_exception");
            output.putString("order_details", input.getString("value"));
            output.putString("orders", orders.toString());
            output.putString("type", _type);
            output.putString("org_name", pairedOrg.getString("name"));
            output.putString("org_tag", pairedOrg.getString("tag"));
            output.putString("position", Integer.toString(orders.length() - 1));

            return output;

        } catch (Exception e) {
            e.printStackTrace();
            output.putString("exception", e.getMessage());
            return output;
        }
    }

    public Bundle addToOutbox(JSONObject input) {

        Bundle output = new Bundle();
        try {
            String _type = input.getString("type");

            JSONArray orgs = storage.getAssociatedOrgs();

            if (orgs != null) {
                Log.d(TAG, "orgs :" + orgs.toString());
            }
            Log.d(TAG, "Type :" + input.getString("type"));

            if (orgs == null || orgs.length() == 0) {
                Log.d(TAG, "orgs_string null");
                output.putString("exception", " Associated Orgs null");
                return output;
            }

            JSONObject pairedOrg = new JSONObject(input.getString("first_org"));
            JSONObject associatedOrg = new JSONObject(input.getString("second_org"));
            JSONObject newOrder = new JSONObject(input.getString("value"));

            boolean org_present = false;

            for (int i = 0; i < orgs.length(); i++) {

                JSONObject org = orgs.getJSONObject(i);

                if (org.has("id") && associatedOrg.has("id") && org.getString("id").equals(associatedOrg.getString("id"))) {
                    org_present = true;
                    Log.d(TAG, "Org is present");
                    break;
                }
            }

            if (!org_present) {
                Log.d(TAG, "Org is not present");
                output.putString("exception", "Org is not present");
                return output;
            }

            if (pairedOrg.has("id")) {

                int no = storage.getNumberOfNotifications(pairedOrg.getString("id") + _type);
                no++;
                Log.d(TAG, "Org notification :" + pairedOrg.getString("id"));
                Log.d(TAG, "Putting value :" + no);
                Log.d(TAG, "Type : inbox");
                storage.setNumberOfNotifications(pairedOrg.getString("id") + _type, no);
            }


            JSONArray orders = storage.getOrdersTo(pairedOrg.getString("tag"));
            int i = 0;
            if (orders == null || orders.length() == 0) {
                orders = new JSONArray();
            } else {
                for (; i < orders.length(); i++) {

                    try {
                        JSONObject order = orders.getJSONObject(i);
                        if (order.has("id") && newOrder.has("id") && newOrder.getString("id").equals(order.getString("id"))) {
                            Log.d(TAG, "Order Found :" + newOrder.toString());
                            orders.put(i, newOrder);
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            //TODO check all order , and same id present then update current

            Log.d(TAG, "Before total number of orders from org " + pairedOrg.getString("tag") + " :" + orders.length());

            if (i >= orders.length()) {

                JSONArray newArr = new JSONArray();
                Log.d(TAG, "Adding new order :" + newOrder.toString());
                newArr.put(0, newOrder);
                Log.d(TAG, "Adding new order at 0 :" + newOrder.getString("order_id"));
                for (int j = 0; j < orders.length(); j++) {
                    if (j > 100) {
                        break;
                    }
                    int tempPosition = j + 1;
                    Log.d(TAG, "Adding old order " + orders.getJSONObject(j).getString("order_id") + " at position " + tempPosition);
                    newArr.put(j + 1, orders.getJSONObject(j));
                }

                orders = newArr;
            }

            Log.d(TAG, "After total number of orders from org " + pairedOrg.getString("tag") + " :" + orders.length());

            storage.setOrdersTo(pairedOrg.getString("tag"), orders);

            output.putString("exception", "no_exception");
            output.putString("order_details", input.getString("value"));
            output.putString("orders", orders.toString());
            output.putString("type", _type);
            output.putString("org_name", pairedOrg.getString("name"));
            output.putString("org_tag", pairedOrg.getString("tag"));
            output.putString("position", Integer.toString(orders.length() - 1));

            return output;

        } catch (Exception e) {
            e.printStackTrace();
            output.putString("exception", e.getMessage());
            return output;
        }
    }
}
