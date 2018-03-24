package com.example.adarsh.quickliftdriver;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adarsh.quickliftdriver.DAO.DatabaseHelper;
import com.example.adarsh.quickliftdriver.Util.GPSTracker;
import com.example.adarsh.quickliftdriver.Util.LoginDurationThread;
import com.example.adarsh.quickliftdriver.services.NotificationService;
import com.example.adarsh.quickliftdriver.services.RequestService;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Welcome extends AppCompatActivity implements Runnable
    {
    public static final int RequestPermissionCode = 1;
    private static LinearLayout ride,profile,account,help;
    private static Switch login_btn;
    private static DatabaseReference db,driver_acc,driver_info;
    private static TextView login_status,login_duration,book,earn,cancel,name,contact;
    private static SharedPreferences pref;
    private static  SharedPreferences.Editor editor,wel_edit;
    private static RatingBar rate;
    private static SharedPreferences log_id,welcome;
    private static DatabaseHelper databaseHelper;
    private static Intent requestService;
    private static Date login_time,logout_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        final SharedPreferences.Editor pref_editor=log_id.edit();

        db = FirebaseDatabase.getInstance().getReference("Drivers");
        driver_acc = FirebaseDatabase.getInstance().getReference("Driver_Account_Info");
        databaseHelper = new DatabaseHelper(getApplicationContext());
        requestService = new Intent(Welcome.this,RequestService.class);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pref = getSharedPreferences("loginPref",MODE_PRIVATE);

        welcome = getSharedPreferences("welcome",MODE_PRIVATE);

        name = (TextView)findViewById(R.id.driver_name);
        contact = (TextView)findViewById(R.id.driver_contact);
        rate = (RatingBar)findViewById(R.id.rateBar);
        rate.setRating(3);
        rate.setIsIndicator(true);

        profile = (LinearLayout)findViewById(R.id.profile);
        ride = (LinearLayout)findViewById(R.id.ride);
        account = (LinearLayout)findViewById(R.id.account);
        help = (LinearLayout)findViewById(R.id.help);
        login_btn = (Switch)findViewById(R.id.login_switch);
        login_status = (TextView)findViewById(R.id.login_status);
        login_duration = (TextView)findViewById(R.id.login_duration);
        book = (TextView)findViewById(R.id.book_no);
        earn = (TextView)findViewById(R.id.earn_no);
        cancel = (TextView)findViewById(R.id.cancel_no);

        // variables storing function values returned by network connection functions
        boolean status1 = haveNetworkConnection();
        boolean status2 = hasActiveInternetConnection();

//         checking user permission
        if(!checkPermission())
        {
            appendLog(getCurrentTime()+"Gathering permissions status:0");

            //requesting permission to access mobile resources
            Intent i = new Intent(this,Login.class);
            startActivity(i);
            requestPermission();
        }
        else {
            appendLog(getCurrentTime() + "Gathered permissions status:1");

            if(status1 && status2)
            {
                appendLog(getCurrentTime()+"Gathering network information status:1");
                Toast.makeText(this, "Active Internet connection", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        }

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,ProfileActivity.class);
                startActivity(intent);
            }
        });

        ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,RidesActivity.class);
                startActivity(intent);
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,AccountActivity.class);
                startActivity(intent);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this,HelpActivity.class);
                startActivity(intent);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Boolean check_status = login_btn.isChecked();
                if (check_status){
                    GregorianCalendar gregorianCalendar=new GregorianCalendar();
                    String date = String.valueOf(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
                    String month = String.valueOf(gregorianCalendar.get(GregorianCalendar.MONTH)+1);
                    String year = String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR));
