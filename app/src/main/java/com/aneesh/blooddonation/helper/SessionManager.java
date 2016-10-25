package com.aneesh.blooddonation.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "BloodBank";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_UUID = "uuid";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.commit();
    }

    public void setUID(String uuid) {
        editor.putString(KEY_UUID, uuid);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public String getName() {
        return pref.getString(KEY_USER_NAME, "Username");
    }

    public String getUID() {
        return pref.getString(KEY_UUID, "0");
    }
}