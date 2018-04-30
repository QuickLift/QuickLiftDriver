package com.example.adarsh.quickliftdriver.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.adarsh.quickliftdriver.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RidesActivity extends AppCompatActivity {

    private Cursor cursor;
    ListView list;
    DatabaseReference db;
    private SharedPreferences log_id;
    ArrayList<Map<String,Object>> ride_list=new ArrayList<Map<String,Object>>();
    Button curr_ride;
    private ProgressDialog progressDialog;
    ImageView no_ride;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference driver_status = FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
        driver_status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    curr_ride.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...\nLoading Rides");

        curr_ride = (Button)findViewById(R.id.curr_ride);
        curr_ride.setVisibility(View.GONE);
        curr_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RidesActivity.this,MapActivity.class));
                finish();
            }
        });
        no_ride = (ImageView)findViewById(R.id.no_ride);
        no_ride.setVisibility(View.GONE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("Rides");

        list=(ListView)findViewById(R.id.list);

        if (!progressDialog.isShowing()){
            progressDialog.show();
        }

        db.orderByChild("driver").equalTo(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() <= 0){
                    no_ride.setVisibility(View.VISIBLE);
                }
                ride_list.clear();

                for (DataSnapshot data:dataSnapshot.getChildren()){
                    ride_list.add((Map<String, Object>) data.getValue());
                    //Toast.makeText(CustomerRides.this, String.valueOf(ride_list.size()), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(CustomerRides.this, ride_list.get(ride_list.size()-1).get("time").toString(), Toast.LENGTH_SHORT).show();
                }
                if (progressDialog.isShowing()){
                    progressDialog.cancel();
                    progressDialog.dismiss();
                }
                //Toast.makeText(CustomerRides.this, String.valueOf(ride_list.size()), Toast.LENGTH_SHORT).show();
                list.setAdapter(new CustomAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ride_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view=getLayoutInflater().inflate(R.layout.ride_info,null);

            final ImageView img=(ImageView)view.findViewById(R.id.image);
            TextView time=(TextView)view.findViewById(R.id.timestamp);
            TextView source=(TextView)view.findViewById(R.id.source);
            TextView destination=(TextView)view.findViewById(R.id.destination);
            TextView amount=(TextView)view.findViewById(R.id.amount);
            final TextView name=(TextView)view.findViewById(R.id.name);
            TextView status = (TextView)view.findViewById(R.id.sta);

            time.setText(ride_list.get(position).get("time").toString());
            source.setText(ride_list.get(position).get("source").toString());
            destination.setText(ride_list.get(position).get("destination").toString());
            amount.setText("Rs. "+ride_list.get(position).get("amount").toString());
            try{
                status.setText(ride_list.get(position).get("status").toString());
            }catch (Exception e){
                status.setText("Unknown");
            }
            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Users");
            dref.child(ride_list.get(position).get("customerid").toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
                    //veh.setText(map.get("veh_type").toString()+" , "+map.get("veh_num").toString());

                    name.setText(map.get("name").toString());
                    if (!map.get("thumb").toString().equals("")) {
                        byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                        img.setImageBitmap(decbyte);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            return view;
        }
    }
}
