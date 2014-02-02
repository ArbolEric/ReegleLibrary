package com.arbol.reegle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.arbol.reegle.fragments.*;
import android.support.v4.app.FragmentManager;

/**
 * Created by user on 12/23/13.
 */

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class MainActivity extends ActionBarActivity implements
        ReegleStuffFragment.ReegleStuffListener {
    private FragmentManager fm;
    public static final String SETTINGS = "Settings";
    public static final String DATA_STUFFED = "dataStuffed?";
    private TabManagerFragment managerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        if (prefs.getBoolean(DATA_STUFFED, false)) {
            renderTabs();
        } else {
            stuffReegle();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean("initiated", true);
    }

    /*
     * Custom Methods
     */

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void toaster(String s){
        final Activity activity = this;
        final String msg = s;
        runOnUiThread(new Runnable() {
            public void run() {
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(activity, msg, duration);
                toast.show();
            }
        });
    }

    public TabManagerFragment currentManagerFragment(){
        return managerFragment;
    }

    private void stuffReegle(){
        setContentView(R.layout.main);
        FragmentManager fm = getSupportFragmentManager();
        ReegleStuffFragment reegleStuffFragment = (ReegleStuffFragment) fm.findFragmentByTag(ReegleStuffFragment.TAG);
        if (reegleStuffFragment == null){
            reegleStuffFragment = new ReegleStuffFragment();
            fm.beginTransaction().add(R.id.fragment_container, reegleStuffFragment, ReegleStuffFragment.TAG).commit();
        }
    }

    private void renderTabs(){
        setContentView(R.layout.main_tabs);
        // Remove ReegleStuffFragment
        FragmentManager fm = getSupportFragmentManager();
        managerFragment = (TabManagerFragment) fm.findFragmentByTag(TabManagerFragment.TAG);
        if (managerFragment == null){
            // create new TabManagerFragment
            managerFragment = new TabManagerFragment();
            fm.beginTransaction().add(managerFragment, TabManagerFragment.TAG).commit();
        }
    }

    /*
     * Implement ReegleDbStuff.ManagerInterface
     */

    @Override
    public void dataStuffed() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().remove(fm.findFragmentByTag(ReegleStuffFragment.TAG)).commit();
        // set SharedPreference dbCreated? to true
        SharedPreferences prefs = getSharedPreferences(SETTINGS, MODE_PRIVATE);
        prefs.edit().putBoolean(DATA_STUFFED, true).commit();
        renderTabs();
    }

    @Override
    public void reegleStuffError(String err) {
        String msg = err + "\nPlease hit the retry button.";
        TextView vErrorMsg = (TextView) findViewById(R.id.reegle_stuff_error_msg);
        vErrorMsg.setText(msg);

        findViewById(R.id.reegle_stuff_error).setVisibility(View.VISIBLE);
        findViewById(R.id.wait_for_reegle_stuff).setVisibility(View.GONE);
    }

}
