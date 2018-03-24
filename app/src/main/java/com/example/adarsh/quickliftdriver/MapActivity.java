package com.example.adarsh.quickliftdriver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.plus.model.people.Person;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

//import android.widget.Toast;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        RoutingListener,
        View.OnClickListener{

    private GoogleMap mMap;
    private static Location location;
    public static final int RequestPermissionCode = 1;
    private GoogleApiClient googleApiClient;
    private double latitude,longitude;
    private static final int REQUEST_CHECK_SETTINGS = 199;
    DatabaseReference customer=FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference db;
    SharedPreferences log_id,loc_pref,ride_info;
    Marker marker_pick,marker_drop;
    Address address;
    LatLng curr_loc,dest_loc,pick_loc;
    private static RelativeLayout pickup,locate,start_trip,drop,end_trip,cancel;
    private static TextView pick_name,pick_address,locate_name,drop_location;
    private static Button locate_btn,start_trip_btn,end_trip_btn,cancel_btn;
    private static ImageButton pick_nav,drop_nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        loc_pref = getSharedPreferences("location",Context.MODE_PRIVATE);
        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);

        pickup = (RelativeLayout)findViewById(R.id.customer_pickup);
        pickup.setVisibility(View.VISIBLE);
        locate = (RelativeLayout)findViewById(R.id.customer_locate);
        locate.setVisibility(View.GONE);
        start_trip = (RelativeLayout)findViewById(R.id.start_trip);
        start_trip.setVisibility(View.GONE);
        drop = (RelativeLayout)findViewById(R.id.customer_drop);
        drop.setVisibility(View.GONE);
        end_trip = (RelativeLayout)findViewById(R.id.end_trip);
        end_trip.setVisibility(View.GONE);
        cancel = (RelativeLayout)findViewById(R.id.cancel);
        cancel.setVisibility(View.VISIBLE);

        pick_name = (TextView)findViewById(R.id.c_pick_name);
        pick_address = (TextView)findViewById(R.id.c_pick_address);
