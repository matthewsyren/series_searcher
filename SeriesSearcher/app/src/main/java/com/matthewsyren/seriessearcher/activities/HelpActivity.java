package com.matthewsyren.seriessearcher.activities;

import android.os.Bundle;

import com.matthewsyren.seriessearcher.R;

/**
 * Activity displays help to the user
 */

public class HelpActivity
        extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to HelpActivity
        super.setSelectedNavItem(R.id.nav_help);
    }
}