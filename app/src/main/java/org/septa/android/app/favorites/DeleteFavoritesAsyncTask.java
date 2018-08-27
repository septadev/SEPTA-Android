package org.septa.android.app.favorites;

import android.content.Context;
import android.os.AsyncTask;

import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;

public class DeleteFavoritesAsyncTask extends AsyncTask<String, Void, Void> {

    private Runnable onCancel;
    private Runnable onPostExecute;
    private Context context;

    public DeleteFavoritesAsyncTask(Context context) {
        this.context = context;
    }

    public DeleteFavoritesAsyncTask(Context context, Runnable onCancel, Runnable onPostExecute) {
        this.context = context;
        this.onCancel = onCancel;
        this.onPostExecute = onPostExecute;
    }

    @Override
    protected Void doInBackground(String... params) {
        SeptaServiceFactory.getFavoritesService().deleteFavorite(context, params[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (onPostExecute != null) {
            onPostExecute.run();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (onCancel != null) {
            onCancel.run();
        }
    }
}
