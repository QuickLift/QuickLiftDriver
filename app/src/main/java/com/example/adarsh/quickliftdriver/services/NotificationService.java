package com.example.adarsh.quickliftdriver.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.adarsh.quickliftdriver.MapActivity;
import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.RequestActivity;
import com.example.adarsh.quickliftdriver.TripHandlerActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class NotificationService extends Service {
    private static SharedPreferences preferences,loc_pref,ride_info;
    private static SharedPreferences.Editor editor;

    public NotificationService() {

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
        editor = loc_pref.edit();
        editor.putFloat("pick_lat",12.832455f);
        editor.putFloat("pick_long",77.701050f);
        editor.putFloat("drop_lat",12.827909f);
        editor.putFloat("drop_long",77.684062f);
        editor.commit();
        preferences = getSharedPreferences("loginPref",MODE_PRIVATE);
        ride_info = getSharedPreferences("ride_info",MODE_PRIVATE);
        if (!preferences.getBoolean("status",true)){
            stopSelf();
        }else{
            notificationHandler();
        }
        return START_NOT_STICKY;
    }

    public void notificationHandler(){
        Uri alarmSound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.notification_tone);
        android.support.v4.app.NotificationCompat.BigPictureStyle bigPictureStyle = new android.support.v4.app.NotificationCompat.BigPictureStyle();
        bigPictureStyle.bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.profile)).build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent cancelIntent = new Intent(NotificationService.this, TripHandlerActivity.class);
        cancelIntent.putExtra("value","cancel");
        PendingIntent cancelPendingIntent = PendingIntent.getActivity(this,23,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent confirmIntent = new Intent(NotificationService.this, TripHandlerActivity.class);
        confirmIntent.putExtra("value","confirm");
        PendingIntent confirmPendingIntent = PendingIntent.getActivity(this,25,confirmIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mapIntent = new Intent(this, RequestActivity.class);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent piIntent = PendingIntent.getActivity(this,(int)Calendar.getInstance().getTimeInMillis(),mapIntent,0);

        android.support.v4.app.NotificationCompat.Builder builder = (android.support.v4.app.NotificationCompat.Builder)new android.support.v4.app.NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Incoming Trip Request")
                .setContentText(ride_info.getString("name",null)+"\t"+ride_info.getString("phone",null))
                .setContentIntent(piIntent)
                .setStyle(bigPictureStyle)
                .setAutoCancel(true)
                .setTimeoutAfter(8000)
                .setSound(alarmSound)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .addAction(R.mipmap.ic_launcher,"Cancel",cancelPendingIntent)
                .addAction(R.mipmap.ic_launcher,"Confirm",confirmPendingIntent);
        notificationManager.notify(0,builder.build());
        SharedPreferences.Editor prefEdit = preferences.edit();
        prefEdit.putBoolean("status",false);
        prefEdit.commit();
    }
}
