package com.example.adarsh.quickliftdriver;

import android.*;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {
    GoogleMap mMap;
    GoogleApiClient gpc;
    Marker marker_pick,marker_drop;
    Polyline line;
    private LocationRequest lct;
    Button accept,reject;
    LinearLayout lin;
    Data d=new Data();
    String key;
    Location curloc;
    DatabaseReference customer=FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference db;
    private int ride=0;
    SharedPreferences log_id;
    private PlaceAutocompleteFragment autocompleteFragment;
    private Marker marker_cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServicesAvailable()) {
            Toast.makeText(this, "Perfect !", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_maps);

            accept=(Button)findViewById(R.id.accept);
            reject=(Button)findViewById(R.id.reject);
            lin=(LinearLayout)findViewById(R.id.lin);
            log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
            db=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/Info");

            autocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

            autocompleteFragment.setBoundsBias(new LatLngBounds(
                    new LatLng(12.864162, 77.438610),
                    new LatLng(13.139807, 77.711895)));
            autocompleteFragment.setHint("Location");
            autocompleteFragment.getView().setBackgroundColor(Color.parseColor("#850000ff"));
            //destn_address=(EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);
            ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.WHITE);
            ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(Color.WHITE);

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    // TODO: Get info about the selected place.
                    //Log.i(TAG, "Place: " + place.getName());
                    //Toast.makeText(Home.this, place.getName(), Toast.LENGTH_SHORT).show();
                    goToLocationZoom(place.getLatLng().latitude, place.getLatLng().longitude, 15);

                    if (marker_cur!=null){
                        marker_cur.remove();
                    }

                    MarkerOptions options = new MarkerOptions()
                            .title(place.getName().toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .position(place.getLatLng())
                            .snippet("Location");
                    marker_cur=mMap.addMarker(options);

                    curloc.setLatitude(options.getPosition().latitude);
                    curloc.setLongitude(options.getPosition().longitude);

                    goToLocationZoom(curloc.getLatitude(), curloc.getLongitude(), 15);

                    if (log_id.getString("ride",null).equals("")) {
                        String userId = log_id.getString("id",null);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(userId, new GeoLocation(curloc.getLatitude(), curloc.getLongitude()));
                    }
                    else {
                        String userId= log_id.getString("id",null);
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);

                        GeoFire geoFire=new GeoFire(ref);
                        geoFire.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));
                    }


