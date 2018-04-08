package com.example.adarsh.quickliftdriver.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.example.adarsh.quickliftdriver.Util.BestRoute;
import com.example.adarsh.quickliftdriver.Util.GPSTracker;
import com.example.adarsh.quickliftdriver.Util.SequenceStack;
import com.example.adarsh.quickliftdriver.Util.UserRequestInfo;
import com.example.adarsh.quickliftdriver.model.SequenceModel;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

public class RouteArrangeService extends Service {

    DatabaseReference customerReq;
    UserRequestInfo userRequestInfo;
    Location location;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 199;
    SharedPreferences log_id,ride_info;
    SharedPreferences.Editor editor;
    Intent notificationServ;
    private Stack<SequenceModel> stack;

    public RouteArrangeService() {

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
        Log.i("TAG","RouteArrangeService");

        if (stack.size()>2) {
            ArrayList<SequenceModel> seq = new ArrayList<>();
            seq.clear();
            //Log.v("TAG",""+stack.size());
            int val=stack.size();
            for (int i = 0; i < val; i++) {
                seq.add(stack.pop());
                Log.v("TAG","stack value"+i);
            }

            GPSTracker gps=new GPSTracker(this);
            StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
            url.append("origin="+gps.getLatitude()+","+gps.getLongitude());

            Location location=new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(seq.get(1).getLat());
            location.setLongitude(seq.get(1).getLng());

            Location location2=new Location(LocationManager.GPS_PROVIDER);
            location2.setLatitude(seq.get(seq.size()-1).getLat());
            location2.setLongitude(seq.get(seq.size()-1).getLng());

            if (gps.getLocation().distanceTo(location)>gps.getLocation().distanceTo(location2))
                url.append("&destination="+location.getLatitude()+","+location.getLongitude());
            else {
                url.append("&destination="+location2.getLatitude()+","+location2.getLongitude());
            }
            url.append("&waypoints=optimize:true");
            for (int i=0;i<seq.size();i++){
                url.append("|"+seq.get(i).getLat()+","+seq.get(i).getLng());
            }
            url.append("&key=AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");

            Object[] datatransfer=new Object[3];
            String form_url=url.toString();
            BestRoute bestRoute=new BestRoute();
            datatransfer[0]=form_url;
            datatransfer[1]=seq;
            datatransfer[2]=stack;
            bestRoute.execute(datatransfer);
        }

        onDestroy();

        return START_STICKY;
    }
}
