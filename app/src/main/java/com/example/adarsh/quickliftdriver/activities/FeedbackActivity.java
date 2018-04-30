package com.example.adarsh.quickliftdriver.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.Util.SequenceStack;
import com.example.adarsh.quickliftdriver.model.SequenceModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class FeedbackActivity extends AppCompatActivity {
    private static Button feed;
    SharedPreferences log_id,ride_info;
    TextView fare;
    private RatingBar rate_bar;
    private DatabaseReference feed_rate;
    boolean submit = false;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        ride_info = getApplicationContext().getSharedPreferences("ride_info",MODE_PRIVATE);
        id = ride_info.getString("customer_id",null);

        rate_bar = (RatingBar)findViewById(R.id.feed_rate);
        feed_rate = FirebaseDatabase.getInstance().getReference("DriverFeedback/"+id);

        fare = (TextView)findViewById(R.id.fare);
        fare.setText(ride_info.getString("price",null));

        feed = (Button)findViewById(R.id.feed_btn);
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                feed_rate.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Map<String,Object> feedback= (Map<String,Object>)dataSnapshot.getValue();
                            int no = Integer.parseInt(feedback.get("no").toString());
                            Float rate = Float.parseFloat(feedback.get("rate").toString());
                            rate = rate * no;
                            Float curr_rate = (Float) ((rate+rate_bar.getRating())/(no+1));

                            HashMap<String,String> current_feed = new HashMap<>();
                            current_feed.put("no",Integer.toString(no+1));
                            current_feed.put("rate",curr_rate.toString());

                            feed_rate.setValue(current_feed);
                            submit = true;

                        }else {
                            HashMap<String,String> current_feed = new HashMap<>();
                            current_feed.put("no",Integer.toString(1));
                            current_feed.put("rate",Float.toString(rate_bar.getRating()));
                            feed_rate.setValue(current_feed);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("OK","failed to create reference");
                    }
                });
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
                            Float earn = Float.parseFloat(map.get("earn").toString());
                            confirm = confirm+1;
                            earn = earn + Float.parseFloat(ride_info.getString("price",null));
                            String key = data.getKey();
                            try {
                                driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
                                driver_acc.child(key).child("earn").setValue(Float.toString(earn));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
                resp.removeValue();
                DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+id);
                cus.removeValue();
                SharedPreferences.Editor editor=log_id.edit();
                editor.putString("ride","");
                editor.commit();
                Stack<SequenceModel> stack = new SequenceStack().getStack();
                if (!stack.isEmpty()){
                    stack.pop();
                    startActivity(new Intent(FeedbackActivity.this,MapActivity.class));
                    finish();
                }else {
                    DatabaseReference tripstatus= FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
                    tripstatus.removeValue();
                    finish();
                }
                //startActivity(new Intent(FeedbackActivity.this,Welcome.class));

            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onStop() {
        if (!submit){
            feed_rate.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Map<String,Object> feedback= (Map<String,Object>)dataSnapshot.getValue();
                        int no = Integer.parseInt(feedback.get("no").toString());
                        Float rate = Float.parseFloat(feedback.get("rate").toString());
                        rate = rate * no;
                        Float curr_rate = (Float) ((rate+5)/(no+1));

                        HashMap<String,String> current_feed = new HashMap<>();
                        current_feed.put("no",Integer.toString(no+1));
                        current_feed.put("rate",curr_rate.toString());

                        feed_rate.setValue(current_feed);
                    }else {
                        HashMap<String,String> current_feed = new HashMap<>();
                        current_feed.put("no",Integer.toString(1));
                        current_feed.put("rate",Float.toString(5));
                        feed_rate.setValue(current_feed);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i("OK","failed to create reference");
                }
            });
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
                        Float earn = Float.parseFloat(map.get("earn").toString());
                        confirm = confirm+1;
                        earn = earn + Float.parseFloat(ride_info.getString("price",null));
                        String key = data.getKey();
                        try {
                            driver_acc.child(key).child("book").setValue(Integer.toString(confirm));
                            driver_acc.child(key).child("earn").setValue(Float.toString(earn));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
            resp.removeValue();
            DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+id);
            cus.removeValue();
            SharedPreferences.Editor editor=log_id.edit();
            editor.putString("ride","");
            editor.commit();
            Stack<SequenceModel> stack = new SequenceStack().getStack();
            if (!stack.isEmpty()){
                stack.pop();
                startActivity(new Intent(FeedbackActivity.this,MapActivity.class));
                finish();
            }else {
                DatabaseReference tripstatus= FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
                tripstatus.removeValue();
                finish();
            }
        }
        super.onStop();
    }
}
