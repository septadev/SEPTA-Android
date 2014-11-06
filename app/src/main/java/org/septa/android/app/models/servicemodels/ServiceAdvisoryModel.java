/**
 * Created by acampbell on 11/3/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models.servicemodels;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServiceAdvisoryModel implements Parcelable {
    public static final String TAG = ServiceAdvisoryModel.class.getName();

    @SerializedName("route_id") private String routeId;
    @SerializedName("route_name") private String routeName;
    @SerializedName("currentMessage") private String currentMessage;
    @SerializedName("advisory_message") private String advisoryMessage;
    @SerializedName("detour_message") private String detourMessage;
    @SerializedName("detour_start_location") private String detourStartLocation;
    @SerializedName("detour_start_date_time") private Date detourStartDateTime;
    @SerializedName("detour_end_date_time") private Date detourEndDateTime;
    @SerializedName("detour_reason") private String detourReason;
    @SerializedName("last_updated") private Date lastUpdated;

    public ServiceAdvisoryModel() {
        this.routeId = null;
        this.routeName = "Empty";
        this.currentMessage = "Empty";
        this.advisoryMessage = "Empty";
        this.detourMessage = "Empty";
        this.detourStartLocation = "Empty";
        this.detourStartDateTime = new Date();
        this.detourEndDateTime = new Date();
        this.detourReason = "Empty";
        this.lastUpdated = new Date();
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public String getAdvisoryMessage() {
        return advisoryMessage;
    }

    public String getDetourMessage() {
        return detourMessage;
    }

    public String getDetourStartLocation() {
        return detourStartLocation;
    }

    public Date getDetourStartDateTime() {
        return detourStartDateTime;
    }

    public Date getDetourEndDateTime() {
        return detourEndDateTime;
    }

    public String getDetourReason() {
        return detourReason;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public static boolean hasValidDetours(List<ServiceAdvisoryModel> alerts) {
        Date now = new Date();
        for(ServiceAdvisoryModel alert : alerts) {
            if (now.after(alert.getDetourStartDateTime()) && now.before(alert.getDetourEndDateTime())
                    && !TextUtils.isEmpty(alert.getDetourMessage())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasValidAdvisory(List<ServiceAdvisoryModel> alerts) {
        Date now = new Date();
        for(ServiceAdvisoryModel alert : alerts) {
            if (now.after(alert.getDetourStartDateTime()) && now.before(alert.getDetourEndDateTime())
                    && !TextUtils.isEmpty(alert.getAdvisoryMessage())) {
                return true;
            }
        }
        return false;
    }

    public static String getAdvisoryMessage(List<ServiceAdvisoryModel> alerts) {
        for(ServiceAdvisoryModel alert : alerts) {
            if (alert.getAdvisoryMessage() != null) {
                return alert.getAdvisoryMessage();
            }
        }
        return "";
    }

    public static ArrayList<ServiceAdvisoryModel> getDetours(List<ServiceAdvisoryModel> alerts) {
        Date now = new Date();
        ArrayList<ServiceAdvisoryModel> filteredAlerts = new ArrayList<ServiceAdvisoryModel>();
        for(ServiceAdvisoryModel alert : alerts) {
            if (now.after(alert.getDetourStartDateTime()) && now.before(alert.getDetourEndDateTime())
                    && !TextUtils.isEmpty(alert.getDetourMessage())) {
                filteredAlerts.add(alert);
            }
        }

        return filteredAlerts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.routeId);
        dest.writeString(this.routeName);
        dest.writeString(this.currentMessage);
        dest.writeString(this.advisoryMessage);
        dest.writeString(this.detourMessage);
        dest.writeString(this.detourStartLocation);
        dest.writeLong(detourStartDateTime != null ? detourStartDateTime.getTime() : -1);
        dest.writeLong(detourEndDateTime != null ? detourEndDateTime.getTime() : -1);
        dest.writeString(this.detourReason);
        dest.writeLong(lastUpdated != null ? lastUpdated.getTime() : -1);
    }

    private ServiceAdvisoryModel(Parcel in) {
        this.routeId = in.readString();
        this.routeName = in.readString();
        this.currentMessage = in.readString();
        this.advisoryMessage = in.readString();
        this.detourMessage = in.readString();
        this.detourStartLocation = in.readString();
        long tmpDetourStartDateTime = in.readLong();
        this.detourStartDateTime = tmpDetourStartDateTime == -1 ? null : new Date(tmpDetourStartDateTime);
        long tmpDetourEndDateTime = in.readLong();
        this.detourEndDateTime = tmpDetourEndDateTime == -1 ? null : new Date(tmpDetourEndDateTime);
        this.detourReason = in.readString();
        long tmpLastUpdated = in.readLong();
        this.lastUpdated = tmpLastUpdated == -1 ? null : new Date(tmpLastUpdated);
    }

    public static final Parcelable.Creator<ServiceAdvisoryModel> CREATOR = new Parcelable.Creator<ServiceAdvisoryModel>() {
        public ServiceAdvisoryModel createFromParcel(Parcel source) {
            return new ServiceAdvisoryModel(source);
        }

        public ServiceAdvisoryModel[] newArray(int size) {
            return new ServiceAdvisoryModel[size];
        }
    };
}
