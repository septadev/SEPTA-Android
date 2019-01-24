package org.septa.android.app.systemstatus;

import org.septa.android.app.TransitType;
import org.septa.android.app.services.apiinterfaces.model.Alert;
import org.septa.android.app.services.apiinterfaces.model.Alerts;

import java.util.HashMap;
import java.util.Map;

public class SystemStatusState {

    private static Map<String, Alert> map = new HashMap<>();

    private static boolean initialized = false;

    private static final String MOBILE_APP_ALERT_ROUTE_ID = "APP", GENERIC_ALERT_ROUTE_ID = "generic";

    public static void update(Alerts alerts){
        if (alerts == null) {
            return;
        }

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

    public static Alert getGenericAlert() {
        return map.get(GENERIC_ALERT_ROUTE_ID);
    }

    public static Alert getAlertForApp() {
        return map.get(MOBILE_APP_ALERT_ROUTE_ID);
    }

}