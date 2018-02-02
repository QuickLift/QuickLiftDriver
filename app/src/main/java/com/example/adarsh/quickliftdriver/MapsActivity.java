package com.example.adarsh.quickliftdriver;

import android.*;
import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
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
    DatabaseReference db=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+FirebaseAuth.getInstance().getCurrentUser().getUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServicesAvailable()) {
            Toast.makeText(this, "Perfect !", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_maps);

            accept=(Button)findViewById(R.id.accept);
            reject=(Button)findViewById(R.id.reject);
            lin=(LinearLayout)findViewById(R.id.lin);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    d.setAccept(1);
                    db.child(key).setValue(d);
                    Toast.makeText(MapsActivity.this, "Accepted !", Toast.LENGTH_SHORT).show();
                    lin.setVisibility(View.GONE);
                    findViewById(R.id.info).setVisibility(View.VISIBLE);

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
                }
            });

            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(curloc.getLatitude(),curloc.getLongitude()));

                    //d.setAccept(2);
                    db.child(key).removeValue();
                    Toast.makeText(MapsActivity.this, "Rejected !", Toast.LENGTH_SHORT).show();
                    lin.setVisibility(View.GONE);
                }
            });

            initMap();
        } else {
            Toast.makeText(this, "Unable to load map !", Toast.LENGTH_SHORT).show();
        }



        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (line!=null)
                            line.remove();
                        drawline();
                    }
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
        //lct.setInterval(60000);

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

            String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userId);
    }
}