/*
                Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin="+marker_pick.getPosition().latitude+","+marker_pick.getPosition().longitude+"&destination="+marker_drop.getPosition().latitude+","+marker_drop.getPosition().longitude+"&travelmode=driving");
                Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        startActivity(unrestrictedIntent);
                    } catch (ActivityNotFoundException innerEx) {
                        Toast.makeText(Home.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }

                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", marker_drop.getPosition().latitude, marker_drop.getPosition().longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
*/
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    // Log.i(TAG, "An error occurred: " + status);
                    Toast.makeText(MapsActivity.this, status.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ride=1;
                    /*
                    d.setAccept(1);
                    d.setD_lat(curloc.getLatitude());
                    d.setD_lng(curloc.getLongitude());
                    db.child(key).setValue(d);
                    Toast.makeText(MapsActivity.this, "Accepted !", Toast.LENGTH_SHORT).show();
                    lin.setVisibility(View.GONE);
                    findViewById(R.id.info).setVisibility(View.VISIBLE);

                    DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
                    customer.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                if (ds.getKey().equals(d.getCustomer_id())){
                                    Customer cust=ds.getValue(Customer.class);
                                    TextView name=(TextView)findViewById(R.id.name);
                                    TextView phone=(TextView)findViewById(R.id.phone);
                                    CircleImageView img=(CircleImageView)findViewById(R.id.img);

                                    name.setText(cust.getName());
                                    phone.setText(cust.getPhone());
                                    if (!cust.getThumb().equals("")) {
                                        byte[] dec = Base64.decode(cust.getThumb(), Base64.DEFAULT);
                                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                                        img.setImageBitmap(decbyte);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    */
                    lin.setVisibility(View.GONE);
                    DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
                    dref.child("resp").setValue("Accept");
                    SharedPreferences.Editor editor=log_id.edit();
                    editor.putString("ride","1");
                    editor.commit();
                    tracktripstatus();
                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ride=0;
                    /*
                    String userId= log_id.getString("id",null);
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));

                    //d.setAccept(2);
                    db.child(key).removeValue();
                    Toast.makeText(MapsActivity.this, "Rejected !", Toast.LENGTH_SHORT).show();
                    */
                    lin.setVisibility(View.GONE);

                    DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
                    dref.child("resp").setValue("Reject");
                    String userId= log_id.getString("id",null);
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));
                    marker_drop.remove();
                    marker_pick.remove();
                    erasePolylines();
                    DatabaseReference delref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);
                    delref.removeValue();
                    db.removeValue();
                }
            });

            initMap();
        } else {
            Toast.makeText(this, "Unable to load map !", Toast.LENGTH_SHORT).show();
        }



        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (log_id.getString("ride", null).equals(""))
                        lin.setVisibility(View.VISIBLE);
                    d = dataSnapshot.getValue(Data.class);

                    //Geocoder gc = new Geocoder(MapsActivity.this);

                    //List<Address> list = gc.getFromLocation(d.getEn_lat(), d.getEn_lng(), 1);
                    //Address address = list.get(0);
                    //String locality = address.getAddressLine(0);
                    if (marker_drop != null) {
                        marker_drop.remove();
                    }

                    MarkerOptions options = new MarkerOptions()
                            .title("Address")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                            .position(new LatLng(d.getEn_lat(), d.getEn_lng()))
                            .snippet("Destination");
                    marker_drop = mMap.addMarker(options);


                    ////   try {
                    //      List<Address> list = gc.getFromLocation(d.getSt_lat(), d.getSt_lng(), 1);
                    //      Address address = list.get(0);
                    //   String locality = address.getAddressLine(0);
                    double lt = d.getSt_lat();
                    double lg = d.getSt_lng();

                    goToLocationZoom(lt, lg, 15);

                    if (marker_pick != null) {
                        marker_pick.remove();
                    }

                    options = new MarkerOptions()
                            .title("Address")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .position(new LatLng(d.getSt_lat(), d.getSt_lng()))
                            .snippet("Destination");
                    marker_pick = mMap.addMarker(options);
                    //   } catch (IOException e) {
                    //      e.printStackTrace();
                    //  }

                    getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());

                    if (!log_id.getString("ride", null).equals(""))
                        tracktripstatus();

                /*
                for (DataSnapshot dt:dataSnapshot.getChildren()){
                    d=dt.getValue(Data.class);
                    if (d.getAccept()==0) {
                        key=dt.getKey();
                        lin.setVisibility(View.VISIBLE);

                        Geocoder gc = new Geocoder(MapsActivity.this);
                        try {
                            List<Address> list = gc.getFromLocation(d.getEn_lat(),d.getEn_lng(),1);
                            Address address = list.get(0);
                            String locality = address.getAddressLine(0);
                            if (marker_drop!=null){
                                marker_drop.remove();
                            }

                            MarkerOptions options = new MarkerOptions()
                                    .title(locality)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                    .position(new LatLng(d.getEn_lat(),d.getEn_lng()))
                                    .snippet("Destination");
                            marker_drop=mMap.addMarker(options);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            List<Address> list = gc.getFromLocation(d.getSt_lat(),d.getSt_lng(),1);
                            Address address = list.get(0);
                            String locality = address.getAddressLine(0);
                            double lt=d.getSt_lat();
                            double lg=d.getSt_lng();

                            goToLocationZoom(lt,lg, 15);

                            if (marker_pick!=null){
                                marker_pick.remove();
                            }

                            MarkerOptions options = new MarkerOptions()
                                    .title(locality)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    .position(new LatLng(d.getSt_lat(),d.getSt_lng()))
                                    .snippet("Destination");
                            marker_pick=mMap.addMarker(options);

                            String userId= log_id.getString("id",null);
                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+userId+"/location");

                            GeoFire geoFire=new GeoFire(ref);
                            geoFire.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (line!=null)
                            line.remove();
                        drawline();
                    }

                }*/
                }
                else {
                    erasePolylines();
                    lin.setVisibility(View.GONE);
                    if (marker_pick!=null)
                        marker_pick.remove();
                    if (marker_drop!=null)
                        marker_drop.remove();
                    String userId= log_id.getString("id",null);
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initMap() {
        MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapfragment.getMapAsync(this);
    }

    private boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to play services !", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //goToLocationZoom(39.008224,-76.8984527,15);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        gpc = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gpc.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lct = LocationRequest.create();
        lct.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //lct.setInterval(5000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(gpc, lct, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void goToLocation(double v, double v1) {
        LatLng ll = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mMap.moveCamera(update);
    }

    private void goToLocationZoom(double v, double v1, float zoom) {
        LatLng ll = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    private String getDirectionsUrl() {
        StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+marker_pick.getPosition().latitude+","+marker_pick.getPosition().longitude);
        googleDirectionsUrl.append("&destination="+marker_drop.getPosition().latitude+","+marker_drop.getPosition().longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyAicFor08br3-Jl-xwUc0bZHC2KMdcGRNo");

        return googleDirectionsUrl.toString();
    }

    private void drawline() {
        PolylineOptions options=new PolylineOptions()
                .add(marker_pick.getPosition())
                .add(marker_drop.getPosition())
                .color(Color.BLUE)
                .width(5);

        line=mMap.addPolyline(options);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location==null){
            Toast.makeText(this, "Cannot get current location !", Toast.LENGTH_SHORT).show();
        } else {
            //LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            /* CameraUpdate update=CameraUpdateFactory.newLatLngZoom(ll,15);
            mmap.animateCamera(update);
            MarkerOptions options=new MarkerOptions()
                    .title("Address")
                    .position(ll);
            mmap.addMarker(options);
*/
            curloc=location;

            Geocoder gc = new Geocoder(this);
            List<Address> list = null;
            try {
                list = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = list.get(0);
            String locality = address.getAddressLine(0);

            //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

            double lat = address.getLatitude();
            double lng = address.getLongitude();

            goToLocationZoom(lat, lng, 15);

            if (log_id.getString("ride",null).equals("")) {
                String userId = log_id.getString("id",null);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

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
    }

    @Override
    protected void onStop() {
        super.onStop();

        String userId= log_id.getString("id",null);
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userId);
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
        findViewById(R.id.canceltrip).setVisibility(View.VISIBLE);
        autocompleteFragment.getView().setVisibility(View.GONE);

        final String userId= log_id.getString("id",null);
        DatabaseReference delref=FirebaseDatabase.getInstance().getReference("DriversAvailable/"+userId);
        delref.removeValue();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);

        GeoFire geoFire=new GeoFire(ref);
        geoFire.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));

        DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status");
        GeoFire loc=new GeoFire(tripstatus);
        loc.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));

        getRouteToMarkerViaThirdPoint(new LatLng(curloc.getLatitude(),curloc.getLongitude()),marker_drop.getPosition(),marker_pick.getPosition());
        //Toast.makeText(MapsActivity.this, d.getCustomer_id(), Toast.LENGTH_SHORT).show();

        customer.child(d.getCustomer_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(MapsActivity.this, dataSnapshot.getKey().toString(), Toast.LENGTH_SHORT).show();
                findViewById(R.id.info).setVisibility(View.VISIBLE);

                Map<String,Object> cust=(Map<String, Object>) dataSnapshot.getValue();
                TextView name=(TextView)findViewById(R.id.name);
                TextView phone=(TextView)findViewById(R.id.phone);
                CircleImageView img=(CircleImageView)findViewById(R.id.img);

                name.setText(cust.get("name").toString());
                phone.setText(cust.get("phone").toString());
                if (!cust.get("thumb").toString().equals("")) {
                    byte[] dec = Base64.decode(cust.get("thumb").toString(), Base64.DEFAULT);
                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    img.setImageBitmap(decbyte);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
        tripstatus.child(log_id.getString("id",null)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    findViewById(R.id.canceltrip).setVisibility(View.GONE);
                    SharedPreferences.Editor editor=log_id.edit();
                    editor.putString("ride","");
                    editor.commit();
                    marker_drop.remove();
                    marker_pick.remove();
                    erasePolylines();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);

                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.removeLocation(userId);

                    String userId = log_id.getString("id",null);
                    DatabaseReference dref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

                    GeoFire gFire = new GeoFire(dref);
                    gFire.setLocation(userId, new GeoLocation(curloc.getLatitude(), curloc.getLongitude()));
                    findViewById(R.id.info).setVisibility(View.GONE);
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
