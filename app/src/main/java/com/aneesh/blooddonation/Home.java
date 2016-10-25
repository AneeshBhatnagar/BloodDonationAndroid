package com.aneesh.blooddonation;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aneesh.blooddonation.helper.SQLiteHandler;
import com.aneesh.blooddonation.helper.SessionManager;


public class Home extends ActionBarActivity {
    private Button logout,request,viewProfile,nearest;
    private SQLiteHandler db;
    private SessionManager session;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        logout = (Button) findViewById(R.id.home_logout);
        tv = (TextView) findViewById(R.id.home_welcome);
        request = (Button) findViewById(R.id.home_request);
        viewProfile = (Button) findViewById(R.id.home_profile);
        nearest = (Button) findViewById(R.id.home_nearest);

        //Initializing Database and Session manager
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        String name = session.getName();
        tv.setText("Welcome, "+ name);


        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this,
                        UserProfile.class);
                startActivity(intent);
            }
        });
        nearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=blood+in+Delhi+or+Noida");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this,
                        RequestBlood.class);
                startActivity(intent);
            }
        });
    }

    private void logoutUser(){
        Toast.makeText(getApplicationContext(),"Logged Out!",Toast.LENGTH_LONG).show();
        session.setLogin(false);
        db.deleteUsers();

        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
