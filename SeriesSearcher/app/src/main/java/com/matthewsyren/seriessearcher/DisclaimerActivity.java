package com.matthewsyren.seriessearcher;

import android.os.Bundle;
import android.widget.Toast;

public class DisclaimerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_disclaimer);

            //Sets the NavigationDrawer for the Activity
            super.onCreateDrawer();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to DisclaimerActivity
        super.setSelectedNavItem(R.id.nav_disclaimer);
    }
}
