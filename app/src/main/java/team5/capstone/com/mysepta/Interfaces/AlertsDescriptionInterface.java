package team5.capstone.com.mysepta.Interfaces;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import team5.capstone.com.mysepta.Models.AlertsDescriptionModel;

/**
 * Retrieve alert data
 * Created by Andrew on 12/2/2015.
 */
public interface AlertsDescriptionInterface {
    /**
     *
     * @param routeID parameter to specify alert
     * @param callback callback to hold alert desc data
     */
    @GET("/hackathon/Alerts/get_alert_data.php")
    void alerts (
            @Query("req1") String routeID,
            Callback<ArrayList<AlertsDescriptionModel>> callback
    );
}
