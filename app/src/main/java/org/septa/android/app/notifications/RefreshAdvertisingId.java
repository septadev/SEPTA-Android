package org.septa.android.app.notifications;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.septa.android.app.support.CrashlyticsManager;

import java.io.IOException;

public class RefreshAdvertisingId extends AsyncTask<Object, Object, Void> {

    // tag for logging purposes
    private static final String TAG = RefreshAdvertisingId.class.getSimpleName();

    private Context context;
    private String id = null;

    public RefreshAdvertisingId(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Object... voids) {

        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);

        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).

//        } catch (GooglePlayServicesAvailabilityException e) {
            // Encountered a recoverable error connecting to Google Play services.

        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely.
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }

        if (adInfo != null) {
            id = adInfo.getId();

            Log.d(TAG, "Advertising ID: " + id);

            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String token = instanceIdResult.getToken();
                    String id = instanceIdResult.getId();
                    // Do whatever you want with your token now
                    // i.e. store it on SharedPreferences or DB
                    // or directly send it to server

                    //for now we are displaying the token in the log
                    //copy it as this method is called only when the new token is generated
                    //and usually new token is only generated when the app is reinstalled or the data is cleared
                    Log.d(TAG, "FCM Token: " + token); // TODO: THIS IS THE ONE WE WANT

                    Log.d(TAG, "Instance ID: " + id); // this is a shortcutted version of the token
                }
            });
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        boolean success = id != null;

        if (!success) {
            CrashlyticsManager.log(Log.ERROR, TAG, "Unable to fetch Advertising ID: ");
        }
    }

}