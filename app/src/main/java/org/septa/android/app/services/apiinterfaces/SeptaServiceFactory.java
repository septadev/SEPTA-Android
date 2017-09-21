package org.septa.android.app.services.apiinterfaces;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.septa.android.app.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jkampf on 8/10/17.
 */

public class SeptaServiceFactory {
    public static final String TAG = SeptaServiceFactory.class.getSimpleName();

    private static String googleKey;

    private static String amazonawsApiKey;

    private static Retrofit googleSingleton = new Retrofit.Builder().client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).addInterceptor(new Interceptor() {
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
            .baseUrl("https://maps.googleapis.com/").addConverterFactory(GsonConverterFactory.create())
            .build();

    static Retrofit singleton = new Retrofit.Builder().client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build())
            .baseUrl("http://www3.septa.org/").addConverterFactory(GsonConverterFactory.create())
            .build();

    static Retrofit singleton2 = new Retrofit.Builder().client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .addHeader("x-api-key", amazonawsApiKey);

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }).build())
            .baseUrl("https://vnjb5kvq2b.execute-api.us-east-1.amazonaws.com").addConverterFactory(GsonConverterFactory.create())
            .build();


    public static NextArrivalService getNextArrivalService() {
        return singleton2.create(NextArrivalService.class);
    }

    public static AlertDetailsService getAlertDetailsService() {
        return singleton2.create(AlertDetailsService.class);
    }

    public static AlertsService getAlertsService() {
        return singleton2.create(AlertsService.class);
    }


    public static GooglePlaceAutoCompleteService getAutoCompletePlaceService() {
        return googleSingleton.create(GooglePlaceAutoCompleteService.class);
    }

    public static Favorites getFavoritesService() {
        return new FavoritesImpl();
    }


    public static String getAmazonawsApiKey() {
        return amazonawsApiKey;
    }

    public static void setAmazonawsApiKey(String amazonawsApiKey) {
        SeptaServiceFactory.amazonawsApiKey = amazonawsApiKey;
    }

    public static void setGoogleKey(String googleKey) {
        SeptaServiceFactory.googleKey = googleKey;
    }

    public static String getGoogleKey() {
        return googleKey;
    }


}
