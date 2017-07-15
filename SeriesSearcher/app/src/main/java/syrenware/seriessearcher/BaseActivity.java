package syrenware.seriessearcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by matthew on 2017/02/04.
 * Class provides a base for the NavigationDrawer that is shared amongst the activities
 */

public class BaseActivity extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Declarations
    private NavigationView navigationView;

    protected void onCreateDrawer() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayUserDetails();

        ImageButton btnMenu = (ImageButton) findViewById(R.id.button_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawer(v);
            }
        });
    }

    //Method displays the user's details in the NavigationDrawer
    public void displayUserDetails(){
        try{
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            View view =  navigationView.getHeaderView(0);
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(new User(this).getUserEmailAddress());
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method opens the NavigationDrawer when the menu button is clicked
    public void toggleDrawer(View view){
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

    @SuppressWarnings("StatementWithEmptyBody")
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