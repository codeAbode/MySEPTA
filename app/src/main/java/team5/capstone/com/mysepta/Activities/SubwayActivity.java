package team5.capstone.com.mysepta.Activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Scene;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import team5.capstone.com.mysepta.Fragment.SubwayArrivalFragment;
import team5.capstone.com.mysepta.Helpers.SubwayScheduleCreatorDbHelper;
import team5.capstone.com.mysepta.Dialogs.SubwayTimePickerFragment;
import team5.capstone.com.mysepta.Managers.FavoritesManager;
import team5.capstone.com.mysepta.Models.SubwayScheduleItemModel;
import team5.capstone.com.mysepta.R;

public class SubwayActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, OnMapReadyCallback {
    private static final String TAG = "SubwayActivity";

    private static final String SQL_SELECT = "SELECT ";
    private static final String SQL_FROM = " FROM ";
    private static final String SQL_SEMI_COLON = ";";
    private static final String SQL_QUOTE = "\"";
    private final static int ANIMATION_TOP = 0;
    private final static int ANIMATION_BOTTOM = 1;

    private static String direction;
    private static String location;
    private static int stopID;
    private static String line;

    private Toolbar toolbar;
    private SQLiteDatabase database;
    private SubwayScheduleCreatorDbHelper dbHelper;
    private Date pickedDate;
    private boolean customTime = false;
    private Menu mOptionsMenu;

    private boolean favorite;

