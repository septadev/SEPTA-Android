package org.septa.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.notifications.PushNotificationManager;
import org.septa.android.app.rating.SharedPreferencesRatingUtil;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alerts;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.systemstatus.SystemStatusState;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends BaseActivity {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        final long timestamp = System.currentTimeMillis();

        // increment number of times app used
        SharedPreferencesRatingUtil.incrementNumberOfUses(getApplicationContext());
        Log.d(TAG, "Number of App Uses: " + SharedPreferencesRatingUtil.getNumberOfUses(getApplicationContext()));

        SeptaServiceFactory.getAlertsService().getAlerts().enqueue(new Callback<Alerts>() {
            @Override
            public void onResponse(Call<Alerts> call, Response<Alerts> response) {
                SystemStatusState.update(response.body());
                complete();
            }

            @Override
            public void onFailure(Call<Alerts> call, Throwable t) {
                complete();
            }

            private void complete() {
                // if the app is opening via push notification click, prepare intent to bring user to relevant screen
                Intent intent = PushNotificationManager.addPushNotifClickIntent(SplashScreenActivity.this, getIntent().getExtras());

                if (!BuildConfig.DEBUG) {
                    while (System.currentTimeMillis() - timestamp < 2000) {
                        try {
                            Thread.sleep(System.currentTimeMillis() - timestamp);
                        } catch (InterruptedException e) {
                        }
                    }
                }

                // check if today is a holiday
                Date now = new Date();
                for (TransitType transitType : TransitType.values()) {
                    CursorAdapterSupplier<Boolean> cursorAdapterSupplier = DatabaseManager.getInstance(getBaseContext()).getHolidayIndicatorCursorAdapterSupplier(transitType);
                    transitType.setHolidayToday(cursorAdapterSupplier.getItemFromId(getBaseContext(), now));
                }

                startActivity(intent);
                SplashScreenActivity.this.finish();
            }
        });
    }

}
