package org.septa.android.app.locationpicker;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.GooglePlaceAutoCompleteService;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.PlaceAutoComplete;
import org.septa.android.app.services.apiinterfaces.model.PlacePredictions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 8/29/17.
 */

class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable, TextWatcher {

    List<String> resultList = new ArrayList<String>(0);

    Context mContext;
    int mResource;
    String locationString;
    Call<PlacePredictions> activeCall;


    GooglePlaceAutoCompleteService mPlaceAPI = SeptaServiceFactory.getAutoCompletePlaceService();

    public PlacesAutoCompleteAdapter(Context context, int resource, LatLng location) {
        super(context, resource);

        locationString = location.latitude + "," + location.longitude;
        mContext = context;
        mResource = resource;
    }

    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.autocomplete_list_item, null);
        }

        TextView autocompleteTextView = (TextView) convertView.findViewById(R.id.autocompleteText);
        View googleLogo = convertView.findViewById(R.id.google_logo);
        if (position != 0) {
            autocompleteTextView.setText(resultList.get(position));
            autocompleteTextView.setVisibility(View.VISIBLE);
            googleLogo.setVisibility(View.GONE);
        } else {
            autocompleteTextView.setVisibility(View.GONE);
            googleLogo.setVisibility(View.VISIBLE);
        }

        return convertView;
    }


    @Override
    public String getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {

                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        if (activeCall != null) {
            if (!activeCall.isExecuted()) {
                activeCall.cancel();
            }
        }

        activeCall = mPlaceAPI.getAutoComplete(charSequence.toString(), locationString);


        activeCall.enqueue(new Callback<PlacePredictions>() {
            @Override
            public void onResponse(Call<PlacePredictions> call, Response<PlacePredictions> response) {
                List<PlaceAutoComplete> serviceResponses = response.body().getPlaces();
                List<String> newResults = new ArrayList<String>(serviceResponses.size() + 1);
                newResults.add(null);
                for (PlaceAutoComplete place : serviceResponses)
                    newResults.add(place.getPlaceDesc());

                resultList = newResults;
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<PlacePredictions> call, Throwable t) {

            }
        });

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}