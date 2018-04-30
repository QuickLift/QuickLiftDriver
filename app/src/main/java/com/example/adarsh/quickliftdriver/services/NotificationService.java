package com.example.adarsh.quickliftdriver.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.activities.RequestActivity;
import com.example.adarsh.quickliftdriver.activities.TripHandlerActivity;
import com.example.adarsh.quickliftdriver.Util.GPSTracker;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import junit.runner.Version;

import java.util.Calendar;

public class NotificationService extends Service {
    private static SharedPreferences preferences,loc_pref,ride_info,Login;
    private static SharedPreferences.Editor editor,pref_edit;
    private static DatabaseReference customerReq;

    NotificationManager notificationManager;
    NotificationChannel notificationChannel;
    String NOTIFICATION_CHANNEL_ID="17";

    public NotificationService() {
        customerReq= FirebaseDatabase.getInstance().getReference("CustomerRequests");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TAG","onStartCommand of NotificationService");
        loc_pref = getSharedPreferences("locations",Context.MODE_PRIVATE);
        Login = getSharedPreferences("Login",Context.MODE_PRIVATE);
        editor = loc_pref.edit();
        editor.putFloat("pick_lat",12.832455f);
        editor.putFloat("pick_long",77.701050f);
        editor.putFloat("drop_lat",12.827909f);
        editor.putFloat("drop_long",77.684062f);
        editor.commit();
        preferences = getSharedPreferences("loginPref",MODE_PRIVATE);
        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);

        if (ride_info.getString("accept",null).equalsIgnoreCase("0")){
            notificationHandler();
        }else {
//            notificationManager.cancel(0);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    public void notificationHandler(){
        Uri alarmSound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.notification_tone);
        android.support.v4.app.NotificationCompat.BigPictureStyle bigPictureStyle = new android.support.v4.app.NotificationCompat.BigPictureStyle();
        bigPictureStyle.bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.profile)).build();
        notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent cancelIntent = new Intent(NotificationService.this, TripHandlerActivity.class);
        cancelIntent.putExtra("value","cancel");
        PendingIntent cancelPendingIntent = PendingIntent.getActivity(this,23,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent confirmIntent = new Intent(NotificationService.this, TripHandlerActivity.class);
        confirmIntent.putExtra("value","confirm");
        PendingIntent confirmPendingIntent = PendingIntent.getActivity(this,25,confirmIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mapIntent = new Intent(this, RequestActivity.class);
        PendingIntent piIntent = PendingIntent.getActivity(this,40,mapIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        try{
            notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

            Notification mBuilder = new NotificationCompat.Builder(getApplicationContext(),NOTIFICATION_CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_PROMO)
                    .setContentTitle("Incoming Trip Request")
                    .setContentText(ride_info.getString("name",null)+"\t"+ride_info.getString("phone",null))
                    .setSmallIcon(R.drawable.logo)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setTimeoutAfter(18000)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .addAction(R.mipmap.ic_launcher,"Cancel",cancelPendingIntent)
                    .addAction(R.mipmap.ic_launcher,"Confirm",confirmPendingIntent)
                    .setContentIntent(piIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}).build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {

                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            notificationManager.notify(0,mBuilder);

        }catch (Exception e){
            Log.e("OK","Error notification : "+e);
        }

        SharedPreferences.Editor prefEdit = preferences.edit();
        prefEdit.putBoolean("status",false);
        prefEdit.commit();
        new getNotifStatus().start();
    }

    private class getNotifStatus extends Thread{
        @Override
        public void run() {
            customerReq.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(Login.getString("id",null))){
                        notificationManager.cancel(0);
                        getCurrentLocation();
                        SharedPreferences.Editor prefEdit = preferences.edit();
                        prefEdit.putBoolean("status",true);
                        prefEdit.commit();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void getCurrentLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


        }
        GPSTracker gps = new GPSTracker(this);

        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            if (Login.getString("ride",null).equals("")) {
                String userId = Login.getString("id",null);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/"+Login.getString("type",null));

                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(latitude, longitude));
            }
        }

    }
}
