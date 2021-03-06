package com.example.adarsh.quickliftdriver.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.adarsh.quickliftdriver.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static Button confirm,cancel;
    private static SharedPreferences preferences,log_id;
    private static SharedPreferences.Editor editor;
    private static TextView user_name,user_add;
    DatabaseReference customerReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        preferences = getSharedPreferences("ride_info",MODE_PRIVATE);
        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        customerReq= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));

        confirm = (Button)findViewById(R.id.confirm_btn);
        cancel = (Button)findViewById(R.id.cancel_btn);
        user_name = (TextView)findViewById(R.id.user_name);
        user_add = (TextView)findViewById(R.id.user_add);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent confirmIntent = new Intent(RequestActivity.this, TripHandlerActivity.class);
                confirmIntent.putExtra("value","confirm");
                startActivity(confirmIntent);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cancelIntent = new Intent(RequestActivity.this, TripHandlerActivity.class);
                cancelIntent.putExtra("value","cancel");
                startActivity(cancelIntent);
                finish();
            }
        });

        customerReq.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey().equalsIgnoreCase(preferences.getString("customer_id",null))){
                    finish();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(Double.parseDouble(preferences.getString("st_lat",null)), Double.parseDouble(preferences.getString("st_lng",null)));
        mMap.addMarker(new MarkerOptions().position(sydney).title("Pick Up"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    protected void onStart() {
        super.onStart();
        user_name.setText(preferences.getString("name",null));
        user_add.setText(preferences.getString("source",null));
    }

}
