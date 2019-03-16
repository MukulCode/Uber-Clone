package com.example.uberclone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button btnRequestCar;
    private Button btnBeep;

    private boolean isUberCancelled = true;

    private boolean isCarReady = false;
    private Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCar = findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(this);

        btnBeep = findViewById(R.id.btnBeepBeep);

        btnBeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDriverUpdates();
            }
        });


        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null) {

                    isUberCancelled = false;
                    btnRequestCar.setText("Cancel your uber request!");

                    getDriverUpdates();
                }
            }
        });


        findViewById(R.id.btnLogoutFromPassengerActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            finish();

                        }
                    }
                });
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    updateCameraPassengerLocation(location);


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

            if (Build.VERSION.SDK_INT < 23) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            } else if (Build.VERSION.SDK_INT >= 23) {

                if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(PassengerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);


                } else {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateCameraPassengerLocation(currentPassengerLocation);


                }
            }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);

            }
        }

    }

    private void updateCameraPassengerLocation(Location pLocation) {


        if (isCarReady == false) {
            LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));

            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!!!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        }
    }

    @Override
    public void onClick(View view) {

        if (isUberCancelled) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (passengerCurrentLocation != null) {

                ParseObject requestCar = new ParseObject("RequestCar");
                requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());
                requestCar.put("passengerLocation", userLocation);

                requestCar.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e == null) {

                            Toast.makeText(PassengerActivity.this, "A car request is sent", Toast.LENGTH_SHORT).show();

                            btnRequestCar.setText("Cancel your uber order");
                            isUberCancelled = false;




                        }
                    }
                });

            } else {

                Toast.makeText(this, "Unknown Error. Something went wrong!!!", Toast.LENGTH_SHORT).show();

            }
        }
        } else {


            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> requestList, ParseException e) {

                    if (requestList.size() > 0 && e == null) {

                        isUberCancelled = true;
                        btnRequestCar.setText("Request a new uber");

                        for (ParseObject uberRequest : requestList) {

                            uberRequest.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e == null) {
                                        Toast.makeText(PassengerActivity.this, "Request/s deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }
                }
            });

        }

    }

    private void getDriverUpdates() {


         t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                ParseQuery<ParseObject> uberRequestQuery = ParseQuery.getQuery("RequestCar");
                uberRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                uberRequestQuery.whereEqualTo("requestAccepted", true);
                uberRequestQuery.whereExists("driverOfMe");

                uberRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {

                        if (objects.size() > 0 && e == null) {

                            isCarReady = true;
                            for (final ParseObject requestObject : objects) {

                                ParseQuery<ParseUser> driverQuery = ParseUser.getQuery();
                                driverQuery.whereEqualTo("username", requestObject.getString("driverOfMe"));
                                driverQuery.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> drivers, ParseException e) {
                                        if (drivers.size() > 0 && e == null) {

                                            for (ParseUser driverOfRequest : drivers) {

                                                ParseGeoPoint driverOfRequestLocation = driverOfRequest.getParseGeoPoint("driverLocation");
                                                if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    Location passengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                    ParseGeoPoint pLocationAsParseGeoPoint = new ParseGeoPoint(passengerLocation.getLatitude(), passengerLocation.getLongitude());

                                                    double milesDistance = driverOfRequestLocation.distanceInMilesTo(pLocationAsParseGeoPoint);

                                                    if (milesDistance < 0.3) {


                                                        requestObject.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {

                                                                    Toast.makeText(PassengerActivity.this, "Your Uber is ready! Hurray", Toast.LENGTH_LONG).show();
                                                                    isCarReady = false;
                                                                    isUberCancelled = true;
                                                                    btnRequestCar.setText("You can order a new uber now!");
                                                                }
                                                            }
                                                        });

                                                    } else {

                                                        float roundedDistance = Math.round(milesDistance * 10) / 10;
                                                        Toast.makeText(PassengerActivity.this, requestObject.getString("driverOfMe") + " is " + roundedDistance + " miles away from you!- Please wait!!!", Toast.LENGTH_LONG).show();


                                                        LatLng dLocation = new LatLng(driverOfRequestLocation.getLatitude(),
                                                                driverOfRequestLocation.getLongitude());


                                                        LatLng pLocation = new LatLng(pLocationAsParseGeoPoint.getLatitude(), pLocationAsParseGeoPoint.getLongitude());

                                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
                                                        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation));

                                                        ArrayList<Marker> myMarkers = new ArrayList<>();
                                                        myMarkers.add(driverMarker);
                                                        myMarkers.add(passengerMarker);

                                                        for (Marker marker : myMarkers) {

                                                            builder.include(marker.getPosition());

                                                        }

                                                        LatLngBounds bounds = builder.build();

                                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
                                                        mMap.animateCamera(cameraUpdate);
                                                    }

                                                }

                                            }

                                        }
                                    }
                                });



                            }
                        } else {
                            isCarReady = false;
                        }
                    }
                });

            }

        }, 0, 3000);



    }

}
