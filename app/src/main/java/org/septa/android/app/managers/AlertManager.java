package org.septa.android.app.managers;

import org.septa.android.app.models.LocationBasedRouteModel;
import org.septa.android.app.models.servicemodels.AlertModel;
import org.septa.android.app.services.adaptors.AlertsAdaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Trey Robinson on 7/9/14.
 *
 * Manage business rules and retrieval of Alerts both Global and Route based.
 */
public class AlertManager implements Callback<ArrayList<AlertModel>> {

    private static final String GLOBAL_ALERT_ROUTE_NAME = "generic";
    public static AlertManager mInstance;

    private List<AlertModel> alerts;


    private AlertModel globalAlert;
    private List<IAlertListener> listeners;


    private AlertManager() {
        listeners = new ArrayList<IAlertListener>();
    }

    /**
     * @return current instance of the manager
     */
    public static AlertManager getInstance() {
        if (mInstance == null) {
            mInstance = new AlertManager();
        }

        return mInstance;
    }

    /**
     * Fetch and cache all alert values.
     */
    public void fetchAlerts() {
        AlertsAdaptor.getAlertsService().alerts(this);
    }

    /**
     * The services do not provide a reliable tie between routes and alerts so we have to make one here.
     * @param route
     * @return
     */
    public AlertModel getAlertForRoute(LocationBasedRouteModel route){
        if(alerts != null ){
            for(AlertModel alert : alerts){
                //@TODO handle MFO/BSL etc lines with odd names that do not relate to key values.
                if(alert.getRouteName().equals(route.getRouteShortName())){
                    return alert;
                }
            }
        }

        return null;
    }

    /**
     * The services do not provide a reliable tie between routes and alerts so we have to make one here.
     * @param routeId is used in the event that LocationBasedRouteModel is not available.
     * @return
     */
    public AlertModel getAlertForRouteShortName(String routeId){
        if(alerts != null ){
            for(AlertModel alert : alerts){
                if(routeId.equals(alert.getRouteId())){
                    return alert;
                }
            }
        }

        return null;
    }


    /**
     * Updates the Global Alert if we have not yet retrieved an update or
     * if the specified duration has passed.
     *
     * Retrieve the existing value (if there is one) by calling getGlobalAlert
     */
    public void fetchGlobalAlert() {

        AlertsAdaptor.getAlertsService().getAlertsForRouteName(GLOBAL_ALERT_ROUTE_NAME, new Callback<ArrayList<AlertModel>>() {
            @Override
            public void success(ArrayList<AlertModel> alertModels, Response response) {
                if (alertModels != null && !alertModels.isEmpty()) {
                    globalAlert = alertModels.get(0);
                    notifyListeners();
                }

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });


    }

    /**
     * Sort the alerts by date. Most recent first.
     */
    private void sortAlerts() {
        if (alerts != null) {
            Collections.sort(alerts, new Comparator<AlertModel>() {
                public int compare(AlertModel o1, AlertModel o2) {
                    return o2.getLastUpdate().compareTo(o1.getLastUpdate());
                }
            });
        }
    }

    private void notifyListeners() {
        for (IAlertListener listener : listeners) {
            listener.alertsDidUpdate();
        }
    }

    /**
     * Add a listener who will receive notification of new alerts.
    * @param listener IAlertListener to be notified when new alerts are received.
    */
    public void addListener(IAlertListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     * @param listener IAlertListener to be removed.
     */
    public void removeListener(IAlertListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns only alerts happening since the given date that have not been shown
     * @param  date all alerts returned after this date.
     */
    public List<AlertModel> getNewAlertsSinceDate(Date date) {
        if (alerts != null) {
            List<AlertModel> filtered = new ArrayList<AlertModel>();
            for (AlertModel alert : alerts) {
                if (alert.getLastUpdate().compareTo(date) > 0)
                    filtered.add(alert);
            }

            return filtered;
        }

        return null;
    }

    /**
     * Current value for the global alert if one exists.
     * @return
     */
    public AlertModel getGlobalAlert() {
        return globalAlert;
    }

    @Override
    public void success(ArrayList<AlertModel> alerts, Response response) {
        this.alerts = alerts;
        sortAlerts();
        notifyListeners();
    }

    @Override
    public void failure(RetrofitError error) {
    }

    public interface IAlertListener {
        public void alertsDidUpdate();
    }

}
