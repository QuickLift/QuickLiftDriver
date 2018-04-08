package com.example.adarsh.quickliftdriver.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.example.adarsh.quickliftdriver.Util.SequenceStack;
import com.example.adarsh.quickliftdriver.Util.UserRequestInfo;
import com.example.adarsh.quickliftdriver.model.SequenceModel;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Stack;

public class RequestService extends Service {

    DatabaseReference customerReq;
    UserRequestInfo userRequestInfo;
    Location location;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 199;
    SharedPreferences log_id,ride_info;
    SharedPreferences.Editor editor;
    Intent notificationServ;
    private Stack<SequenceModel> stack;

    public RequestService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        stack = new SequenceStack().getStack();
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TAG","RequestService");
        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        customerReq= FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        customerReq.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0){
                    Log.i("TAG","data present");
                    for (final DataSnapshot data : dataSnapshot.getChildren()){
                        final Map<String,Object> map = (Map<String,Object>)data.getValue();
                        Log.i("TAG","Request for driver : "+map.toString());
                        if (map.get("accept").toString() == "0"){

                            ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);
                            editor = ride_info.edit();
                            editor.putString("accept",map.get("accept").toString());
                            editor.putString("customer_id",map.get("customer_id").toString());
                            editor.putString("d_lat",map.get("d_lat").toString());
                            editor.putString("d_lng",map.get("d_lng").toString());
                            editor.putString("destination",map.get("destination").toString());
                            editor.putString("en_lat",map.get("en_lat").toString());
                            editor.putString("en_lng",map.get("en_lng").toString());
                            editor.putString("otp",map.get("otp").toString());
                            editor.putString("price",map.get("price").toString());
                            editor.putString("seat",map.get("seat").toString());
                            editor.putString("source",map.get("source").toString());
                            editor.putString("st_lat",map.get("st_lat").toString());
                            editor.putString("st_lng",map.get("st_lng").toString());
                            DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users/"+map.get("customer_id"));
                            user.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String,Object> userMap = (Map<String,Object>)dataSnapshot.getValue();
                                    editor.putString("name",userMap.get("name").toString());
                                    editor.putString("phone",userMap.get("phone").toString());
                                    editor.putString("email",userMap.get("email").toString());
                                    editor.commit();
                                    notificationServ = new Intent(RequestService.this, NotificationService.class);
                                    startService(notificationServ);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            Log.i("TAG","Notification Service Started");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return START_STICKY;
    }
}
