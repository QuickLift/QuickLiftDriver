package com.example.adarsh.quickliftdriver.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.adarsh.quickliftdriver.MapActivity;
import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.RequestActivity;
import com.example.adarsh.quickliftdriver.Util.UserRequestInfo;
import com.example.adarsh.quickliftdriver.Welcome;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RequestService extends Service {

    DatabaseReference customerReq= FirebaseDatabase.getInstance().getReference("CustomerRequests");
    UserRequestInfo userRequestInfo;
    Location location;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 199;
    SharedPreferences log_id;

    public RequestService() {

    }


    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TAG","RequestService");
        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        customerReq.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    Log.i("TAG","data present");
                }
                if (dataSnapshot.child(log_id.getString("id",null)).exists()){
                    Log.i("TAG",""+log_id.getString("id",null));
//                    userRequestInfo = new UserRequestInfo();
//                    userRequestInfo.setUserKey(dataSnapshot.getKey());
//                    Log.i("TAG",""+dataSnapshot.getKey());
//
//                    Intent notificationServ = new Intent(RequestService.this, NotificationService.class);
//                    startService(notificationServ);
                    customerReq.child(log_id.getString("id",null)+"/Info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            SharedPreferences ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);
                            final SharedPreferences.Editor editor = ride_info.edit();
                            Map<String,Object> hashMap = (Map<String, Object>)dataSnapshot.getValue();
                            if (hashMap.get("accept").toString() == "0"){
                                String customer_id = hashMap.get("customer_id").toString();
                                editor.putString("customer_id",hashMap.get("customer_id").toString());
                                editor.putString("en_lat",hashMap.get("en_lat").toString());
                                editor.putString("en_lng",hashMap.get("en_lng").toString());
                                editor.putString("st_lat",hashMap.get("st_lat").toString());
                                editor.putString("st_lng",hashMap.get("st_lng").toString());
                                editor.putString("source",hashMap.get("source").toString());
                                editor.putString("destination",hashMap.get("destination").toString());
                                editor.putString("otp",hashMap.get("otp").toString());
                                editor.putString("price",hashMap.get("price").toString());
                                DatabaseReference customer_info= FirebaseDatabase.getInstance().getReference("Users/"+customer_id);
                                customer_info.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map<String,Object> hashMap = (Map<String, Object>)dataSnapshot.getValue();
                                        editor.putString("name",hashMap.get("name").toString());
                                        editor.putString("phone",hashMap.get("phone").toString());
//                                    editor.putString("add",hashMap.get("address").toString());
                                        editor.commit();

                                        Intent notificationServ = new Intent(RequestService.this, NotificationService.class);
                                        startService(notificationServ);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return START_STICKY;
    }
}
