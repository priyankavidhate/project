package com.rohitvyavahare.extensions.Contact;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.rohitvyavahare.project.R;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by rohitvyavahare on 6/25/17.
 */

public class ContactViewHolder {

    private View view;

    private ImageView imageView;
    private CircularImageView circularImage;
    private ViewSwitcher switcher;
    private TextView nameView, phoneNumberView;

    public View getView() {
        return view;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public CircularImageView getCircularImage() {
        return circularImage;
    }

    public ViewSwitcher getSwitcher() {
        return switcher;
    }

    public TextView getNameView() {
        return nameView;
    }

    public TextView getPhoneNumberView() {
        return phoneNumberView;
    }

    public ContactViewHolder(View view) {
        this.view = view;
        imageView = (ImageView) view.findViewById(R.id.imageView);
        nameView = (TextView) view.findViewById(R.id.textView);
        phoneNumberView = (TextView) view.findViewById(R.id.textView1);
        switcher = (ViewSwitcher) view.findViewById(R.id.image_switcher);
        circularImage = (CircularImageView) view.findViewById(R.id.circularImageView);
    }
}
