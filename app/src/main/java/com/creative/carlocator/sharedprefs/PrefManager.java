package com.creative.carlocator.sharedprefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;


public class PrefManager {
    private static final String TAG = PrefManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "com.creative.namajihelper";


    private static final String KEY_LOGIN_TYPE = "login_type";


    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

    }

    public void setLoginType(String type) {
        editor = pref.edit();

        editor.putString(KEY_LOGIN_TYPE, type);

        // commit changes
        editor.commit();
    }

    public String getLoginType() {
        return pref.getString(KEY_LOGIN_TYPE, "");
    }

    public void setLatList(ArrayList<String> list) {
        editor = pref.edit();
        editor.putInt("Count", list.size());
        int count = 0;
        for (String i : list) {
            editor.putString("lat_" + count++, i);
        }

        editor.commit();
    }

    public void setLngList(ArrayList<String> list) {
        editor = pref.edit();
        editor.putInt("Count", list.size());
        int count = 0;
        for (String i : list) {
            editor.putString("lng_" + count++, i);
        }

        editor.commit();
    }

    public ArrayList<String> getLatList() {


        ArrayList<String> temp = new ArrayList<String>();

        int count = pref.getInt("Count", 0);
        temp.clear();
        for (int i = 0; i < count; i++) {
            temp.add(pref.getString("lat_" + i, ""));
        }
        return temp;
    }

    public ArrayList<String> getLngList() {
        ArrayList<String> temp = new ArrayList<String>();

        int count = pref.getInt("Count", 0);
        temp.clear();
        for (int i = 0; i < count; i++) {
            temp.add(pref.getString("lng_" + i, ""));
        }
        return temp;
    }


}