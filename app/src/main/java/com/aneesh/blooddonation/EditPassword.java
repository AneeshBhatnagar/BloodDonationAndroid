package com.aneesh.blooddonation;

import android.app.ProgressDialog;
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


public class EditPassword extends ActionBarActivity {
    private static final String TAG = EditPassword.class.getSimpleName();
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        final EditText currentPassword, newPass1, newPass2;
        Button submit;

        final ProgressDialog pDialog;
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setTitle("Updating Password!");
        pDialog.setMessage("Loading...");

        currentPassword = (EditText) findViewById(R.id.changepass_current);
        newPass1 = (EditText) findViewById(R.id.changepass_new1);
        newPass2 = (EditText) findViewById(R.id.changepass_new2);

        submit = (Button) findViewById(R.id.changepass_submit);
        session = new SessionManager(getApplicationContext());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPassword.getText().toString().isEmpty() || newPass1.getText().toString().isEmpty() || newPass2.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter all the fields here!", Toast.LENGTH_LONG).show();
                } else {
                    final String pass1, pass2, oldPass;
                    oldPass = currentPassword.getText().toString();
                    pass1 = newPass1.getText().toString();
                    pass2 = newPass2.getText().toString();
                    if (!pass1.equals(pass2)) {
                        Toast.makeText(getApplicationContext(), "New Passwords do not match!", Toast.LENGTH_LONG).show();
                    } else {
                        pDialog.show();
                        String tag_string_req = "req_editPass";
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
                                        String errorMsg = jObj.getString("error_msg");
                                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                                        // Launch Home activity
                                        finish();
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
                                Log.e(TAG, "Request Blood Error: " + error.getMessage());
                                Toast.makeText(getApplicationContext(),
                                        error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                // Posting parameters to login url
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("tag", "changepass");
                                params.put("uuid", user);
                                params.put("password", oldPass);
                                params.put("newpass", pass1);

                                return params;
                            }

                        };

                        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                    }
                }
            }
        });

    }
}
