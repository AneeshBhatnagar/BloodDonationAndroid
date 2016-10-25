package com.aneesh.blooddonation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.aneesh.blooddonation.app.AppConfig;
import com.aneesh.blooddonation.app.AppController;
import com.aneesh.blooddonation.helper.SessionManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SignIn extends ActionBarActivity {
    private static final String TAG = SignIn.class.getSimpleName();
    private Button forgot, signin;
    private EditText emailid, pwd;
    private ProgressDialog pDialog;
    private SessionManager session;
    GoogleCloudMessaging gcmObj;
    Context applicationContext;
    String regId = "NULL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        applicationContext = getApplicationContext();
        forgot = (Button) findViewById(R.id.signin_forgot);
        signin = (Button) findViewById(R.id.signin_submit);
        emailid = (EditText) findViewById(R.id.signin_email);
        pwd = (EditText) findViewById(R.id.signin_password);

        //Progress Dialog to show login Process
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Session Manager variable to check if user is already logged in
        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            pDialog.setMessage("Logging you in automatically...");
            showDialog();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideDialog();
                    Intent intent = new Intent(SignIn.this, Home.class);
                    SignIn.this.startActivity(intent);
                    finish();
                }
            },2000);
        }

        //OnClick Actions for the two buttons

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(SignIn.this, Forgot.class);
                SignIn.this.startActivity(mainIntent);
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailid.getText().toString();
                String pass = pwd.getText().toString();
                if ((email.trim().length() > 0) && (pass.trim().length() > 0)) {
                    checkLogin(email, pass);
                    /*
                    Intent mainIntent = new Intent(SignIn.this, Home.class);
                    SignIn.this.startActivity(mainIntent);
                    */
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter proper credentials!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");

                    // Check for error node in json
                    if (error.equalsIgnoreCase("0")) {
                        // user successfully logged in
                        // Create login session
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String fnames = user.getString("fname");
                        String lnames = user.getString("lname");
                        String gcmid = user.getString("gcmid");
                        session.setName(fnames + " " + lnames);
                        session.setUID(uid);

                        if(!gcmid.equals("NULL")) {
                            hideDialog();
                            session.setLogin(true);

                            Intent intent = new Intent(SignIn.this,
                                    Home.class);
                            startActivity(intent);
                            finish();
                        }else{
                            registerWithServer(uid);
                        }




                        // Launch Home activity

                    } else {
                        hideDialog();
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
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void registerWithServer(final String uid) {
        final String tag_string_req = "req_gcm";
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcmObj == null) {
                        gcmObj = GoogleCloudMessaging
                                .getInstance(applicationContext);
                    }
                    regId = gcmObj.register(AppConfig.GOOGLE_PROJ_ID);
                    msg = "Registration ID :" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                hideDialog();
                if (!TextUtils.isEmpty(regId)) {
                    StringRequest strReq = new StringRequest(Request.Method.POST,
                            AppConfig.URL_REGISTER, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "GCM Update Response: " + response.toString());

                            try {
                                JSONObject jObj = new JSONObject(response);
                                String error = jObj.getString("error");

                                // Check for error node in json
                                if (error.equalsIgnoreCase("0")) {


                                        session.setLogin(true);
                                        Intent intent = new Intent(SignIn.this,
                                                Home.class);
                                        startActivity(intent);
                                        finish();
                                    // Launch Home activity

                                } else {
                                    // Error in login. Get the error message
                                    String errorMsg = jObj.getString("error_msg");
                                    Toast.makeText(getApplicationContext(),
                                            "Error Signing you in. Please try later!", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                // JSON error
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Login Error: " + error.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_LONG).show();
                            hideDialog();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            // Posting parameters to login url
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("tag", "gcmreg");
                            params.put("uid", uid);
                            params.put("gcmid", regId);

                            return params;
                        }

                    };

                    // Adding request to request queue
                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                } else {
                    Toast.makeText(
                            applicationContext,
                            "Error Logging you in!", Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
