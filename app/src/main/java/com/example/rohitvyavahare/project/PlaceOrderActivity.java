package com.example.rohitvyavahare.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
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

import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.extensions.Adapter.Data;
import com.rohitvyavahare.extensions.Adapter.DataAdapter;
import com.rohitvyavahare.extensions.Spinner.NothingSelectedSpinnerAdapter;
import com.rohitvyavahare.webservices.GetOrgItems;
import com.rohitvyavahare.webservices.PostOrder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.rohitvyavahare.project.R.id.addMore;

public class PlaceOrderActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "PlaceOrderActivity";
    private HashMap<String, Integer> nameToid;
    final int[] result = new int[1];
    private int count = 0;
    private Storage storage;
    JSONObject defaultOrg;
    JSONArray pairedOrgs;
    private JSONObject selectedPairOrg;
    private String[] itemList;
    private ArrayList<String> pairedOrgsNames;
    private Map<Integer, String> dropDownMap = new HashMap<>();

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
//            createTable();

//            findViewById(R.id.addMore).setOnClickListener(PlaceOrderActivity.this);
//            findViewById(R.id.PlaceOrder).setOnClickListener(PlaceOrderActivity.this);
//            findViewById(R.id.removeItem).setOnClickListener(PlaceOrderActivity.this);

            pairedOrgsNames = new ArrayList<>();
            nameToid = new HashMap<>();
            for (int i = 0; i < pairedOrgs.length(); i++) {

                JSONObject obj = pairedOrgs.getJSONObject(i);
                if (obj.has("name")) {
                    pairedOrgsNames.add(obj.getString("name"));
                    nameToid.put(obj.getString("name"), i);
                }
            }

            for (String key : nameToid.keySet()) {
                Log.d(TAG, "nameToid key : " + key + " value :" + nameToid.get(key));
            }

            Set<String> hs = new HashSet<>();
            hs.addAll(pairedOrgsNames);
            pairedOrgsNames.clear();
            pairedOrgsNames.addAll(hs);


            final CharSequence orgNames[] = pairedOrgsNames.toArray(new String[0]);
//            final TextView orgName = (TextView) findViewById(R.id.OrgName);
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
//                    String selected_org = orgNames[which].toString();
//                    orgName.setText("Order to Organization : " + selected_org);
                    createTable();
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
            selectedPairOrg = pairedOrgs.getJSONObject(nameToid.get(orgName.getText().toString().split("Order to Organization : ")[1]));

            final List<String> list = new ArrayList<String>();
            final List<Data> dList = new ArrayList<>();
            Log.d(TAG, "Item list length :" + itemList.length);
            for (String item : itemList) {
                Log.d(TAG, "Adding Item :" + item);
                list.add(item);
                String [] temp = item.split(" ");
                String [] out = new String[2] ;

                if(temp.length > 0) {
                    out[0] = temp[0];
                }

                if(temp.length >= 1) {
                    out[1] = temp[1];
                }

                if(temp.length >=2) {
                    out[1] += " " + temp[2];
                }
                dList.add(new Data(out[0], out[1]));
            }

//            for(int i=0; i<10; i++) {
//                dList.add(new Data("test", "abcd", "efgh"));
//            }

