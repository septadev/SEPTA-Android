package org.septa.android.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.activities.FareInformationGetMoreDetailsActionBarActivity;
import org.septa.android.app.models.adapterhelpers.TextSubTextImageModel;

/**
 * Created by bmayo on 6/10/14.
 */
public class NextToArrive_MenuDialog_ListViewItem_ArrayAdapter extends ArrayAdapter<TextSubTextImageModel> {
    private final Context context;
    private final TextSubTextImageModel[] values;

    private long secondsUntilNextRefresh = 0;

    private boolean isRefreshEnabled = false;

    private boolean isSaveToFavoriteEnabled = false;
    private boolean isRemoveSavedFavoriteEnabled = false;

    public NextToArrive_MenuDialog_ListViewItem_ArrayAdapter(Context context, TextSubTextImageModel[] values) {
        super(context, R.layout.fareinformation_listview_item, values);
        this.context = context;
        this.values = values;
    }

    public void enableRefresh() {

        this.isRefreshEnabled = true;
    }

    public void disableRefresh() {

        this.isRefreshEnabled = false;
    }

    public void enableSaveAsFavorite() {

        this.isSaveToFavoriteEnabled = true;
    }

    public void disableSaveAsFavorite() {

        this.isSaveToFavoriteEnabled = false;
    }

    public void enableRemoveSavedFavorite() {

        this.isRemoveSavedFavoriteEnabled = true;
    }

    public void disableRemovedSavedFavorite() {

        this.isRemoveSavedFavoriteEnabled = false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;

        rowView = inflater.inflate(R.layout.nexttoarrive_menudialog_listview_item, parent, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.nexttoarrive_menudialog_listview_items_imageview);
        TextView mainTextView = (TextView) rowView.findViewById(R.id.nexttoarrive_menudialog_listview_items_maintext);
        TextView subTextView = (TextView) rowView.findViewById(R.id.nexttoarrive_menudialog_listview_items_subtext);

        mainTextView.setText(values[position].getMainText());
        subTextView.setText(values[position].getSubText());

        // if we are filling out the refresh view and the seconds count is greater than
        // 0, change the text to reflect that
        if (position == 0) {
            if (secondsUntilNextRefresh > 0) {
                subTextView.setText("refreshing in "+secondsUntilNextRefresh+" seconds");
            }
        }

        if (position == 1) {
            if (isSaveToFavoriteEnabled) {
                subTextView.setText("add route to favorites");
            }

            if (isRemoveSavedFavoriteEnabled) {
                subTextView.setText("remove route from favorites");
            }
        }

        String resourceName = values[position].getImageNameBase().concat(values[position].getImageNameSuffix());
        int id = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        imageView.setImageResource(id);

        return rowView;
    }

    public void setNextRefreshInSecondsValue(long secondsUntilNextRefresh) {
        this.secondsUntilNextRefresh = secondsUntilNextRefresh;
        notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled() {

        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 0) return isRefreshEnabled;

        if (position == 1) return isSaveToFavoriteEnabled || isRemoveSavedFavoriteEnabled;

        return true;
    }
}