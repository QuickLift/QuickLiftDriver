package com.example.adarsh.quickliftdriver;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {
    private static Button fare_charge;
    private static TextView fare_text;
    private static ImageButton center,office;
    private static String text = "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200\n" +
            "Car\tRs.20\nBike\tRs.10\nAuto\tRs.90\nRicksaw\tRs.200";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        office = (ImageButton)findViewById(R.id.call_office);
        center = (ImageButton)findViewById(R.id.call_center);

        fare_charge = (Button)findViewById(R.id.fare_button);
        fare_text = (TextView)findViewById(R.id.fare_text);
        fare_text.setText(text);
        fare_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fare_text.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_top);
                fare_text.startAnimation(animation);
            }
        });

        office.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_CALL);
                dialIntent.setData(Uri.parse("tel:9678436389"));
                startActivity(dialIntent);
            }
        });

        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_CALL);
                dialIntent.setData(Uri.parse("tel:9678436389"));
                startActivity(dialIntent);
            }
        });
    }
}
