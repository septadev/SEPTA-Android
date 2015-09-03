package org.septa.android.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.septa.android.app.R;
import org.septa.android.app.models.RealTimeMenuItem;

import java.util.List;

/**
 * Created by jhunchar on 4/17/15.
 */
public class RealTimeMenuAdapter extends BaseAdapter {

    private final List<RealTimeMenuItem> menuItemList;

    public RealTimeMenuAdapter(List<RealTimeMenuItem> menuItemList) {
        super();
        this.menuItemList = menuItemList;
    }

    @Override
    public int getCount() {
        if (menuItemList != null) {
            return menuItemList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (menuItemList != null && position < menuItemList.size()) {
            return menuItemList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        Object item = getItem(position);
        if (item != null) {
            return item.hashCode();
        }
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {

        // Get the menu item
        RealTimeMenuItem realTimeMenuItem = (RealTimeMenuItem) getItem(position);

        // Return true if not disabled
        if (realTimeMenuItem != null && !realTimeMenuItem.isDisabled()) {
            return true;
        }

        // Otherwise, return false
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.realtime_menu_icontext_item, parent, false);

            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.realtime_menu_icontext_item_imageView);
            holder.title = (TextView) convertView.findViewById(R.id.realtime_menu_icontext_item_textView);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        RealTimeMenuItem realTimeMenuItem = (RealTimeMenuItem) getItem(position);

        StringBuilder resourceNameBuilder = new StringBuilder();
        resourceNameBuilder.append("realtime_menu_").append(realTimeMenuItem.getSelectableIcon().toLowerCase());

        if (!isEnabled(position)) {
            resourceNameBuilder.append("_disabled");
        }

        String resourceName = resourceNameBuilder.toString();
        String title = realTimeMenuItem.getTitle();

        int id = holder.icon.getContext().getResources().getIdentifier(resourceName, "drawable", holder.icon.getContext().getPackageName());
        holder.icon.setImageResource(id);
        holder.title.setText(title);

        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
    }

}