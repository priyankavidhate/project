package com.rohitvyavahare.extensions.Inbox;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.rohitvyavahare.project.R;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by rohitvyavahare on 7/28/17.
 */

public class ViewHolder {

    private View view;

    private ImageView imageView;
    private CircularImageView circularImage;
    private ViewSwitcher switcher;

    private TextView textView, textView2, textView1, textView3;

    public ViewHolder(View view) {
        this.view = view;
        imageView = (ImageView) view.findViewById(R.id.imageView);
        textView = (TextView) view.findViewById(R.id.textView);
        textView1 = (TextView) view.findViewById(R.id.textView1);
        textView2 = (TextView) view.findViewById(R.id.textView2);
        textView3 = (TextView) view.findViewById(R.id.OrgTag);
        switcher = (ViewSwitcher) view.findViewById(R.id.image_switcher);
        circularImage = (CircularImageView) view.findViewById(R.id.circularImageView);
    }

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

    public TextView getTextView() {
        return textView;
    }

    public TextView getTextView2() {
        return textView2;
    }

    public TextView getTextView1() {
        return textView1;
    }

    public TextView getTextView3() {
        return textView3;
    }
}
