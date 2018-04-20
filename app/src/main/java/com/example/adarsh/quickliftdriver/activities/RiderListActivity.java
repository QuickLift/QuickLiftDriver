package com.example.adarsh.quickliftdriver.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adarsh.quickliftdriver.R;
import com.example.adarsh.quickliftdriver.Util.SequenceStack;
import com.example.adarsh.quickliftdriver.model.RiderList;
import com.example.adarsh.quickliftdriver.model.SequenceModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Stack;


public class RiderListActivity extends AppCompatActivity {

    private ListView rider;
    private ArrayList<RiderList> riders;
    private DatabaseReference request,response,users;
    private SharedPreferences log_id;
    private Stack<SequenceModel> stack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_list);

        log_id = getSharedPreferences("Login",MODE_PRIVATE);
        request = FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null));
        response = FirebaseDatabase.getInstance().getReference("Response");
        users = FirebaseDatabase.getInstance().getReference("Users");
        stack = new SequenceStack().getStack();

        riders = new ArrayList<>();
        rider = (ListView)findViewById(R.id.rider_list);

        request.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                riders.clear();
                if (dataSnapshot.getChildrenCount() > 0){
                    Log.i("TAG","request listener");
                    for (DataSnapshot data : dataSnapshot.getChildren()){
                        final String c_id = data.getKey();
                        response.child(c_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i("TAG","response listener");
                                String resp = dataSnapshot.child("resp").getValue().toString();
                                if (!resp.equalsIgnoreCase("Trip Ended") || !resp.equalsIgnoreCase("Cancel")){
                                    Log.i("TAG","response not ended nor canceled");
                                    final RiderList ride = new RiderList();
                                    ride.setC_id(c_id);
                                    if (resp.equalsIgnoreCase("Trip Started")){
                                        Log.i("TAG","response started");
                                        ride.setEnable(false);
                                    }else{
                                        Log.i("TAG","response not started");
                                        ride.setEnable(true);
                                    }
                                    users.child(c_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String name = dataSnapshot.child("name").getValue().toString();
                                            Log.i("TAG","user name : "+name);
                                            ride.setC_name(name);
                                            riders.add(ride);
                                            rider.setAdapter(new CustomAdapter());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void cancel_trip(String customer){
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
                    int cancel = Integer.parseInt(map.get("cancel").toString());
                    cancel = cancel+1;
                    String key = data.getKey();
                    try {
                        driver_acc.child(key).child("cancel").setValue(Integer.toString(cancel));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        getCurrentLocation();
//        moveMap();
//        stack.push(model);
        DatabaseReference resp=FirebaseDatabase.getInstance().getReference("Response/"+customer);
        resp.child("resp").setValue("Cancel");
        DatabaseReference cus=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log_id.getString("id",null)+"/"+customer);
        cus.removeValue();
        SharedPreferences.Editor editor=log_id.edit();
        editor.putString("ride","");
        editor.commit();
        int size = stack.size();
        Log.i("TAG","Stak SIze : "+size);
        ArrayList<SequenceModel> sequenceModels = new ArrayList<>();
        for(int i = 0;i < size;i++){
            Log.i("TAG","Stak pop : "+i);
            SequenceModel deleteModel =  stack.pop();
            if (deleteModel.getId() != customer){
                sequenceModels.add(deleteModel);
                Log.i("TAG","item for pushing : "+sequenceModels.size());
            }
        }
        if (sequenceModels.size() > 0){
            for (int i = sequenceModels.size()-1; i >= 0;i--){
                Log.i("TAG","Stak push : "+i);
                stack.push(sequenceModels.get(i));
            }
            startActivity(new Intent(RiderListActivity.this,MapActivity.class));
            finish();
        }else {
            DatabaseReference tripstatus=FirebaseDatabase.getInstance().getReference("Status/"+log_id.getString("id",null));
            tripstatus.removeValue();
            DatabaseReference working = FirebaseDatabase.getInstance().getReference("DriversWorking/"+log_id.getString("type",null)+"/"+log_id.getString("id",null));
            working.removeValue();
            finish();
        }
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return riders.size();
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
        public View getView(final int position, View view, ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.rider_card,null);
            TextView name = (TextView)view.findViewById(R.id.c_name);
            Button cancel = (Button)view.findViewById(R.id.cancel);

            name.setText(riders.get(position).getC_name());
            Log.i("TAG","name in adapter : "+name.getText().toString());
            if (riders.get(position).isEnable()){
//                cancel.setVisibility(View.VISIBLE);
                cancel.setEnabled(true);
            }else {
                cancel.setEnabled(false);
//                cancel.setVisibility(View.GONE);
            }
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel_trip(riders.get(position).getC_id());
                }
            });

            return view;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        startActivity(new Intent(RiderListActivity.this,MapActivity.class));
        finish();
    }
}
