package team5.capstone.com.mysepta.Fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import team5.capstone.com.mysepta.Adapters.AlertsAdapter;
import team5.capstone.com.mysepta.Models.AlertsModel;
import team5.capstone.com.mysepta.R;

/**
 * Displays General Alerts
 */
public class GenAlertsFragment extends Fragment {

    private RecyclerView alertsRecyclerView;
    private View view;
    public ArrayList<AlertsModel> generalList;

    private AlertsAdapter alertsAdapter;

    public GenAlertsFragment() {}

    /**
     * Constructor
     * @param generalList arraylist of general alerts
     */
    @SuppressLint("ValidFragment")
    public GenAlertsFragment(ArrayList<AlertsModel> generalList){
        this.generalList = generalList;
    }

    /**
     * Creates view for General Alerts
     * @param inflater inflates the view and formats the data
     * @param container holds the view
     * @param savedInstanceState saved state on close
     * @return view
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_alerts, container, false);

        alertsRecyclerView = (RecyclerView) view.findViewById(R.id.alertsRecyclerView);
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        //generalList = new ArrayList<>();
        alertsAdapter = new AlertsAdapter(generalList, view.getContext());
        alertsRecyclerView.setAdapter(alertsAdapter);

        return view;
    }
}
