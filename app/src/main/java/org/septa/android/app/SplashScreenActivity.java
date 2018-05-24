package org.septa.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import org.septa.android.app.database.SEPTADatabase;
import org.septa.android.app.database.SEPTADatabaseUtils;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alerts;
import org.septa.android.app.systemstatus.SystemStatusState;

import java.io.File;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreenActivity extends AppCompatActivity {

    public static final String TAG = SplashScreenActivity.class.getSimpleName();

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

                startActivity(intent);
                SplashScreenActivity.this.finish();

                // initialize new database, if version updated
                int currentDBVersion = SEPTADatabase.getDatabaseVersion();
                int versionDownloaded = SEPTADatabaseUtils.getVersionDownloaded(SplashScreenActivity.this);
                int versionInstalled = SEPTADatabaseUtils.getVersionInstalled(SplashScreenActivity.this);
                if (versionDownloaded == versionInstalled && versionInstalled > currentDBVersion) {
                    initializeNewDatabase(versionInstalled);
                }
            }
        });
    }

    private void initializeNewDatabase(int newDbVersionToUse) {
        // look at directory of current files and find any databases that aren't newDbVersionToUse
        // -- SEPTA.sqlite is legacy filename, starting after 282 version numbers will be added in filenames
        // SEPTA_14_sqlite.zip (externaL) and SEPTA_14.sqlite (internal)
        // delete old copies of '<DBNAME>-journal' like '<DBNAME>' ex. SEPTA_14.sqlite and SEPTA_14.sqlite-journal
        // journal file is made when db started up for the first time

        final File internalDir = new File(new File(getApplicationInfo().dataDir), "databases");

        // clean up old database files
        for (File fileToDelete : internalDir.listFiles()) {
            String newDatabaseFilename = new StringBuilder("SEPTA_").append(newDbVersionToUse).append(".sqlite").toString();

            // delete internal files unrelated to current database
            if (!(fileToDelete.getName().contains(newDatabaseFilename))) {
                Log.e(TAG, "Deleting " + fileToDelete.getName());
                fileToDelete.delete();
            }
        }

        // notify user that database update complete
        Toast.makeText(this, R.string.notification_database_updated, Toast.LENGTH_SHORT).show();

    }
}
