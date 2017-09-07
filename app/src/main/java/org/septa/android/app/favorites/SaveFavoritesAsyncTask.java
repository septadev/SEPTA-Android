package org.septa.android.app.favorites;

import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;

import org.septa.android.app.R;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Favorite;

/**
 * Created by jkampf on 9/7/17.
 */

public class SaveFavoritesAsyncTask extends AsyncTask<Favorite, Void, Void> {

    Runnable onCancel;
    Runnable onPostExecute;
    Context context;

    public SaveFavoritesAsyncTask(Context context, Runnable onCancel, Runnable onPostExecute) {
        this.context = context;
        this.onCancel = onCancel;
        this.onPostExecute = onPostExecute;
    }

    @Override
    protected Void doInBackground(Favorite... params) {
        SeptaServiceFactory.getFavoritesService().addFavorites(context, params[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onPostExecute.run();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onCancel.run();
    }
}
