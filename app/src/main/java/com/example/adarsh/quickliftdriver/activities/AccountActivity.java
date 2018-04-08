package com.example.adarsh.quickliftdriver.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.model.Feed;
import com.example.adarsh.quickliftdriver.model.RideHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private static Calendar myCalendar;
    private static EditText from_date,to_date;
    private Button mLoadData;
    private ListView listView;
    private TextView mDriveDate,mDriveName,mTotle,mDriveTotle;
    DatabaseReference db;
    SharedPreferences preferences;
    ArrayList<Map<String,Object>> mRidesListDateWise=new ArrayList<Map<String,Object>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        myCalendar = Calendar.getInstance();
        from_date= (EditText) findViewById(R.id.from_date);
        to_date = (EditText)findViewById(R.id.to_date);
        mLoadData = (Button) findViewById(R.id.load_btn);//load info
        listView=(ListView)findViewById(R.id.list);
        mDriveDate=(TextView)findViewById(R.id.drive_date);
        mDriveDate=(TextView)findViewById(R.id.drive_name);
        mDriveDate=(TextView)findViewById(R.id.totle);
        mDriveDate=(TextView)findViewById(R.id.drive_totle);

        preferences=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);




    }

    @SuppressWarnings("deprecation")
    public void date_from(View view) {
        new DatePickerDialog(AccountActivity.this, fromDate, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @SuppressWarnings("deprecation")
    public void date_to(View view) {
        new DatePickerDialog(AccountActivity.this, toDate, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    // load button
    @SuppressWarnings("deprecation")
    public void load_btn(View view) {
        if(from_date.getText().toString().isEmpty() &&to_date.getText().toString().isEmpty()){
            Toast.makeText(this, "Please enter your field", Toast.LENGTH_SHORT).show();
        }else{

            db= FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+preferences.getString("id",null));
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String fromDate=from_date.getText().toString();
                    String toDate=to_date.getText().toString();

                    List<RideHistory> rideHistoryList = new ArrayList<>();

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                RideHistory rideHistory = new RideHistory();
                                if (fromDate.compareToIgnoreCase(data.getKey()) <= 0 && toDate.compareToIgnoreCase(data.getKey()) >= 0) {
                                Log.i("AccountActivity", "data.getKey():::  " + data.getKey());
                                Log.i("AccountActivity", "data.getValue():::  " + data.getValue().toString());
                                    Feed feed = new Feed();
                                    for(DataSnapshot actualFeed : data.getChildren() ) {
                                    Map<String, String> feedMap  = (Map<String, String>) actualFeed.getValue();
                                    feed.setBookedRideCount(Integer.parseInt(feedMap.get("book")));
                                    feed.setCanceledRidesCount(Integer.parseInt(feedMap.get("cancel")));
                                    feed.setTotalEarning(Integer.parseInt(feedMap.get("earn")));
                                    feed.setRejectedRideCount(Integer.parseInt(feedMap.get("reject")));
                                }
                                rideHistory.setDate(data.getKey());
                                rideHistory.setFeed(feed);
                                Log.i("RideDate ::: ","Date"+rideHistory.getDate().toString());
                                Log.i("RideDate ::: ","Feed: "+rideHistory.getFeed().getBookedRideCount());
                                Log.i("RideDate ::: ","Feed: "+rideHistory.getFeed().getCanceledRidesCount());

                                rideHistoryList.add(rideHistory);
                                Log.i("Ride history list : ", "rideHistoryList" + rideHistoryList.get(0).getDate());
                                Log.i("Ride history list : ", "rideHistoryList" + rideHistoryList.get(0).getFeed().getBookedRideCount());
                            } else {
                                Toast.makeText(AccountActivity.this, "NO data found", Toast.LENGTH_SHORT).show();
                            }
                        }

                   // ArrayAdapter<String>arrayAdapter = new ArrayAdapter<String>(this, R.layout.custom_feed_item,rideHistoryList);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
           updateFromLabel( year, monthOfYear,dayOfMonth);
        }
    };

    DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            updateToLabel( year, monthOfYear,dayOfMonth);
        }
    };

    private void updateFromLabel(int year,int month,int day){
        from_date.setText(new StringBuilder().append(year).append("-")
                .append(month+1).append("-").append(day));
    }

    private void updateToLabel(int year,int month,int day){
        to_date.setText(new StringBuilder().append(year).append("-")
                .append(month+1).append("-").append(day));
    }
}
