package com.example.adarsh.quickliftdriver.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.Util.SequenceStack;
import com.example.adarsh.quickliftdriver.model.SequenceModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Stack;

public class FeedbackActivity extends AppCompatActivity {
    private static Button feed;
    SharedPreferences log_id,ride_info;
    TextView fare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        ride_info = getApplicationContext().getSharedPreferences("ride_info",MODE_PRIVATE);

        fare = (TextView)findViewById(R.id.fare);
        fare.setText(ride_info.getString("price",null));

        feed = (Button)findViewById(R.id.feed_btn);
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference tripstatus= FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
                tripstatus.removeValue();
                DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
                resp.removeValue();
                DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
                cus.removeValue();
                SharedPreferences.Editor editor=log_id.edit();
                editor.putString("ride","");
                editor.commit();
                Stack<SequenceModel> stack = new SequenceStack().getStack();
                if (!stack.isEmpty()){
                    startActivity(new Intent(FeedbackActivity.this,MapActivity.class));
                    finish();
                }
                //startActivity(new Intent(FeedbackActivity.this,Welcome.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
