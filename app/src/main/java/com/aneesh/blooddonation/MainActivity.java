package com.aneesh.blooddonation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sign, register;

        sign = (Button) findViewById(R.id.buttonSignIn);
        register = (Button) findViewById(R.id.buttonRegister);

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(MainActivity.this, SignIn.class);
                MainActivity.this.startActivity(mainIntent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(MainActivity.this, Register.class);
                MainActivity.this.startActivity(mainIntent);
            }
        });
    }


}
