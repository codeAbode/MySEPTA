package team5.capstone.com.mysepta.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArraySet;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team5.capstone.com.mysepta.Adapters.HomeViewAdapter;
import team5.capstone.com.mysepta.Fragment.FavoritesFragment;
import team5.capstone.com.mysepta.MainActivity;
import team5.capstone.com.mysepta.Models.FavoriteRailModel;
import team5.capstone.com.mysepta.Models.RailLocationData;
import team5.capstone.com.mysepta.Models.SubwayScheduleItemModel;
import team5.capstone.com.mysepta.R;

/**
 * Created by Andrew on 10/24/2015.
 */
public class FavoritesManager{
    private static ArrayList<SubwayScheduleItemModel> subwayFavoriteList;
    private static ArrayList<FavoriteRailModel> railFavoriteList;
    private static FavoritesManager fragmentManager = null;
    private SharedPreferences prefs;
    private static MainActivity context;
    private static RecyclerView recyclerView;

    private FavoritesManager() {
        subwayFavoriteList = new ArrayList<>();
        railFavoriteList = new ArrayList<>();
    }

    public static FavoritesManager getInstance() {
        if(fragmentManager == null) {
            fragmentManager = new FavoritesManager();
        }
        return fragmentManager;
    }

    public void setContext(MainActivity context){
        this.context = context;
        buildFromPreferences();
    }

    public void setRecyclerView(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }

    private void buildFromPreferences() {
        subwayFavoriteList = new ArrayList<>();
        railFavoriteList = new ArrayList<>();

        prefs = context.getSharedPreferences(context.getString(R.string.favorites_key), Context.MODE_PRIVATE);

        Log.d("Favorite's Manager","Preferences Init");

        Gson gson = new Gson();
        String json = prefs.getString(context.getString(R.string.subway_favorites_key), "");

        if(json.length() > 0){
            subwayFavoriteList = gson.fromJson(json, new TypeToken<ArrayList<SubwayScheduleItemModel>>(){}.getType());
        }

        Set<String> rail = prefs.getStringSet(context.getString(R.string.rail_favorites_key),new HashSet<String>());

        String[] station;
        for(String railString : rail){
            station = railString.split("-",2);
            railFavoriteList.add(new FavoriteRailModel(station[0],station[1]));
        }


        // Exists only to defeat instantiation, and testing purposes until mocking is unnecessary.
//        SubwayScheduleItemModel d = new SubwayScheduleItemModel();
//        d.setFormattedTimeStr("Subway Arrival #1");
//        subwayFavoriteList.add(d);

/*
        FavoriteRailModel f = new FavoriteRailModel("Philmont","Ambler");
        railFavoriteList.add(f);
        f = new FavoriteRailModel("test","Rail Arrival #1");
        railFavoriteList.add(f);
        f = new FavoriteRailModel("test","Rail Arrival #1");
        railFavoriteList.add(f);
        */
    }

    public ArrayList getFavoriteList(){
        ArrayList fullList = new ArrayList<>();
        fullList.addAll(subwayFavoriteList);
        fullList.addAll(railFavoriteList);
        return fullList;
    }

    public ArrayList getSubwayList(){
        return subwayFavoriteList;
    }

    public ArrayList getRailList(){
        return railFavoriteList;
    }

    public static boolean addSubwayLineToFavorites(SubwayScheduleItemModel item){
        SubwayScheduleItemModel newSubway = new SubwayScheduleItemModel();
        newSubway.setLine(item.getLine());
        newSubway.setLocation(item.getLocation());
        newSubway.setDirection(item.getDirection());
        newSubway.setStopID(item.getStopID());

        subwayFavoriteList.add(newSubway);
        recyclerView.getAdapter().notifyItemInserted(subwayFavoriteList.size() + (HomeViewAdapter.HEADER_AMOUNT - 1));
        return false;
    }

    public static boolean addRailLineToFavorites(String startStation, String endStation){
        FavoriteRailModel favoriteRailModel = new FavoriteRailModel(startStation,endStation);

        if(checkForFavoriteRailModel(favoriteRailModel) == -1){
            railFavoriteList.add(favoriteRailModel);
            recyclerView.getAdapter().notifyItemInserted(railFavoriteList.size() + HomeViewAdapter.HEADER_AMOUNT);
            return true;
        }

        return false;
    }

    public static boolean removeRailLineFromFavorites(String startStation,String endStation){
        FavoriteRailModel favoriteRailModel = new FavoriteRailModel(startStation,endStation);

        int loc = checkForFavoriteRailModel(favoriteRailModel);

        if(loc != -1){
            railFavoriteList.remove(loc);
            recyclerView.getAdapter().notifyItemRemoved(loc + subwayFavoriteList.size() + HomeViewAdapter.HEADER_AMOUNT);
            recyclerView.getAdapter().notifyItemRangeChanged(loc, subwayFavoriteList.size());
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            context.fragmentPagerAdapter.notifyDataSetChanged();
            return true;
        }

        return false;
    }

    public static boolean checkForFavoriteRailLine(String startStation,String endStation){
        FavoriteRailModel favoriteRailModel = new FavoriteRailModel(startStation,endStation);

        if(checkForFavoriteRailModel(favoriteRailModel) != -1){
            return true;
        }

        return false;
    }

    private static int checkForFavoriteRailModel(FavoriteRailModel favoriteRailModel){
        for(int i=0;i<railFavoriteList.size();i++){
            FavoriteRailModel model = railFavoriteList.get(i);
            if(model.getStartingStation().equalsIgnoreCase(favoriteRailModel.getStartingStation())
                    && model.getEndingStation().equalsIgnoreCase(favoriteRailModel.getEndingStation()))
                return i;
        }

        return -1;
    }

    public void storeSharedPreferences(){
        if(prefs == null)
            return;

        Set<String> rail = new HashSet<>();

        Gson gson = new Gson();
        String json = gson.toJson(subwayFavoriteList, new TypeToken<ArrayList<SubwayScheduleItemModel>>(){}.getType());

        for(FavoriteRailModel model : railFavoriteList){
            rail.add(model.getStartingStation() + "-" + model.getEndingStation());
        }


        prefs.edit().putString(context.getString(R.string.subway_favorites_key), json).apply();
        prefs.edit().putStringSet(context.getString(R.string.rail_favorites_key), rail).apply();
    }

}
