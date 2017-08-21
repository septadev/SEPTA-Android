package org.septa.android.app.services.apiinterfaces;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jkampf on 8/10/17.
 */

public class SeptaServiceFactory {

    static Retrofit singleton = new Retrofit.Builder().client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build())
            .baseUrl("http://www3.septa.org/").addConverterFactory(GsonConverterFactory.create())
            .build();

    static Retrofit singleton2 = new Retrofit.Builder().client(new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build())
            .baseUrl("http://apitest.septa.org/").addConverterFactory(GsonConverterFactory.create())
            .build();

    public static NextToArriveService getNextToArriveService() {
        return singleton.create(NextToArriveService.class);
    }

    public static NextArrivalService getNextArrivalService() {
        return singleton2.create(NextArrivalService.class);
    }

}
