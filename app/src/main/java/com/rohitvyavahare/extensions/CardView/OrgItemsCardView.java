package com.rohitvyavahare.extensions.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rohitvyavahare.project.R;
import com.rohitvyavahare.Data.Storage;

import org.json.JSONArray;

/**
 * Created by rohitvyavahare on 8/6/17.
 */

public class OrgItemsCardView extends RecyclerView.Adapter<OrgItemsCardView.OrgItemViewHolder> {

    private static final String TAG = "OrgItemsCardView";
    //    List<OrgItems> items;
    JSONArray items;
    Context c;
    Storage storage;
    String orgTag;

    public OrgItemsCardView(JSONArray items, Context c, Storage storage, String orgTag) {
        Log.d(TAG, "OrgItemsCardView Orders size :" + items.length());
        this.items = items;
        this.c = c;
        this.storage = storage;
        this.orgTag = orgTag;
    }

//    @Override
//    public int getItemViewType(int position) {
//        if (position % 5 == 0)
//            return 0;
//        return 1;
//    }

    public static class OrgItemViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView item;
        TextView brand;
        TextView hsnCode;
        TextView title;

        OrgItemViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            item = (TextView) itemView.findViewById(R.id.item);
            brand = (TextView) itemView.findViewById(R.id.brand);
            hsnCode = (TextView) itemView.findViewById(R.id.hsnCode);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

//    public static class ViewHolderAdMob extends RecyclerView.ViewHolder {
//        public AdView mAdView;
//
//        public ViewHolderAdMob(View view) {
//            super(view);
//            mAdView = (AdView) view.findViewById(R.id.adView);
//            AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                    .build();
//            mAdView.loadAd(adRequest);
//        }
//    }

    @Override
    public int getItemCount() {
        return items.length();
    }

    @Override
    public OrgItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_org_items, viewGroup, false);
        OrgItemViewHolder pvh = new OrgItemViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(OrgItemViewHolder orgItemViewHolder, int i) {
        try {
            String item = items.getJSONObject(i).has("item") ? items.getJSONObject(i).getString("item") : "";
            String brand = items.getJSONObject(i).has("brand") ? items.getJSONObject(i).getString("brand") : "";
            String hsnCode = items.getJSONObject(i).has("hsnCode") ? items.getJSONObject(i).getString("hsnCode") : "";
            orgItemViewHolder.item.setText(item);
            orgItemViewHolder.brand.setText(brand);
            orgItemViewHolder.hsnCode.setText(hsnCode);
            orgItemViewHolder.title.setText("Item No : " + (i + 1));

            final int position = i;
            orgItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    handleOnClick(position);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOnClick(final int position) {
        try {
            AlertDialog alertDialog;

            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Do you want to delete an entry?");

            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            removeItem(position);
                        }
                    });

            builder.setNegativeButton("No",
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
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void removeItem(int position) {
        try {
            JSONArray list = new JSONArray();
            int len = this.items.length();
            if (this.items == null) {
                return;
            }
            for (int i = 0; i < len; i++) {
                //Excluding the item at position
                if (i != position) {
                    list.put(this.items.get(i));
                }
            }
            this.items = list;
            storage.setOrgItems(this.orgTag, this.items.toString());
            Log.d(TAG, "Deleted itme from list at position :" + position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,  this.items.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
