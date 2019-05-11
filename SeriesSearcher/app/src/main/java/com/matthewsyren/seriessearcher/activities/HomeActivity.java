package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.BaseActivity.IOnDataSavingPreferenceChangedListener;
import com.matthewsyren.seriessearcher.adapters.ShowAdapter;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.network.ApiConnection.IApiConnectionResponse;
import com.matthewsyren.seriessearcher.services.FirebaseService;
import com.matthewsyren.seriessearcher.utilities.AsyncTaskUtilities;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.NetworkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity allows the user to see the Shows that they have added to 'My Series'
 */

public class HomeActivity
        extends BaseActivity
        implements IApiConnectionResponse,
        IOnDataSavingPreferenceChangedListener {
    //View bindings
    @BindView(R.id.recycler_view_my_shows) RecyclerView mRecyclerViewMyShows;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_no_shows) TextView mTextNoShows;
    @BindView(R.id.button_add_shows) Button mButtonAddShows;
    @BindView(R.id.cl_no_internet_connection) ConstraintLayout mClNoInternetConnection;

    //Variables
    private ArrayList<Show> mShows = new ArrayList<>();
    private ShowAdapter mAdapter;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mIsSignInRequestSent = false;
    private int mScrollPosition;
    private ApiConnection mApiConnection;
    private boolean mSignedOut;
    private boolean mListSorted = false;

    //Constants
    private static final String SCROLL_POSITION_BUNDLE_KEY = "scroll_position_bundle_key";
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";

    //Codes
    private static final int SIGN_IN_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer(this);

        //Sets the title of the Activity
        setTitle(getString(R.string.title_activity_home));

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

        //Cancels the AsyncTask
        AsyncTaskUtilities.cancelAsyncTask(mApiConnection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Only saves the mShows ArrayList if the user hasn't just signed out (this will prevent the user from seeing the old user's Shows)
        if(mShows.size() > 0 && !mSignedOut){
            outState.putParcelableArrayList(SHOWS_BUNDLE_KEY, mShows);
        }

        if(mRecyclerViewMyShows.getLayoutManager() != null){
            mScrollPosition = ((LinearLayoutManager)mRecyclerViewMyShows.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();

            outState.putInt(SCROLL_POSITION_BUNDLE_KEY, mScrollPosition);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE){
            //Resets flag variable
            mIsSignInRequestSent = false;

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
        else if(requestCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_REQUEST_CODE){
            //Refreshes the Activity if the user added/removed a Show from My Series on the SpecificShowActivity
            if(resultCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED){
                //Determines which Shows have been added to My Series by the user
                Show.markShowsInMySeries(this, mShows, new DataReceiver(new Handler()));
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();

        //Performs the appropriate actions if the user signs out
        if(id == R.id.nav_sign_out){
            //Signs the user out
            AuthUI.getInstance()
                    .signOut(this);

            //Closes the NavigationDrawer
            super.closeNavigationDrawer();
            return true;
        }

        return super.onNavigationItemSelected(item);
    }

    /**
     * Restores any saved data
     * @param savedInstanceState The Bundle containing the Activity's data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            //Restores mShows
            mShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides ProgressBar
            mProgressBar.setVisibility(View.GONE);

            //Displays the RecyclerView and hides other unnecessary Views
            toggleViewVisibility(View.VISIBLE,View.INVISIBLE);
        }

        if(savedInstanceState.containsKey(SCROLL_POSITION_BUNDLE_KEY)){
            mScrollPosition = savedInstanceState.getInt(SCROLL_POSITION_BUNDLE_KEY);
        }
    }

    /**
     * Checks if the user is signed in, and signs them in if they aren't signed in already
     */
    private void setUpAuthListener(){
        //Initialises mFirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Initialises mAuthStateListener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Initialises firebaseUser
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                if(firebaseUser == null){
                    //Performs sign out tasks
                    signOut();

                    //Takes the user to the sign in screen
                    if(!mIsSignInRequestSent){
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setLogo(R.mipmap.ic_launcher)
                                        .setTheme(R.style.LoginTheme)
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                                new AuthUI.IdpConfig.GoogleBuilder().build()))
                                        .build(),
                                SIGN_IN_REQUEST_CODE);

                        //Updates flag variable
                        mIsSignInRequestSent = true;
                    }
                }
                else{
                    //Clears the Shows if the user had signed out immediately before signing in (to prevent the user from seeing the old user's Shows)
                    if(mSignedOut){
                        mShows.clear();

                        //Resets the variable to indicate that the Activity will no longer contain the old user's Shows
                        mSignedOut = false;
                    }

                    //Saves the user's unique key
                    UserAccountUtilities.setUserKey(getApplicationContext(), firebaseUser.getUid());

                    //Sets up the Activity
                    setUpActivity();
                }
            }
        };

        //Adds the AuthStateListener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    /**
     * Performs tasks when the user signs out
     */
    private void signOut(){
        //Clears the user's key from SharedPreferences
        UserAccountUtilities.setUserKey(this, null);

        //Cancels the AsyncTask
        AsyncTaskUtilities.cancelAsyncTask(mApiConnection);

        //Clears the user's series
        mShows.clear();

        //Refreshes the Adapter
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }

        //Sets variable to true, signifying that the user has just signed out
        mSignedOut = true;
    }

    /**
     * Sets up the Adapter
     */
    private void setUpAdapter(){
        //Sets a custom adapter for the RecyclerView to display the user's Shows
        mAdapter = new ShowAdapter(this, mShows, true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.scrollToPosition(mScrollPosition);
        mRecyclerViewMyShows.setLayoutManager(linearLayoutManager);
        mRecyclerViewMyShows.setAdapter(mAdapter);
        mRecyclerViewMyShows.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

    /**
     * Sets up the Activity once the user signs in
     */
    private void setUpActivity(){
        //Sets up the Adapter
        setUpAdapter();

        //Displays the user's email address in the NavigationDrawer
        super.displayUserDetails();

        if(mShows.size() == 0){
            //Fetches the Shows the user has added to My Series
            fetchUsersShows();
        }
    }

    /**
     * Fetches the user's Shows
     */
    public void fetchUsersShows(){
        //Displays ProgressBar
        mProgressBar.setVisibility(View.VISIBLE);

        //Displays the RecyclerView and hides other unnecessary Views
        toggleViewVisibility(View.VISIBLE, View.INVISIBLE);

        //Displays/hides Views based on Internet connection status
        boolean online = NetworkUtilities.isOnline(this);
        toggleNoInternetMessageVisibility(online);

        //Fetches Shows if there is an Internet connection
        if(online){
            Show.getShowIdsInMySeries(this, new DataReceiver(new Handler()));
        }
    }

    /**
     * Toggles the visibility of a no Internet connection message
     * @param online A boolean indicating whether there is an Internet connection or not
     */
    private void toggleNoInternetMessageVisibility(boolean online){
        if(online){
            mClNoInternetConnection.setVisibility(View.GONE);
        }
        else{
            mClNoInternetConnection.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Takes user to SearchActivity
     */
    public void openSearchShows(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    /**
     * Fetches the user's Shows
     */
    public void refreshActivity(View view){
        if(UserAccountUtilities.getUserKey(this) != null){
            fetchUsersShows();
        }
    }

    /**
     * Fetches the shows the user has added to 'My Series' using the keys passed in with the ArrayList
     * @param shows An ArrayList of links to the user's Shows
     */
    private void getUserShowData(ArrayList<String> shows){
        if(shows.size() > 0){
            //Transfers the data from shows to an array containing the necessary links to the API (an array of links can be passed in to the ApiConnection class to fetch data from the API)
            String[] arrShows = new String[shows.size()];
            for(int i = 0; i < shows.size(); i++){
                arrShows[i] = LinkUtilities.getShowInformationLink(shows.get(i));
            }

            //Fetches the data from the TVMaze API
            mApiConnection = new ApiConnection();
            mApiConnection.setApiConnectionResponse(this);
            mApiConnection.execute(arrShows);
        }
        else{
            toggleViewVisibility(View.INVISIBLE,View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sets the visibility of the views based on the parameters passed in
     */
    private void toggleViewVisibility(int RecyclerViewVisibility, int otherViewVisibility){
        mTextNoShows.setVisibility(otherViewVisibility);
        mButtonAddShows.setVisibility(otherViewVisibility);
        mRecyclerViewMyShows.setVisibility(RecyclerViewVisibility);
    }

    /**
     * Parses the JSON returned from the API and displays the information in the RecyclerView
     * @param response The JSON response retrieved from the API
     */
    @Override
    public void parseJsonResponse(String response) {
        try{
            //JSONArray stores the JSON returned from the TVMaze API
            if(response != null && mFirebaseAuth.getCurrentUser() != null){
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

                        //Performs the appropriate action based on the start of the URL (which determines if the information is about a Show or the Show's next episode)
                        if(url.startsWith(LinkUtilities.SHOW_LINK)){
                            //Adds the Show to the mShows ArrayList
                            mShows.add(JsonUtilities.parseShowJson(json, this, this, true, true));
                            mAdapter.notifyDataSetChanged();
                        }
                        else if(url.startsWith(LinkUtilities.EPISODE_LINK)){
                            //Gets the next episode information
                            String displayText = JsonUtilities.parseShowEpisodeDate(json, this, false);

                            //Sets the next episode date for the appropriate series
                            for(int s = 0; s < mShows.size(); s++){
                                //Fetches the series name and replaces non-letter characters
                                String seriesName = mShows.get(s).getShowTitle().toLowerCase();
                                seriesName = seriesName.replaceAll("[^a-z A-Z]", "");
                                seriesName = seriesName.replaceAll(" ", "-");

                                //Adds the next episode date to the series if the URL contains the series name
                                if(url.toLowerCase().contains(seriesName)){
                                    mShows.get(s).setShowNextEpisode(displayText);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
            else{
                //Displays a no Internet connection message
                toggleNoInternetMessageVisibility(false);
            }

            //Hides ProgressBar
            mProgressBar.setVisibility(View.INVISIBLE);

            //Sorts the mShows ArrayList alphabetically by Show Title
            if(!mListSorted){
                Collections.sort(mShows, new Show.ShowTitleComparator());
                mListSorted = true;
            }
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDataSavingPreferenceChanged() {
        //Updates the images in the RecyclerView
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Used to receive data from Services
     */
    private class DataReceiver
            extends ResultReceiver{

        /**
         * Constructor
         */
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == FirebaseService.ACTION_GET_SHOW_IDS_RESULT_CODE){
                //Fetches the user's Show data from the Service
                ArrayList<String> showIds = resultData.getStringArrayList(FirebaseService.EXTRA_SHOW_IDS);

                //Fetches the Show IDs from the Service. The ArrayList is then passed to the getUserShowData method, which fetches the JSON data for each Show from the TVMAze API
                if(showIds != null){
                    getUserShowData(showIds);
                }
            }
            else if(resultCode == FirebaseService.ACTION_MARK_SHOWS_IN_MY_SERIES_RESULT_CODE){
                //Updates the mShows ArrayList with the new data
                if(resultData != null && resultData.containsKey(FirebaseService.EXTRA_SHOWS)){
                    mShows = resultData.getParcelableArrayList(FirebaseService.EXTRA_SHOWS);

                    //Refreshes the RecyclerView's data
                    mAdapter.setShows(mShows);
                }

                //Removes a Show if it has not been added to My Series
                for(int i = 0; i < mShows.size(); i++){
                    if(!mShows.get(i).isShowAdded()){
                        //Removes the Show and decrements the i variable to cater for the removed object
                        mShows.remove(i);
                        i--;
                    }
                }

                //Displays a message telling the user to add Shows if mShows is empty
                if(mShows.size() == 0){
                    toggleViewVisibility(View.GONE, View.VISIBLE);
                }

                //Refreshes the RecyclerView's data
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}