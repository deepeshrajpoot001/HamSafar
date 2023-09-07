package com.biet.hamsafar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.biet.hamsafar.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener {


    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location lastLocation;

    private ActivityMapsBinding binding;
    public static final int REQUEST_LOCATION_CODE = 99;
    private static final int REQUEST_CHECK_SETTINGS = 123;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d("ramram", "hi my name deepesh rajpoot1");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("ramram", "hi my name deepesh rajpoot2");
           checkLocationPermission();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        Log.d("ramram","onCreateend");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Log.d("ramram","onMapReady");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            mMap.setMyLocationEnabled(true);
        }

    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("ramram","onLocationChanged");
        lastLocation = location;
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("CurrentLocation");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);
        /*
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(this);
        }
          */

        Log.d("ramram", markerOptions.getPosition().toString());
        Log.d("ramram", location.getLatitude() + " "+location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

       //mMap.animateCamera(CameraUpdateFactory.zoomBy(20),5000,null);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(20.0F).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Log.d("ramram","checkLocationSetting");

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. You can start requesting location updates.
                startLocationUpdates();
                Log.d("ramram","checkLocationSettingSuccess");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("ramram","checkLocationSettingfails");
                int statusCode = ((ResolvableApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but they can be resolved.
                        try {
                            // Show the user a dialog to enable location settings
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied and cannot be resolved.
                        // Handle this case accordingly.
                        break;
                }
            }
        });
    }

    private void startLocationUpdates() {
        Log.d("ramram","startLocationUpdate");

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setIntervalMillis(1000)
                .setWaitForAccurateLocation(true)
                //.setMinUpdateIntervalMillis(1222)
                //.setMaxUpdateAgeMillis(12122)
                .build();
        // Request location updates with the FusedLocationProviderClient
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, this, Looper.myLooper());
            // 2 argument in above function call onLocationChanged()
            Log.d("ramram","startLocationUpdate2");
        }

    }

    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {               Log.d("ramram","changeLocationPermission1");


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                Log.d("ramram","changeLocationPermission2");
                ActivityCompat.requestPermissions(this,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                Log.d("ramram","changeLocationPermission3");
                ActivityCompat.requestPermissions(this,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else {
            checkLocationSettings();
            Log.d("ramram","changeLocationPermission4");
            return true;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // User enabled location settings
               // startLocationUpdates();
                Log.d("ramram", "onActivityResult1");
                checkLocationSettings();
            } else {
                // User canceled or declined to enable location settings
                Log.d("ramram", "onActivityResult2");
                // Handle this case accordingly.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                Log.d("ramram", "onRequestPermissionResult "+requestCode);
                Log.d("ramram", grantResults.length +"onRequestPermissionResult "+ grantResults[0]);
                Log.d("ramram", "onRequestPermissionResult "+ PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (fusedLocationProviderClient == null) {
                            Log.d("ramram", "onRequestPermissionResult1");
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                        }
                        mMap.setMyLocationEnabled(true);
                        checkLocationSettings();
                        Log.d("ramram", "onRequestPermissionResult2");
                    }
                } else {
                    Log.d("ramram", "onRequestPermissionResult3");
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }

    }

}