//
                    final String formateDate = year+"-"+month+"-"+date;
                    login_time = new Date();
                    editor = pref.edit();
                    editor.putBoolean("status",true);
                    editor.commit();
                    databaseHelper.insertLoginData(formateDate,"login","0.0ms");
                    startService(requestService);
                    login_status.setText("Login");
                    login_duration.setText("Running...");
                    wel_edit = welcome.edit();
                    wel_edit.putString("date",formateDate);
                    wel_edit.putString("login_time",login_time.toString());
                    wel_edit.commit();
                }else{
                    editor = pref.edit();
                    editor.putBoolean("status",false);
                    editor.commit();
                    databaseHelper.insertLoginData(welcome.getString("date",null),"logout","100.0ms");
                    stopService(requestService);
                    login_status.setText("Logout");
                    logout_time = new Date();
                    long diff = logout_time.getTime() - login_time.getTime();
                    long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = diff / (60 * 1000) % 60;
                    long diffHours = diff / (60 * 60 * 1000);
                    String second,minute,hour;
                    if (diffSeconds < 10){
                        second = "0"+diffSeconds;
                    }else {
                        second = ""+diffSeconds;
                    }
                    if (diffMinutes < 10){
                        minute = "0"+diffMinutes;
                    }else {
                        minute = ""+diffMinutes;
                    }
                    if (diffHours < 10){
                        hour = "0"+diffHours;
                    }else {
                        hour = ""+diffHours;
                    }
                    String dura = hour+":"+minute+":"+second+" hr";
                    login_duration.setText(dura);
                    driver_info = FirebaseDatabase.getInstance().getReference("Driver_Login_Info/"+log_id.getString("id",null)+"/"+welcome.getString("date",null));
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("Login_Time",login_time.toString());
                    map.put("Logout_Time",logout_time.toString());
                    map.put("Duration",dura);
                    driver_info.push().setValue(map);
                }
            }
        });
    }

    @Override
    public void run() {
//        loginDurationThread.getDuration();
    }
    @Override
    protected void onStart() {
        super.onStart();
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayLocationSettingsRequest(getApplicationContext());

        }
        GPSTracker gps = new GPSTracker(this);

        // check if GPS enabled
        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            if (log_id.getString("ride",null).equals("")) {
                String userId = log_id.getString("id",null);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
//            ref.push().setValue("hello");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(latitude, longitude));
            }
            else {
                String userId= log_id.getString("id",null);
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversWorking/"+userId);

                GeoFire geoFire=new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation(latitude,longitude));
            }
        }
    }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            editor = pref.edit();
            editor.putBoolean("status",false);
        }

        @Override
    protected void onResume() {
        super.onResume();
        if (pref.getBoolean("status",false)){
            login_btn.setChecked(true);
            login_duration.setText("Running...");
            login_status.setText("Login");
        }else {
            login_btn.setChecked(false);
            login_status.setText("Logout");
        }
            db.child(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() > 0){
                        Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();

                        Log.i("TAG","name : "+map.get("name").toString());
                        name.setText(map.get("name").toString());
                        contact.setText(map.get("phone").toString());
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            driver_acc.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GregorianCalendar gregorianCalendar=new GregorianCalendar();
                    String date = String.valueOf(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
                    String month = String.valueOf(gregorianCalendar.get(GregorianCalendar.MONTH)+1);
                    String year = String.valueOf(gregorianCalendar.get(GregorianCalendar.YEAR));
                    final String formateDate = year+"-"+month+"-"+date;

                    if (dataSnapshot.hasChild(log_id.getString("id",null))){
                        driver_acc.child(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(formateDate)){
                                    driver_acc.child(log_id.getString("id",null)+"/"+formateDate).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String key = dataSnapshot.getKey();
                                            for (DataSnapshot data : dataSnapshot.getChildren()){
                                                Map<String,Object> map = (Map<String, Object>) data.getValue();
                                                book.setText(map.get("book").toString());
                                                earn.setText("Rs. "+map.get("earn").toString());
                                                cancel.setText(map.get("cancel").toString());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }else {
                                    book.setText("0");
                                    earn.setText("Rs. 0");
                                    cancel.setText("0");
                                    HashMap<String,Object> driver_info = new HashMap<>();
                                    driver_info.put("book","0");
                                    driver_info.put("earn","0");
                                    driver_info.put("reject","0");
                                    driver_info.put("cancel","0");
                                    driver_acc.child(log_id.getString("id",null)).child(formateDate).push().setValue(driver_info);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }else {
                        book.setText("0");
                        earn.setText("Rs. 0");
                        cancel.setText("0");
                        HashMap<String,Object> driver_info = new HashMap<>();
                        driver_info.put("book","0");
                        driver_info.put("earn","0");
                        driver_info.put("reject","0");
                        driver_info.put("cancel","0");
                        driver_acc.child(log_id.getString("id",null)).child(formateDate).push().setValue(driver_info);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean hasActiveInternetConnection()
    {
        // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(Welcome.this, new String[]
                {
                        WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE,
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        CALL_PHONE

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
                    boolean ReadStorgaePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean CallPermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && WriteStoragePermission && ReadStorgaePermission && CallPermission) {

                        //Toast.makeText(AnimationActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
//                        Toast.makeText(Welcome.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        appendLog(getCurrentTime()+"Few permissions denied status:0");

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int FourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SixthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(),CALL_PHONE);

        return SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourthPermissionResult ==PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult ==PackageManager.PERMISSION_GRANTED &&
                SixthPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    static public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime())+"\t";
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(Welcome.this, 199);
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
}