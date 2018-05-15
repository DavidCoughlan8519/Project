package com.google.collegepalapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //Fire base
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private DatabaseReference myRef;
    private DatabaseReference checkRef;
    private TextView settingsText;
    //For granting permission for location
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    //Adapter for list view and for processing classes and notifications
    private ArrayAdapter<String> itemsAdapter;
    private ArrayList<String> classes = new ArrayList<>();
    Map<String, Integer> mapClass = new HashMap<String, Integer>();
    boolean hasTimeTable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // on start up check if permissions granted for location
        if(!mLocationPermissionGranted){
            getLocationPermission();
        }

        //Get the currently logged in user
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = mAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("LOGGED:", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("LOGGED:", "onAuthStateChanged:signed_out");
                }
            }
        };

        settingsText = (TextView)findViewById(R.id.textViewSettings);

        settingsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        user = mAuth.getCurrentUser();
        //check if the user has a timetable written to their database
        //gets the schedule if they do and writes a message for the user if they don't
        checkIfHasTimeTable(user.getUid().toString());

        populateTimeToClasses();

        registerNotifications();

        //get the list view for displaying the classes
        ListView listView = (ListView) findViewById(R.id.listView);

        //put information from classes into list-adapter to be displayed
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, classes);
        //Display it to the user
        listView.setAdapter(itemsAdapter);
        //if they have a timetable let them click on a class or the show all button.
        //and if they have permissions granted

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    String locationName = classes.get(position);
                    //passes this to the next activity
                    intent.putExtra("Location Name", locationName);
                    startActivity(intent);
                }
            });

            Button btnShowAll = (Button) findViewById(R.id.btnShowAll);
            btnShowAll.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putStringArrayListExtra("classesList", (ArrayList<String>) classes);
                    startActivity(intent);
                }
            });
    }

    //---------------------------------------------------------------------------------------------
    // For setting the check whether they have classes in their timetable or not
    //---------------------------------------------------------------------------------------------
    public void setHasTimeTable(boolean hasTimeTable) {
        this.hasTimeTable = hasTimeTable;
    }

    public boolean getHasTimeTable() {
        return hasTimeTable;
    }

    //---------------------------------------------------------------------------------------------
    // Checks if the user has a Timetable in their database.
    //---------------------------------------------------------------------------------------------
    public void checkIfHasTimeTable(String uid)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Check if the user has a timetable or not
        checkRef = database.getReference("users/" + uid);
        checkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("timetable")) {
                    setHasTimeTable(true);
                    getUsersSchedule(user.getUid().toString());
                }
                else{
                    classes.add("NO CLASSES TODAY");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //---------------------------------------------------------------------------------------------
    // Register the notification for the classes
    //---------------------------------------------------------------------------------------------
    public void registerNotifications()
    {
        for(Map.Entry<String, Integer> entry: mapClass.entrySet())
        {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            //The class has not passed yet
            if(entry.getValue() > 0)
            {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Intent notificationIntent = new Intent(this, AlarmReceiver.class);
                PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Calendar cal = Calendar.getInstance();
                //set the delay to the amount of seconds before class
                cal.add(Calendar.SECOND, entry.getValue() - 300); // Five minutes before class
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);

                Intent classIntent = new Intent(this,AlarmReceiver.class);
                classIntent.putExtra("ClassAboutToStart",entry.getKey());
                startActivity(classIntent);
            }
        }
    }

    //---------------------------------------------------------------------------------------------
    // For getting the current users timetable
    //---------------------------------------------------------------------------------------------
    public void getUsersSchedule(String uid){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users/" + uid + "/timetable");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> items = dataSnapshot.getChildren();
                String entry = "";
                for(DataSnapshot snap : items)
                {
                    entry+=snap.getKey() +" ";
                    for(DataSnapshot item:snap.getChildren()){
                        entry+=item.getValue()+" ";
                    }
                    addToClassesList(entry);
                    itemsAdapter.notifyDataSetChanged();
                    entry = "";
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //---------------------------------------------------------------------------------------------
    // For adding to the list from within inner classes
    //---------------------------------------------------------------------------------------------
    public void addToClassesList(String data)
    {
        classes.add(data);
    }

    public String getLocationName(String selectedListItem){
        String locationName = "";
        String[] selectedData;

        if(selectedListItem != null)
        {
            selectedData = selectedListItem.split(" ");
            locationName = selectedData[1];
        }
        return locationName;
    }
    //---------------------------------------------------------------------------------------------
    // Extracts the time from teh information gotten from fire base
    //---------------------------------------------------------------------------------------------
    public static String getClassTime(String selectedListItem) {
        String time = "";
        String[] selectedData;
        // 15:00 B212 Data Driven Microservices
        if (selectedListItem != null) {
            selectedData = selectedListItem.split(" ");
            time = selectedData[0];
        }
        return time;
    }
    //---------------------------------------------------------------------------------------------
    // Writes the information about the classes as a string and the amount of time away from now to a map.
    //---------------------------------------------------------------------------------------------
    public Map<String, Integer> populateTimeToClasses()
    {
        for(String item: classes)
        {
            String timeOfClass = getClassTime(item);
            int timeToClass = timeUntilClass(timeOfClass);
            mapClass.put(item, timeToClass);
        }

        return mapClass;
    }

    //---------------------------------------------------------------------------------------------
    // Calculates the time of the class from now in seconds.
    //---------------------------------------------------------------------------------------------
    public int timeUntilClass(String classTime)
    {
        int seconds = 0;
        Date nowDate = null;
        Date classDate  = null;

        //formatter
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date now = new Date();
        String strDateNow = sdf.format(now);
        String classTimeDate = makeClassTimeDate(classTime);
        System.out.println(strDateNow);
        System.out.println(classTimeDate);
        try {
            nowDate = sdf.parse(strDateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            classDate = sdf.parse(classTimeDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(nowDate.before(classDate)){
            long secondsToClass = (classDate.getTime()-nowDate.getTime())/1000;
            seconds = (int)secondsToClass;
        }
        return seconds;
    }

    //---------------------------------------------------------------------------------------------
    // Puts he time of the class in a string object HH:MM ->  dd:MM:yyyy:HH:mm
    //---------------------------------------------------------------------------------------------
    public String makeClassTimeDate(String classTime)
    {
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        date = date + " " + classTime + ":00";
        return date;
    }

    //---------------------------------------------------------------------------------------------
    // Requests permission to use FINE LOCATION from the user
    //---------------------------------------------------------------------------------------------
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
}
