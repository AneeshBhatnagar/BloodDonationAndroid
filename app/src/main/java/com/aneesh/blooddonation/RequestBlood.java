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
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;


public class RequestBlood extends ActionBarActivity {
    private static final String TAG = RequestBlood.class.getSimpleName();
    EditText bgroup, contact, location;
    Button requestNow;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_blood);
        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setTitle("Requesting Blood!");
        pDialog.setMessage("Loading...");
        bgroup = (EditText) findViewById(R.id.request_bgroup);
        contact = (EditText) findViewById(R.id.request_contact);
        location = (EditText) findViewById(R.id.request_location);
        requestNow = (Button) findViewById(R.id.request_submit);
        session = new SessionManager(getApplicationContext());

        requestNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bgroup.getText().toString().isEmpty() || contact.getText().toString().isEmpty() || location.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_LONG).show();
                } else {
                    pDialog.show();
                    String tag_string_req = "req_blood_request";
                    final String bloodGroup = bgroup.getText().toString(), phone = contact.getText().toString(), address = location.getText().toString();
                    final String senderID = session.getUID();
                    StringRequest strReq = new StringRequest(Request.Method.POST,
                            AppConfig.URL_REQUEST, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "Blood Request Response: " + response.toString());
                            pDialog.hide();

                            try {
                                JSONObject jObj = new JSONObject(response);
                                String error = jObj.getString("error");
                                // Check for error node in json
                                if (error.equalsIgnoreCase("0")) {
                                    String errorMsg = jObj.getString("error_msg");
                                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                                    // Launch Home activity
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
                            Log.e(TAG, "Request Blood Error: " + error.getMessage());
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            // Posting parameters to login url
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("tag", "request");
                            params.put("bgroup", bloodGroup);
                            params.put("sender", senderID);
                            params.put("location", address);
                            params.put("contact", phone);

                            return params;
                        }

                    };

                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                }
            }
        });

    }

}
