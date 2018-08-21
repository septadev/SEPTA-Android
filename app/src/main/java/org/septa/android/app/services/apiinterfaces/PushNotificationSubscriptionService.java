package org.septa.android.app.services.apiinterfaces;

import org.septa.android.app.services.apiinterfaces.model.PushNotifSubscriptionRequest;
import org.septa.android.app.services.apiinterfaces.model.PushNotifSubscriptionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PushNotificationSubscriptionService {

    @POST("/prod/pushnotification/subscription")
    Call<PushNotifSubscriptionResponse> setNotificationSubscription(@Body PushNotifSubscriptionRequest request);

}