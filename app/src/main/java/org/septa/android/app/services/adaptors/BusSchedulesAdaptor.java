package org.septa.android.app.services.adaptors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.septa.android.app.models.servicemodels.BusScheduleModel;
import org.septa.android.app.models.servicemodels.BusSchedulesModel;
import org.septa.android.app.services.ServiceErrorHandler;
import org.septa.android.app.services.apiinterfaces.BusSchedulesService;

import java.lang.reflect.Type;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class BusSchedulesAdaptor {
    public static final String TAG = BusSchedulesAdaptor.class.getName();

    public static BusSchedulesService getBusSchedulesService(String routeShortName) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(BusSchedulesModel.class, new BusSchedulesModelDeserializer(routeShortName))
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://www3.septa.org")       // The base API endpoint.
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new ServiceErrorHandler())
                .build();

        return restAdapter.create(BusSchedulesService.class);
    }
}

class BusSchedulesModelDeserializer implements JsonDeserializer<BusSchedulesModel>
{
    String routeShortName;

    public BusSchedulesModelDeserializer(String routeShortName) {
        //hack for bad data from service
        if(routeShortName.equals("BSL")){
            this.routeShortName = "BSS";
        } else {
            this.routeShortName = routeShortName;
        }

    }

    @Override
    public BusSchedulesModel deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException
    {
        BusSchedulesModel busSchedulesModel = new BusSchedulesModel();

        JsonObject jsonObject = je.getAsJsonObject();

        Gson gson = new Gson();
        for (final JsonElement busScheduleModelAsJson: jsonObject.getAsJsonArray(routeShortName)) {
            BusScheduleModel busScheduleModel = gson.fromJson(busScheduleModelAsJson, new TypeToken<BusScheduleModel>(){}.getType());

            busSchedulesModel.addBusScheduleModel(busScheduleModel);
        }

        return busSchedulesModel;
    }
}