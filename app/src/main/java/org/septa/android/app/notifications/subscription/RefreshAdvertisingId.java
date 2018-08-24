package org.septa.android.app.notifications.subscription;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.support.CrashlyticsManager;

import java.io.IOException;

public class RefreshAdvertisingId extends AsyncTask<Object, Object, Void> {

    // tag for logging purposes
    private static final String TAG = RefreshAdvertisingId.class.getSimpleName();

    private Context context;
    private Runnable onCancel;
    private Runnable onPostExecute;
    private String id = null;

    public RefreshAdvertisingId(Context context, Runnable onCancel, Runnable onPostExecute) {
        this.context = context;
        this.onCancel = onCancel;
        this.onPostExecute = onPostExecute;
    }

    @Override
    protected Void doInBackground(Object... voids) {

        AdvertisingIdClient.Info adInfo;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);

            id = adInfo.getId();

            Log.d(TAG, "Advertising ID: " + id);

            // save google play advertising ID as unique device ID
            SeptaServiceFactory.getNotificationsService().setDeviceId(context, id);

        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).
            CrashlyticsManager.logException(TAG, e);

//        } catch (GooglePlayServicesAvailabilityException e) {
            // Encountered a recoverable error connecting to Google Play services.

        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely.
            CrashlyticsManager.logException(TAG, e);

        } catch (GooglePlayServicesRepairableException e) {
            CrashlyticsManager.logException(TAG, e);
            CrashlyticsManager.log(Log.ERROR, TAG, e.getMessage());

        } catch (Exception e) {
            CrashlyticsManager.logException(TAG, e);
            CrashlyticsManager.log(Log.ERROR, TAG, e.getMessage());

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (id == null) {
            String token = SeptaServiceFactory.getNotificationsService().getRegistrationToken(context);
            if (!token.isEmpty()) {
                CrashlyticsManager.log(Log.ERROR, TAG, "Unable to fetch Advertising ID for FCM token: " + token);
            } else {
                CrashlyticsManager.log(Log.ERROR, TAG, "Unable to fetch Advertising ID");
            }
            onCancel.run();

        } else {
            onPostExecute.run();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onCancel.run();
    }
}