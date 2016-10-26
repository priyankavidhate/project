package com.example.rohitvyavahare.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    JSONObject account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Log.d(TAG, "account " + getIntent().getStringExtra("account"));
            account = new JSONObject(getIntent().getStringExtra("account"));
            setTitle("Hello " + account.getString("first_name"));


            TextView one = (TextView) findViewById(R.id.OrderInbox);
            one.setOnClickListener(this); // calling onClick() method
            TextView two = (TextView) findViewById(R.id.OrderOutbox);
            two.setOnClickListener(this);
            TextView three = (TextView) findViewById(R.id.PlaceOrder);
            three.setOnClickListener(this);
            TextView four = (TextView) findViewById(R.id.AddEmployee);
            four.setOnClickListener(this);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.OrderInbox: {
                Intent intent = new Intent(HomeActivity.this, InboxOrderActivity.class);
                intent.putExtra("account", account.toString());
                startActivity(intent);
                break;
            }


            case R.id.OrderOutbox:
                // do your code
                break;

            case R.id.PlaceOrder: {
                Intent intent = new Intent(HomeActivity.this, PlaceOrderActivity.class);
                intent.putExtra("account", account.toString());
                startActivity(intent);
                // do your code
                break;
            }


            case R.id.AddEmployee: {
                Intent intent = new Intent(HomeActivity.this, AddEmployeeActivity.class);
                intent.putExtra("account", account.toString());
                startActivity(intent);
                break;
            }


            default:
                break;
        }


    }
}
