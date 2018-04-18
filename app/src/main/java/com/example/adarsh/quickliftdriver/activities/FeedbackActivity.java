package com.example.adarsh.quickliftdriver.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.Map;
import java.util.Stack;

public class FeedbackActivity extends AppCompatActivity {
    private static Button feed;
    SharedPreferences log_id,ride_info;
    TextView fare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        ride_info = getApplicationContext().getSharedPreferences("ride_info",MODE_PRIVATE);

        fare = (TextView)findViewById(R.id.fare);
        fare.setText(ride_info.getString("price",null));

        feed = (Button)findViewById(R.id.feed_btn);
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                            earn = earn + Integer.parseInt(ride_info.getString("price",null));
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

                DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
                resp.removeValue();
                DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+ride_info.getString("customer_id",null));
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
}
