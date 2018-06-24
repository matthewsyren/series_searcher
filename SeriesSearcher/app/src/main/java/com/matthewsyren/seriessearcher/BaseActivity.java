package com.matthewsyren.seriessearcher;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by matthew on 2017/02/04.
 * Class provides a base for the NavigationDrawer that is shared amongst the activities
 */

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Declarations
    private NavigationView navigationView;

    protected void onCreateDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        displayUserDetails();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        registerListeners();
    }

    //Method registers listeners for the appropriate Views
    public void registerListeners(){
        //Registers an OnCheckedChangedListener for the Data Saving Mode Switch, and displays the appropriate
        final Menu menu = navigationView.getMenu();
        final MenuItem menuItem = menu.findItem(R.id.nav_data_saving_mode);

        View actionView = menuItem.getActionView();
        final Switch navSwitch = actionView.findViewById(R.id.switch_data_saving_mode);
        navSwitch.setChecked(User.getDataSavingPreference(this));

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
        boolean currentPreference = User.getDataSavingPreference(getApplicationContext());

        //Saves the user's new preference for Data Saving Mode
        SharedPreferences preferences = getSharedPreferences("", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("dataSavingMode", !currentPreference);
        editor.apply();

        //Displays a message confirming the user's action
        if(!currentPreference){
            Toast.makeText(getApplicationContext(), "Data Saving Mode activated - Images will not be downloaded while in Data Saving Mode", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Data Saving Mode deactivated", Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the user's details in the NavigationDrawer
    public void displayUserDetails(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View view =  navigationView.getHeaderView(0);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(new User(this).getUserEmailAddress());
    }

    //Method sets the selected item in the Navigation Drawer
    public void setSelectedNavItem(int id){
        navigationView.setCheckedItem(id);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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

        if(id == R.id.nav_home){
            intent = new Intent(getApplicationContext(), HomeActivity.class);
        }
        else if(id == R.id.nav_random_shows){
            intent = new Intent(getApplicationContext(), RandomShowsActivity.class);
        }
        else if(id == R.id.nav_search){
            intent = new Intent(getApplicationContext(), SearchActivity.class);
        }
        else if(id == R.id.nav_disclaimer){
            intent = new Intent(getApplicationContext(), DisclaimerActivity.class);
        }
        else if(id == R.id.nav_help){
            intent = new Intent(getApplicationContext(), HelpActivity.class);
        }
        else if(id == R.id.nav_data_saving_mode){
            return false;
        }
        else if(id == R.id.nav_sign_out){
            //Signs the user out of Firebase
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signOut();

            //Removes the user's email and key from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("userEmail", null);
            editor.putString("userKey", null);
            editor.apply();

            //Takes the user back to the LoginActivity
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            });
        }

        //Takes the user to the appropriate Activity
        if(intent != null){
            finish();
            startActivity(intent);
        }

        //Closes NavigationDrawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}