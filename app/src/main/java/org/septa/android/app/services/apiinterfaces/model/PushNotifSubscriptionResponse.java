package org.septa.android.app.services.apiinterfaces.model;

import java.io.Serializable;

public class PushNotifSubscriptionResponse implements Serializable {

    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "PushNotifSubscriptionResponse{" +
                "success=" + success +
                '}';
    }
}
