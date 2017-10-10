package com.rohitvyavahare.extensions.Inbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.rohitvyavahare.project.R;
import com.example.rohitvyavahare.project.Utils;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by rohitvyavahare on 7/28/17.
 */

public class Adapter extends BaseAdapter {

    private static final String TAG = "InboxAdapter";
    private List<ListData> inboxDataList;
    private Context context;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    Utils util = new Utils();
    private SharedPreferences prefs;
    private Handler handler;

    public Adapter(Context c, List<ListData> inboxDataList) {
        this.inboxDataList = inboxDataList;
        this.context = c;
        this.mDrawableBuilder = TextDrawable.builder().round();
        handler = new Handler();
        prefs = c.getSharedPreferences(c.getString(R.string.private_file), MODE_PRIVATE);
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public int getCount() {
        return this.inboxDataList.size();
    }

    @Override
    public ListData getItem(int position) {
        return this.inboxDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = View.inflate(this.context, R.layout.list_item_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListData item = getItem(position);

        // provide support for selected state
        updateCheckedState(holder, item);
        String name = util.toTitleCase(item.getData());
        if(name.length() > 30){
            holder.getTextView3().setText(name.substring(0,27) + "...");
        }
        else {
            holder.getTextView().setText(name);
        }
        if (item.getData2() > 0) {
            holder.getTextView2().setVisibility(View.VISIBLE);
            holder.getTextView2().setText("" + item.getData2());
        } else {
            holder.getTextView2().setVisibility(View.GONE);
        }

        if(item.getData1() != -1){
            holder.getTextView1().setText("Total orders in process: " + item.getData1());
        }
        if(item.getTag().length() > 8){

            holder.getTextView3().setText("@"+item.getTag().substring(0,5) + "...");
        }
        else {
            holder.getTextView3().setText("@"+item.getTag());
        }


        return convertView;
    }

    private void updateCheckedState(ViewHolder holder, ListData item) {

        Log.d(TAG, "setting image view");
        Character c;
        if (Character.isLetter(item.getData().charAt(0))) {
            c = Character.toUpperCase(item.getData().charAt(0));
        } else {
            c = 'Y';
        }

        Log.d(TAG, "Color number :" + mColorGenerator.getColor(item.getData()));
        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(c), mColorGenerator.getColor(item.getData()));

        Log.d(TAG, "item.data " + item.getProfile_pic());

        String pic = "null";
        if(!item.getProfile_pic().equals("default")){
            pic = prefs.getString(item.getTag() + "_pic", "null");
        }

        if(!pic.equals("null")){
            holder.getSwitcher().showNext();
            holder.getCircularImage().setImageBitmap(util.StringToBitMap(pic));

        }
        else if (!item.getProfile_pic().equals("default") ) {
            Log.d(TAG, "org pic");
            final String image = item.getProfile_pic();
            final String tag = item.getTag();
            final ViewHolder vHolder = holder;
            final Context context = this.context;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitMap = util.getBitmapFromURL(image, tag, context);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            vHolder.getSwitcher().showNext();
                            vHolder.getCircularImage().setImageBitmap(bitMap);
                        }
                    });
                }
            });

        } else {
            Log.d(TAG, "no org_pic");
            holder.getImageView().setImageDrawable(drawable);
        }


        holder.getView().setBackgroundColor(Color.TRANSPARENT);
    }
}
