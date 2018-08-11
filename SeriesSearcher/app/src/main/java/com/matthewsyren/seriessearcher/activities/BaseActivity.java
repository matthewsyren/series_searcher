package com.matthewsyren.seriessearcher.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by matthew on 2017/02/04.
 * Class provides a base for the NavigationDrawer that is shared amongst the activities
 */

public class BaseActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //View bindings
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    protected void onCreateDrawer() {
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        displayUserDetails();

        //Registers the appropriate listeners
        registerListeners();
    }

    //Method registers listeners for the appropriate Views
    public void registerListeners(){
        //Sets up the OnItemSelectedListener
        mNavigationView.setNavigationItemSelectedListener(this);

        //Sets up the DrawerListener
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Registers an OnCheckedChangedListener for the Data Saving Mode Switch, and displays the appropriate
        final Menu menu = mNavigationView.getMenu();
        final MenuItem menuItem = menu.findItem(R.id.nav_data_saving_mode);

        View actionView = menuItem.getActionView();
        final Switch navSwitch = actionView.findViewById(R.id.switch_data_saving_mode);
        navSwitch.setChecked(UserAccountUtilities.getDataSavingPreference(this));

        navSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleDataSavingPreference();
            }
        });

        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                navSwitch.performClick();
                return false;
            }
        });
    }

    //Method changes the user's preferences for Data Saving Mode
    public void toggleDataSavingPreference(){
        //Fetches the user's current preferences for Data Saving Mode
        boolean currentPreference = UserAccountUtilities.getDataSavingPreference(getApplicationContext());

        //Saves the user's new preference for Data Saving Mode
        SharedPreferences preferences = getSharedPreferences("", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("dataSavingMode", !currentPreference);
        editor.apply();

        //Displays a message confirming the user's action
        if(!currentPreference){
            Toast.makeText(getApplicationContext(), R.string.data_saving_mode_activated, Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), R.string.data_saving_mode_deactivated, Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the user's details in the NavigationDrawer
    public void displayUserDetails(){
        View view = mNavigationView.getHeaderView(0);
        TextView textView = view.findViewById(R.id.textView);
        String emailAddress = UserAccountUtilities.getUserEmailAddress();

        if(emailAddress != null){
            textView.setText(emailAddress);
        }
    }

    //Method sets the selected item in the Navigation Drawer
    public void setSelectedNavItem(int id){
        mNavigationView.setCheckedItem(id);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeNavigationDrawer();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = null;

        switch (id) {
            case R.id.nav_home:
                intent = new Intent(getApplicationContext(), HomeActivity.class);
                break;
            case R.id.nav_random_shows:
                intent = new Intent(getApplicationContext(), RandomShowsActivity.class);
                break;
            case R.id.nav_search:
                intent = new Intent(getApplicationContext(), SearchActivity.class);
                break;
            case R.id.nav_disclaimer:
                intent = new Intent(getApplicationContext(), DisclaimerActivity.class);
                break;
            case R.id.nav_help:
                intent = new Intent(getApplicationContext(), HelpActivity.class);
                break;
            case R.id.nav_data_saving_mode:
                return false;
            case R.id.nav_sign_out:
                //Signs the user out of Firebase
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signOut();

                //Removes the user's email and key from SharedPreferences
                SharedPreferences preferences = getSharedPreferences("", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userEmail", null);
                editor.putString("userKey", null);
                editor.apply();

                //Signs the user out
                AuthUI.getInstance()
                        .signOut(this);

                intent = new Intent(getApplicationContext(), HomeActivity.class);
                break;
        }

        //Takes the user to the appropriate Activity
        if(intent != null){
            startActivity(intent);
            finish();
        }

        //Closes NavigationDrawer
        closeNavigationDrawer();
        return true;
    }

    //Closes the NavigationDrawer
    protected void closeNavigationDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }
}