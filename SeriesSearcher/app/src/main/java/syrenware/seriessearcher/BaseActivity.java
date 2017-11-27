package syrenware.seriessearcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by matthew on 2017/02/04.
 * Class provides a base for the NavigationDrawer that is shared amongst the activities
 */

public class BaseActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Declarations
    private NavigationView navigationView;

    protected void onCreateDrawer() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayUserDetails();
        registerListeners();
        setCurrentSelectedNavItem();

        //Creates a Listener for the DrawerLayout (used to set selected item when the user drags to open the NavigationDrawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                setCurrentSelectedNavItem();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    //Method registers listeners for the appropriate Views
    public void registerListeners(){
        try{
            //Registers an OnClickListener for the NavigationDrawer button
            ImageButton btnMenu = findViewById(R.id.button_menu);
            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleDrawer(v);
                }
            });

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
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method sets the activated item of the NavigationDrawer to the current Activity
    public void setCurrentSelectedNavItem(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        ComponentName componentName = taskInfo.get(0).topActivity;
        String currentActivity = componentName.getShortClassName();

        //Sets selected item
        switch (currentActivity){
            case ".HomeActivity" :
                setSelectedNavItem(R.id.nav_home);
                break;
            case ".RandomShowsActivity":
                setSelectedNavItem(R.id.nav_random_shows);
                break;
            case ".SearchActivity":
                setSelectedNavItem(R.id.nav_search);
                break;
            case ".HelpActivity":
                setSelectedNavItem(R.id.nav_help);
                break;
            case ".DisclaimerActivity":
                setSelectedNavItem(R.id.nav_disclaimer);
                break;
        }
    }

    //Method changes the user's preferences for Data Saving Mode
    public void toggleDataSavingPreference(){
        try{
            //Fetches the user's current preferences for Data Saving Mode
            boolean currentPreference = User.getDataSavingPreference(getApplicationContext());

            //Saves the user's new preference for Data Saving Mode
            SharedPreferences preferences = getSharedPreferences("", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dataSavingMode", !currentPreference);
            editor.apply();
            if(!currentPreference){
                Toast.makeText(getApplicationContext(), "Data Saving Mode activated - Images will not be downloaded while in Data Saving Mode", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Data Saving Mode deactivated", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the user's details in the NavigationDrawer
    public void displayUserDetails(){
        try{
            NavigationView navigationView = findViewById(R.id.nav_view);
            View view =  navigationView.getHeaderView(0);
            TextView textView = view.findViewById(R.id.textView);
            textView.setText(new User(this).getUserEmailAddress());
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method opens the NavigationDrawer when the menu button is clicked
    public void toggleDrawer(View view){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(GravityCompat.START);
    }

    //Method sets the selected item in the Navigation Drawer
    public void setSelectedNavItem(int id){
        navigationView.setCheckedItem(id);
    }

    //Method takes user to All Shows activity
    public void openAllShows(View view) {
        Intent intent = new Intent(this, RandomShowsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_home){
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
        if(id == R.id.nav_random_shows){
            startActivity(new Intent(getApplicationContext(), RandomShowsActivity.class));
        }
        else if(id == R.id.nav_search){
            startActivity(new Intent(getApplicationContext(), SearchActivity.class));
        }
        else if(id == R.id.nav_disclaimer){
            startActivity(new Intent(getApplicationContext(), DisclaimerActivity.class));
        }
        else if(id == R.id.nav_help){
            startActivity(new Intent(getApplicationContext(), HelpActivity.class));
        }
        else if(id == R.id.nav_data_saving_mode){
            setCurrentSelectedNavItem();
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
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        //Closes NavigationDrawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}