package com.google.collegepalapp;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private DatabaseReference myRef;
    private ArrayList<String> classes = new ArrayList<>();
    String[] ClassInfo = new String[4];

    private CameraPosition mCameraPosition;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 10;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Get the currently logged in user

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("LOGGED:", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("LOGGED:", "onAuthStateChanged:signed_out");
                }
            }
        };

        //Check the Intent
        Intent intent = getIntent();

        if (intent.hasExtra("classesList")) {
            classes = intent.getStringArrayListExtra("classesList");
        } else {
            String classData = intent.getStringExtra("Location Name");
            ClassInfo[0] = getClassTime(classData);
            ClassInfo[1] = getModule(classData);
            ClassInfo[2] = getLocationName(classData);
            ClassInfo[3] = getFloor(getLocationName(classData));
        }

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //----------------------------------------------------------------------------------------------
    // Methods for extracting data from the selected item from the listView of timetable entries in
    // the Main Activity.
    //----------------------------------------------------------------------------------------------

    //12:00 C134x Adv. OS & Virt.
    public String getLocationName(String selectedListItem) {
        String locationName = "";
        String[] selectedData;

        if (selectedListItem != null) {
            selectedData = selectedListItem.split(" ");
            locationName = selectedData[1];
        }
        return locationName;
    }

    public String getBlock(String roomName) {
        char letter = roomName.charAt(0);
        return Character.toUpperCase(letter) + " Block";
    }

    public String getFloor(String roomName) {
        char letter = roomName.charAt(1);
        return Character.toUpperCase(letter) + " Floor";
    }

    public String getClassTime(String selectedListItem) {
        String time = "";
        String[] selectedData;
        if (selectedListItem != null) {
            selectedData = selectedListItem.split(" ");
            time = selectedData[0];
        }
        return time;
    }

    public static String getModule(String selectedListItem) {
        String module = "";
        if (selectedListItem != null) {
            int firstSpace = selectedListItem.indexOf(" ");
            int secondSpace = selectedListItem.indexOf(" ", firstSpace + 1);
            module = selectedListItem.substring(secondSpace, selectedListItem.length());
        }
        return module;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        if (classes.size() > 0) {
            for (String scheduledClass : classes) {
                getCoordsFromFirebase(getLocationName(scheduledClass));
            }
        } else {
            String singleLoc = ClassInfo[2];
            getCoordsFromFirebase(singleLoc);
        }

        //Get the users location on start up
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();

                // Get the current location of the device and set the position of the map.
                getDeviceLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    public void getCoordsFromFirebase(final String locationName) {
        System.out.println("LOCATION: " + locationName);
        if (!locationName.isEmpty()) {
            //Get the location for the room
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference("locations/" + getBlock(locationName) + "/" + getFloor(locationName) + "/" + locationName);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("Latitude") && dataSnapshot.hasChild("Longitude")) {
                        double latitude = dataSnapshot.child("Latitude").getValue(Double.class);
                        double longitude = dataSnapshot.child("Longitude").getValue(Double.class);

                        LatLng location = new LatLng(latitude,longitude);
                        if (classes.size() > 0) {
                                for (String item : classes) {
                                    if (item.contains(locationName)) {
                                        String time = getClassTime(item);
                                        String module = getModule(item);
                                        String roomName = getLocationName(item);
                                        String floor = getFloor(roomName).substring(0, 2);
                                        putMarkerOnMap(location, time, module, roomName, floor);
                                    }
                            }
                        } else {

                            String time = ClassInfo[0];
                            String module = ClassInfo[1];
                            String roomName = ClassInfo[2];
                            String floor = ClassInfo[3].substring(0, 2);
                            putMarkerOnMap(location, time, module, roomName, floor);
                        }

                    } else {
                        System.out.println("Long or Lat were null!!!");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });
        }
    }

    public void putMarkerOnMap(LatLng destination, String classTime, String moduleName, String roomName, String Floor) {
        mMap.addMarker(new MarkerOptions()
                .position(destination)
                .anchor(0.5f, 0.5f)
                .title("Time: " + classTime)
                .snippet("Module : " + moduleName + "\n" + "Room: " + roomName + "\nFloor: " + Floor).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))).showInfoWindow();
        mMap.setInfoWindowAdapter(new CustomWindowAdapter(MapsActivity.this));
    }


    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.i("", "Current location is null. Using defaults.");
                            Log.e("", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
