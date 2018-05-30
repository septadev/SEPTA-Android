package org.septa.android.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageView;

import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.database.SEPTADatabaseUtils;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alerts;
import org.septa.android.app.support.CursorAdapterSupplier;
import org.septa.android.app.systemstatus.SystemStatusState;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

    final int DELAY = 500;

    int[] images = new int[]{R.drawable.bus_image, R.drawable.bg_trolley_image, R.drawable.subway_septa};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        ImageView image = (ImageView) findViewById(R.id.splash_image);

        Calendar date = Calendar.getInstance();

        for (int i = images.length - 1; i >= 0; i--) {
            if (date.get(Calendar.SECOND) % (i + 1) == 0) {
                image.setImageResource(images[i]);
                break;
            }
        }

        // restart application if redirected from notification
        if (SEPTADatabaseUtils.getNeedToRestart(SplashScreenActivity.this)) {
            SEPTADatabaseUtils.setNeedToRestart(SplashScreenActivity.this, false);
            restartApplication();
        }

        final long timestamp = System.currentTimeMillis();

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
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);

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

    private void restartApplication() {
        // restart the app
        Intent restartIntent = SplashScreenActivity.this.getPackageManager().getLaunchIntentForPackage(SplashScreenActivity.this.getPackageName());
        PendingIntent intent = PendingIntent.getActivity(SplashScreenActivity.this, 0, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) SplashScreenActivity.this.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + DELAY, intent);
        System.exit(2);
    }
}
