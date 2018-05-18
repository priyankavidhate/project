package com.rohitvyavahare.extensions.Contact;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bigital.rohitvyavahare.project.R;

import java.util.List;

/**
 * Created by rohitvyavahare on 6/25/17.
 */

public class ContactAdapter extends BaseAdapter {

    private static final String TAG = "ContactAdapter";
    private List<ContactListData> contactDataList;
    private Context context;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public ContactAdapter(Context c, List<ContactListData> contactDataList) {
        this.contactDataList = contactDataList;
        this.context = c;
        mDrawableBuilder = TextDrawable.builder()
                .round();
    }

    @Override
    public int getCount() {
        return this.contactDataList.size();
    }

    @Override
    public ContactListData getItem(int position) {
        return this.contactDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ContactViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(this.context, R.layout.list_contacts, null);
            holder = new ContactViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ContactViewHolder) convertView.getTag();
        }

        ContactListData item = getItem(position);

        // provide support for selected state
        updateCheckedState(holder, item);

        return convertView;
    }

    private void updateCheckedState(ContactViewHolder holder, ContactListData item) {

        Log.d(TAG, "Updating checked state");
        Character c;
        Log.d(TAG, "View Data: Name " + item.getName() + " Phone number " + item.getNumber() + " Profile Pic " + item.getProfile_pic());
        if (Character.isLetter(item.getName().charAt(0))) {
            c = Character.toUpperCase(item.getName().charAt(0));
        } else {
            c = 'Y';
        }

        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(c), mColorGenerator.getColor(item.getName()));
        holder.getImageView().setImageDrawable(drawable);
        holder.getNameView().setText(item.getName());
        holder.getPhoneNumberView().setText(item.getNumber());
        holder.getView().setBackgroundColor(Color.TRANSPARENT);

        /*
            String pic = "null";
            if (!item.profile_pic.equals("default")) {
                pic = prefs.getString(item.getNumber() + "_pic", "null");
            }

            if (!pic.equals("null")) {
                holder.switcher.showNext();
                holder.circularImage.setImageBitmap(util.StringToBitMap(pic));

            } else if (!item.profile_pic.equals("default")) {
                Log.d(TAG, "profile pic");
                final String image = item.profile_pic;
                final String tag = item.getNumber();
                final ViewHolder vHolder = holder;
                final Context context = ViewOrgDetailsActivity.this;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitMap = util.getBitmapFromURL(image, tag, context);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                vHolder.switcher.showNext();
                                vHolder.circularImage.setImageBitmap(bitMap);
                            }
                        });
                    }
                });

            } else {
                Log.d(TAG, "no profile pic");
                holder.imageView.setImageDrawable(drawable);
            }
        */
    }
}
