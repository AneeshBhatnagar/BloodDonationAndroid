package com.aneesh.blooddonation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.aneesh.blooddonation.app.AppConfig;
import com.aneesh.blooddonation.app.AppController;
import com.aneesh.blooddonation.helper.SQLiteHandler;
import com.aneesh.blooddonation.helper.SessionManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Register extends ActionBarActivity {
    private EditText fname, lname, email, password, password2, bgroup, phone, city;
    private CheckBox forall, vphone, emailpref;
    private static final String TAG = Register.class.getSimpleName();
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    GoogleCloudMessaging gcmObj;
    Context applicationContext;
    String regId = "NULL";
    private String toastMsg = "Please enter all fields Correctly!";
    String fnames, lnames, pass1s, pass2s, bgroups, citys, phones, emails;
    int all = 0, visible = 0, email_to = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        applicationContext = getApplicationContext();
        Button register;
        SessionManager session;
        //Variable Mapping
        fname = (EditText) findViewById(R.id.register_fname);
        lname = (EditText) findViewById(R.id.register_lname);
        email = (EditText) findViewById(R.id.register_email);
        password = (EditText) findViewById(R.id.register_pass);
        password2 = (EditText) findViewById(R.id.register_pass2);
        bgroup = (EditText) findViewById(R.id.register_blood);
        phone = (EditText) findViewById(R.id.register_phone);
        city = (EditText) findViewById(R.id.register_city);
        forall = (CheckBox) findViewById(R.id.register_notifall);
        vphone = (CheckBox) findViewById(R.id.register_phonevi);
        emailpref = (CheckBox) findViewById(R.id.register_emailalerts);
        register = (Button) findViewById(R.id.register_submit);

        //Initializing Database and Progress Dialog Bar
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());

        db = new SQLiteHandler(getApplicationContext());

        if (session.isLoggedIn()) {
            Toast.makeText(getApplicationContext(),"You are already logged In!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Register.this,
                    Home.class);
            startActivity(intent);
            finish();
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fnames = fname.getText().toString();
                lnames = lname.getText().toString();
                pass1s = password.getText().toString();
                pass2s = password2.getText().toString();
                bgroups = bgroup.getText().toString();
                citys = city.getText().toString();
                phones = phone.getText().toString();
                emails = email.getText().toString();

                if (forall.isChecked())
                    all = 1;
                if (vphone.isChecked())
                    visible = 1;
                if (emailpref.isChecked())
                    email_to = 1;
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String msg = "";
                        try {
                            if (gcmObj == null) {
                                gcmObj = GoogleCloudMessaging
                                        .getInstance(applicationContext);
                            }
                            regId = gcmObj
                                    .register(AppConfig.GOOGLE_PROJ_ID);
                            msg = "Registration ID :" + regId;

                        } catch (IOException ex) {
                            msg = "Error :" + ex.getMessage();
                        }
                        return msg;
                    }

                    @Override
                    protected void onPostExecute(String msg) {
                        if (!TextUtils.isEmpty(regId)) {
                            if (checkRegFields(fnames, lnames, pass1s, pass2s, bgroups, citys, phones, emails)) {
                                registerUser(fnames, lnames, pass1s, bgroups, citys, phones, emails, all, visible, email_to);
                            } else {
                                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(
                                    applicationContext,
                                    "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time."
                                            + msg, Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute(null, null, null);
            }
        });
    }

    private boolean checkRegFields(String fnames, String lnames, String pass1s, String pass2s, String bgroups, String citys, String phones, String emails) {

        if (fnames.isEmpty() || lnames.isEmpty() || pass1s.isEmpty() || pass2s.isEmpty() || bgroups.isEmpty() || citys.isEmpty() || phones.isEmpty() || emails.isEmpty()) {
            toastMsg = "Please enter all the fields for the form!";
            return false;
        }
        if (!pass1s.equals(pass2s)) {
            toastMsg = "Both the passwords don't match!";
            return false;
        }
        if (!isValidPhone(phones)) {
            toastMsg = "The phone number entered is invalid";
            return false;
        }
        return true;
    }

    private void registerUser(final String fnames, final String lnames, final String pass1s, final String bgroups, final String citys, final String phones, final String emails, final int foralls, final int vphones, final int emailprefs) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering you... Please Wait");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");
                    if (error.equals("0")){
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String fname = user.getString("fname");
                        String lname = user.getString("lname");
                        String email = user.getString("email");

                        // Inserting row in users table
                        db.addUser(fname, lname, email, uid);

                        // Launch login activity
                        Intent intent = new Intent(
                                Register.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "register");
                params.put("fname", fnames);
                params.put("lname", lnames);
                params.put("email", emails);
                params.put("password", pass1s);
                params.put("bgroup", bgroups);
                params.put("phone", phones);
                params.put("city", citys);
                params.put("forall", Integer.toString(foralls));
                params.put("vphone", Integer.toString(vphones));
                params.put("emailpref", Integer.toString(emailprefs));
                params.put("gcmid",regId);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private boolean isValidPhone(String phone) {
        String regex = "[0-9]+";
        return phone.matches(regex);
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
