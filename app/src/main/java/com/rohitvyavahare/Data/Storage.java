package com.rohitvyavahare.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rohitvyavahare.project.R;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rohitvyavahare on 7/30/17.
 */

public class Storage {

    private static final String TAG = "Storage";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private JSONArray associatedOrgs;

    private Context context;

    private String _defaultOrg;
    private String _outbox;
    private String _inbox;
    private String _associatedOrgs;
    private String _pairedOrgs;
    private String _userName;
    private String _profilePic;
    private String _hardResetInbox;
    private String _hardResetOutbox;
    private String _hardResetPairedOrgs;
    private String _numberOfNotifications;
    private String _refreshToken;
    private String _firstToken;
    private String _lastActive;
    private String _orgItems;
    private String  _lastpairedOrgsTime;
    private String _incomingPairOrgRequest;
    private String _outgoingPairOrgRequest;

    private static final String _nullArr = "[{null}]";
    private static final String _nulObj = "{null}";
    private static final String _orgTag = "tag";
    private static final String _uid = "uid";
    private static final String _nullStr = "null";
    private static final Integer _defaultInt = 0;
    private static final Long _defaultLong = 0L;

    public Storage(Context c) {
        this.context = c;
        this.prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
        this.editor = prefs.edit();
        _outbox = c.getString(R.string.outbox);
        _inbox = c.getString(R.string.inbox);
        _associatedOrgs = c.getString(R.string.associated_orgs);
        _pairedOrgs = c.getString(R.string.paired_orgs);
        _hardResetInbox = c.getString(R.string.hard_reload_inbox);
        _hardResetOutbox = c.getString(R.string.hard_reload_outbox);
        _hardResetPairedOrgs = c.getString(R.string.hard_reload_pair_org);
        _userName = c.getString(R.string.user_name);
        _profilePic = c.getString(R.string.user_profile_pic);
        _numberOfNotifications = c.getString(R.string.number_of_notification);
        _refreshToken = c.getString(R.string.refresh_token);
        _firstToken = c.getString(R.string.first_token);
        _lastActive = c.getString(R.string.last_active);
        _defaultOrg = c.getString(R.string.default_org);
        _orgItems = c.getString(R.string.org_itmes);
        _lastpairedOrgsTime = c.getString(R.string.last_time_paired_orgs);
        _incomingPairOrgRequest = c.getString(R.string.incoming_request);
        _outgoingPairOrgRequest = c.getString(R.string.outgoing_request);
    }

    public String getUid() {
        return prefs.getString(_uid, _nullStr);
    }

    public void setUid(String uid) {
        editor.putString(_uid, uid);
        editor.apply();
    }

    public JSONArray getAssociatedOrgs() throws Exception{
        if(this.associatedOrgs != null) {
            return associatedOrgs;
        }
        String temp = prefs.getString(_associatedOrgs, _nullArr);
        if(temp.equals(_nullArr)) {
            return null;
        }
        this.associatedOrgs = new JSONArray(temp);
        Log.d(TAG, "Associated orgs :" + this.associatedOrgs.toString());
        return this.associatedOrgs;
    }

    public void setAssociatedOrg(JSONArray associatedOrg) throws Exception {
        Log.d(TAG, "Setting Associate Org :" + associatedOrg.toString());
        this.associatedOrgs = associatedOrg;
        this.editor.putString(_associatedOrgs, this.associatedOrgs.toString());
        editor.apply();
    }

    public JSONArray getOrdersTo(String orgTag) throws Exception {
        String temp = prefs.getString(orgTag + _outbox, _nullArr);
        if(temp.equals(_nullArr)) {
            return null;
        }
        JSONArray ordersTo = new JSONArray(temp);
        Log.d(TAG, "ordersTo for org tag " + orgTag + " is :" + ordersTo.toString());
        return ordersTo;
    }

    public void setOrdersTo(String orgTag, JSONArray orderArr) throws Exception {
        this.editor.putString(orgTag + _outbox, orderArr.toString());
        editor.apply();
        Log.d(TAG, "Setting ordersTo for org tag " + orgTag + " is :" + orderArr.toString());
    }

    public JSONArray getOrdersFrom(String orgTag) throws Exception {
        String temp = prefs.getString(orgTag + _inbox, _nullArr);
        if(temp.equals(_nullArr)) {
            return null;
        }
        JSONArray ordersFrom = new JSONArray(temp);
        Log.d(TAG, "ordersFrom for org tag " + orgTag +  " is :"+ ordersFrom.toString());
        return ordersFrom;
    }

    public void setOrdersFrom(String orgTag, JSONArray orderArr) throws Exception {
        this.editor.putString(orgTag + _inbox, orderArr.toString());
        editor.apply();
        Log.d(TAG, "Setting ordersFrom for org tag " + orgTag + " is :" + orderArr.toString());
    }

    public JSONArray getPairedOrgs(String orgTag) throws Exception {
        String temp = prefs.getString(orgTag + _pairedOrgs, _nullArr);
        if(temp.equals(_nullArr)){
            Log.d(TAG, "Returning null for paired orgs");
            return null;
        }
        JSONArray tempArr = new JSONArray((temp));
        Log.d(TAG, "Paired orgs for org tag :" + orgTag + " are :"+ tempArr.length());
        return tempArr;
    }

