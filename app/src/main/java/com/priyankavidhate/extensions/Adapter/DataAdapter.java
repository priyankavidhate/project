package com.priyankavidhate.extensions.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by priyankavidhate on 10/8/17.
 */

public class DataAdapter  extends ArrayAdapter<Data> {

    public DataAdapter(Context context, List<Data> objects) {
        super(context, android.R.layout.simple_list_item_2, objects);
    }

    @Override //don't override if you don't want the default spinner to be a two line view
    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView);
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return initView(position, convertView);
    }

    private View initView(int position, View convertView) {
        if(convertView == null)
            convertView = View.inflate(getContext(),
                    android.R.layout.simple_list_item_2,
                    null);
        TextView tvText1 = (TextView)convertView.findViewById(android.R.id.text1);
        tvText1.setTypeface(Typeface.DEFAULT_BOLD);
        TextView tvText2 = (TextView)convertView.findViewById(android.R.id.text2);


        tvText1.setText(getItem(position).getItem());

        if(getItem(position).getBrandAndHsnCode().length() > 1 )
            tvText2.setText(getItem(position).getBrandAndHsnCode());
        return convertView;
    }
}
