package com.priyankavidhate.Notification;

import android.content.Context;
import android.util.Log;

import com.bigital.priyankavidhate.project.Utils;
import com.priyankavidhate.Data.Storage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by priyankavidhate on 8/16/17.
 */

public class PairedOrg {

    private static final String TAG = "PairedOrg";
    private Context c;
    private Storage storage;
    private static final String _nullStr = "null";

    public PairedOrg(Context c, Storage storage) {
        this.c = c;
        this.storage = storage;
    }

    public void sendRequest(JSONObject input) {
        try {

            if (!input.has("org_name") || !input.has("value")) {
                return;
            }

            JSONArray arr;

            String pairOrgRequest = storage.getOutgoingPairOrgRequest(input.getString("org_tag"));

            Log.d(TAG, "pair_org_request :" + pairOrgRequest);

            JSONObject object = new JSONObject(input.getString("value"));

            if (pairOrgRequest.equals(_nullStr)) {
                arr = new JSONArray();

            } else {
                arr = new JSONArray(pairOrgRequest);
            }
            arr.put(object);
            storage.setOutgoingPairOrgRequest(input.getString("org_tag"), arr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void accept() {

    }

    public void reject() {

    }

    public void getRequest(JSONObject input) {
        try {

            if (!input.has("org_name") || !input.has("value")) {
                return;
            }

            JSONArray arr;

            String pairOrgRequest = storage.getIncomingPairOrgRequest(input.getString("org_tag"));
            Log.d(TAG, "pair org request :" + pairOrgRequest);

            JSONObject object = new JSONObject(input.getString("value"));

            if (pairOrgRequest.equals(_nullStr)) {
                arr = new JSONArray();

            } else {
                arr = new JSONArray(pairOrgRequest);
            }

            arr.put(object);
            storage.setIncomingPairOrgRequest(input.getString("org_tag"), arr.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(JSONObject input) {
        try {

            String pairOrgRequest = storage.getIncomingPairOrgRequest(input.getString("org_tag"));
            JSONObject incomingOrg = new JSONObject(input.getString("value"));

            if (pairOrgRequest.equals(_nullStr)) {
                return;
            }
            JSONArray arr = new JSONArray(pairOrgRequest);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject pairOrgInConsideration = arr.getJSONObject(i);

                if (!pairOrgInConsideration.has("id") || !incomingOrg.has("id") ||
                        !pairOrgInConsideration.getString("id").equals(incomingOrg.getString("id"))) {
                    continue;
                }

                List<JSONObject> list = new ArrayList<>();
                for (int j = 0; j < arr.length(); j++) {
                    list.add(arr.getJSONObject(j));
                }
                list.remove(i);

                if (list.size() > 0) {
                    arr = new JSONArray(Arrays.asList(list));
                } else {
                    arr = new JSONArray();
                }

                storage.setIncomingPairOrgRequest(input.getString("org_tag"), arr.toString());
                break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(JSONObject input) {
        try {
            Utils util = new Utils();
            JSONArray pairedOrgs = storage.getPairedOrgs(input.getString("org_tag"));

            if (pairedOrgs == null) {
                Log.d(TAG, "No paired orgs present");
                return;
            }

            JSONArray newPairedOrgs = new JSONArray();
            JSONObject pairedOrgInConsideration = new JSONObject(input.getString("value"));
            for (int i = 0; i < pairedOrgs.length(); i++) {
                JSONObject org = pairedOrgs.getJSONObject(i);
                if (pairedOrgInConsideration.getString("tag").equals(org.getString("tag"))) {
                    Log.d(TAG, "Updating org :" + org.getString("tag"));
                    newPairedOrgs.put(pairedOrgInConsideration);
                    if (pairedOrgInConsideration.has("org_pic") && !pairedOrgInConsideration.getString("org_pic").equals("default")) {
                        util.getBitmapFromURL(pairedOrgInConsideration.getString("org_pic"), pairedOrgInConsideration.getString("tag"), c);
                    }
                } else {
                    newPairedOrgs.put(org);
                }
            }

            Log.d(TAG, "Previous number of orgs :" + pairedOrgs.length());
            Log.d(TAG, "New number of orgs :" + newPairedOrgs.length());
            Log.d(TAG, newPairedOrgs.toString());

            storage.setPairedOrgs(input.getString("org_tag"), newPairedOrgs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

