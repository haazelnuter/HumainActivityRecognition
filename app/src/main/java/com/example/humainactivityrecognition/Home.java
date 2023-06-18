package com.example.humainactivityrecognition;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.PendingIntent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback {
    private DBHelper dbHelper;

    private Polyline polyline;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView activity,distance1,speed1;
    private Location previousLocation;
    private float totalDistance;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mStepCounter;

    private List<Float> accelerometerValues;
    private List<Float> gyroscopeValues;
    private int stepCount;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // use ActionBar utility methods
        ActionBar actionBar = getSupportActionBar();
        // providing title for the ActionBar
        actionBar.setTitle("Home");
        dbHelper = new DBHelper(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        activity = findViewById(R.id.activity);
        distance1 = findViewById(R.id.distance);
        speed1 = findViewById(R.id.speed);


        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        accelerometerValues = new ArrayList<>();
        gyroscopeValues = new ArrayList<>();
        stepCount = 0;

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        // Create a notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("activity_channel", "Activity Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        startLocationUpdates();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        stopLocationUpdates();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                Toast.makeText(getApplicationContext(), "Profile", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent1);
                return true;

            case R.id.historique:
                Toast.makeText(getApplicationContext(), "Historical", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(getApplicationContext(), Historique.class);
                startActivity(intent2);
                return true;
            case R.id.logout:
                Toast.makeText(getApplicationContext(), "Log Out", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            accelerometerValues.add(acceleration);
        } else if (event.sensor == mGyroscope) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float rotation = (float) Math.sqrt(x * x + y * y + z * z);

            gyroscopeValues.add(rotation);
        } else if (event.sensor == mStepCounter) {
            // Increment step count
            stepCount = (int) event.values[0];
        }

        // Update activity based on the latest sensor values
        updateActivity(getHighestAcceleration(), getHighestRotation(), stepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private float getHighestAcceleration() {
        float highestAcceleration = 0;

        for (float acceleration : accelerometerValues) {
            if (acceleration > highestAcceleration) {
                highestAcceleration = acceleration;
            }
        }

        return highestAcceleration;
    }

    private float getHighestRotation() {
        float highestRotation = 0;

        for (float rotation : gyroscopeValues) {
            if (rotation > highestRotation) {
                highestRotation = rotation;
            }
        }

        return highestRotation;
    }

    private long lastCleaningTime = 0;

    private void updateActivity(float acceleration, float rotation, int stepCount) {
        String activityText;

        // Clean sensor values every 20 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleaningTime >= 20000) {
            Toast.makeText(getApplicationContext(), "Activity Updated", Toast.LENGTH_SHORT).show();
            accelerometerValues.clear();
            gyroscopeValues.clear();
            lastCleaningTime = currentTime;
        }

        if (acceleration > 15 && rotation > 5) {
            activityText = "Running";
        } else if (acceleration > 10 && rotation > 2) {
            activityText = "Walking";
        } else if (acceleration > 5 && rotation > 1) {
            activityText = "Standing";
        } else {
            activityText = "Sitting";
        }

        activity.setText(activityText);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        polyline = mMap.addPolyline(new PolylineOptions()
                .width(5f)
                .color(Color.RED));

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            requestLocationPermission();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update your location on the map or perform other operations
                    updateLocationOnMap(location);
                }
            }
        };
    }
    private static final float MIN_MOVEMENT_THRESHOLD = 0.3f; // Adjust this value as needed

    private List<LatLng> polylinePoints = new ArrayList<>(); // Declare as a member variable
    // Declare the previousActivity variable as a class member variable
    private String previousActivity = "";

    private void updateLocationOnMap(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear(); // Clear previous markers
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
        totalDistance = 0;

        // Inside the method where you update the activity
        String currentActivity = activity.getText().toString();

        if (!currentActivity.equalsIgnoreCase(previousActivity)) {
            sendNotification(currentActivity);
            String email = user.getEmail();
            String date = getCurrentDate();
            String time = getCurrentTime();
            dbHelper.insertActivityLog(email, currentActivity, date, time);
           // Toast.makeText(getApplicationContext(), "Send to dbHelper", Toast.LENGTH_SHORT).show();
        }

        previousActivity = currentActivity;

        if (currentActivity.equalsIgnoreCase("Sitting") || currentActivity.equalsIgnoreCase("Standing")) {
            float speed = 0;
            distance1.setText(speed + " meters");
            speed1.setText(speed + " m/s");

            // Clear the polyline when sitting or standing
            polylinePoints.clear();
        } else {
            if (previousLocation != null) {
                float distance = previousLocation.distanceTo(location);
                if (distance >= MIN_MOVEMENT_THRESHOLD) {
                    totalDistance += distance;
                    distance1.setText(String.format("%.2f meters", totalDistance));

                    long timeElapsed = location.getTime() - previousLocation.getTime();
                    float speed = (distance / timeElapsed) * 1000; // Convert to meters per second

                    speed1.setText(String.format("%.2f m/s", speed));

                    // Capture the LatLng coordinates
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    polylinePoints.add(currentLatLng);

                    // Update the polyline on the map
                    polyline.setPoints(polylinePoints);
                }
            }
            previousLocation = location;
        }
    }





    private void sendNotification(String activityName) {
        // Create an intent to open the app when the notification is clicked
        Intent intent = new Intent(this, Home.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE); // Use FLAG_IMMUTABLE

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "activity_channel")
                .setContentTitle("TAP TAP ACTIVITY UPDATED")
                .setContentText("Current activity: " + activityName)
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }



    ///////////////////////////////////////////////
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date currentTime = new Date();
        return timeFormat.format(currentTime);
    }






}
