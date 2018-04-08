package com.example.adarsh.quickliftdriver.Util;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.adarsh.quickliftdriver.model.SequenceModel;
import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by adarsh on 6/4/18.
 */

public class BestRoute extends AsyncTask<Object,String,String> {
    private Stack<SequenceModel> stack;
    String url,data;
    ArrayList<SequenceModel> sequence;

    @Override
    protected String doInBackground(Object... objects) {
        url = (String) objects[0];
        sequence=(ArrayList<SequenceModel>) objects[1];
        stack=(Stack<SequenceModel>) objects[2];
        //duration=(String) objects[2];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            data = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        JSONArray jsonArray=null;
        JSONObject jsonObject = null;
        Log.v("TAG","hi"+stack.size()+" "+sequence.size());
        try {
            jsonObject=new JSONObject(s);
            jsonArray=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").length()-1;i>=0;i--) {
                Log.v("TAG",""+sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)).getName()+" "+sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)).getType());
                stack.push(sequence.get(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)));
            }
            Log.v("TAG",""+stack.size());
//            for (int i=0;i<jsonArray.length()-1;i++) {
//                Log.v("TAG", jsonArray.getJSONObject(i).getJSONObject("distance").getString("text"));
//               // Log.v("TAG", jsonArray.getJSONObject(1).getJSONObject("distance").getString("text"));
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}