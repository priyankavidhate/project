package com.rohitvyavahare.webservices;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bigital.rohitvyavahare.project.R;
import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.webservices.REST.Call;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rohitvyavahare on 11/11/17.
 */

public class PostPairOrgAction extends AsyncTask<Bundle, Void, Bundle> {
    private static final String TAG = "PostPairOrgAction";

    private Storage storage;
    private Context c;

    public PostPairOrgAction(Context c, Storage storage) {
        this.c = c;
        this.storage = storage;
    }

    @Override
    protected Bundle doInBackground(Bundle... params) {
        Bundle output = new Bundle();
        try {
            Bundle input = params[0];

            final JSONObject body = new JSONObject(input.getString("body"));

            Log.d(TAG, "In background job");


            Uri uri = new Uri.Builder()
                    .scheme(c.getString(R.string.http))
                    .encodedAuthority(c.getString(R.string.server_ur_templ))
                    .path(c.getString(R.string.perform_action_on_org))
                    .appendPath(body.getString("id"))
                    .build();

            output = new Call("POST", uri, storage.getUid(), body.toString(), c).Run();
            switch (output.getInt("response")) {
                case 200: {
                    output.putString("exception", "no_exception");

                    int position = Integer.parseInt(body.getString("position"));
                    JSONObject defaultOrg = storage.getDefaultOrg();

                    JSONArray pairOrgRequests =  new JSONArray(storage.getIncomingPairOrgRequest(defaultOrg.getString("tag")));

                    List<String> list = new ArrayList<>();
                    for(int i = 0; i < pairOrgRequests.length(); i++){
                        list.add(pairOrgRequests.getJSONObject(i).getString("name"));
                    }
                    list.remove(position);

                    if(list.size() > 0){
                        pairOrgRequests = new JSONArray(Arrays.asList(list));
                    }
                    else {
                        pairOrgRequests = new JSONArray();
                    }

                    if(body.getString("action") != null && body.getString("action").equals("accept")
                            && defaultOrg.has("tag")){
                        JSONArray pairedOrgs = storage.getPairedOrgs(defaultOrg.getString("tag"));
                        JSONObject newOrg = body.getJSONObject("first_org");
                        int i = 0;
                        Log.d(TAG, "Number of Paired Orgs before:" + pairedOrgs.length());
                        if(pairedOrgs == null){
                            Log.d(TAG, "Creating new array for paired org");
                            pairedOrgs = new JSONArray();
                        } else {
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
                    }
                    storage.setIncomingPairOrgRequest(defaultOrg.getString("tag"), pairOrgRequests.toString());

                    Log.d(TAG, "Final Paired Orgs : " + storage.getPairedOrgs(defaultOrg.getString("tag")));
                    return output;
                }
                case 409: {
                    output.putString("exception", "Request already exist");
                    return output;
                }
                default: {
                    if (output.getString("exception") != null ) {
                        return output;
                    }
                    output.putString("exception", output.getString("output"));
                    return output;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            output.putString("exception", e.getMessage());
            return output;
        }
    }
}
