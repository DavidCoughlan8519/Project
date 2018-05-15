package com.google.collegepalroomadder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    LocationManager locationManager;
    LocationListener locationListener;
    Location location;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private Button listRooms;
    private TextView logOutButton;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            startListening();
        }
    }

    public void startListening()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void updateLocationInfo(Location location)
    {
        Log.i("LocationInfo",location.toString());
        TextView latText = (TextView)findViewById(R.id.latitudeTextview);
        TextView longText = (TextView)findViewById(R.id.longitudeTextView);

        latText.setText("Latitude : " + location.getLatitude());
        longText.setText("Longitude : " + location.getLongitude());

    }


    public void submitToDb(View view)
    {
        String roomName = "",roomLat = "",roomLong = "";

        TextView nameOfRoom = (TextView) findViewById(R.id.roomNameEditText);



        if(nameOfRoom.getText().toString() != null)
        {
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                //check if we have permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                //we already have permission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location != null)
                {
                    updateLocationInfo(location);
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
            Date date = new Date();
            String room = nameOfRoom.getText().toString();
            //Commit to DB here
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference myRef = database.getReference("locations/"+ getBlock(room));
            DatabaseReference floorRef =  myRef.child(getFloor(room));
            DatabaseReference roomRef = floorRef.child(room);
            roomRef.child("Latitude").setValue(location.getLatitude());
            roomRef.child("Longitude").setValue(location.getLongitude());
            roomRef.child("Date").setValue(sdf.format(date));
            Log.i("INFORMATION:","SUCCESS");
            EditText userEditText = (EditText) findViewById(R.id.roomNameEditText);
            userEditText.setText("");
            Toast toast = Toast.makeText(MainActivity.this, "Success:Sent to FireBase", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    public String getBlock(String roomName)
    {
        char letter = roomName.charAt(0);
        return  Character.toUpperCase(letter) + " Block";
    }

    public String getFloor(String roomName)
    {
        char letter = roomName.charAt(1);
        return Character.toUpperCase(letter) + " Floor";
    }

    public boolean isLink(String roomName)
    {
        boolean isLink = false;

        if(roomName.charAt(roomName.length() -1) == 'l' || roomName.charAt(roomName.length() -1) == 'L')
        {
            return true;
        }
        return isLink;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("LOGGED:", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("LOGGED:", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        logOutButton = (TextView)findViewById(R.id.textViewLogOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationInfo(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(Build.VERSION.SDK_INT < 23)
        {
            startListening();
        }else
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                //check if we have permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                //we already have permission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location != null)
                {
                    updateLocationInfo(location);
                }
            }
        }
    }
}
