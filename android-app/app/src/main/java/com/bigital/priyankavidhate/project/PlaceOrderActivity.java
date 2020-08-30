package com.bigital.priyankavidhate.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.priyankavidhate.Data.Storage;
import com.priyankavidhate.extensions.Adapter.Data;
import com.priyankavidhate.extensions.Adapter.DataAdapter;
import com.priyankavidhate.extensions.Spinner.NothingSelectedSpinnerAdapter;
import com.priyankavidhate.webservices.GetOrgItems;
import com.priyankavidhate.webservices.PostOrder;
import com.priyankavidhate.webservices.UpdateOrder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bigital.priyankavidhate.project.R.id.addMore;

public class PlaceOrderActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "PlaceOrderActivity";
    private HashMap<String, Integer> nameToid;
    private HashMap<String, Integer> tagToid;
    final int[] result = new int[1];
    private int count = 1;
    private Storage storage;
    JSONObject defaultOrg;
    JSONArray pairedOrgs;
    private JSONObject selectedPairOrg;
    private String[] itemList;
    private ArrayList<String> pairedOrgsNames;
    private Map<Integer, String> dropDownMap = new HashMap<>();
    private Bundle bundle;
    private Map<String, Integer> itemToPosition = new HashMap<>();
    TypedArray t2;
    TypedArray t;
    boolean editOrder = false;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order_v2);
        setTitle("Place an order");
        storage = new Storage(this);

        ViewSwitcher switcher;
        try {

            Toolbar toolbar;
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            } catch (java.lang.NullPointerException e) {
                e.printStackTrace();
            }

            t2 = getResources().obtainTypedArray(R.array.quantityIds);
            t = getResources().obtainTypedArray(R.array.itemIds);
            pd = new ProgressDialog(PlaceOrderActivity.this);
            pd.setMessage("Loading...");

            // get bundle and check if it is edit order

            defaultOrg = storage.getDefaultOrg();

            if (defaultOrg == null) {
                showMessageOnUi("Default Organization is not set");
                return;
            }

            pairedOrgs = storage.getPairedOrgs(defaultOrg.getString("tag"));

            Log.d(TAG, "default org found: " + defaultOrg.getString("name"));
            Log.d(TAG, "Paired orgs :" + pairedOrgs.length());

            if (pairedOrgs.length() <= 0) {
                showMessageOnUi("No Paired organization found");
                return;
            }
            switcher = (ViewSwitcher) findViewById(R.id.my_switcher_3);
            switcher.showNext();

            pairedOrgsNames = new ArrayList<>();
            nameToid = new HashMap<>();
            tagToid = new HashMap<>();
            for (int i = 0; i < pairedOrgs.length(); i++) {

                JSONObject obj = pairedOrgs.getJSONObject(i);

                if (obj.has("name") && obj.has("tag")) {
                    pairedOrgsNames.add(obj.getString("name"));
                    nameToid.put(obj.getString("name"), i);
                    tagToid.put(obj.getString("tag"), i);
                }
            }

            for (String key : nameToid.keySet()) {
                Log.d(TAG, "nameToid key : " + key + " value :" + nameToid.get(key));
            }

            Set<String> hs = new HashSet<>();
            hs.addAll(pairedOrgsNames);
            pairedOrgsNames.clear();
            pairedOrgsNames.addAll(hs);

            bundle = getIntent().getExtras();

            if (bundle != null && bundle.containsKey("orderBundle")) {
                setTitle("Edit an order");
                editOrder = true;
                handleEditOrder();
                return;
            }


            final CharSequence orgNames[] = pairedOrgsNames.toArray(new String[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Intent intent = new Intent(PlaceOrderActivity.this, InboxActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setTitle("Pick an organization");
            builder.setItems(orgNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result[0] = which;
                    createTable("", "");
                }
            });
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private void addOrgName() {

        CharSequence orgNames[] = pairedOrgsNames.toArray(new String[0]);
        final TextView orgName = (TextView) findViewById(R.id.OrgName);
        String selected_org = orgNames[result[0]].toString();
        orgName.setText("Order to Organization : " + selected_org);
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner(final Spinner spinner, final int position) {
        try {
            TextView orgName = (TextView) findViewById(R.id.OrgName);
            if(selectedPairOrg == null) {
                selectedPairOrg = pairedOrgs.getJSONObject(nameToid.get(orgName.getText().toString().split("Order to Organization : ")[1]));
            }

            final List<String> list = new ArrayList<String>();
            final List<Data> dList = new ArrayList<>();
            Log.d(TAG, "Item list length :" + itemList.length);
            int i = 0;
            for (String item : itemList) {
                Log.d(TAG, "Adding Item :" + item);
                list.add(item);
                String [] temp = item.split(" ");
                String [] out = new String[2] ;

                if(temp.length > 0) {
                    out[0] = temp[0];
                }

                if(temp.length >= 2) {
                    out[1] = temp[1];
                }

                if(temp.length >=3) {
                    out[1] += " " + temp[2];
                }
                dList.add(new Data(out[0], out[1]));
                itemToPosition.put(item, i);
                i++;
            }
            spinner.setAdapter(
                    new NothingSelectedSpinnerAdapter(
                            new DataAdapter(this, dList),
                            R.layout.contact_spinner_row_nothing_selected,
                            // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                            this));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.d(TAG, "onItemSelected Pos :" + pos);
                    if (pos > 0) {
                        Log.d(TAG, "Adding to dropDownMap key " + position + " and value " + list.get(pos - 1));
                        dropDownMap.put(position, list.get(pos - 1));
                    }

                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            Log.d(TAG, "Something clicked");

            int i = v.getId();
            if (i == R.id.PlaceOrder) {

                if (!validateShipment()) {
                    Log.d(TAG, "Failed to validate shipment");
                    return;
                }
                if(editOrder) {
                    updateOrder();
                } else {
                    createOrder();
                }
                t2.recycle();
                t.recycle();
            } else if (i == addMore) {
                addItem(t, t2);
            } else if (i == R.id.removeItem) {
                removeItem(t, t2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }

    }

    private void createOrder() {
        try {
            TextView orgName = (TextView) findViewById(R.id.OrgName);
            EditText editMsg = (EditText) findViewById(R.id.editMessage);


            String accountId = storage.getUid();

            JSONObject orderDetails = new JSONObject();
            JSONObject orderObj = new JSONObject();
            JSONArray orderArr;

            if (itemList == null || itemList.length == 0) {
                orderArr = createOrderWithoutOrgItems();
            } else {
                orderArr = createOrderWithOrgItems();
            }

            orderDetails.put("status", "created");
            orderDetails.put("shipment", orderArr);

            Log.d(TAG, "Total paired orgs :" + pairedOrgs.length() + " , org chosen :" + result[0]);
            Log.d(TAG, "Selected org index :" + nameToid.get(orgName.getText().toString().split("Order to Organization : ")[1]));

            selectedPairOrg = pairedOrgs.getJSONObject(nameToid.get(orgName.getText().toString().split("Order to Organization : ")[1]));

            if (!(defaultOrg.has("id") || !defaultOrg.has("_id")) && !(selectedPairOrg.has("id") || !selectedPairOrg.has("_id"))) {
                Log.d(TAG, "dorg or org has not id");
                return;
            }
            String to = selectedPairOrg.has("id") ? selectedPairOrg.getString("id") : selectedPairOrg.getString("_id");
            orderDetails.put("to", to);
            String from = defaultOrg.has("id") ? defaultOrg.getString("id") : defaultOrg.getString("_id");
            orderDetails.put("from", from);

            if (editMsg.getText().toString().trim().length() > 0 && !accountId.equals("null")) {
                JSONArray messages = new JSONArray();
                JSONObject msg = new JSONObject();
                msg.put("id", from);
                msg.put("text", editMsg.getText().toString().trim());
                msg.put("account_id", accountId);
                messages.put(msg);
                orderDetails.put("messages", messages);
            }

            Log.d(TAG, "Paired Orgs :" + pairedOrgs.toString());

            Log.d(TAG, "Placing an order to :" + selectedPairOrg.toString());
            orderObj.put("to", selectedPairOrg);
            orderObj.put("from", defaultOrg);
            orderObj.put("order", orderDetails);
            Bundle input = new Bundle();
            input.putString("tag", selectedPairOrg.getString("tag"));
            input.putString("body", orderObj.toString());
            handlePostOrder(input);
            //TODO clear data

        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private JSONArray createOrderWithOrgItems() throws Exception {
        JSONArray orderArr = new JSONArray();
        JSONObject order;

        EditText editQty;
        int j = 0;
        for (Map.Entry<Integer, String> entry : dropDownMap.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            order = new JSONObject();

            JSONObject orderValues = getValuesFromDropDownItem(value);

            if (orderValues.has("item")) {
                order.put("item", orderValues.getString("item"));
            } else {
                continue;
            }

            if (orderValues.has("brand")) {
                order.put("brand", orderValues.getString("brand"));
            } else {
                continue;
            }
            if (orderValues.has("hsnCode")) {
                order.put("hsnCode", orderValues.getString("hsnCode"));
            }

            editQty = (EditText) findViewById(t2.getResourceId(j, 0));
            order.put("quantity", editQty.getText().toString().trim());
            orderArr.put(order);
            j++;

        }
        return orderArr;
    }

    @SuppressWarnings("ResourceType")
    private JSONArray createOrderWithoutOrgItems() throws Exception {
        JSONArray orderArr = new JSONArray();
        JSONObject order;

        EditText editItem;
        EditText editQty;

        for (int j = 0; j < count; j++) {
            order = new JSONObject();
            editItem = (EditText) findViewById(t.getResourceId(j, 0));
            editQty = (EditText) findViewById(t2.getResourceId(j, 0));
            order.put("item", editItem.getText().toString().trim());
            order.put("quantity", editQty.getText().toString().trim());
            orderArr.put(order);
        }
        return orderArr;
    }

    private JSONObject getValuesFromDropDownItem(String input) throws Exception {

        Log.d(TAG, "getValuesFromDropDownItem input :" + input);

        JSONObject orderValues = new JSONObject();
        String[] values = input.split(" ");
        if (values.length > 0 && values[0] != null) {
            orderValues.put("item", values[0]);
        }
        if (values.length > 1 && values[1] != null) {
            orderValues.put("brand", values[1]);
        }
        if (values.length > 2 && values[2] != null) {
            orderValues.put("hsnCode", values[2]);
        }
        return orderValues;
    }

    @SuppressWarnings("ResourceType")
    private void handleEditOrder() {
        try {

            Log.d(TAG, "Handle Edit Order");

            for (String key: bundle.keySet())
            {
                Log.d (TAG, key + " is a key in the bundle");
            }

            bundle = (Bundle) bundle.get("orderBundle");

            JSONObject obj = new JSONObject(bundle.get("order_details").toString());
            Log.d(TAG, "Order details :" + obj.toString());

            String type = bundle.getString("type");

            String orgTag = bundle.getString("org_tag");

            Log.d(TAG, "Calling Create Table");

            selectedPairOrg = pairedOrgs.getJSONObject(tagToid.get(orgTag));


            if (type.equals("inbox")) {
                createTable(this.storage.getDefaultOrg().getString("tag"), this.storage.getDefaultOrg().getString("id") );
            } else {
                Log.d(TAG, "To :" + orgTag);
                if (!selectedPairOrg.has("tag")) {
                    return;
                }
                Log.d(TAG, "Selected Pair Org :" + selectedPairOrg.getString("tag"));
                createTable(selectedPairOrg.getString("tag"), selectedPairOrg.getString("id"));
            }


            Log.d(TAG, "Done Create Table");


            final TextView orgName = (TextView) findViewById(R.id.OrgName);
            if (obj.has("order_id")) {
                orgName.setText("Edit Order: " + obj.getString("order_id"));
            } else {
                orgName.setText("Edit Order");
            }

            JSONArray shipment = obj.getJSONArray("shipment");

            String orderType;

            if (itemList == null || itemList.length == 0) {
                orderType = "edittxt";
            } else {
                orderType = "spinner";
            }

            Log.d(TAG, "Order Type: "+ orderType);

            for (int i=0; i<shipment.length(); i++) {

                if( i != 0) {
                    addItem(t,t2);
                }
                StringBuilder itemBrandCode = new StringBuilder();
                JSONObject currentShipment = shipment.getJSONObject(i);
                try {
                    Log.d(TAG, "Shipment in consideration :"+ currentShipment.toString());
                    if (currentShipment.has("item")) {
                        itemBrandCode.append(currentShipment.getString("item"));
                        itemBrandCode.append(" ");
                    }

                    if (currentShipment.has("brand")) {
                        itemBrandCode.append(currentShipment.getString("brand"));
                        itemBrandCode.append(" ");
                    }

                    if (currentShipment.has("hsnCode")) {
                        itemBrandCode.append(currentShipment.getString("hsnCode"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }

                Log.d(TAG, "Looking for :" + itemBrandCode.toString().trim().length());

                if(orderType.equals("spinner")) {
                    Spinner item = (Spinner) findViewById(t.getResourceId(i, 0));

                    item.setSelection(itemToPosition.get(itemBrandCode.toString().trim()) + 1);
                } else {
                    EditText item = (EditText) findViewById(t.getResourceId(i, 0));
                    item.setText(currentShipment.getString("item"));
                }
                EditText quantity = (EditText) findViewById(t2.getResourceId(i, 0));
                if (currentShipment.has("quantity")) {
                    quantity.setVisibility(View.VISIBLE);
                    quantity.setText(currentShipment.getString("quantity"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());

        }
    }

    private boolean validateShipment() {

        String type;
        if (itemList == null || itemList.length == 0) {
            type = "edittxt";
        } else {
            type = "spinner";
        }

        Log.d(TAG, "Type is :" + type);
        EditText editQty;


        if (type.equals("spinner")) {


            for (int i = 0; i < count; i++) {
                Log.d(TAG, "Validating Spinner at position :" + i);
                if (dropDownMap == null || !dropDownMap.containsKey(i)) {
                    Log.d(TAG, "Spinner is empty at position :" + i);
                    showMessageOnUi("Item name is required at position " + "" + (i+1));
                    return false;
                }

                editQty = (EditText) findViewById(t2.getResourceId(i, 0));
                Log.d(TAG, "Validating Edit quantity at position :" + i);
                if (editQty.getText().toString().trim().equals("")) {
                    Log.d(TAG, "Edit Quantity is empty :" + editQty.getText().toString());
                    editQty.setError("Quantity is required");
                    return false;
                }
            }

        } else {
            EditText editItem;

            for (int i = 0; i < count; i++) {

                editItem = (EditText) findViewById(t.getResourceId(i, 0));
                if (editItem.getText().toString().trim().equals("")) {
                    editItem.setError("Item name is required");
                    return false;
                }
                if (!Character.isLetter(editItem.getText().toString().trim().charAt(0))) {
                    editItem.setError("Item should start with letter");
                    return false;
                }

                editQty = (EditText) findViewById(t2.getResourceId(i, 0));
                if (editQty.getText().toString().trim().equals("")) {
                    editQty.setError("Quantity is required");
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("ResourceType")
    private void addItem(TypedArray t, TypedArray t2) {

        if (count >= t.length()) {
            return;
        }
        if (count == 1) {
            TextView txView = (TextView) findViewById(R.id.removeItem);
            txView.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Adding item number :" + count + " and id :" + t.getResourceId(count, 0));
        Log.d(TAG, "Adding quantity number :" + count + " and id :" + t2.getResourceId(count, 0));

        View item = findViewById(t.getResourceId(count, 0));
        item.setVisibility(View.VISIBLE);
        item = findViewById(t2.getResourceId(count, 0));
        item.setVisibility(View.VISIBLE);
        count++;
    }

    private void removeItem(TypedArray t, TypedArray t2) {

        if (count == 0) {
            return;
        }

        Log.d(TAG, "Removing item number :" + (count - 1) + " and id :" + t.getResourceId(count - 1, 0));
        Log.d(TAG, "Removing quantity number :" + (count - 1) + " and id :" + t2.getResourceId(count - 1, 0));


        View item = findViewById(t.getResourceId(count - 1, 0));
        item.setVisibility(View.GONE);
        item = findViewById(t2.getResourceId(count - 1, 0));
        item.setVisibility(View.GONE);
        count--;

        if (count == 0) {
            TextView txView = (TextView) findViewById(R.id.removeItem);
            txView.setVisibility(View.GONE);
        }
//        t.recycle();
//        t2.recycle();
    }

    private void handlePostOrder(final Bundle input) {

        final Storage s = storage;
        final Context c = this;
        pd.show();

        //start a new thread to process job
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    Bundle output = new PostOrder(c, s).execute(input).get();

                    if (!output.getString("exception").equals("no_exception")) {
                        output.putString("first_msg", "Error");
                        output.putString("second_msg", output.getString("exception"));
                    } else {
                        JSONObject newOrder = new JSONObject(output.getString("output"));
                        output.putString("first_msg", "Success");
                        output.putString("second_msg", "Order Id: " + newOrder.getString("order_id"));
                    }
                    msg.setData(output);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    input.putString("first_msg", e.getMessage());
                    input.putString("second_msg", "null");
                    msg.setData(input);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
            if (msg.getData().getString("second_msg").equals("null")) {
                showMessageOnUi(msg.getData().getString("first_msg"));
            } else {
                showAlertBox(msg.getData().getString("first_msg"), msg.getData().getString("second_msg"));
            }
        }
    };

    private void showAlertBox(String titleMsg, String subjectMsg) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(titleMsg);
            builder.setMessage(subjectMsg);
            builder.setCancelable(true);
            builder.setNeutralButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Intent intent = new Intent(PlaceOrderActivity.this, InboxActivity.class);
                            startActivity(intent);
                        }
                    });

            AlertDialog alert11 = builder.create();
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private String[] getItems(String tag, String id) {
        try {

            Log.d(TAG, "Get Items :"+tag+" "+id);

            if (tag.length() == 0 && id.length() == 0) {
                CharSequence orgNames[] = pairedOrgsNames.toArray(new String[0]);
                String selectedOrg = orgNames[result[0]].toString();
                selectedPairOrg = pairedOrgs.getJSONObject(nameToid.get(selectedOrg));
                if (!selectedPairOrg.has("tag")) {
                    return null;
                }
                id = selectedPairOrg.getString("id");
                tag = selectedPairOrg.getString("tag");
            }

            JSONArray itemArr;
            Log.d(TAG, "Getting org Items from backend :" + tag);

            Bundle input = new Bundle();
            input.putString("id", id);
            input.putString("tag", tag);

            Bundle output = new GetOrgItems(this, storage)
                        .execute(input).get();

            if (!output.getString("exception").equals("no_exception")) {
                showMessageOnUi(output.getString("exception"));
                return null;
            }

            itemArr = storage.getOrgItems(tag);

            if (itemArr == null || itemArr.length() == 0) {
                return null;
            }

            Log.d(TAG, "Items Array :"+ itemArr.toString());

            String[] outputArr = new String[itemArr.length()];

            for (int i = 0; i < itemArr.length(); i++) {
                JSONObject item = itemArr.getJSONObject(i);
                StringBuilder itemBuilder = new StringBuilder();
                for (int j = 0; j < item.names().length(); j++) {
                    Log.d(TAG, "Adding item key :" + item.names().getString(j));
                    itemBuilder.append(item.getString(item.names().getString(j)));
                    itemBuilder.append(" ");
                }
                Log.d(TAG, "Building item :" + itemBuilder.toString());

                outputArr[i] = itemBuilder.toString().trim();
            }
            if (outputArr.length == 0) {
            }
            itemList = outputArr;
            return outputArr;
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
            return null;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(PlaceOrderActivity.this, InboxActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    private void showMessageOnUi(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (message.length() > 50) {
                    Toast.makeText(PlaceOrderActivity.this, message.substring(0,50), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlaceOrderActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressWarnings("ResourceType")
    private void createTable(String tag, String id) {

        String type;
        Spinner spinner = null;
        getItems(tag, id);
        Spinner[] spinners = new Spinner[t.length()];

        if (itemList == null || itemList.length == 0) {
            type = "edittxt";
        } else {
            type = "spinner";
        }

        TableLayout tl = (TableLayout) findViewById(R.id.orderFrom);

        TableRow.LayoutParams tlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        tlp.span = 2;
        tlp.setMargins(0, 50, 0, 10);

        TableRow tr1 = new TableRow(this);
        tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TableRow tr2 = new TableRow(this);
        tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TableRow tr3 = new TableRow(this);
        tr3.setLayoutParams(tlp);

        TableRow tr4 = new TableRow(this);
        tr4.setLayoutParams(tlp);

        TableRow tr5 = new TableRow(this);
        tr5.setLayoutParams(tlp);

        TableRow tr6 = new TableRow(this);
        tr6.setLayoutParams(tlp);

        TableRow tr7 = new TableRow(this);
        tr7.setLayoutParams(tlp);

        TextView orgName, addMore, removeItem;

        orgName = new TextView(this);
        orgName.setId(R.id.OrgName);
        orgName.setLayoutParams(tlp);
        orgName.setTextSize(20);

        addMore = new TextView(this);
        addMore.setText("+Add More");
        addMore.setId(R.id.addMore);
        addMore.setTextColor(0xFFFE7E24);
        addMore.setLayoutParams(tlp);
        addMore.setGravity(Gravity.END);
        addMore.setClickable(true);

        removeItem = new TextView(this);
        removeItem.setText("-Remove Last Item");
        removeItem.setId(R.id.removeItem);
        removeItem.setTextColor(0xFFBF4040);
        removeItem.setLayoutParams(tlp);
        removeItem.setGravity(Gravity.END);
        removeItem.setClickable(true);
        removeItem.setVisibility(View.GONE);

        EditText editItem, editQuantity, editMessage;


        if (type.equals("spinner")) {
            spinner = new Spinner(this);
            spinner.setId(t.getResourceId(0, 0));
            spinner.setLayoutParams(tlp);
            spinner.setPrompt("Item, brand and HSN Code (Optional)");
            tr2.addView(spinner);
            spinners[0] = spinner;
        } else {
            editItem = new EditText(this);
            editItem.setId(t.getResourceId(0, 0));
            editItem.setLayoutParams(tlp);
            editItem.setHint(getString(R.string.item));
            editItem.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            tr2.addView(editItem);
        }

        editQuantity = new EditText(this);
        editQuantity.setId(t2.getResourceId(0, 0));
        editQuantity.setLayoutParams(tlp);
        editQuantity.setHint(getString(R.string.quantity));
        editQuantity.setInputType(InputType.TYPE_CLASS_TEXT);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(15);
        editQuantity.setFilters(FilterArray);

        editMessage = new EditText(this);
        editMessage.setId(R.id.editMessage);
        editMessage.setLayoutParams(tlp);
        editMessage.setHint(getString(R.string.message));
        editMessage.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editMessage.setMaxLines(10);
        editMessage.setMinLines(5);
        editMessage.setLines(8);
        editMessage.setGravity(Gravity.CENTER);
        editMessage.setBackgroundResource(R.drawable.back);
        editMessage.setVerticalScrollBarEnabled(true);

        TableRow.LayoutParams tlp2 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        tlp2.span = 2;
        tlp2.setMargins(0, 50, 0, 50);

        Button b = new Button(this);
        b.setId(R.id.PlaceOrder);
        b.setText("Place an Order");
        b.setLayoutParams(tlp2);
        b.setBackgroundColor(0xFF00BFFF);
        b.setGravity(Gravity.CENTER);
        b.setTextColor(0xFFFFFFFF);

        tr1.addView(orgName);
        tr3.addView(editQuantity);

        ArrayList<TableRow> rows = new ArrayList<>();
        for (int i = 1; i < t.length(); i++) {
            Log.d(TAG, "Adding item box:" +i);

            View v;

            if (type.equals("spinner")) {
                v = addSpinner(t.getResourceId(i, 0), tlp);
                spinners[i] = (Spinner) v;
            } else {
                v = addEditText(t.getResourceId(i, 0), tlp);
            }

            Log.d(TAG, "Adding qunatity textbox:" + i);
            EditText editQuantity2 = new EditText(this);
            editQuantity2.setId(t2.getResourceId(i, 0));
            editQuantity2.setLayoutParams(tlp);
            editQuantity2.setHint(getString(R.string.quantity));
            editQuantity2.setInputType(InputType.TYPE_CLASS_TEXT);
            FilterArray[0] = new InputFilter.LengthFilter(15);
            editQuantity2.setFilters(FilterArray);
            editQuantity2.setVisibility(View.GONE);

            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(v);
            rows.add(tr);
            TableRow tr3a = new TableRow(this);
            tr3a.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr3a.addView(editQuantity2);
            rows.add(tr3a);
        }

        tr4.addView(addMore);
        tr5.addView(removeItem);

        tr6.addView(editMessage);
        tr7.addView(b);

        tl.addView(tr1, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        tl.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        tl.addView(tr3, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        for (TableRow row : rows) {
            tl.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        }
        tl.addView(tr4, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        tl.addView(tr5, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        tl.addView(tr6, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
        tl.addView(tr7, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        findViewById(R.id.addMore).setOnClickListener(PlaceOrderActivity.this);
        findViewById(R.id.PlaceOrder).setOnClickListener(PlaceOrderActivity.this);
        findViewById(R.id.removeItem).setOnClickListener(PlaceOrderActivity.this);

        addOrgName();
        if (type.equals("spinner") && spinner != null) {
            int i = 0;
            for (Spinner s : spinners) {
                addItemsOnSpinner(s, i);
                i++;
            }
        }
    }

    private EditText addEditText(int id, TableRow.LayoutParams tlp) {

        EditText editItem = new EditText(this);
        editItem.setId(id);
        editItem.setLayoutParams(tlp);
        editItem.setHint(getString(R.string.item));
        editItem.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editItem.setVisibility(View.GONE);
        return editItem;
    }

    private Spinner addSpinner(int id, TableRow.LayoutParams tlp) {

        Spinner spinner = new Spinner(this);
        spinner.setId(id);
        spinner.setLayoutParams(tlp);
        spinner.setPrompt("Item, brand and HSN Code (Optional)");
        spinner.setVisibility(View.GONE);
        return spinner;
    }

    private void updateOrder() {
        try {

            JSONArray orderArr;

            if (itemList == null || itemList.length == 0) {
                orderArr = createOrderWithoutOrgItems();
            } else {
                orderArr = createOrderWithOrgItems();
            }

            JSONObject orderObj = new JSONObject();
            JSONObject orderDetails = new JSONObject(bundle.get("order_details").toString());
            orderDetails.put("shipment", orderArr);

            //Log.d(TAG, "Selected Pair Org 2:" + selectedPairOrg.getString("tag"));

            if (bundle.getString("type").equals("inbox")) {
                orderObj.put("from", selectedPairOrg);
                orderObj.put("to", defaultOrg);

            } else {
                orderObj.put("to", selectedPairOrg);
                orderObj.put("from", defaultOrg);
            }
            orderObj.put("order", orderDetails);

            final Bundle input = new Bundle();
            input.putString("tag", selectedPairOrg.getString("tag"));
            input.putString("body", orderObj.toString());
            input.putString("id", orderDetails.getString("id"));
            input.putString("message", "false");
            input.putString("edit", "true");
            input.putString("paired_org", selectedPairOrg.toString());
            input.putString("type", bundle.getString("type"));

            final Storage s = storage;
            pd.show();

            //start a new thread to process job
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();

                    try {
                        Bundle output = new UpdateOrder(PlaceOrderActivity.this, s).execute(input).get();

                        if (!output.getString("exception").equals("no_exception")) {
                            showMessageOnUi(output.getString("exception"));
                            output.putString("first_msg", output.getString("exception"));
                            output.putString("second_msg", "null");
                        } else {
                            JSONObject newOrder = new JSONObject(output.getString("output"));
                            output.putString("first_msg", "Success");
                            output.putString("second_msg", "Order Id: " + newOrder.getString("order_id"));
                            output.putString("third_msg", bundle.getString("type"));
                        }
                        msg.setData(output);
                        sHandler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        input.putString("first_msg", e.getMessage());
                        input.putString("second_msg", "null");
                        msg.setData(input);
                        sHandler.sendMessage(msg);
                    }
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private void showEditOrderAlertBox(String titleMsg, String subjectMsg, final String type) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(titleMsg);
            builder.setMessage(subjectMsg);
            builder.setCancelable(true);
            builder.setNeutralButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (type.equals("inbox")) {
                                Intent intent = new Intent(PlaceOrderActivity.this, InboxActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(PlaceOrderActivity.this, OutboxActivity.class);
                                startActivity(intent);
                            }

                        }
                    });

            AlertDialog alert11 = builder.create();
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
            if (msg.getData().getString("second_msg").equals("null")) {
                showMessageOnUi(msg.getData().getString("first_msg"));
            } else {
                showEditOrderAlertBox(msg.getData().getString("first_msg"), msg.getData().getString("second_msg"),
                        msg.getData().getString("third_msg"));
            }
        }
    };

}