package com.example.adarsh.quickliftdriver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.adarsh.quickliftdriver.DAO.DatabaseHelper;
import com.example.adarsh.quickliftdriver.Util.GPSTracker;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class TripHandlerActivity extends AppCompatActivity {

    DatabaseReference db;
    SharedPreferences log_id;
    SharedPreferences.Editor editor;
    GPSTracker gps;
    double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_handler);

        log_id = getSharedPreferences("Login", MODE_PRIVATE);
        editor = log_id.edit();
        db=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/Info");

        gps = new GPSTracker(this);

        Toast.makeText(this, "Trip Handler Activity", Toast.LENGTH_SHORT).show();
        Intent extras = getIntent();
        String value = extras.getStringExtra("value");
        Toast.makeText(this, "value1 = " + value, Toast.LENGTH_SHORT).show();
        if (value.equalsIgnoreCase("cancel")) {
            cancelTrip();
        } else if (value.equalsIgnoreCase("confirm")) {
            confirmTrip();
        }
    }

    public void cancelTrip() {
        if (gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Toast.makeText(this, "Trip Canceled", Toast.LENGTH_SHORT).show();
            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
            dref.child("resp").setValue("Reject");
            String userId= log_id.getString("id",null);
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(latitude,longitude));

            DatabaseReference delref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);
            delref.removeValue();
            db.removeValue();
        }

        GregorianCalendar gregorianCalendar=new GregorianCalendar();
        String date = String.valueOf(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
        String month = String.valueOf(gregorianCalendar.get(GregorianCalendar.MONTH)+1);
        String year = String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR));
        final String formateDate = year+"-"+month+"-"+date;

        final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);
        driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String,Object>)data.getValue();
                    int cancel = Integer.parseInt(map.get("cancel").toString());
                    cancel = cancel+1;
                    String key = data.getKey();
                    try {
                        driver_acc.child(key).child("cancel").setValue(Integer.toString(cancel));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Intent intent = new Intent(this, Welcome.class);
        startActivity(intent);
    }

    public void confirmTrip() {
        DatabaseReference customerReq = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("id", null) + "/Info");

        customerReq.child("accept").setValue(1);
        DatabaseReference response = FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
        response.child("resp").setValue("Accept");

        editor.putString("ride", "ride");
        editor.commit();

        getCurrentLocation();

        GregorianCalendar gregorianCalendar=new GregorianCalendar();
        String date = String.valueOf(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
        String month = String.valueOf(gregorianCalendar.get(GregorianCalendar.MONTH)+1);
        String year = String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR));
        final String formateDate = year+"-"+month+"-"+date;

        final DatabaseReference driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+log_id.getString("id",null)+"/"+formateDate);
        driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Map<String,Object> map = (Map<String,Object>)data.getValue();
                    int confirm = Integer.parseInt(map.get("book").toString());
                    int earn = Integer.parseInt(map.get("earn").toString());
                    confirm = confirm+1;
                    earn = earn + 100;
                    String key = data.getKey();
                    try {
                        driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
                        driver_acc.child(key).child("earn").setValue(Integer.toString(earn));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void getCurrentLocation() {
        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            final String userId= log_id.getString("id",null);
            DatabaseReference delref=FirebaseDatabase.getInstance().getReference("DriversAvailable/"+userId);
            delref.removeValue();
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);
            Log.i("TAG","latitude : "+latitude+"\nLongitude : "+longitude);
            GeoFire geoFire=new GeoFire(ref);
            geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
            Log.i("TAG","latitude : "+latitude+"\nLongitude : "+longitude);
            DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status");
            GeoFire loc=new GeoFire(tripstatus);
            loc.setLocation(userId,new GeoLocation(latitude,longitude));
        }
    }
}
