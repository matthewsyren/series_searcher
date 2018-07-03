package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.adapters.HomeListViewAdapter;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;
import com.matthewsyren.seriessearcher.services.FirebaseService;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity
        extends BaseActivity
        implements IAPIConnectionResponse {
    //View bindings
    @BindView(R.id.list_view_my_shows) ListView mListViewMyShows;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_no_shows) TextView mTextNoShows;
    @BindView(R.id.button_add_shows) Button mButtonAddShows;

    //Declarations
    private ArrayList<Show> lstShows = new ArrayList<>();
    private HomeListViewAdapter adapter;
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //Request codes
    private static final int SIGN_IN_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer();

        //Restores data if possible
        if(savedInstanceState != null && FirebaseAuth.getInstance().getCurrentUser() != null){
            restoreData(savedInstanceState);
        }

        //Checks if the user is signed in, and signs them in if they aren't
        setUpAuthListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to Home
        super.setSelectedNavItem(R.id.nav_home);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Removes the AuthStateListener
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(lstShows.size() > 0){
            outState.putParcelableArrayList(SHOWS_BUNDLE_KEY, lstShows);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE){
            //Adapted from https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md?Response%20codes#response-codes
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_CANCELED){
                if(response != null && response.getError() != null && response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //Displays a message if there is no Internet connection
                    Toast.makeText(getApplicationContext(), R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
                }
                else{
                    //Displays a message if the user cancels the sign in
                    Toast.makeText(getApplicationContext(), getString(R.string.sign_in_cancelled), Toast.LENGTH_LONG).show();
                }

                //Closes the app
                finish();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();

        switch (id){
            case R.id.nav_sign_out:
                //Signs the user out
                AuthUI.getInstance()
                        .signOut(this);

                //Closes the NavigationDrawer
                super.closeNavigationDrawer();
                return true;
        }
        return super.onNavigationItemSelected(item);
    }

    //Restores any saved data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            lstShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides ProgressBar
            toggleProgressBar(View.GONE);

            //Displays the ListView and hides other unnecessary Views
            toggleViewVisibility(View.VISIBLE,View.INVISIBLE);
        }
    }

    //Checks if the user is signed in, and signs them in if they aren't signed in already
    private void setUpAuthListener(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser == null){
                    //Performs sign out tasks
                    signOut();

                    //Takes the user to the sign in screen
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            SIGN_IN_REQUEST_CODE
                    );
                }
                else{
                    //Requests the user's key if it hasn't been set, otherwise requests their series
                    if(UserAccountUtilities.getUserKey(getApplicationContext()) == null){
                        UserAccountUtilities.requestUserKey(getApplicationContext(), new DataReceiver(new Handler()));
                    }
                    else{
                        setUpActivity();
                    }
                }
            }
        };

        //Adds the AuthStateListener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    //Performs tasks when the user signs out
    private void signOut(){
        //Clears the user's key from SharedPreferences
        UserAccountUtilities.setUserKey(this, null);

        //Clears the user's series
        if(lstShows != null){
            lstShows.clear();

            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }
    }

    //Sets up the Activity once the user signs in
    private void setUpActivity(){
        //Sets a custom adapter for the list_view_search_results ListView to display the search results
        adapter = new HomeListViewAdapter(this, lstShows);
        mListViewMyShows.setAdapter(adapter);

        //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
        mListViewMyShows.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Intent intent = new Intent(HomeActivity.this, SpecificShowActivity.class);
                intent.putExtra("showNumber", "" + lstShows.get(pos).getShowId());
                startActivity(intent);
            }
        });

        if(lstShows.size() == 0){
            //Displays ProgressBar
            toggleProgressBar(View.VISIBLE);

            //Displays the ListView and hides other unnecessary Views
            toggleViewVisibility(View.VISIBLE,View.INVISIBLE);

            //Gets the unique key used by Firebase to store information about the user signed in, and fetches data based on the keys fetched
            getUserShowKeys(UserAccountUtilities.getUserKey(this));
        }
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBar(int visibility){
        mProgressBar.setVisibility(visibility);
    }

    //Method takes user to SearchActivity
    public void openSearchShows(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    //Method fetches all show keys (show ID's) associated with the user's key, and adds them to an ArrayList. The ArrayList is then passed to the getUserShowData method, which fetches the JSON data for each show from the TVMAze API
    public void getUserShowKeys(String userKey){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all shows and adds each show key to the lstShows ArrayList
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                ArrayList<String> lstShows = new ArrayList<>();
                for(DataSnapshot snapshot : lstSnapshots){
                    String showKey = snapshot.getKey();
                    if((boolean) snapshot.getValue()){
                        lstShows.add(showKey);
                    }
                }
                //Removes EventListener from the Firebase Database and fetches the data for the Shows
                databaseReference.removeEventListener(this);
                getUserShowData(lstShows);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    //Method fetches the shows the user has added to 'My Series' using the keys passed in with the ArrayList
    public void getUserShowData(ArrayList<String> lstShows){
        if(lstShows.size() > 0){
            //Transfers the data from lstShows to an array containing the necessary links to the API (an array can be passed in to the APIConnection class to fetch data from the API)
            String[] arrShows = new String[lstShows.size()];
            for(int i = 0; i < lstShows.size(); i++){
                arrShows[i] = "http://api.tvmaze.com/shows/" + lstShows.get(i);
            }

            //Fetches the data from the TVMaze API
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute(arrShows);
        }
        else{
            toggleViewVisibility(View.INVISIBLE,View.VISIBLE);
            toggleProgressBar(View.INVISIBLE);
        }
    }

    //Method sets the visibility of the views based on the parameters passed in
    public void toggleViewVisibility(int listViewVisibility, int otherViewVisibility){
        mTextNoShows.setVisibility(otherViewVisibility);
        mButtonAddShows.setVisibility(otherViewVisibility);
        mListViewMyShows.setVisibility(listViewVisibility);
    }

    //Method parses the JSON returned from the API and displays the information in the list_view_my_shows ListView
    @Override
    public void parseJsonResponse(String response) {
        try{
            //JSONArray stores the JSON returned from the TVMaze API
            if(response != null){
                //Creates JSONArray from the response
                response = "[\n" + response + "\n]";
                JSONArray jsonArray = new JSONArray(response);

                //Loops through all Shows returned from the TVMaze API search result
                for(int i = 0; i < jsonArray.length(); i++){
                    //Instantiates JSONObject to store the results returned from the API
                    JSONObject json = jsonArray.getJSONObject(i);

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        String url = json.getString("url");
                        if(url.contains("shows")){
                            int id = json.getInt("id");
                            String name = json.getString("name");
                            String status = json.getString("status");
                            String rating = json.getJSONObject("rating").getString("average");
                            String imageUrl;

                            //Gets the image URL for the current show if there is a URL provided, otherwise sets the URL to null
                            if(!json.getString("image").equals("null")){
                                imageUrl = json.getJSONObject("image").getString("medium");
                            }
                            else{
                                imageUrl = null;
                            }

                            //Ensures that the data returned in the JSON is valid
                            if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                                rating = getString(R.string.n_a);
                            }

                            JSONObject links = json.getJSONObject("_links");
                            if(links.has("nextepisode")){
                                String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");
                                //Fetches data from the TVMaze API using the link
                                APIConnection api = new APIConnection();
                                api.delegate = this;
                                api.execute(nextEpisodeLink);
                            }

                            //Instantiates a Show object and adds it to the lstShows ArrayList
                            Show show = new Show(id, name, rating, status, imageUrl);
                            show.setShowNextEpisode(getString(R.string.n_a));
                            lstShows.add(show);
                            adapter.notifyDataSetChanged();
                        }
                        else if(url.contains("episodes")){
                            String season = json.getString("season");
                            String episode = json.getString("number");
                            String airDate = json.getString("airdate");

                            //Sets the next episode information
                            if(airDate == null){
                                airDate = getString(R.string.n_a);
                            }
                            else{
                                airDate = airDate + " (S: " + season + " E: " + episode + ")";
                            }

                            //Sets the next episode date for the appropriate series
                            for(int s = 0; s < lstShows.size(); s++){
                                String seriesName = lstShows.get(s).getShowTitle().toLowerCase();
                                seriesName = seriesName.replaceAll("[^a-z A-Z]", "");
                                seriesName = seriesName.replaceAll(" ", "-");
                                if(url.toLowerCase().contains(seriesName)){
                                    lstShows.get(s).setShowNextEpisode(airDate);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.error_fetching_data_no_internet_connection, Toast.LENGTH_LONG).show();
            }

            //Hides ProgressBar
            toggleProgressBar(View.INVISIBLE);
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    //Used to retrieve results from the FirebaseService
    private class DataReceiver
            extends ResultReceiver {

        //Constructor
        DataReceiver(Handler handler) {
            super(handler);
        }

        //Performs the appropriate action based on the result
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == FirebaseService.ACTION_GET_USER_KEY_RESULT_CODE){
                //Gets the user's key
                String key = resultData.getString(FirebaseService.USER_KEY_EXTRA);

                if(key != null){
                    //Saves the key to SharedPreferences and initialises the map
                    UserAccountUtilities.setUserKey(getApplicationContext(), key);
                    setUpActivity();
                }
            }
        }
    }
}