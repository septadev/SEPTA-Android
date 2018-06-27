package org.septa.android.app.locationpicker;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import org.septa.android.app.domain.StopModel;
import org.septa.android.app.support.CursorAdapterSupplier;

import java.util.ArrayList;
import java.util.List;

public class LoadStopsForPicker extends AsyncTask<Object, Object, Void> {

    // tag for logging purposes
    private static final String TAG = LoadStopsForPicker.class.getSimpleName();

    private Context context;
    private LoadStopsForPickerListener mListener;
    private ByStopTabActivityHandler.StationNameAdapter2 adapter;
    private CursorAdapterSupplier<StopModel> cursorAdapterSupplier;

    public LoadStopsForPicker(Context context, LoadStopsForPickerListener listener, CursorAdapterSupplier<StopModel> cursorAdapterSupplier) {
        this.context = context;
        this.mListener = listener;
        this.cursorAdapterSupplier = cursorAdapterSupplier;
    }

    @Override
    protected Void doInBackground(Object... voids) {
        List<StopModel> stops = new ArrayList<>();
        Cursor c = cursorAdapterSupplier.getCursor(context, null);
        if (c.moveToFirst()) {
            do {
                StopModel stop = cursorAdapterSupplier.getCurrentItemFromCursor(c);
                stops.add(stop);
            } while (c.moveToNext());
        }
        adapter = new ByStopTabActivityHandler.StationNameAdapter2(context, stops);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        // stops loaded
        mListener.afterStopsLoaded(adapter);
    }

    public interface LoadStopsForPickerListener {
        void afterStopsLoaded(ByStopTabActivityHandler.StationNameAdapter2 adapter);
    }

}
