package team5.capstone.com.mysepta.Services;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.util.Date;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import team5.capstone.com.mysepta.Interfaces.AlertsInterface;
import team5.capstone.com.mysepta.Interfaces.NextToArriveRailInterface;

/**
 * Created by Matt on 11/9/2015.
 */
public class AlertsService {

    public static AlertsInterface getAlertsInterface() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://www3.septa.org")
                .setConverter(new GsonConverter(gson))
                .setErrorHandler(new ServiceErrorHandler())
                .build();

        return restAdapter.create(AlertsInterface.class);
    }
}
