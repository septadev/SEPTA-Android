package org.septa.android.app.favorites;

import android.content.Context;
import android.os.AsyncTask;

import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.NextArrivalFavorite;

public class SaveFavoritesAsyncTask extends AsyncTask<NextArrivalFavorite, Void, Void> {

    private Runnable onCancel;
    private Runnable onPostExecute;
    private Context context;

    public SaveFavoritesAsyncTask(Context context, Runnable onCancel, Runnable onPostExecute) {
        this.context = context;
        this.onCancel = onCancel;
        this.onPostExecute = onPostExecute;
    }

    @Override
    protected Void doInBackground(NextArrivalFavorite... params) {
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
