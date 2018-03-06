package syrenware.seriessearcher;

import android.os.Bundle;
import android.widget.Toast;

public class DisclaimerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_disclaimer);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to DisclaimerActivity
            super.onCreateDrawer();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
