package com.aneesh.blooddonation;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;


public class NotificationReceived extends ActionBarActivity {
    private TextView blood,contact,location;
    private Button callNow;
    private String bloodGroup, contactDetails , locationDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_received);
        bloodGroup = contactDetails = locationDetails = "UNDEFINED";
        Intent i = getIntent();
        String json = i.getStringExtra("jsonMessage");
        blood = (TextView) findViewById(R.id.notif_bgroup);
        contact = (TextView) findViewById(R.id.notif_contact);
        location = (TextView) findViewById(R.id.notif_location);
        callNow = (Button) findViewById(R.id.notif_callNow);
        callNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel: "+contactDetails));
                startActivity(i);
            }
        });
        try{
            JSONObject jObj = new JSONObject(json);
            bloodGroup = jObj.getString("bgroup");
            contactDetails = jObj.getString("contact");
            locationDetails = jObj.getString("location");
        }catch (Exception e){
            e.printStackTrace();
        }
        blood.setText("Blood Group: "+ bloodGroup);
        contact.setText("Contact Number: " + contactDetails);
        location.setText("Location: " + locationDetails);
        location.setText("Location: " + locationDetails);
    }
}
