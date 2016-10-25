package com.aneesh.blooddonation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.aneesh.blooddonation.app.AppConfig;
import com.aneesh.blooddonation.app.AppController;
import com.aneesh.blooddonation.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


public class UserProfile extends ActionBarActivity {
    TextView name, email, phone, bgroup, editPassword;
    CheckBox emailpref, notifgroup, phonepref;
    Button updatePref;
    String nameval = "Name: ", emailval = "Email: ", phoneval = "Phone: ", bgroupval = "Blood Group: ";
    boolean emailprefval = false, phoneprefval = false, notifgroupval = false;
    ProgressDialog pDialog;
    private static final String TAG = EditPassword.class.getSimpleName();
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setTitle("Fetching Information");
        pDialog.setMessage("Loading...");
        session = new SessionManager(getApplicationContext());


        name = (TextView) findViewById(R.id.userprofile_name);
        email = (TextView) findViewById(R.id.userprofile_email);
        phone = (TextView) findViewById(R.id.userprofile_phone);
        bgroup = (TextView) findViewById(R.id.userprofile_bgroup);
        editPassword = (TextView) findViewById(R.id.userprofile_passwordEdit);

        emailpref = (CheckBox) findViewById(R.id.userprofile_emailpref);
        notifgroup = (CheckBox) findViewById(R.id.userprofile_notifgroup);
        phonepref = (CheckBox) findViewById(R.id.userprofile_phonepref);

        updatePref = (Button) findViewById(R.id.userprofile_update);
        updatePref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ePref, pPref, nPref;
                if (emailpref.isChecked()) {
                    ePref = "1";
                } else {
                    ePref = "0";
                }
                if (phonepref.isChecked()) {
                    pPref = "1";
                } else {
                    pPref = "0";
                }
                if (notifgroup.isChecked()) {
                    nPref = "1";
                } else {
                    nPref = "0";
                }
                updatePreferencesOnServer(ePref, pPref, nPref);
            }
        });
        editPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next = new Intent(UserProfile.this, EditPassword.class);
                startActivity(next);
            }
        });

        getInfoFromServer();

    }

    private void updatePreferencesOnServer(final String ePref, final String pPref, final String nPref) {
        pDialog.setTitle("Updating Preferences");
        pDialog.show();
        String tag_string_req = "req_updatepref";
        final String user = session.getUID();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_USERINFO, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update User Profile Response: " + response);
                pDialog.hide();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");
                    // Check for error node in json
                    if (error.equalsIgnoreCase("0")) {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    } else {

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
                pDialog.hide();
                Log.e(TAG, "User Profile Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Error connecting to the Server!", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "updateuser");
                params.put("uuid", user);
                params.put("emailpref", ePref);
                params.put("phonepref", pPref);
                params.put("notifgroup", nPref);
                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void getInfoFromServer() {
        pDialog.show();
        String tag_string_req = "req_userinfo";
        final String user = session.getUID();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_USERINFO, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Blood Request Response: " + response);
                pDialog.hide();

                try {
                    JSONObject jObj = new JSONObject(response);
                    String error = jObj.getString("error");
                    // Check for error node in json
                    if (error.equalsIgnoreCase("0")) {
                        nameval = nameval + jObj.getString("fname") + " " + jObj.getString("lname");
                        emailval = emailval + jObj.getString("email");
                        phoneval = phoneval + jObj.getString("phone");
                        bgroupval = bgroupval + jObj.getString("bloodgroup");
                        if (jObj.getString("emailpref").equals("1")) {
                            emailprefval = true;
                        }
                        if (jObj.getString("phonepref").equals("1")) {
                            phoneprefval = true;
                        }
                        if (jObj.getString("notifgroup").equals("1")) {
                            notifgroupval = true;
                        }
                        setViewValues();
                    } else {

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
                pDialog.hide();
                Log.e(TAG, "User Profile Fetch Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Error connecting to the Server!", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "userinfo");
                params.put("uuid", user);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void setViewValues() {
        name.setText(nameval);
        phone.setText(phoneval);
        email.setText(emailval);
        bgroup.setText(bgroupval);
        emailpref.setChecked(emailprefval);
        phonepref.setChecked(phoneprefval);
        notifgroup.setChecked(notifgroupval);
    }

}
