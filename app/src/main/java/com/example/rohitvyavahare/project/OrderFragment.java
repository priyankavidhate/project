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
        getActivity().setTitle("Inbox");

        Log.e(TAG, "creating order fragment");

        // We need to use a different list item layout for devices older than Honeycomb
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;

        try{

            Bundle b = getArguments();
            ArrayList<String> list = b.getStringArrayList("list");
            for(String str : list) {
                JSONObject obj = new JSONObject(str);
                obj = obj.getJSONObject("value");
                arr.add(obj.getString("from") + "-" + obj.getString("id"));
            }

            Log.e(TAG, "got to: " + arr.get(0));
            // Create an array adapter for the list view, using the Ipsum headlines array
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

        Log.e(TAG, "on start method of order fragment");

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.article_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            Log.e(TAG, "attaching");
            mCallback = (OrderFragment.OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.e(TAG, "onViewCreated");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.e(TAG, "onActivityCreated");

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Log.e(TAG, "array element :" + arr.get(0));
        // Notify the parent activity of selected item
        mCallback.onArticleSelected(position);

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }
}
