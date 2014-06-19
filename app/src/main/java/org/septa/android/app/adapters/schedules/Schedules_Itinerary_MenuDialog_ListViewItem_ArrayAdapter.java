package org.septa.android.app.adapters.schedules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.adapterhelpers.TextSubTextImageModel;

public class Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter extends ArrayAdapter<TextSubTextImageModel> {
    private final Context context;
    private final TextSubTextImageModel[] values;

    private boolean isSaveToFavoriteEnabled = false;
    private boolean isRemoveSavedFavoriteEnabled = false;

    private long secondsUntilNextRefresh = 0;

    public Schedules_Itinerary_MenuDialog_ListViewItem_ArrayAdapter(Context context, TextSubTextImageModel[] values) {
        super(context, R.layout.fareinformation_listview_item, values);
        this.context = context;
        this.values = values;
    }

    public void setNextRefreshInSecondsValue(long secondsUntilNextRefresh) {
        this.secondsUntilNextRefresh = secondsUntilNextRefresh;
    }

    public void enableSaveAsFavorite() {

        this.isSaveToFavoriteEnabled = true;
        notifyDataSetChanged();
    }

    public void disableSaveAsFavorite() {

        this.isSaveToFavoriteEnabled = false;
        notifyDataSetChanged();
    }

    public void enableRemoveSavedFavorite() {

        this.isRemoveSavedFavoriteEnabled = true;
        notifyDataSetChanged();
    }

    public void disableRemoveSavedFavorite() {

        this.isRemoveSavedFavoriteEnabled = false;
        notifyDataSetChanged();
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

        if (position == 0) {
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

    @Override
    public boolean areAllItemsEnabled() {

        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position == 0) return isSaveToFavoriteEnabled || isRemoveSavedFavoriteEnabled;

        return true;
    }
}