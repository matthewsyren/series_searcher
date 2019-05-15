package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
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
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Class provides a base for the NavigationDrawer that is shared amongst the Activities
 */

public class BaseActivity
        extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //View bindings
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    //Variables
    private IOnDataSavingPreferenceChangedListener mOnDataSavingPreferenceChangedListener;

    /**
     * Sets up the Activity
     * @param iOnDataSavingPreferenceChangedListener The instance of the IOnDataSavingPreferenceChangedListener class which is to be notified when data saving preferences are changed
     */
    protected void onCreateDrawer(IOnDataSavingPreferenceChangedListener iOnDataSavingPreferenceChangedListener) {
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        displayUserDetails();

        //Assigns a Listener for when the user's data saving preference changes
        mOnDataSavingPreferenceChangedListener = iOnDataSavingPreferenceChangedListener;

        //Registers the appropriate listeners
        registerListeners();
    }

    /**
     * Registers listeners for the appropriate Views
     */
    protected void registerListeners(){
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

        //Registers a Listener if the user has signed in and verified their email address
        if(UserAccountUtilities.getUserKey(getApplicationContext()) != null){
            //Initialises variables
            final Menu menu = mNavigationView.getMenu();
            final MenuItem menuItem = menu.findItem(R.id.nav_data_saving_mode);

            //Displays the appropriate value for the Data Saving Mode Switch (checked or not checked)
            View actionView = menuItem.getActionView();
            final Switch navSwitch = actionView.findViewById(R.id.switch_data_saving_mode);
            navSwitch.setChecked(UserAccountUtilities.getDataSavingPreference(this));

            //Registers an OnCheckedChangedListener for the Data Saving Mode Switch
            navSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //Toggles the user's data saving preference
                    UserAccountUtilities.toggleDataSavingPreference(buttonView.getContext());

                    //Updates the images that are currently being displayed
                    if(mOnDataSavingPreferenceChangedListener != null){
                        mOnDataSavingPreferenceChangedListener.onDataSavingPreferenceChanged();
                    }

                    //Closes the NavigationDrawer
                    closeNavigationDrawer();
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
    }

    /**
     * Displays the user's details in the NavigationDrawer
     */
    protected void displayUserDetails(){
        View view = mNavigationView.getHeaderView(0);
        TextView textView = view.findViewById(R.id.textView);
        String emailAddress = UserAccountUtilities.getUserEmailAddress();

        if(emailAddress != null){
            textView.setText(emailAddress);
        }
    }

    /**
     * Sets the selected item in the Navigation Drawer
     * @param id The ID of the item in the Navigation Drawer that is selected
     */
    protected void setSelectedNavItem(int id){
        mNavigationView.setCheckedItem(id);
    }

    @Override
    public void onBackPressed() {
        //Either closes the Navigation Drawer (if it is open), or calls super.onBackPressed()
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeNavigationDrawer();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Gets the ID of the selected item and initialises an Intent variable
        int id = item.getItemId();
        Intent intent = null;

        if(UserAccountUtilities.getUserKey(getApplicationContext()) != null || id == R.id.nav_sign_out){
            //Assigns the appropriate value to the intent variable based on which item was clicked
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
                    //Signs the user out
                    AuthUI.getInstance()
                            .signOut(this);

                    //Sets the intent variable
                    intent = new Intent(getApplicationContext(), HomeActivity.class);
                    break;
            }
        }
        else{
            //Displays a message to the user telling them to verify their email address
            Toast.makeText(getApplicationContext(), R.string.error_email_not_verified, Toast.LENGTH_LONG).show();

            //Closes NavigationDrawer
            closeNavigationDrawer();
            return false;
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

    /**
     * Closes the NavigationDrawer
     */
    protected void closeNavigationDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START, true);
    }

    /**
     * Interface is used to tell the appropriate Activities when the data saving preferences are changed
     */
    protected interface IOnDataSavingPreferenceChangedListener {
        /**
         * Executed when the user changes their data saving preference
         */
        void onDataSavingPreferenceChanged();
    }
}