//        locate_name = (TextView)findViewById(R.id.c_locate_name);
        drop_location = (TextView)findViewById(R.id.c_drop_address);

        locate_btn = (Button)findViewById(R.id.locate_button);
        locate_btn.setOnClickListener(this);
        start_trip_btn = (Button)findViewById(R.id.start_trip_btn);
        start_trip_btn.setOnClickListener(this);
        end_trip_btn = (Button)findViewById(R.id.end_trip_btn);
        end_trip_btn.setOnClickListener(this);
        cancel_btn = (Button)findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(this);

        pick_nav = (ImageButton)findViewById(R.id.pick_navigation);
        pick_nav.setOnClickListener(this);
        drop_nav = (ImageButton)findViewById(R.id.drop_navigation);
        drop_nav.setOnClickListener(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/Info");

        tracktripstatus();
    }

    private void getCurrentLocation() {
//        Toast.makeText(this, "Getting Current Location", Toast.LENGTH_SHORT).show();
        Log.i("TAG","Getting Current Location");
//        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//        Toast.makeText(this, "Current Location : "+location, Toast.LENGTH_SHORT).show();
        Log.i("TAG","Current Location : "+location);
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            SupportMapFragment mapFragment = mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }else {
//            Toast.makeText(this, "Current Location is null", Toast.LENGTH_SHORT).show();
            Log.i("TAG","Current Location is null");

            LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            if(!gps_enabled && !network_enabled) {
                displayLocationSettingsRequest(getApplicationContext());
            }
        }
    }

    private void moveMap() {

//        Toast.makeText(this, "Moving map to location : "+latitude+", "+longitude, Toast.LENGTH_SHORT).show();
//        Log.i("TAG","Moving map to location : "+latitude+", "+longitude);

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude,longitude))
                .draggable(true)
                .title("current Location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if (log_id.getString("ride",null).equals("")) {
            Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
            String userId = log_id.getString("id",null);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
//            ref.push().setValue("hello");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
        }
        else {
            String userId= log_id.getString("id",null);
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);

            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        Toast.makeText(this, "Map Connected", Toast.LENGTH_SHORT).show();

         getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Toast.makeText(this, "Map Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAddress(Double.parseDouble(ride_info.getString("st_lat",null)),Double.parseDouble(ride_info.getString("st_lng",null)));
        pick_name.setText(ride_info.getString("name",null));
        pick_address.setText(new String(address.getFeatureName() + "\n" + address.getLocality() +"\n" + address.getAdminArea() + "\n" + address.getCountryName()));
        getAddress(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
        drop_location.setText(new String(address.getFeatureName() + "\n" + address.getLocality() +"\n" + address.getAdminArea() + "\n" + address.getCountryName()));
        getCurrentLocation();
        curr_loc = new LatLng(latitude,longitude);
        pick_loc = new LatLng(Double.parseDouble(ride_info.getString("st_lat",null)),Double.parseDouble(ride_info.getString("st_lng",null)));
        dest_loc = new LatLng(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
        //getRouteToMarker(curr_loc,pick_loc);
        getRouteToMarkerViaThirdPoint(curr_loc,pick_loc,dest_loc);
        if (latitude == Double.parseDouble(ride_info.getString("st_lat",null)) && longitude == Double.parseDouble(ride_info.getString("st_lng",null))){
            pickup.setVisibility(View.GONE);
            locate.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null){
            mMap.clear();
        }
        Log.i("TAG","onMapReady() method start ");
        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
        if (checkPermission()){
            mMap.setMyLocationEnabled(true);
        }else {
            requestPermission();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
        Marker marker = mMap.addMarker(new MarkerOptions()
                .title("current Location")
                .position(userLocation)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        marker.showInfoWindow();
        Log.i("TAG","onMapReady() method completed");


        moveMap();
//        LatLng latLng = new LatLng(Double.parseDouble(ride_info.getString("st_lat",null)), Double.parseDouble(ride_info.getString("st_lng",null)));
//        addMarkers(latLng);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null){
            this.location = location;
            moveMap();
        }else {
            getCurrentLocation();
            moveMap();
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MapActivity.this, new String[]
                {
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,


                }, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean BluetoothPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean BluetoothAdminPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    //           boolean SystemAlertPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && WriteStoragePermission && BluetoothPermission && BluetoothAdminPermission) {

                        //Toast.makeText(AnimationActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
//                        Toast.makeText(MapActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,Welcome.class);
        startActivity(intent);
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("TAG", "All location settings are satisfied.");
//                        startActivity(new Intent(MapActivity.this,MapActivity.class));
//                        finish();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("TAG", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private List<Polyline> polylines=new ArrayList<>();

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        //for (int i = 0; i <route.size(); i++) {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(7);
        polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);

        //time.setText(String.valueOf(route.get(shortestRouteIndex).getDurationValue()/60));
        //Toast.makeText(getApplicationContext(),String.valueOf(shortestRouteIndex)+"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        //}

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRouteToMarker(LatLng pickupLatLng, LatLng destnLatLng) {
        if (pickupLatLng != null && destnLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(destnLatLng, pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void getRouteToMarkerViaThirdPoint(LatLng pickupLatLng, LatLng destnLatLng, LatLng waypoint) {
        if (pickupLatLng != null && destnLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(destnLatLng, waypoint,pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

    private void tracktripstatus() {
        getCurrentLocation();

        final String userId= log_id.getString("id",null);
        DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status");
        tripstatus.child(log_id.getString("id",null)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
//                    findViewById(R.id.canceltrip).setVisibility(View.GONE);
                    SharedPreferences.Editor editor=log_id.edit();
                    editor.putString("ride","");
                    editor.commit();
//                    marker_drop.remove();
//                    marker_pick.remove();
                    erasePolylines();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);

                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.removeLocation(userId);

                    String userId = log_id.getString("id",null);
                    DatabaseReference dref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

                    GeoFire gFire = new GeoFire(dref);
                    gFire.setLocation(userId, new GeoLocation(latitude, longitude));
//                    findViewById(R.id.info).setVisibility(View.GONE);
                    Toast.makeText(MapActivity.this, "Ride canceled bu customer", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MapActivity.this,Welcome.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void cancel_trip(View v){
        findViewById(R.id.canceltrip).setVisibility(View.GONE);
        DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
        tripstatus.removeValue();
        DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
        resp.removeValue();
        DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        cus.removeValue();
        SharedPreferences.Editor editor=log_id.edit();
        editor.putString("ride","");
        editor.commit();
    }

    public void getAddress(Double latitude,Double longitude){
        try{
            Geocoder geo = new Geocoder(MapActivity.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                Toast.makeText(this, "Waiting for Location", Toast.LENGTH_SHORT).show();
            }
            else {
                if (addresses.size() > 0) {
                    for (int i = 0;i<addresses.size();i++){
                        Log.d("TAG",i+"th Address Result"+addresses.get(i).getFeatureName() + "," + addresses.get(i).getLocality() +", " + addresses.get(i).getAdminArea() + ", " + addresses.get(i).getCountryName());
                    }
                    address = addresses.get(0);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null)+"/resp");
        int id = v.getId();
        if (id == locate_btn.getId()){
            ref.setValue("Located");
            locate.setVisibility(View.GONE);
            pickup.setVisibility(View.GONE);
            start_trip.setVisibility(View.VISIBLE);
        }else if(id == start_trip_btn.getId()){
            ref.setValue("Trip Started");
            startActivityForResult(new Intent(this,OTPActivity.class),2);
        }else if(id == pick_nav.getId()){
            getAddress(Double.parseDouble(ride_info.getString("st_lat",null)),Double.parseDouble(ride_info.getString("st_lng",null)));
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q="+ride_info.getString("st_lat",null)+","+ride_info.getString("st_lng",null)));
            startActivityForResult(intent,1);
        }else if (id == drop_nav.getId()){
            getAddress(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q="+ride_info.getString("en_lat",null)+","+ride_info.getString("en_lng",null)));
            startActivityForResult(intent,3);
        }else if (id == end_trip_btn.getId()){
            DatabaseReference rides = FirebaseDatabase.getInstance().getReference("Rides");
            DatabaseReference LastRide = FirebaseDatabase.getInstance().getReference("LastRide");
            getAddress(Double.parseDouble(ride_info.getString("st_lat",null)),Double.parseDouble(ride_info.getString("st_lng",null)));
            String source = address.toString();
            getAddress(Double.parseDouble(ride_info.getString("en_lat",null)),Double.parseDouble(ride_info.getString("en_lng",null)));
            String destination = address.toString();
            HashMap<String,Object> map= new HashMap<>();

            map.put("amount",ride_info.getString("price",null));
            map.put("customerid",ride_info.getString("customer_id",null));
            map.put("destination",ride_info.getString("destination",null));
            map.put("driver",log_id.getString("id",null));
            map.put("source",ride_info.getString("source",null));
            map.put("time",new Date().toString());

            String key = rides.push().getKey();
            rides.child(key).setValue(map);
            ref.setValue("Trip Ended");

            HashMap<String,Object> last_ride = new HashMap<>();
            last_ride.put("date",(new Date()).toString());
            last_ride.put("destination",ride_info.getString("destination",null));
            last_ride.put("driver",log_id.getString("id",null));
            last_ride.put("lat",ride_info.getString("en_lat",null));
            last_ride.put("lng",ride_info.getString("en_lng",null));
            last_ride.put("rideid",key);
            last_ride.put("status","");

            LastRide.push().setValue(last_ride);
            
            startActivity(new Intent(this,FeedbackActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                pickup.setVisibility(View.GONE);
                locate.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Pick up arrived", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                pickup.setVisibility(View.VISIBLE);
                locate.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Pickup is not yet Arrived", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                start_trip.setVisibility(View.GONE);
                drop.setVisibility(View.VISIBLE);
                Toast.makeText(this, "OTP entered", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "OTP is not yet entered", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == 3){
            if (resultCode == Activity.RESULT_OK){
                startActivity(new Intent(this,FeedbackActivity.class));
                finish();
                Toast.makeText(this, "Feedback completed", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                drop.setVisibility(View.VISIBLE);
                end_trip.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Feedback not yet entered", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addMarkers(LatLng latLng){
        erasePolylines();
        mMap.clear();
        getCurrentLocation();
        LatLng currLoc = new LatLng(latitude,longitude);
        mMap.addMarker(new MarkerOptions()
                .title("Current Location")
                .position(currLoc)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}