    public void setPairedOrgs(String orgTag, JSONObject pairedOrg) {
        try {
            JSONArray pairedOrgs = this.getPairedOrgs(orgTag);
            if(pairedOrgs == null){
                pairedOrgs = new JSONArray();
            }
            pairedOrgs.put(pairedOrg);
            editor.putString(orgTag + this.context.getString(R.string.paired_orgs), pairedOrgs.toString());
            editor.apply();
            Log.d(TAG, "Setting paired org for org tag " + orgTag + " is :" + pairedOrg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPairedOrgs(String orgTag, JSONArray pairedOrgs) {
        try {
            editor.putString(orgTag + this.context.getString(R.string.paired_orgs), pairedOrgs.toString());
            editor.apply();
            Log.d(TAG, "Setting paired orgs for org tag " + orgTag + " is :" + pairedOrgs.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO add updatePairedOrg

    public JSONObject getDefaultOrg() {
        try {
            String temp = prefs.getString(this._defaultOrg, _nullStr);
            if(temp.equals(_nullStr)) {
                return null;
            }
            return new JSONObject(temp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setDefaultOrg(String _defaultOrg) {
        Log.d(TAG, "Setting defaultOrg :" +  _defaultOrg);
        editor.putString(this._defaultOrg, _defaultOrg);
        editor.apply();
    }

    public String getHardResetInbox() {
        String temp = prefs.getString(this._hardResetInbox, _nullStr);
        Log.d(TAG, "Getting HardResetInbox :" + temp);
        return temp;
    }

    public void setHardResetInbox(String _hardResetInbox) {
        editor.putString(this._hardResetInbox, _hardResetInbox);
        editor.apply();
    }

    public String getHardResetOutbox() {
        return prefs.getString(this._hardResetOutbox, _nullStr);
    }

    public void setHardResetOutbox(String _hardResetOutbox) {
        editor.putString(this._hardResetOutbox, _hardResetOutbox);
        editor.apply();
    }

    public String getHardResetPairedOrgs() {
        return prefs.getString(this._hardResetPairedOrgs, _nullStr);
    }

    public void setHardResetPairedOrgs(String _hardResetPairedOrgs) {
        editor.putString(this._hardResetPairedOrgs, _hardResetPairedOrgs);
        editor.apply();
    }

    public String getUserName() {
        return prefs.getString(this._userName, _nullStr);
    }

    public void setUserName(String _userName) {
        editor.putString(this._userName, _userName);
        editor.apply();
    }

    public String getProfilePic() {
        return prefs.getString(this._profilePic, _nullStr);
    }

    public void setProfilePic(String _profilePic) {
        editor.putString(this._profilePic, _profilePic);
        editor.apply();
    }

    public Integer getNumberOfNotifications(String orgId) {
        return prefs.getInt(orgId + this._numberOfNotifications, _defaultInt);
    }

    public void setNumberOfNotifications(String orgId,  Integer _numberOfNotifications) {
        editor.putInt(orgId + this._numberOfNotifications, _numberOfNotifications);
        editor.apply();
    }

    public String getRefreshToken() {
        return prefs.getString(this._refreshToken, _nullStr);
    }

    public void setRefreshToken(String _refreshToken) {
        editor.putString(this._refreshToken, _refreshToken);
        editor.apply();
    }

    public String getFirstToken() {
        return prefs.getString(this._firstToken, _nullStr);
    }

    public void setFirstToken(String _firstToken) {
        editor.putString(this._firstToken, _firstToken);
        editor.apply();
    }

    public String getLastActive() {
        String temp = prefs.getString(this._lastActive, "1");
        Log.d(TAG, "Getting Last active :" + temp);
        return temp;
    }

    public void setLastActive(String _lastActive) {
        Log.d(TAG, "Setting Last active :" + _lastActive);
        editor.putString(this._lastActive, _lastActive);
        editor.apply();
    }

    public JSONArray getOrgItmes(String orgTag) {
        try {
            String temp = prefs.getString(orgTag + this._orgItems, _nullArr);
            if(temp.equals(_nullArr)) {
                return null;
            }
            return new JSONArray(temp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setOrgItems(String orgTag, String _orgItmes) {
        editor.putString(orgTag + this._orgItems, _orgItmes);
        editor.apply();
    }

    public Long getLastPairedOrgsTime(String orgTag) {
        return prefs.getLong(orgTag + this._lastpairedOrgsTime, _defaultLong);
    }

    public void setLastPairedOrgsTime(String orgTag) {
        Long current_time = System.currentTimeMillis();
        editor.putLong(orgTag + this._lastpairedOrgsTime, current_time);
        editor.apply();
    }

    public String getIncomingPairOrgRequest(String orgTag) {
        return prefs.getString(orgTag + this._incomingPairOrgRequest, _nullStr);
    }

    public void setIncomingPairOrgRequest(String orgTag, String _incomingPairOrgRequest) {
        editor.putString(orgTag + this._incomingPairOrgRequest, _incomingPairOrgRequest);
        editor.apply();
    }

    public String getOutgoingPairOrgRequest(String orgTag) {
        return prefs.getString(orgTag + this._outgoingPairOrgRequest, _nullStr);
    }

    public void setOutgoingPairOrgRequest(String orgTag, String _outgoingPairOrgRequest) {
        editor.putString(orgTag + this._outgoingPairOrgRequest, _outgoingPairOrgRequest);
        editor.apply();
    }
}
