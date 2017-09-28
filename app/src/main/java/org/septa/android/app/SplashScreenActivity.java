package org.septa.android.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import org.septa.android.app.database.DatabaseManager;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alerts;
import org.septa.android.app.systemstatus.SystemStatusState;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Response;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreenActivity extends AppCompatActivity {

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

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatabaseManager.getInstance(SplashScreenActivity.this);
                try {
                    Response<Alerts> response = SeptaServiceFactory.getAlertsService().getAlerts().execute();
                    SystemStatusState.update(response.body());
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    startActivity(intent);
                    SplashScreenActivity.this.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
