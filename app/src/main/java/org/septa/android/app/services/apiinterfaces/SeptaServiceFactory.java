package org.septa.android.app.services.apiinterfaces;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import org.septa.android.app.R;
import org.septa.android.app.systemstatus.SystemStatusResultsActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SeptaServiceFactory {
    public static final String TAG = SeptaServiceFactory.class.getSimpleName();

    private static String googleKey;

    private static String amazonawsApiKey;

    private static String septaWebServicesBaseUrl;

    private static Retrofit googleSingleton;

    private static String googleApiBaseUrl;

    static Retrofit septaAmazonServicesSingleton;

    private static Favorites favoritesService = new FavoritesImpl();

    public static void init() {
        septaAmazonServicesSingleton = new Retrofit.Builder().client(new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("x-api-key", amazonawsApiKey);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        }).build())
                .baseUrl(septaWebServicesBaseUrl).addConverterFactory(GsonConverterFactory.create())
                .build();

        googleSingleton = new Retrofit.Builder().client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("key", googleKey)
                        .build();

                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        }).build())
                .baseUrl(googleApiBaseUrl).addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static NextArrivalService getNextArrivalService() {
        return septaAmazonServicesSingleton.create(NextArrivalService.class);
    }

    public static AlertDetailsService getAlertDetailsService() {
        return septaAmazonServicesSingleton.create(AlertDetailsService.class);
    }

    public static AlertsService getAlertsService() {
        return septaAmazonServicesSingleton.create(AlertsService.class);
    }


    public static GooglePlaceAutoCompleteService getAutoCompletePlaceService() {
        return googleSingleton.create(GooglePlaceAutoCompleteService.class);
    }

    public static Favorites getFavoritesService() {
        return favoritesService;
    }


    public static String getAmazonawsApiKey() {
        return amazonawsApiKey;
    }

    public static void setAmazonawsApiKey(String amazonawsApiKey) {
        SeptaServiceFactory.amazonawsApiKey = amazonawsApiKey;
    }

    public static String getGoogleApiBaseUrl() {
        return googleApiBaseUrl;
    }

    public static void setGoogleApiBaseUrl(String googleApiBaseUrl) {
        SeptaServiceFactory.googleApiBaseUrl = googleApiBaseUrl;
    }

    public static void setGoogleKey(String googleKey) {
        SeptaServiceFactory.googleKey = googleKey;
    }

    public static String getGoogleKey() {
        return googleKey;
    }

    public static String getSeptaWebServicesBaseUrl() {
        return septaWebServicesBaseUrl;
    }

    public static void setSeptaWebServicesBaseUrl(String septaWebServicesBaseUrl) {
        SeptaServiceFactory.septaWebServicesBaseUrl = septaWebServicesBaseUrl;
    }

    public static void displayWebServiceError(View rootView, final Activity activity) {
        Snackbar snackbar = Snackbar.make(rootView, R.string.realtime_failure_message, Snackbar.LENGTH_INDEFINITE);

        // redirect to schedules
        if (activity instanceof SeptaServiceFactoryCallBacks) {
            final SeptaServiceFactoryCallBacks listener = (SeptaServiceFactoryCallBacks) activity;
            snackbar.setAction(R.string.snackbar_no_connection_link_text, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.gotoSchedules();
                }
            });
        }

        // when looking at system status details, instead of redirecting to schedules just send them back
        if (activity instanceof SystemStatusResultsActivity) {
            snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    try {
                        activity.onBackPressed();
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }

        View snackbarView = snackbar.getView();
        android.widget.TextView tv = (android.widget.TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setMaxLines(10);
        snackbar.show();
    }

    public interface SeptaServiceFactoryCallBacks {
        void gotoSchedules();
    }

}