    private Scene sceneSchedules;
    private Scene sceneLineMap;
    private FrameLayout swappingFragment;
    private List<View> viewsToAnimate = new ArrayList<>();



    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    private MapFragment mapFragment;
    private boolean mapIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subway);
        initSetup();

        createFragmentForArrivals(true);

        setUpButtons();
    }


    /**
     * createFragmentForArrivals
     * @param transition parent container
     */
    private void createFragmentForArrivals(boolean transition) {
        ArrayList<SubwayScheduleItemModel> arrivals = retrieveDatabaseInfo();

        String json = new Gson().toJson(arrivals);

        SubwayArrivalFragment arrivalFragment = new SubwayArrivalFragment();
        if(transition){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                setSlideAnimation(arrivalFragment, ANIMATION_TOP);
            }
        }
        Bundle args = new Bundle();
        args.putString(getString(R.string.SUBWAY_DIRECTION_KEY), direction);
        args.putString(getString(R.string.SUBWAY_ARRIVALS_LIST), json);

        arrivalFragment.setArguments(args);
        arrivalFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(R.id.swappingFragment, arrivalFragment, getString(R.string.sub_arrival_frag_tag)).commit();
    }


    private void initSetup(){
        Bundle bundle = this.getIntent().getExtras();
        stopID = bundle.getInt(getString(R.string.STOP_ID_KEY));
        location = bundle.getString(getString(R.string.LOCATION_KEY));
        direction = bundle.getString(getString(R.string.DIRECTION_KEY));
        line = bundle.getString(getString(R.string.LINE_KEY));

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //ENABLE BACK BUTTON
        getSupportActionBar().setTitle("");

        setupHeaderText();

        /**
         * This is a temporary bug fix for a crash, column name in database is incorrect
         */
        if(location.equalsIgnoreCase("34th St")){
            location = "34th";
        }else if(location.equalsIgnoreCase("56th St")){
            location = "56nd St";
        }

        dbHelper = new SubwayScheduleCreatorDbHelper(this);


    }

    private void setUpButtons() {
        swappingFragment = (FrameLayout) findViewById(R.id.swappingFragment);

        View buttonLocation = findViewById(R.id.schedulesButton);
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swappingFragment.setVisibility(View.INVISIBLE);
                mapIsOpen = false;
                CardView card = (CardView) findViewById(R.id.subwayHeader);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    animateRevealColorFromCoordinates(card, R.color.broadStreetOrange, card.getWidth() / 2, 0);
                }
                createFragmentForArrivals(true);
                getFragmentManager().executePendingTransactions();
                swappingFragment.setVisibility(View.VISIBLE);
            }
        });

        View buttonLine = findViewById(R.id.lineMapButton);
        final OnMapReadyCallback callback = this;
        buttonLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swappingFragment.setVisibility(View.INVISIBLE);
                mapIsOpen = true;
                CardView card = (CardView) findViewById(R.id.subwayHeader);

                MapFragment f = (MapFragment) getFragmentManager().findFragmentByTag(getString(R.string.sub_line_map_tag));

                if (f != null) {
                    getFragmentManager().beginTransaction().replace(R.id.swappingFragment, f, getString(R.string.sub_line_map_tag)).commit();
                    getFragmentManager().executePendingTransactions();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        animateRevealColorFromCoordinates(card, R.color.marketFrankfordBlue, card.getWidth() / 2, 0);
                    }
                    swappingFragment.setVisibility(View.VISIBLE);
                } else {
                    mapFragment = new MapFragment();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        setSlideAnimation(mapFragment, ANIMATION_BOTTOM);
                    }
                    mapFragment.setAllowEnterTransitionOverlap(true);
                    mapFragment.setAllowReturnTransitionOverlap(true);
                    mapFragment.getMapAsync(callback);
                    getFragmentManager().beginTransaction().replace(R.id.swappingFragment, mapFragment, getString(R.string.sub_arrival_frag_tag)).commit();
                    getFragmentManager().executePendingTransactions();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        animateRevealColorFromCoordinates(card, R.color.marketFrankfordBlue, card.getWidth() / 2, 0);
                    }
                    swappingFragment.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setSlideAnimation(Fragment fragment, int type) {
        Slide slideTransition;
        if(type == ANIMATION_TOP){
            slideTransition = new Slide(Gravity.TOP);
        }else{
            slideTransition = new Slide(Gravity.BOTTOM);
        }
        slideTransition.setDuration(getResources().getInteger(R.integer.anim_duration_medium));
        fragment.setEnterTransition(slideTransition);
        fragment.setAllowEnterTransitionOverlap(true);
        fragment.setAllowReturnTransitionOverlap(true);
    }

    private void setupHeaderText() {

        CardView cardView = (CardView) findViewById(R.id.subwayHeader);
        if(line.equalsIgnoreCase("MFL")){
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.marketFrankfordBlue));
        }else{
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.broadStreetOrange));
        }

        TextView headerText = (TextView) findViewById(R.id.locationHeaderText);
        headerText.setText(location);
        headerText.setTextColor(Color.WHITE);

    }

    /**
     * retrieveDatabaseInfo
     * @return ArrayList
     */
    private ArrayList retrieveDatabaseInfo() {
        StringBuilder completeSQL = new StringBuilder();
        completeSQL.append(SQL_SELECT);
        completeSQL.append(SQL_QUOTE);
        completeSQL.append(location);
        completeSQL.append(SQL_QUOTE);
        completeSQL.append(SQL_FROM);
        completeSQL.append(chooseTable());
        completeSQL.append(SQL_SEMI_COLON);

        Log.d(TAG, completeSQL.toString());

        open();

        Cursor c = database.rawQuery(completeSQL.toString(), null);
        ArrayList<SubwayScheduleItemModel> arrivals = new ArrayList<>();
        ArrayList<SubwayScheduleItemModel> arrivalsAfterMidnight = new ArrayList<>();

        SimpleDateFormat oldFormat = new SimpleDateFormat("hh:mma");
        SimpleDateFormat newFormat = new SimpleDateFormat("hh:mm aa");
        String midnight = "12:00 am";

        Calendar cal = Calendar.getInstance();
        Calendar calCompare = Calendar.getInstance();
        boolean timePM = false;

        try {
            if (c.getCount() > 0)
            {
                c.moveToFirst();
                do {
                    Date midnightTime = newFormat.parse(midnight);

                    String arrival = c.getString(c.getColumnIndex(location));
                    if(!arrival.equalsIgnoreCase("X")){
                        Date oldTime = oldFormat.parse(arrival.trim());
                        String arrivalNewFormat = newFormat.format(oldTime);
                        Date arrivalDate = newFormat.parse(arrivalNewFormat);

                        calCompare.setTime(arrivalDate);

                        SubwayScheduleItemModel newArrival = new SubwayScheduleItemModel();
                        newArrival.setFormattedTimeStr(arrivalNewFormat);
                        newArrival.setLine(line);
                        newArrival.setTimeObject(arrivalDate);

                        if(!timePM){
                            if(calCompare.get(Calendar.AM_PM) == Calendar.PM){
                                timePM = true;
                            }
                        }
                        if(timePM && calCompare.get(Calendar.AM_PM) == Calendar.AM){
                            arrivalsAfterMidnight.add(newArrival);
                        }else{
                            arrivals.add(newArrival);
                        }
                    }
                } while (c.moveToNext());
                c.close();
            }

            close();

            Collections.sort(arrivals);
            ArrayList<SubwayScheduleItemModel> arrivalTemp = new ArrayList<>(arrivals);

            for(SubwayScheduleItemModel item: arrivalTemp){

                Date time = newFormat.parse(item.getFormattedTimeStr());
                String currentTimeString = newFormat.format(cal.getTime());

                if(customTime){
                    currentTimeString = newFormat.format(pickedDate.getTime());
                }

                Date currentTime = newFormat.parse(currentTimeString);

//                Log.d(TAG, "Current time: " + currentTimeString);
//                Log.d(TAG, "Item time: " + item.getFormattedTimeStr());
//                Log.d(TAG, "Difference: " + time.before(currentTime));
                if(time.before(currentTime)){
                    arrivals.remove(item);
                }else{
                    break;
                }
            }
            arrivals.addAll(arrivalsAfterMidnight);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return arrivals;
    }

    public void open() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * chooseTable for database
     * @return String
     */

    private String chooseTable() {
//        ((SubwayArrivalFragment) getFragmentManager().findFragmentById(R.id.arrivalsFragment)).changeDirectionText(direction + "BOUND");
        String tableName = "";
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        if(line.equalsIgnoreCase("MFL")){
            switch(dayOfWeek){
                case Calendar.SATURDAY:
                    if(direction.equalsIgnoreCase("EAST")){
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_MFL_SAT_EASTBOUND;
                    }else{
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_MFL_SAT_WESTBOUND;
                    }
                    break;
                case Calendar.SUNDAY:
                    if(direction.equalsIgnoreCase("EAST")){
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_MFL_SAT_EASTBOUND;
                    }else{
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_MFL_SAT_WESTBOUND;
                    }
                    break;
                default:
                    if(direction.equalsIgnoreCase("EAST")){
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_MFL_WEEKDAY_EASTBOUND;
                    }else{
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_MFL_WEEKDAY_WESTBOUND;
                    }
                    break;
            }

        }else if(line.equalsIgnoreCase("BSL")){
            switch(dayOfWeek){
                case Calendar.SATURDAY:
                    if(direction.equalsIgnoreCase("NORTH")){
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_BSL_SAT_NORTHBOUND;
                    }else{
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_BSL_SAT_SOUTHBOUND;
                    }
                    break;
                case Calendar.SUNDAY:
                    if(direction.equalsIgnoreCase("NORTH")){
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_BSL_SUN_NORTHBOUND;
                    }else{
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_BSL_SUN_SOUTHBOUND;
                    }
                    break;
                default:
                    if(direction.equalsIgnoreCase("NORTH")){
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_BSL_WEEKDAY_NORTHBOUND;
                    }else{
                        tableName = SubwayScheduleCreatorDbHelper.TABLE_BSL_WEEKDAY_SOUTHBOUND;
                    }
                    break;
            }
        }
        return tableName;
    }

    private void changeDirection() {
        if(direction.equalsIgnoreCase("NORTH")){
            direction = "SOUTH";
        }else if(direction.equalsIgnoreCase("SOUTH")){
            direction = "NORTH";
        }else if(direction.equalsIgnoreCase("EAST")){
            direction = "WEST";
        }else if(direction.equalsIgnoreCase("WEST")){
            direction = "EAST";
        }

        ((SubwayArrivalFragment) getFragmentManager().findFragmentById(R.id.swappingFragment)).changeDirectionText(direction);

        ArrayList arrivals = retrieveDatabaseInfo();
        ((SubwayArrivalFragment) getFragmentManager().findFragmentById(R.id.swappingFragment)).switchArrivalsList(arrivals);
    }

    private void launchTimePicker() {
        DialogFragment newFragment = new SubwayTimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mOptionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subway, menu);

        checkFavorite();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.change_direction:
                if(!mapIsOpen){
                    changeDirection();
                }
                return true;
            case R.id.timePicker:
                if(!mapIsOpen){
                    launchTimePicker();
                }
                return true;
            case R.id.favoriteIcon:
                if(favorite){
                    removeLineFromFavorites();
                }else{
                    addLineToFavorites();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Create fragment view
     * @param viewRoot View
     * @param color int
     * @param x int
     * @param y int
     * @return Animator
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Animator animateRevealColorFromCoordinates(View viewRoot, @ColorRes int color, int x, int y) {
        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        viewRoot.setBackgroundColor(ContextCompat.getColor(this, color));
        anim.setDuration(getResources().getInteger(R.integer.anim_duration_long));
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        return anim;
    }

    private void removeLineFromFavorites() {
        FavoritesManager.removeSubwayLineFromFavorites(line, location);
        mOptionsMenu.findItem(R.id.favoriteIcon).setIcon(R.drawable.star_outline);
        favorite = false;
        Toast.makeText(SubwayActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
    }

    private void addLineToFavorites() {
        SubwayScheduleItemModel favSubModel = new SubwayScheduleItemModel();
        favSubModel.setLocation(location);
        favSubModel.setDirection(direction);
        favSubModel.setStopID(String.valueOf(stopID));
        favSubModel.setLine(line);
        FavoritesManager.addSubwayLineToFavorites(favSubModel);
        mOptionsMenu.findItem(R.id.favoriteIcon).setIcon(R.drawable.star_icon);
        favorite = true;
        Toast.makeText(SubwayActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
    }

    private void checkFavorite(){
        if(FavoritesManager.checkForFavoriteSubwayLine(line, location)){
            mOptionsMenu.findItem(R.id.favoriteIcon).setIcon(R.drawable.star_icon);
            favorite=true;
        }else{
            mOptionsMenu.findItem(R.id.favoriteIcon).setIcon(R.drawable.star_outline);
            favorite=false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FavoritesManager favoritesManager = FavoritesManager.getInstance();
        favoritesManager.storeSharedPreferences();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, "Time : " + hourOfDay + " " + minute);
        customTime = true;
        int hour = hourOfDay;
        String zero = "";
        String a = "AM";
        if(hourOfDay > 12){
            hour = hourOfDay%12;
            a = "PM";
        }else if(hour < 10){
            hour = 5;
            zero = "0";
        }

        StringBuilder time = new StringBuilder();
        time.append(zero);
        time.append(String.valueOf(hour));
        time.append(":");
        time.append(String.valueOf(minute));
        time.append(a);

        SimpleDateFormat format = new SimpleDateFormat("hh:mma");
        try {
            pickedDate = format.parse(time.toString());
            Log.d(TAG, "Time chose : "+time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<SubwayScheduleItemModel> arrivals = retrieveDatabaseInfo();
        ((SubwayArrivalFragment) getFragmentManager().findFragmentById(R.id.swappingFragment)).switchArrivalsList(arrivals);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        setZoomAndGPSLocation();
    }

    private void setZoomAndGPSLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //Currently this if statement is causing issues
        // will address at a later time
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MapsActivity.this, "GPS Permission Needed", Toast.LENGTH_SHORT).show();
//            return;
//        }
        Geocoder coder = new Geocoder(this);
        List<Address> addresses;
        Address address;
        try {
            String googleString;
            if(line.equalsIgnoreCase("MFL") && !location.contains("Center")){
                googleString = location + " Station - MFL, Philadelphia, PA";
            }else if(line.equalsIgnoreCase("BSL") && !location.contains("Center")){
                googleString = location + " Station - BSL, Philadelphia, PA";
            }else{
                googleString = location + ", Philadelphia, PA";
            }
            Log.d(TAG, googleString);
            addresses = coder.getFromLocationName(googleString, 2);
            address = addresses.get(0);
            address.getLatitude();
            address.getLongitude();

            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16)
                    .bearing(0)
                    .tilt(0)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(location));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e){
            Toast.makeText(SubwayActivity.this, "Issue loading subway station location. Try pressing Location Map again", Toast.LENGTH_LONG).show();
            LatLng latLng = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16)
                    .bearing(0)
                    .tilt(0)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        googleMap.setMyLocationEnabled(true);
    }
}
