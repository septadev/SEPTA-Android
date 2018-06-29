package org.septa.android.app.locationpicker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.GooglePlaceAutoCompleteService;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.PlaceAutoComplete;
import org.septa.android.app.services.apiinterfaces.model.PlacePredictions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class PlacesAutoCompleteAdapter extends ArrayAdapter<PlaceAutoComplete> implements Filterable, TextWatcher {

    private List<PlaceAutoComplete> resultList = new ArrayList<>(0);

    private Context mContext;
    private String locationString;
    private Call<PlacePredictions> activeCall;

    private GooglePlaceAutoCompleteService mPlaceAPI = SeptaServiceFactory.getAutoCompletePlaceService();

    PlacesAutoCompleteAdapter(Context context, int resource, LatLng location) {
        super(context, resource);

        locationString = location.latitude + "," + location.longitude;
        mContext = context;
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
            convertView = inflater.inflate(R.layout.item_autocomplete_list, null);
        }

        TextView streetText = convertView.findViewById(R.id.street_text);
        TextView cityStateZip = convertView.findViewById(R.id.city_state_zip_text);
        View entryView = convertView.findViewById(R.id.entry_view);
        View googleLogo = convertView.findViewById(R.id.google_logo);

        // The terms and service says that we have to show the google logo when we use the google
        // autocomplete API.  So show this in the first element of the autocomplete.
        if (position != 0) {
            streetText.setText(resultList.get(position).getStructuredFormatting().getMainText());
            cityStateZip.setText(resultList.get(position).getStructuredFormatting().getSecondaryText());
            entryView.setVisibility(View.VISIBLE);
            googleLogo.setVisibility(View.GONE);
        } else {
            entryView.setVisibility(View.GONE);
            googleLogo.setVisibility(View.VISIBLE);
        }

        return convertView;
    }


    @Override
    public PlaceAutoComplete getItem(int position) {
        return resultList.get(position);
    }

    @NonNull
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
                List<PlaceAutoComplete> newResults = new ArrayList<>(serviceResponses.size() + 1);
                newResults.add(null);
                newResults.addAll(serviceResponses);

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