//            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
//                    android.R.layout.simple_spinner_item, list);
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            spinner.setAdapter(dataAdapter);
//
//            spinner.setAdapter(
//                    new NothingSelectedSpinnerAdapter(
//                            dataAdapter,
//                            R.layout.contact_spinner_row_nothing_selected,
//                            // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
//                            this));
            spinner.setAdapter(
                    new NothingSelectedSpinnerAdapter(
                            new DataAdapter(this, dList),
                            R.layout.contact_spinner_row_nothing_selected,
                            // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                            this));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.d(TAG, "Pos :" + pos);
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
            TypedArray t2 = getResources().obtainTypedArray(R.array.quantityIds);
            TypedArray t = getResources().obtainTypedArray(R.array.itemIds);

            int i = v.getId();
            if (i == R.id.PlaceOrder) {

                if (!validateShipment()) {
                    Log.d(TAG, "Failed to validate shipment");
                    return;
                }
                createOrder();
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
                orderArr = createOrderWithoutOrgItmes();
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

        TypedArray t2 = getResources().obtainTypedArray(R.array.quantityIds);

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

            if (key == 0) {

                editQty = (EditText) findViewById(R.id.editQuantity);
                order.put("quantity", editQty.getText().toString().trim());
                orderArr.put(order);
                continue;
            }

            editQty = (EditText) findViewById(t2.getResourceId(j, 0));
            order.put("quantity", editQty.getText().toString().trim());
            orderArr.put(order);
            j++;

        }
        t2.recycle();
        return orderArr;
    }

    private JSONArray createOrderWithoutOrgItmes() throws Exception {
        JSONArray orderArr = new JSONArray();
        JSONObject order = new JSONObject();

        TypedArray t = getResources().obtainTypedArray(R.array.itemIds);
        TypedArray t2 = getResources().obtainTypedArray(R.array.quantityIds);

        EditText editItem = (EditText) findViewById(R.id.editItem);
        EditText editQty = (EditText) findViewById(R.id.editQuantity);

        order.put("item", editItem.getText().toString().trim());
        order.put("quantity", editQty.getText().toString().trim());
        orderArr.put(order);
        for (int j = 0; j < count; j++) {
            order = new JSONObject();
            editItem = (EditText) findViewById(t.getResourceId(j, 0));
            editQty = (EditText) findViewById(t2.getResourceId(j, 0));
            order.put("item", editItem.getText().toString().trim());
            order.put("quantity", editQty.getText().toString().trim());
            orderArr.put(order);
        }
        t.recycle();
        t2.recycle();
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

    private void handleEditOrder() {
        try {

            // @TODO check is it customer or owner
            //

        } catch (Exception e) {

        }
    }

    private boolean validateShipment() {
        TypedArray t = getResources().obtainTypedArray(R.array.itemIds);
        TypedArray t2 = getResources().obtainTypedArray(R.array.quantityIds);

        String type;
        if (itemList == null || itemList.length == 0) {
            type = "edittxt";
        } else {
            type = "spinner";
        }

        Log.d(TAG, "Type is :" + type);

        EditText editQty = (EditText) findViewById(R.id.editQuantity);

        if (editQty.getText().toString().trim().equals("")) {
            Log.d(TAG, "Edit Qunatity is empty :" + editQty.getText().toString());
            editQty.setError("Quantity is required");
            return false;
        }


        if (type.equals("spinner")) {

            if (dropDownMap == null || !dropDownMap.containsKey(0)) {
                Log.d(TAG, "Spinner is empty at position :" + 0);
                showMessageOnUi("Item name is required at position " + "" + (0 + 1));
                return false;
            }


            for (int i = 0; i < count; i++) {
                Log.d(TAG, "Validating Spinner at position :" + i);
                if (dropDownMap == null || !dropDownMap.containsKey(i + 1)) {
                    Log.d(TAG, "Spinner is empty at position :" + i);
                    showMessageOnUi("Item name is required at position " + "" + (i + 1));
                    return false;
                }

                editQty = (EditText) findViewById(t2.getResourceId(i, 0));
                Log.d(TAG, "Validating Edit qunatity at position :" + i);
                if (editQty.getText().toString().trim().equals("")) {
                    Log.d(TAG, "Edit Qunatity is empty :" + editQty.getText().toString());
                    editQty.setError("Quantity is required");
                    return false;
                }
            }

        } else {

            EditText editItem = (EditText) findViewById(R.id.editItem);
            if (editItem.getText().toString().trim().equals("")) {
                editItem.setError("Item name is required");
                return false;
            }

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

        t.recycle();
        t2.recycle();
        return true;
    }

    private void addItem(TypedArray t, TypedArray t2) {

        if (count >= t.length()) {
            return;
        }
        if (count == 0) {
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
        t.recycle();
        t2.recycle();
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
        t.recycle();
        t2.recycle();
    }

    private void handlePostOrder(Bundle input) {
        try {
            Bundle output = new PostOrder(this, storage).execute(input).get();

            if (!output.getString("exception").equals("no_exception")) {
                showAlertBox("Error", output.getString("exception"));
                return;
            }
            JSONObject newOrder = new JSONObject(output.getString("output"));
            showAlertBox("Success", "Order Id: " + newOrder.getString("order_id"));
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

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
                        }
                    });

            AlertDialog alert11 = builder.create();
            alert11.show();
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private String[] getItems() {
        try {

            CharSequence orgNames[] = pairedOrgsNames.toArray(new String[0]);
            String selectedOrg = orgNames[result[0]].toString();
            selectedPairOrg = pairedOrgs.getJSONObject(nameToid.get(selectedOrg));
            if (!selectedPairOrg.has("tag")) {
                return null;
            }

            Long lastCheckedTime = storage.getLastPairedOrgsTime(selectedPairOrg.getString("tag"));
            JSONArray itemArr;

            if (lastCheckedTime != 0L && lastCheckedTime < (86400000 * 15)) {
                itemArr = storage.getOrgItmes(selectedPairOrg.getString("tag"));

            } else {

                Bundle input = new Bundle();
                input.putString("id", selectedPairOrg.getString("id"));
                input.putString("tag", selectedPairOrg.getString("tag"));

                Bundle output = new GetOrgItems(this, storage)
                        .execute(input).get();

                if (!output.getString("exception").equals("no_exception")) {
                    showMessageOnUi(output.getString("exception"));
                    return null;
                }

                itemArr = storage.getOrgItmes(selectedPairOrg.getString("tag"));
            }

            if (itemArr == null || itemArr.length() == 0) {
                return null;
            }

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
                Toast.makeText(PlaceOrderActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTable() {

        String type;
        Spinner spinner = null;
        getItems();

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
            spinner.setId(R.id.editItem);
            spinner.setLayoutParams(tlp);
            spinner.setPrompt("Item, brand and HSN Code (Optional)");
            tr2.addView(spinner);
        } else {
            editItem = new EditText(this);
            editItem.setId(R.id.editItem);
            editItem.setLayoutParams(tlp);
            editItem.setHint(getString(R.string.item));
            editItem.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            tr2.addView(editItem);
        }

        editQuantity = new EditText(this);
        editQuantity.setId(R.id.editQuantity);
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

        TypedArray t = getResources().obtainTypedArray(R.array.itemIds);
        TypedArray t2 = getResources().obtainTypedArray(R.array.quantityIds);
        ArrayList<TableRow> rows = new ArrayList<>();
        Spinner[] spinners = new Spinner[t.length()];
        for (int i = 0; i < t.length(); i++) {
            Log.d(TAG, "Adding item :" + t.getResourceId(i, 0));

            View v;

            if (type.equals("spinner")) {
                v = addSpinner(t.getResourceId(i, 0), tlp);
                spinners[i] = (Spinner) v;
            } else {
                v = addEditText(t.getResourceId(i, 0), tlp);
            }

            Log.d(TAG, "Adding qunatiy :" + t2.getResourceId(i, 0));
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
        t.recycle();
        t2.recycle();

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
            addItemsOnSpinner(spinner, i);
            i++;
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
}