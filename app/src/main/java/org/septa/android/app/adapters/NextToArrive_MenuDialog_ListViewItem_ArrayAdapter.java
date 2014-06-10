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

    public NextToArrive_MenuDialog_ListViewItem_ArrayAdapter(Context context, TextSubTextImageModel[] values) {
        super(context, R.layout.fareinformation_listview_item, values);
        this.context = context;
        this.values = values;
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
        return true;
    }
}