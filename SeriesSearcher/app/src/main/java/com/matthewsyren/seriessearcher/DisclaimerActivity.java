package com.matthewsyren.seriessearcher;

import android.os.Bundle;

public class DisclaimerActivity
        extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to DisclaimerActivity
        super.setSelectedNavItem(R.id.nav_disclaimer);
    }
}