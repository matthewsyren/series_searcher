package com.matthewsyren.seriessearcher.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.BaseActivity.IOnDataSavingPreferenceChangedListener;
import com.matthewsyren.seriessearcher.adapters.ShowAdapter;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.NetworkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.matthewsyren.seriessearcher.viewmodels.ShowViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity displays a list of random Shows to the user
 */

public class RandomShowsActivity
        extends BaseActivity
        implements IOnDataSavingPreferenceChangedListener{
    //View bindings
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.recycler_view_random_shows) RecyclerView mRecyclerViewRandomShows;
    @BindView(R.id.cl_no_internet_connection) ConstraintLayout mClNoInternetConnection;

    //Variables
    private ArrayList<Show> mShows = new ArrayList<>();
    private ShowAdapter mAdapter;
    private ShowViewModel mShowViewModel;

    //Constants
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_shows);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer(this);

        //Registers Observers for the ShowViewModel
        registerShowViewModelObservers();

        //Ensures that the user's key has been fetched
        UserAccountUtilities.checkUserKey(this);

        //Restores data if possible
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        //Sets up the Adapter
        setUpAdapter();

        //Fetches data if it hasn't been fetched already
        if(mShows.size() == 0){
            getRandomShows();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to RandomShowsActivity
        super.setSelectedNavItem(R.id.nav_random_shows);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_REQUEST_CODE){
            //Updates the RecyclerView if the user added/removed a Show from My Series on the SpecificShowActivity
            if(resultCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED){
                //Determines which Shows have been added to My Series by the user
                mShowViewModel.markShowsInMySeries(mShows);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mShows.size() > 0){
            outState.putParcelableArrayList(SHOWS_BUNDLE_KEY, mShows);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_random_shows, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Refreshes the Shows if the user clicks on the Refresh MenuItem
        if(id == R.id.mi_refresh){
            getRandomShows();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Registers Observers for the ShowViewModel
     */
    private void registerShowViewModelObservers(){
        //Initialises the ShowViewModel
        mShowViewModel = ViewModelProviders.of(this).get(ShowViewModel.class);

        //Registers an Observer to keep track of whether an operation is ongoing or not
        mShowViewModel.getObservableOngoingOperation().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean ongoingOperation) {
                if(ongoingOperation != null){
                    if(ongoingOperation){
                        //Hides the RecyclerView and displays the ProgressBar
                        mRecyclerViewRandomShows.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    else{
                        //Hides the ProgressBar and displays the RecyclerView
                        mRecyclerViewRandomShows.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        //Registers an Observer to retrieve responses from the TVMaze API
        mShowViewModel.getObservableResponse().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String response) {
                if(response != null && response.length() > 0){
                    //Parses the JSON response retrieved from the API
                    parseJsonResponse(response);

                    //Marks Shows that have been added to My Series
                    mShowViewModel.markShowsInMySeries(mShows);

                    //Displays the Shows
                    if(mShows.size() > 0){
                        mAdapter.setShows(mShows);
                        mAdapter.notifyDataSetChanged();
                    }

                    //Resets the observable variable
                    mShowViewModel.getObservableResponse().setValue(null);
                }
            }
        });

        //Registers an Observer to keep track of changes to the shows ArrayList
        mShowViewModel.getObservableShows().observe(this, new Observer<ArrayList<Show>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Show> shows) {
                if(shows != null && shows.size() > 0){
                    //Updates the mShows variable
                    mShows = shows;

                    //Refreshes the RecyclerView's data
                    if(mAdapter != null){
                        mAdapter.setShows(mShows);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * Sets up the Adapter
     */
    private void setUpAdapter(){
        //Sets up Adapter to RecyclerView
        mAdapter = new ShowAdapter(this, mShows, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewRandomShows.setLayoutManager(linearLayoutManager);
        mRecyclerViewRandomShows.setAdapter(mAdapter);
        mRecyclerViewRandomShows.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

    /**
     * Fetches up to 20 random Shows
     */
    private void getRandomShows(){
        //Clears the previous Shows
        mShows.clear();
        mShowViewModel.getObservableShows().setValue(new ArrayList<Show>());
        mAdapter.notifyDataSetChanged();
        mRecyclerViewRandomShows.getLayoutManager().scrollToPosition(0);

        //Displays/hides Views based on Internet connection status
        boolean online = NetworkUtilities.isOnline(this);
        toggleNoInternetMessageVisibility(online);

        //Fetches the Shows if there is an Internet connection
        if(online){
            //Fetches JSON from API (the Math.random() method chooses a random page from the API to fetch)
            int page = (int) (Math.random() * 100);
            mShowViewModel.requestJsonResponse(LinkUtilities.getMultipleShowPageLink(page));
        }
    }

    /**
     * Restores any saved data
     * @param savedInstanceState The Bundle containing the Activity's data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            mShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides the ProgressBar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Fetches random Shows
     */
    public void refreshActivity(View view){
        getRandomShows();
    }

    /**
     * Parses the JSON from the API
     * @param response The JSON response retrieved from the API
     */
    private void parseJsonResponse(String response) {
        try{
            if(response != null){
                //JSONArray stores the JSON returned from the TVMaze API
                JSONArray jsonArray = new JSONArray(response);

                //Math.random() is used to choose a random starting point to fetch data from the API. This allows the app to fetch different shows each time it runs
                int startingShow = (int) (Math.random() * 230 + 1);

                //Loops through the 20 randomly chosen shows returned from the TVMaze API
                for(int i = 0; i < 20 && (startingShow + i) < jsonArray.length() - 1; i++){
                    //Creates new JSONObject to parse the data returned
                    JSONObject json = jsonArray.getJSONObject(startingShow + i);

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        mShows.add(JsonUtilities.parseShowJson(json, this, false, null, mShowViewModel));
                    }
                    else{
                        //Exits the loop if the JSON returned is null
                        break;
                    }
                }

                //Sorts the Shows alphabetically
                Collections.sort(mShows, new Show.ShowTitleComparator());

                //Determines which Shows have been added to My Series by the user
                mShowViewModel.markShowsInMySeries(mShows);
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

    /**
     * Toggles the visibility of a no Internet connection message
     * @param online A boolean indicating whether there is an Internet connection or not
     */
    private void toggleNoInternetMessageVisibility(boolean online){
        if(online || mShows.size() > 0){
            mClNoInternetConnection.setVisibility(View.GONE);
        }
        else{
            mClNoInternetConnection.setVisibility(View.VISIBLE);
            mShows.clear();
        }
    }

    @Override
    public void onDataSavingPreferenceChanged() {
        //Updates the images in the RecyclerView
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }
}