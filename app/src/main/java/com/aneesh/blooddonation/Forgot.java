package com.aneesh.blooddonation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.aneesh.blooddonation.app.AppConfig;
import com.aneesh.blooddonation.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Forgot extends ActionBarActivity {
    private static final String TAG = Forgot.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setTitle("Requesting Password Reset!");
        pDialog.setMessage("Loading...");
        Button submit = (Button) findViewById(R.id.forgot_button);
        final EditText email = (EditText) findViewById(R.id.forgot_email);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!email.getText().toString().isEmpty()) {
                    final String email_reset = email.getText().toString();
                    String tag_string_req = "req_forgot";
                    pDialog.show();

                    StringRequest strReq = new StringRequest(Request.Method.POST,
                            AppConfig.URL_FORGOT, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "Login Response: " + response.toString());
                            pDialog.hide();

                            try {
                                JSONObject jObj = new JSONObject(response);
                                String error = jObj.getString("error");
                                // Check for error node in json
                                if (error.equalsIgnoreCase("0")) {

                                    Toast.makeText(getApplicationContext(), "Password Reset email sent successfully!", Toast.LENGTH_LONG).show();
                                    // Launch Home activity
                                    Intent intent = new Intent(Forgot.this,
                                            MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Error in login. Get the error message
                                    String errorMsg = jObj.getString("error_msg");
                                    Toast.makeText(getApplicationContext(),
                                            errorMsg, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                // JSON error
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Forgot Password Error: " + error.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            // Posting parameters to login url
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("tag", "forgot");
                            params.put("email", email_reset);

                            return params;
                        }

                    };

                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

                    /*
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.hide();
                            Toast.makeText(getApplicationContext(),"Password Reset Link sent!", Toast.LENGTH_LONG).show();
                        }
                    },2500);*/

                } else {
                    Toast.makeText(getApplicationContext(), "Please enter your email ID", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
