package org.septa.android.app.systemstatus;

import android.os.AsyncTask;

import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.SeptaServiceFactory;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.AlertDetail;
import org.septa.android.app.services.apiinterfaces.model.Alerts;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jkampf on 9/12/17.
 */

public class SystemStatusState {
    private static Map<String, Alert> map = new HashMap<String, Alert>();

    private static boolean initialized = false;


    public static void update(Alerts alerts){
        for (Alert alert : alerts.getAlerts()) {
            map.put(alert.getRouteId(), alert);
        }

        initialized = true;
    }


    public static boolean isInitialized() {
        return initialized;
    }

    public static Alert getAlertForLine(TransitType transitType, String lineId) {
        String alertId = transitType.getAlertId(lineId);
        Alert returnAlert = map.get(alertId);
        if (returnAlert == null) {
            returnAlert = new Alert();
            returnAlert.setRouteId(alertId);
        }

        return returnAlert;
    }


}
