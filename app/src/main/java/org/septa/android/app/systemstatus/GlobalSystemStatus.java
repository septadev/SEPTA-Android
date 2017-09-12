package org.septa.android.app.systemstatus;

import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/12/17.
 */

public class GlobalSystemStatus {
    private AlertDetail globalAlertDetails;

    private static GlobalSystemStatus instance = new GlobalSystemStatus();

    public static void triggerUpdate() {
        SeptaServiceFactory.getAlertDetailsService().getAlertDetails("generic").enqueue(new Callback<AlertDetail>() {
            @Override
            public void onResponse(Call<AlertDetail> call, Response<AlertDetail> response) {
                GlobalSystemStatus.instance.globalAlertDetails = response.body();
            }

            @Override
            public void onFailure(Call<AlertDetail> call, Throwable t) {

            }
        });
    }

    public static AlertDetail getGlobalAlertDetails() {
        return instance.globalAlertDetails;
    }
}
