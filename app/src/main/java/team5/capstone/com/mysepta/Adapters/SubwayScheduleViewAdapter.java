package team5.capstone.com.mysepta.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import team5.capstone.com.mysepta.Models.SubwayLocationData;
import team5.capstone.com.mysepta.R;
import team5.capstone.com.mysepta.SubwayActivity;

/**
 * Created by Andrew on 9/20/2015.
 */
public class SubwayScheduleViewAdapter extends RecyclerView.Adapter<SubwayScheduleViewAdapter.SubwayScheduleHolder> {
    private static final String TAG = "SubwayScheduleViewAdapter";
    private static final String STOP_ID_KEY = "StopID";

    private Context context;
    private String[] listLocations;
    private SubwayLocationData subwayLocationData;

    public SubwayScheduleViewAdapter(String line, Context context, SubwayLocationData subwayLocationData){
        if(line.equalsIgnoreCase("MFL")){
            listLocations = context.getResources().getStringArray(R.array.market_frankford_line_sorted);
        }else if(line.equalsIgnoreCase("BSL")){
            listLocations = context.getResources().getStringArray(R.array.broad_street_line_sorted);
        }else{
            Log.d(TAG, "Messed up mere");
        }
        this.subwayLocationData = subwayLocationData;
        this.context = context;
    }


    @Override
    public SubwayScheduleViewAdapter.SubwayScheduleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subway_list_item_card, parent, false);
        return new SubwayScheduleHolder(view);
    }

    @Override
    public void onBindViewHolder(SubwayScheduleHolder holder, final int position) {
        holder.locationText.setText(listLocations[position]);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stopID = subwayLocationData.getStopId(listLocations[position], "default");
                Intent startSubwayActivity = new Intent(context, SubwayActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(STOP_ID_KEY, stopID);
                bundle.putString("direction", "default");
                bundle.putString("location", listLocations[position]);
                startSubwayActivity.putExtras(bundle);
                context.startActivity(startSubwayActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listLocations.length;
    }

    public class SubwayScheduleHolder extends RecyclerView.ViewHolder{
        TextView locationText;
        CardView cardView;

        public SubwayScheduleHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.subway_schedule_item_card);
            locationText = (TextView) itemView.findViewById(R.id.nextStopText);
        }
    }
}