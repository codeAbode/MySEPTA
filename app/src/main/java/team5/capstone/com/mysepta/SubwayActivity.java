package team5.capstone.com.mysepta;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Property;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import team5.capstone.com.mysepta.Adapters.SubwayScheduleViewAdapter;
import team5.capstone.com.mysepta.DatabaseHelpers.SubwayScheduleCreatorDbHelper;
import team5.capstone.com.mysepta.Dialogs.SubwayTimePickerFragment;
import team5.capstone.com.mysepta.Managers.FavoritesManager;
import team5.capstone.com.mysepta.Models.SubwayScheduleItemModel;

public class SubwayActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{
    private static final String TAG = "SubwayActivity";

    private static final String STOP_ID_KEY = "StopID";
    private static final String LOCATION_KEY = "Location";
    private static final String DIRECTION_KEY = "Direction";
    private static final String LINE_KEY = "Line";
    private static final String SQL_SELECT = "SELECT ";
    private static final String SQL_FROM = " FROM ";
    private static final String SQL_SEMI_COLON = ";";
    private static final String SQL_QUOTE = "\"";

    private static String direction;
    private static String location;
    private static int stopID;
    private static String line;

    private TextView text;
    private Toolbar toolbar;
    private RecyclerView recyclerScheduleView;
    private SubwayScheduleViewAdapter subwayScheduleViewAdapter;
    private SQLiteDatabase database;
    private SubwayScheduleCreatorDbHelper dbHelper;
    private Date pickedDate;
    private boolean customTime = false;
    private Menu mOptionsMenu;
    private TextView directionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subway);
        initSetup();
        ArrayList arrivals = retrieveDatabaseInfo();
        setUpRecyclerView(arrivals);
    }


    private void initSetup(){
        Bundle bundle = this.getIntent().getExtras();
        stopID = bundle.getInt(STOP_ID_KEY);
        location = bundle.getString(LOCATION_KEY);
        direction = bundle.getString(DIRECTION_KEY);
        line = bundle.getString(LINE_KEY);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //ENABLE BACK BUTTON
        getSupportActionBar().setTitle("");

        animateHeaderText();

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

    private void animateHeaderText() {
        CardView cardView = (CardView) findViewById(R.id.subwayHeader);
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.headerBlue));
        TextView headerText = (TextView) findViewById(R.id.locationHeaderText);
        headerText.setText(location);
        headerText.setTextColor(Color.GRAY);

        final Property<TextView, Integer> property = new Property<TextView, Integer>(int.class, "textColor") {
            @Override
            public Integer get(TextView object) {
                return object.getCurrentTextColor();
            }

            @Override
            public void set(TextView object, Integer value) {
                object.setTextColor(value);
            }
        };

        final ObjectAnimator animator = ObjectAnimator.ofInt(headerText, property, Color.WHITE);
        animator.setStartDelay(800L);
        animator.setDuration(333L);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.start();
    }

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
        SimpleDateFormat oldFormat = new SimpleDateFormat("hh:mma");
        int i = 0;

        try {
            if (c.getCount() > 0)
            {
                c.moveToFirst();
                do {

                    String arrival = c.getString(c.getColumnIndex(location));
                    if(!arrival.equalsIgnoreCase("X")){
                        oldFormat = new SimpleDateFormat("hh:mma");
                        Date time = oldFormat.parse(arrival.trim());
                        oldFormat.applyPattern("hh:mm a");
                        String arrivalNewFormat = oldFormat.format(time);
                        if(arrivalNewFormat.startsWith("0")){
                            arrivalNewFormat = arrivalNewFormat.substring(1, arrivalNewFormat.length());
                        }

                        SubwayScheduleItemModel newArrival = new SubwayScheduleItemModel();
                        newArrival.setFormattedTimeStr(arrivalNewFormat);

                        arrivals.add(newArrival);

                        i++;
                    }
                } while (c.moveToNext());
                c.close();
            }

            close();

            Calendar cal = Calendar.getInstance();
            ArrayList<SubwayScheduleItemModel> arrivalTemp = (ArrayList<SubwayScheduleItemModel>) arrivals.clone();
            for(SubwayScheduleItemModel item: arrivalTemp){
                Date time = oldFormat.parse(item.getFormattedTimeStr());
                String currentTimeString = oldFormat.format(cal.getTime());

                if(customTime){
                    currentTimeString = oldFormat.format(pickedDate.getTime());
                }

                Date currentTime = oldFormat.parse(currentTimeString);
                if(time.before(currentTime)){
                    arrivals.remove(item);
                }else{
                    break;
                }
                System.out.println("Current time: " + currentTimeString);
                System.out.println("Item time: "+item.getFormattedTimeStr());
                System.out.println("Difference: "+time.before(currentTime));
            }

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

    private void setUpRecyclerView(ArrayList<SubwayScheduleItemModel> arrivals) {
        recyclerScheduleView = (RecyclerView) findViewById(R.id.subwayScheduleView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerScheduleView.setLayoutManager(layoutManager);

        subwayScheduleViewAdapter = new SubwayScheduleViewAdapter(arrivals);

        recyclerScheduleView.setAdapter(subwayScheduleViewAdapter);

    }

    private String chooseTable() {
        directionText = (TextView) findViewById(R.id.direction);
        directionText.setText(direction+"BOUND");
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
        ObjectAnimator animation = ObjectAnimator.ofFloat(directionText, "rotationX", 0.0f, 360f);
        animation.setDuration(800);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();
        if(direction.equalsIgnoreCase("NORTH")){
            direction = "SOUTH";
        }else if(direction.equalsIgnoreCase("SOUTH")){
            direction = "NORTH";
        }else if(direction.equalsIgnoreCase("EAST")){
            direction = "WEST";
        }else if(direction.equalsIgnoreCase("WEST")){
            direction = "EAST";
        }

        ArrayList arrivals = retrieveDatabaseInfo();
        subwayScheduleViewAdapter = new SubwayScheduleViewAdapter(arrivals);
        recyclerScheduleView.swapAdapter(subwayScheduleViewAdapter, true);


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
                changeDirection();
                return true;
            case R.id.timePicker:
                launchTimePicker();
                return true;
            case R.id.favoriteIcon:
                addLineToFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void addLineToFavorites() {
        FavoritesManager.addSubwayLineToFavorites(location);
        mOptionsMenu.findItem(R.id.favoriteIcon).setIcon(R.drawable.star_icon);
        Toast.makeText(SubwayActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, "Time : "+hourOfDay+" "+minute);
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
        time.append(zero+String.valueOf(hour));
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
        ArrayList arrivals = retrieveDatabaseInfo();
        subwayScheduleViewAdapter = new SubwayScheduleViewAdapter(arrivals);
        recyclerScheduleView.swapAdapter(subwayScheduleViewAdapter, true);
    }
}
