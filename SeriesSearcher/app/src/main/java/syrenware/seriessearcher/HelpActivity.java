package syrenware.seriessearcher;

import android.os.Bundle;
import android.widget.Toast;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_help);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to HelpActivity
            super.onCreateDrawer();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
