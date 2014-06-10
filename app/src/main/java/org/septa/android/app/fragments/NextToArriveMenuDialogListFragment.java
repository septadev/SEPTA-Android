package org.septa.android.app.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.septa.android.app.R;
import org.septa.android.app.adapters.NextToArrive_MenuDialog_ListViewItem_ArrayAdapter;
import org.septa.android.app.models.adapterhelpers.TextSubTextImageModel;

/**
 * Created by bmayo on 6/10/14.
 */
public class NextToArriveMenuDialogListFragment  extends ListFragment {
    public static final String TAG = NextToArriveMenuDialogListFragment.class.getName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.id.nexttoarrive_menudialog_fragmentlistview, null);

        String iconPrefix = getResources().getString(R.string.nexttoarrive_menu_icon_imageBase);
        String[] texts = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_texts);
        String[] subTexts = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_subtexts);
        String[] iconSuffix = getResources().getStringArray(R.array.nexttoarrive_menu_listview_items_iconsuffixes);

        TextSubTextImageModel[] listMenuItems = new TextSubTextImageModel[texts.length];
        for (int i=0; i<texts.length; i++) {
            TextSubTextImageModel textSubTextImageModel = new TextSubTextImageModel(texts[i], subTexts[i], iconPrefix, iconSuffix[i]);
            listMenuItems[i] = textSubTextImageModel;
        }

        ListView menuListView = (ListView)getActivity().findViewById(R.id.nexttoarrive_menudialog_fragmentlistview);
        menuListView.setAdapter(new NextToArrive_MenuDialog_ListViewItem_ArrayAdapter(getActivity(), listMenuItems));

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "detected a listfragment item being clicked");
    }
}