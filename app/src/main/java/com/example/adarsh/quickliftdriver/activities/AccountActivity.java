package com.example.adarsh.quickliftdriver.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.adapters.DriveListAdapter;
import com.example.adarsh.quickliftdriver.model.Feed;
import com.example.adarsh.quickliftdriver.model.RideHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    /***
     * this class for account info, will get histories from cloud of
     * cancel drive, earning,booked..
     * all these displaying & Sumations , bitween given date
     *
     * */

    private Calendar myCalendar;
    private EditText from_date,to_date;
    private ListView listView;
    private LinearLayout linearLayout;
    private TextView mDriveDate,mDriveName, mTotal, mDriveTotal;
    private Spinner spinner;
    DatabaseReference db;
    SharedPreferences preferences;
    List<RideHistory> rideHistoryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        // stop auto showing keyboard
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // attetch components with id's
        myCalendar = Calendar.getInstance();
        from_date= (EditText) findViewById(R.id.from_date);
        from_date.setEnabled(false);
        from_date.setFocusable(false);

        to_date = (EditText)findViewById(R.id.to_date);
        to_date.setFocusable(false);
        to_date.setEnabled(false);

        listView=(ListView)findViewById(R.id.list);
        mDriveDate=(TextView)findViewById(R.id.drive_date);
        mDriveName=(TextView)findViewById(R.id.drive_name);
        mTotal =(TextView)findViewById(R.id.total);
        mDriveTotal =(TextView)findViewById(R.id.drive_total);
        spinner=(Spinner)findViewById(R.id.select);
        linearLayout=(LinearLayout)findViewById(R.id.list_layout);
        linearLayout.setVisibility(View.GONE);
        //read shared preferences of log-in
        preferences=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
    }

    @SuppressWarnings("deprecation")
    public void date_from(View view) {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpDialog = new DatePickerDialog(this, fromDate, mYear, mMonth, mDay);
        dpDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dpDialog.show();
    }

    @SuppressWarnings("deprecation")
    public void date_to(View view) {
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpDialog = new DatePickerDialog(this, toDate, mYear, mMonth, mDay);
        dpDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dpDialog.show();
    }
    // load button
    @SuppressWarnings("deprecation")
    public void load_btn(View view) {
        if (validate()){
            if(from_date.getText().toString().isEmpty() &&to_date.getText().toString().isEmpty()){
                Toast.makeText(this, "Please enter your field", Toast.LENGTH_SHORT).show();
            }else{
                rideHistoryList = new ArrayList<>();
                //fiels is not empty
                db= FirebaseDatabase.getInstance().getReference("Driver_Account_Info/"+preferences.getString("id",null));
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fromDate=from_date.getText().toString();
                        String toDate=to_date.getText().toString();


                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            RideHistory rideHistory = new RideHistory();
                            if (fromDate.compareToIgnoreCase(data.getKey()) <= 0 && toDate.compareToIgnoreCase(data.getKey()) >= 0) {
                                //Log.i("AccountActivity", "data.getKey():::  " + data.getKey());
                                Feed feed = new Feed();
                                for(DataSnapshot actualFeed : data.getChildren() ) {
                                    Map<String, String> feedMap  = (Map<String, String>) actualFeed.getValue();//store value in map
                                    //set all values in feed class object;
                                    feed.setBookedRideCount(Integer.parseInt(feedMap.get("book")));
                                    feed.setCanceledRidesCount(Integer.parseInt(feedMap.get("cancel")));
                                    feed.setTotalEarning(Integer.parseInt(feedMap.get("earn")));
                                    feed.setRejectedRideCount(Integer.parseInt(feedMap.get("reject")));
                                }
                                /**
                                 * In rideHistory fields: data and feed
                                 * set key in data(contain date)
                                 * and components of value pair store in Feed class object
                                 * */
                                rideHistory.setDate(data.getKey());
                                rideHistory.setFeed(feed);

                                rideHistoryList.add(rideHistory);
                            } else {
                               // Toast.makeText(AccountActivity.this, "NO data found", Toast.LENGTH_SHORT).show();
                            }
                        }


                        //if (getActivity()!=null){
                        linearLayout.setVisibility(View.VISIBLE);
                        mDriveName.setText(spinner.getSelectedItem().toString());
                        mDriveDate.setText("Date");
                        // call custom adapter to display list of drive info
                        DriveListAdapter listAdapter=null;
                        if(spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Booking History")){
                            //Log.i("yogendra","yogendra: "+spinner.getSelectedItem().toString().trim());
                            listAdapter = new DriveListAdapter(rideHistoryList,"BookedRideCount");
                            //call for total count
                            totalDriveInfo(rideHistoryList,0);
                        }else if(spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Cancel History")) {
                            listAdapter = new DriveListAdapter(rideHistoryList,"CanceledRidesCount");
                            //call for total count
                            totalDriveInfo(rideHistoryList,1);
                        }else if(spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Earnings History")) {
                            listAdapter = new DriveListAdapter(rideHistoryList,"TotalEarning");
                            //call for total count
                            totalDriveInfo(rideHistoryList,2);
                        }
                        listView.setAdapter(listAdapter);
                        // }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

    }

    private boolean validate(){
        boolean status = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try{
            if (from_date.getText().toString().isEmpty()){
                from_date.setError("From date should not be empty");
            }else if (to_date.getText().toString().isEmpty()){
                to_date.setError("To date should not be empty");
            }else{
                Date from = sdf.parse(from_date.getText().toString().trim());
                Date to = sdf.parse(to_date.getText().toString().trim());
                if (from.after(to)){
                    from_date.setError("From date should not greater than To date");
                }else {
                    status = true;
                }
            }
        }catch(Exception e){
            Log.e("TAG",""+e.getLocalizedMessage());
            status = false;
        }
        return status;
    }

    private void totalDriveInfo(List<RideHistory> rideHistoryList, int caseCount) {
        /**
         * sum of drive info values
         * */
        mTotal.setText("TOTAL ");
        String temp;
        int possition;
        int cancel=0;
        int booked_rides=0;
        double earning=0;
        switch (caseCount){
            case 0: for (possition =0;possition<=rideHistoryList.size()-1;possition++) {
                booked_rides=booked_rides+rideHistoryList.get(possition).getFeed().getBookedRideCount();
            }
            mDriveTotal.setText(Integer.toString(booked_rides));break;
            case 1: for (possition =0;possition<=rideHistoryList.size()-1;possition++) {
                cancel=cancel+rideHistoryList.get(possition).getFeed().getCanceledRidesCount();
            }
                mDriveTotal.setText(Integer.toString(cancel));break;
            case 2: for (possition =0;possition<=rideHistoryList.size()-1;possition++) {
                earning=earning+rideHistoryList.get(possition).getFeed().getTotalEarning();
            }
                mDriveTotal.setText(Double.toString(earning));break;
                default:
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
