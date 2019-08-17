package com.matthewsyren.seriessearcher.activities;

import android.app.FragmentManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.BaseActivity.IOnDataSavingPreferenceChangedListener;
import com.matthewsyren.seriessearcher.adapters.ShowAdapter;
import com.matthewsyren.seriessearcher.fragments.EnterPasswordFragment;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.NetworkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.matthewsyren.seriessearcher.viewmodels.EmailVerificationViewModel;
import com.matthewsyren.seriessearcher.viewmodels.ShowViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity allows the user to see the Shows that they have added to My Series
 */

public class HomeActivity
        extends BaseActivity
        implements IOnDataSavingPreferenceChangedListener,
        EnterPasswordFragment.IEnterPasswordFragmentOnClickListener {
    //View bindings
    @BindView(R.id.rv_my_shows) RecyclerView mRvMyShows;
    @BindView(R.id.pb_home) ProgressBar mPbHome;
    @BindView(R.id.cl_no_internet_connection) ConstraintLayout mClNoInternetConnection;
    @BindView(R.id.cl_email_not_verified) ConstraintLayout mClEmailNotVerified;
    @BindView(R.id.rl_no_series_added_to_my_series) RelativeLayout mRlNoSeriesAddedToMySeries;

    //Variables
    private ArrayList<Show> mShows = new ArrayList<>();
    private ShowAdapter mAdapter;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mIsSignInRequestSent = false;
    private int mScrollPosition;
    private boolean mSignedOut;
    private boolean mListSorted = false;
    private FirebaseUser mFirebaseUser;
    private boolean mAttemptedVerification = false;
    private EnterPasswordFragment mEnterPasswordFragment;
    private boolean mOngoingOperation;
    private EmailVerificationViewModel mEmailVerificationViewModel;
    private ShowViewModel mShowViewModel;
    private boolean mShowsLoaded = false;

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

        //Registers Observers for the EmailVerificationViewModel
        registerEmailVerificationViewModelObservers();

        //Sets the title of the Activity
        setTitle(getString(R.string.title_activity_home));

        //Restores data if possible
        if(savedInstanceState != null){
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

        //Only saves the mShows ArrayList if the user hasn't just signed out (this will prevent the user from seeing the old user's Shows)
        if(mShows.size() > 0 && !mSignedOut){
            outState.putParcelableArrayList(SHOWS_BUNDLE_KEY, mShows);
        }

        if(mRvMyShows.getLayoutManager() != null){
            mScrollPosition = ((LinearLayoutManager)mRvMyShows.getLayoutManager())
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
            //Updates the RecyclerView if the user added/removed a Show from My Series on the SpecificShowActivity
            if(resultCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED && !mOngoingOperation){
                //Updates the showAdded attribute of the Show that has been changed
                if(data != null){
                    //Fetches data from the Intent
                    String showId = data.getStringExtra(SpecificShowActivity.SHOW_ID_KEY);
                    boolean isShowAdded = data.getBooleanExtra(SpecificShowActivity.SHOW_IS_ADDED_KEY, false);

                    //Updates the appropriate Show's showAdded attribute
                    if(showId != null){
                        for(int i = 0; i < mShows.size(); i++){
                            if((String.valueOf(mShows.get(i).getShowId())).equals(showId)){
                                if(!isShowAdded){
                                    //Ensures that the Adapter is not null
                                    if(mAdapter == null){
                                        setUpAdapter();
                                    }

                                    //Removes the Show as the Show is no longer in My Series
                                    mAdapter.removeShowFromHomeActivityRecyclerView(i);
                                }
                                break;
                            }
                        }
                    }
                }
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

            //Displays the RecyclerView and hides other unnecessary Views
            toggleViewVisibility(View.VISIBLE,View.GONE);
        }

        if(savedInstanceState.containsKey(SCROLL_POSITION_BUNDLE_KEY)){
            mScrollPosition = savedInstanceState.getInt(SCROLL_POSITION_BUNDLE_KEY);
        }

        //Fetches the EnterPasswordFragment
        mEnterPasswordFragment = (EnterPasswordFragment) getFragmentManager()
                .findFragmentByTag(EnterPasswordFragment.ENTER_PASSWORD_FRAGMENT_TAG);

        //Ensures updates from EnterPasswordFragment are sent to this Activity
        if(mEnterPasswordFragment != null){
            mEnterPasswordFragment.setEnterPasswordFragmentOnClickListener(this);
        }
    }

    /**
     * Registers the EmailVerificationViewModel Observers
     */
    private void registerEmailVerificationViewModelObservers() {
        //Initialises the EmailVerificationViewModel
        mEmailVerificationViewModel = ViewModelProviders.of(this).get(EmailVerificationViewModel.class);

        //Registers an Observer for ongoing operations
        mEmailVerificationViewModel.getObservableOngoingOperation().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean ongoingOperation) {
                //Performs the appropriate task based on the value of ongoingOperation
                if(ongoingOperation != null){
                    if(ongoingOperation){
                        //Hides the verify email message and displays a ProgressBar
                        mClEmailNotVerified.setVisibility(View.GONE);
                        mPbHome.setVisibility(View.VISIBLE);
                    }
                    else{
                        //Hides the ProgressBar and displays the verify email message
                        mClEmailNotVerified.setVisibility(View.VISIBLE);
                        mPbHome.setVisibility(View.GONE);
                    }

                    //Updates the mOngoingOperation variable
                    mOngoingOperation = ongoingOperation;
                }
            }
        });

        //Registers an Observer for sending a verification email to the user
        mEmailVerificationViewModel.getObservableVerificationEmailSent().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer resultCode) {
                //Performs the appropriate action based on the value of resultCode
                if(resultCode != null){
                    if(resultCode == EmailVerificationViewModel.VERIFICATION_EMAIL_SENT){
                        //Displays a message saying the verification email has been sent
                        Toast.makeText(getApplicationContext(), R.string.verification_email_sent, Toast.LENGTH_LONG).show();
                    }
                    else if(resultCode == EmailVerificationViewModel.VERIFICATION_EMAIL_NOT_SENT){
                        //Displays an error message (either a generic error message, or a no Internet connection message if there is no Internet connection)
                        if(NetworkUtilities.isOnline(getApplicationContext())){
                            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
                        }
                    }

                    //Resets the observable variable
                    mEmailVerificationViewModel.getObservableVerificationEmailSent().setValue(null);
                }
            }
        });

        //Registers an Observer for reauthentication
        mEmailVerificationViewModel.getObservableReauthenticationSuccessful().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer resultCode) {
                //Performs the appropriate action based on the value of resultCode
                if(resultCode != null){
                    if(resultCode == EmailVerificationViewModel.REAUTHENTICATION_SUCCESSFUL){
                        //Refreshes the AuthListener, as the user has successfully reauthenticated
                        mAttemptedVerification = true;

                        //Removes the AuthStateListener
                        if(mAuthStateListener != null){
                            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                        }

                        setUpAuthListener();
                    }
                    else if(resultCode == EmailVerificationViewModel.REAUTHENTICATION_WRONG_PASSWORD){
                        //Displays an error message saying the user has entered the incorrect password
                        Toast.makeText(getApplicationContext(), R.string.error_incorrect_password, Toast.LENGTH_LONG).show();
                    }
                    else if(resultCode == EmailVerificationViewModel.REAUTHENTICATION_ERROR){
                        //Displays an error message (either a generic error message, or a no Internet connection message if there is no Internet connection)
                        if(NetworkUtilities.isOnline(getApplicationContext())){
                            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
                        }
                    }

                    //Resets the observable variable
                    mEmailVerificationViewModel.getObservableReauthenticationSuccessful().setValue(null);
                }
            }
        });
    }

    /**
     * Registers the ShowViewModel Observers
     */
    private void registerShowViewModelObservers(){
        //Initialises the ShowViewModel
        mShowViewModel = ViewModelProviders.of(this).get(ShowViewModel.class);

        //Registers an Observer to keep track of changes to the shows ArrayList
        mShowViewModel.getObservableShows().observe(this, new Observer<ArrayList<Show>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Show> shows) {
                if(shows != null && (mShows.size() - shows.size() <= 1)){
                    //Updates the mShows variable
                    mShows = shows;

                    //Removes Shows that have not been added to My Series
                    for(int i = 0; i < shows.size(); i++){
                        if(!shows.get(i).isShowAdded()){
                            //Removes the Show and decrements the i variable to cater for the removed object
                            shows.remove(i);
                            i--;
                        }
                    }

                    //Displays a message telling the user to add Shows if mShows is empty
                    if(mShows.size() == 0){
                        toggleViewVisibility(View.GONE, View.VISIBLE);
                    }

                    //Refreshes the RecyclerView's data
                    if(mAdapter != null){
                        mAdapter.setShows(mShows);
                    }
                }
            }
        });

        //Registers an Observer to retrieve responses from the TVMaze API
        mShowViewModel.getObservableResponse().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String response) {
                if(response != null && response.length() > 0){
                    //Parses the response retrieved from the API
                    parseJsonResponse(response);

                    //Updates the visibility of Views once the Shows have been loaded
                    if(!mShowsLoaded){
                        //Updates flag variable
                        mShowsLoaded = true;

                        //Displays the Shows if there are any, otherwise displays a message telling the user to add Shows to My Series
                        if(mShows.size() > 0){
                            //Sorts the mShows ArrayList alphabetically by Show Title
                            if(!mListSorted){
                                Collections.sort(mShows, new Show.ShowTitleComparator());
                                mListSorted = true;
                            }

                            //Displays the Shows
                            mAdapter.setShows(mShows);
                            toggleViewVisibility(View.VISIBLE, View.GONE);
                        }
                        else{
                            //Displays a message telling the user to add Shows to My Series
                            toggleViewVisibility(View.GONE, View.VISIBLE);
                        }
                    }

                    //Resets the observable variable
                    mShowViewModel.getObservableResponse().setValue(null);
                }
            }
        });

        //Registers an Observer to keep track of whether an operation is ongoing or not
        mShowViewModel.getObservableOngoingOperation().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean ongoingOperation) {
                if(ongoingOperation != null){
                    //Updates the mOngoingOperation variable
                    mOngoingOperation = ongoingOperation;

                    if(ongoingOperation){
                        //Hides the RecyclerView and displays the ProgressBar
                        mRlNoSeriesAddedToMySeries.setVisibility(View.GONE);
                        mPbHome.setVisibility(View.VISIBLE);
                        mRvMyShows.setVisibility(View.GONE);
                    }
                    else{
                        //Hides the ProgressBar and displays the RecyclerView
                        mPbHome.setVisibility(View.GONE);
                        mRvMyShows.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
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
                //Initialises mFirebaseUser
                mFirebaseUser = firebaseAuth.getCurrentUser();

                if(mFirebaseUser == null){
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
                    //Displays the user's details in the NavigationDrawer
                    displayUserDetailsInNavigationDrawer();

                    //Clears the Shows if the user had signed out immediately before signing in (to prevent the user from seeing the old user's Shows)
                    if(mSignedOut){
                        mShows.clear();

                        //Resets the variable to indicate that the Activity will no longer contain the old user's Shows
                        mSignedOut = false;
                    }

                    //Hides the no Internet connection and no series added to My Series messages
                    mClNoInternetConnection.setVisibility(View.GONE);
                    mRlNoSeriesAddedToMySeries.setVisibility(View.GONE);

                    //Fetches the user's Shows if the user's email address has been verified, otherwise instructs the user to verify their email address
                    if(mFirebaseUser.isEmailVerified()){
                        //Informs the user that their email has successfully been verified
                        if(mAttemptedVerification){
                            Toast.makeText(getApplicationContext(), R.string.email_verified, Toast.LENGTH_LONG).show();

                            //Resets variable to prevent the message from being displayed again
                            mAttemptedVerification = false;
                        }

                        //Hides the verify email message
                        mClEmailNotVerified.setVisibility(View.GONE);

                        //Saves the user's unique key
                        UserAccountUtilities.setUserKey(getApplicationContext(), mFirebaseUser.getUid());

                        //Sets up the Activity
                        setUpActivity();
                    }
                    else{
                        //Displays an error message if the user tries to confirm their verification but their email address is still not verified
                        if(mAttemptedVerification){
                            Toast.makeText(getApplicationContext(), R.string.error_email_still_not_verified, Toast.LENGTH_LONG).show();

                            //Resets variable to prevent the message from being displayed again
                            mAttemptedVerification = false;
                        }

                        //Displays a help message to the user if there is not an ongoing operation
                        if(!mOngoingOperation){
                            //Displays a message that explains how to verify the user's email address and hides the ProgressBar
                            mClEmailNotVerified.setVisibility(View.VISIBLE);
                            mPbHome.setVisibility(View.GONE);
                        }
                    }
                }
            }
        };

        //Adds the AuthStateListener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    /**
     * Displays the user's details in the NavigationDrawer
     */
    private void displayUserDetailsInNavigationDrawer(){
        super.displayUserDetails();
    }

    /**
     * Sends an email to the user to verify their email address
     * @param view The View that was clicked on
     */
    public void sendVerificationEmail(View view){
        mEmailVerificationViewModel.sendVerificationEmail(mFirebaseUser);
    }

    /**
     * Confirms the user's verification by making them enter their password
     * @param view The View that was clicked on
     */
    public void confirmVerification(View view){
        //Initialises a DialogFragment that allows the user to enter their password
        FragmentManager fragmentManager = getFragmentManager();
        mEnterPasswordFragment = new EnterPasswordFragment();

        //Sends data to the DialogFragment
        mEnterPasswordFragment.setEnterPasswordFragmentOnClickListener(this);

        //Displays the DialogFragment
        mEnterPasswordFragment.show(fragmentManager, EnterPasswordFragment.ENTER_PASSWORD_FRAGMENT_TAG);
    }

    /**
     * Performs tasks when the user signs out
     */
    private void signOut(){
        //Clears the user's key from SharedPreferences
        UserAccountUtilities.setUserKey(this, null);

        //Resets the ShowViewModel
        if(mShowViewModel != null){
            //Cancels any ongoing AsyncTasks
            mShowViewModel.cancelAsyncTasks();

            //Removes Observers for the ShowViewModel
            mShowViewModel.getObservableOngoingOperation().removeObservers(this);
            mShowViewModel.getObservableShows().removeObservers(this);
            mShowViewModel.getObservableResponse().removeObservers(this);

            //Marks any ongoing operations as cancelled
            mShowViewModel.getObservableOngoingOperation().setValue(false);
        }

        //Resets flag variables
        mOngoingOperation = false;
        mListSorted = false;
        mShowsLoaded = false;
        mSignedOut = true;

        //Clears the user's series
        mShows.clear();

        //Refreshes the Adapter
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets up the Adapter
     */
    private void setUpAdapter(){
        //Sets a custom adapter for the RecyclerView to display the user's Shows
        mAdapter = new ShowAdapter(this, mShows, true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.scrollToPosition(mScrollPosition);
        mRvMyShows.setLayoutManager(linearLayoutManager);
        mRvMyShows.setAdapter(mAdapter);
        mRvMyShows.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

    /**
     * Sets up the Activity once the user signs in
     */
    private void setUpActivity(){
        //Sets up the Adapter
        setUpAdapter();

        //Registers Observers for the ShowViewModel
        registerShowViewModelObservers();

        if(mShows.size() == 0){
            //Fetches the Shows the user has added to My Series
            fetchUsersShows();
        }
    }

    /**
     * Fetches the user's Shows
     */
    public void fetchUsersShows(){
        //Displays the RecyclerView and hides other unnecessary Views
        toggleViewVisibility(View.VISIBLE, View.GONE);

        //Displays/hides Views based on Internet connection status
        boolean online = NetworkUtilities.isOnline(this);
        toggleNoInternetMessageVisibility(online);

        //Fetches Shows if there is an Internet connection
        if(online && !mOngoingOperation){
            //Registers the ShowViewModel Observers if they haven't been registered already
            if(mShowViewModel == null){
                registerShowViewModelObservers();
            }

            //Requests a list of Shows that the user has added to My Series
            mShowViewModel.requestShowsInMySeriesJson();
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
            mRlNoSeriesAddedToMySeries.setVisibility(View.GONE);
            mClEmailNotVerified.setVisibility(View.GONE);
            mRvMyShows.setVisibility(View.GONE);
        }
    }

    /**
     * Takes user to SearchActivity
     * @param view The View that was clicked on
     */
    public void openSearchShows(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Fetches the user's Shows
     * @param view The View that was clicked on
     */
    public void refreshActivity(View view){
        if(UserAccountUtilities.getUserKey(this) != null){
            fetchUsersShows();
        }
    }

    /**
     * Sets the visibility of the Views based on the parameters passed in
     * @param recyclerViewVisibility The intended visibility of the RecyclerView
     * @param otherViewVisibility The intended visibility of other Views
     */
    private void toggleViewVisibility(int recyclerViewVisibility, int otherViewVisibility){
        mRlNoSeriesAddedToMySeries.setVisibility(otherViewVisibility);
        mRvMyShows.setVisibility(recyclerViewVisibility);
    }

    /**
     * Parses the JSON from the API
     * @param response The JSON response retrieved from the API
     */
    private void parseJsonResponse(String response) {
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
                            mShows.add(JsonUtilities.parseShowJson(json, this, true, true, mShowViewModel));
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
                                    mAdapter.notifyItemChanged(s);
                                    break;
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
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDataSavingPreferenceChanged() {
        //Updates the images in the RecyclerView
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEnterPasswordFragmentClick(String password) {
        //Attempts to reauthenticate the user if they have entered their password
        if(password != null && password.length() > 0 && mFirebaseUser != null && mFirebaseUser.getEmail() != null){
            mEmailVerificationViewModel.reauthenticateUser(mFirebaseUser, password);
        }
    }
}