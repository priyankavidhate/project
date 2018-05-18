package com.bigital.rohitvyavahare.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rohitvyavahare.Data.Storage;
import com.rohitvyavahare.extensions.CardView.OrgItemsCardView;
import com.rohitvyavahare.webservices.GetOrgItems;
import com.rohitvyavahare.webservices.PostOrgItems;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class OrgItemsActivity extends AppCompatActivity
        implements View.OnClickListener {
    private Button btnAdd;
    private EditText et;
    private EditText brand;
    private EditText hsnCode;
    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private Storage storage;
    private HashMap<String, Integer> nameToid;
    ArrayList<String> orgsNameArray = new ArrayList<>();
    private static final String TAG = "OrgItemsActivity";
    final int[] result = new int[1];
    private String orgTag;
    private JSONArray adapterData;
    RecyclerView rv;
    private static final int SELECT_FILE = 1;
    private final static String[] values = {"item", "brand", "hsnCode"};
    private JSONArray arrData;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_items);
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        storage = new Storage(this);
        rv = (RecyclerView) findViewById(R.id.rv);
        et = (EditText) findViewById(R.id.editItem);
        brand = (EditText) findViewById(R.id.editBrand);
        hsnCode = (EditText) findViewById(R.id.editHsn);
        btnAdd = (Button) findViewById(R.id.addTaskBtn);
        saveBtn = (Button) findViewById(R.id.saveTaskBtn);
        btnAdd.setOnClickListener(OrgItemsActivity.this);
        saveBtn.setOnClickListener(OrgItemsActivity.this);


        setOrgsData();
        chooseOrg();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        switch (i) {
            case R.id.addTaskBtn : {
                if(validateForm())
                    handleAddBtn();
                break;
            }

            case R.id.saveTaskBtn : {
                handlePostOrgItems();
                break;
            }
        }
    }

    private boolean validateForm() {

        String input = et.getText().toString();
        if (input.trim().length() < 1) {
            et.setError("Item name is required");
            return false;
        }

        input = brand.getText().toString();
        if (input.trim().length() < 1) {
            brand.setError("Brand name is required");
            return false;
        }

        return true;
    }

    private void handleAddBtn() {
        try {
            JSONArray arr = new JSONArray();
            String [] itemList = new String[3];
            itemList[0] = et.getText().toString().trim();
            itemList[1] = brand.getText().toString().trim();
            itemList[2] = hsnCode.getText().toString().trim();

            JSONObject obj = new JSONObject();
            for(int i=0; i<itemList.length; i++) {

                obj.put(values[i], itemList[i]);
            }
            arr.put(obj);

            addDataToCardView(arr);

            AlertDialog alertDialog;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Success!");
            builder.setMessage(itemList[0] + " added succesfully");

            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alertDialog = builder.show();
            TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.LEFT);
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private void chooseOrg() {
        try {
            if (orgsNameArray.size() == 1) {
                setTitle("Add items to organization : " + orgsNameArray.get(0));
                return;
            }
            final CharSequence orgNames[] = orgsNameArray.toArray(new String[0]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick an organization");
            builder.setItems(orgNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result[0] = which;
                    String selectedOrg = orgNames[which].toString();
                    setTitle("Add items to organization : " + selectedOrg);
                    Log.d(TAG, "Calling Get Orgs");
                    showDetailMessage();
                }
            });
            builder.setCancelable(false);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDetailMessage() {
        try {
            AlertDialog alertDialog;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("How to add items?");
            builder.setMessage("You can add items either by entering them manually or via .txt file. " +
                    "File should have one comma(,) separated entry on each line. " +
                    "Example: biscuit, ParleG, 1234abc." +
                    "For deleting an item click on item. ");

            builder.setPositiveButton("File",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            handleAlertClick();
                            dialog.cancel();
                            handleFileClick();
                        }
                    });

            builder.setNeutralButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            // TODO start differnet activity
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton("Manually",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            handleAlertClick();
                            dialog.cancel();
                            dialog.cancel();
                        }
                    });

            alertDialog = builder.show();
            TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
            messageText.setGravity(Gravity.LEFT);
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private void handleFileClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, SELECT_FILE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == SELECT_FILE && resultCode == RESULT_OK && null != data) {
                Uri selectedFile = data.getData();
                Log.d(TAG, "selectedFile Path: " + selectedFile.getPath());
                Log.d(TAG, "selectedFile Uri: " + selectedFile.toString());
                String file = handleInputFile(selectedFile);
                arrData = sliceData(file);
                addDataToCardView(arrData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private JSONArray sliceData(String input) {
        try {
            String[] lines = input.split("\n");
            JSONArray output = new JSONArray();
            for (String line : lines) {
                JSONObject obj = new JSONObject();
                String[] itemDetails = line.split(",");
                int i = 0;
                for (String str : itemDetails) {
                    str = str.replaceAll("\\s+", "");
                    obj.put(values[i], str);
                    i++;
                }
                if (i < 2) {
                    showMessageOnUi("Input data is not in correct format (item, brand, HSN code. HSN Code is optional)");
                    return null;
                }
                Log.d(TAG, "Slice data putting obj to arr :" + obj.toString());
                output.put(obj);
            }
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
            return null;
        }
    }

    private void handleAlertClick() {
        handleGetOrgItems();
        setupAdapter();
    }

    private void setupAdapter() {
        adapterData = storage.getOrgItems(orgTag);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        if (adapterData != null) {
            addDataToCardView(null);
        }
    }

    private void addDataToCardView(JSONArray input) {
        try {
            if (input != null) {
                input = concatArray(input, adapterData);
                if (input.length() > 100) {
                    showMessageOnUi("Can not add more than 100 items. Delete existing itmes by clicking them");
                    return;
                }
                saveBtn.setBackgroundColor(Color.parseColor("#bf4040"));
                adapterData = input;
                storage.setOrgItems(orgTag, adapterData.toString());
            } else {
                input = adapterData;
            }
            Log.d(TAG, "Adding data to adapter :" + input.toString());
            OrgItemsCardView adapter = new OrgItemsCardView(input, this, storage, this.orgTag);
            rv.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private void setOrgsData() {
        try {
            JSONArray associatedOrgs = storage.getAssociatedOrgs();
            if (associatedOrgs == null) {
                // handle null associatedOrgs
            }
            nameToid = new HashMap<>();
            for (int i = 0; i < associatedOrgs.length(); i++) {
                JSONObject obj = associatedOrgs.getJSONObject(i);
                if (obj.has("name")) {
                    orgsNameArray.add(obj.getString("name"));
                    nameToid.put(obj.getString("name"), i);
                }
            }

            for (String key : nameToid.keySet()) {
                Log.d(TAG, "nameToid key : " + key + " value :" + nameToid.get(key));
            }

            Set<String> hs = new HashSet<>();
            hs.addAll(orgsNameArray);
            orgsNameArray.clear();
            orgsNameArray.addAll(hs);

        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }
    }

    private void handleGetOrgItems() {
        try {
            JSONObject obj = storage.getAssociatedOrgs().getJSONObject(result[0]);
            orgTag = obj.getString("tag");
            Bundle input = new Bundle();
            input.putString("id", obj.getString("id"));
            input.putString("tag", orgTag);

            Bundle output = new GetOrgItems(this, storage)
                    .execute(input).get();

            if (!output.getString("exception").equals("no_exception")) {
                showMessageOnUi(output.getString("exception"));
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
        }

    }

    private void handlePostOrgItems() {
        try {
            String items = storage.getOrgItems(this.orgTag).toString();
            JSONObject obj = storage.getAssociatedOrgs().getJSONObject(result[0]);
            Bundle input = new Bundle();
            input.putString("body", items);
            input.putString("tag", obj.getString("tag"));
            input.putString("id", obj.getString("id"));
            Bundle output = new PostOrgItems(this, storage)
                    .execute(input).get();

            if (!output.getString("exception").equals("no_exception")) {
                showMessageOnUi(output.getString("exception"));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());

        }
    }

    private String handleInputFile(Uri uri) {
        try {
            InputStream inputStream = null;
            String str = "";
            StringBuffer buf = new StringBuffer();
            inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
                buf.append("\n");
            }
            inputStream.close();
            Log.d(TAG, "File :" + buf.toString());
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            showMessageOnUi(e.getMessage());
            return null;
        }
    }

//    public final void readCsv(Context context) {
//        List<String[]> questionList = new ArrayList<String[]>();
//        AssetManager assetManager = context.getAssets();
//
//        try {
//            InputStream csvStream = assetManager.open(CSV_PATH);
//            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
//            CSVReader csvReader = new CSVReader(csvStreamReader);
//            String[] line;
//
//            // throw away the header
//            csvReader.readNext();
//
//            while ((line = csvReader.readNext()) != null) {
//                questionList.add(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return questionList;
//    }

    private void showMessageOnUi(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(OrgItemsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private JSONArray concatArray(JSONArray arr1, JSONArray arr2)
            throws JSONException {

        if(arr2 == null) {
            return arr1;
        }

        if(arr1 == null) {
            return arr1;
        }

        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (int i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent = new Intent(OrgItemsActivity.this, SettingActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }
}
