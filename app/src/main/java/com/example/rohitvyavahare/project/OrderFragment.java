package com.example.rohitvyavahare.project;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class OrderFragment extends ListFragment {
    OrderFragment.OnHeadlineSelectedListener mCallback;

    private static final String TAG = "OrderFragment";
    private ArrayList<String> arr = new ArrayList<>();

    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnHeadlineSelectedListener {
        /**
         * Called by HeadlinesFragment when a list item is selected
         */
        void onArticleSelected(int position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();

        Log.d(TAG, "Type: " +  b.getString("type"));

        if(b.getString("type").equals("inbox")) {
            getActivity().setTitle("Inbox");
        }
        else{
            getActivity().setTitle("Outbox");
        }

        Log.d(TAG, "creating order fragment");

        // We need to use a different list item layout for devices older than Honeycomb
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;

        try{

            ArrayList<String> list = b.getStringArrayList("list");
            for(String str : list) {
                JSONObject obj = new JSONObject(str);
                obj = obj.getJSONObject("value");
                if(b.getString("type").equals("inbox")) {
                    Log.d(TAG, "id: " + obj.getString("id"));
                    arr.add(obj.getString("from") + " - " + obj.getString("id"));
                }
                else {
                    arr.add(obj.getString("to") + " - " + obj.getString("id"));

                }
            }
            if(list.size() < 1) {
                Toast.makeText(getActivity(), "You don't have any orders in inbox", Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "setListAdapter");
            setListAdapter(new ArrayAdapter<>(getActivity(), layout, arr));

        }
        catch (org.json.JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Something went wrong while retrieving Inbox, Please try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "on start method of order fragment");

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
//        if (getFragmentManager().findFragmentById(R.id.article_fragment) != null) {
//            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            Log.d(TAG, "attaching");
            mCallback = (OrderFragment.OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated");

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Log.d(TAG, "array element :" + arr.get(0));
        // Notify the parent activity of selected item
        mCallback.onArticleSelected(position);

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }
}
