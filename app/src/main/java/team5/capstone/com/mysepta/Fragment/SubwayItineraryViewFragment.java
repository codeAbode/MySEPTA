package team5.capstone.com.mysepta.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;

import team5.capstone.com.mysepta.Adapters.SubwayItineraryViewAdapter;
import team5.capstone.com.mysepta.Adapters.SubwayLocationViewAdapter;
import team5.capstone.com.mysepta.Activities.MainActivity;
import team5.capstone.com.mysepta.Models.SubwayLocationData;
import team5.capstone.com.mysepta.R;

/**
 * Fragment for subway section of home screen.
 * Created by Andrew on 9/20/2015.
 */
public class SubwayItineraryViewFragment extends Fragment implements SubwayItineraryViewAdapter.ChangeItineraryListener {
    /*DEBUG TAG*/
    private static final String TAG = "SubwayItineraryViewFragment";

    /*Keep track of the rootView to pass to the adapters*/
    private View rootView;

    /*Recycler View*/
    private RecyclerView recyclerSubwayView;
    /*The initial adapter for the Subway tab*/
    private SubwayItineraryViewAdapter subwayItineraryViewAdapter;
    private SubwayLocationViewAdapter subwayLocationViewAdapter;
    /*This is the Material Adapter that is wrapping our SubwayItineraryViewAdapter*/
    private RecyclerView.Adapter materialWrapperAdapter;

    /*Location HashMap*/
    private SubwayLocationData subLocData;

    /**
     * Statically create a new instance of this Fragment
     * @return new fragment
     */
    public static SubwayItineraryViewFragment newInstance() {
        return new SubwayItineraryViewFragment();
    }

    /**
     * Create view.
     * @param inflater layout inflater
     * @param container parent view group
     * @param savedInstanceState saved state on close
     * @return current inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subway_itinerary_view, container, false);
    }


    /**
     * Initialize view
     * @param view current item view
     * @param savedInstanceState saved state on close
     */
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        subLocData = new SubwayLocationData(rootView.getContext());
        recyclerSubwayView = (RecyclerView) rootView.findViewById(R.id.subwayItineraryView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity()); //Notice LinearLayoutManager
        recyclerSubwayView.setLayoutManager(layoutManager);
        recyclerSubwayView.setHasFixedSize(false);

        subwayItineraryViewAdapter = new SubwayItineraryViewAdapter(getActivity(), this);
        materialWrapperAdapter = new RecyclerViewMaterialAdapter(subwayItineraryViewAdapter);

        recyclerSubwayView.setAdapter(materialWrapperAdapter);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), recyclerSubwayView, null);
    }

    /*Implementation for BSL and MFL Button clicks in Subway Tab */
    @Override
    public void itineraryChosen(String line) {
        subwayLocationViewAdapter = new SubwayLocationViewAdapter(line, (MainActivity) getActivity(), subLocData, this);
        materialWrapperAdapter = new RecyclerViewMaterialAdapter(subwayLocationViewAdapter);
        recyclerSubwayView.setAdapter(materialWrapperAdapter);
    }

    /**
     * Initial and default Subway Tab View
     */
    @Override
    public void itineraryBack() {
        if(subwayLocationViewAdapter != null){

            recyclerSubwayView = (RecyclerView) rootView.findViewById(R.id.subwayItineraryView);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity()); //Notice LinearLayoutManager
            recyclerSubwayView.setLayoutManager(layoutManager);
            recyclerSubwayView.setHasFixedSize(false);

            materialWrapperAdapter = new RecyclerViewMaterialAdapter(subwayItineraryViewAdapter);
            recyclerSubwayView.setAdapter(materialWrapperAdapter);
        }
    }

}
