package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.constraint.ConstraintLayout;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class RandomShowsActivity
        extends BaseActivity
        implements IApiConnectionResponse,
        IOnDataSavingPreferenceChangedListener{
    //View bindings
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.recycler_view_random_shows) RecyclerView mRecyclerViewRandomShows;
    @BindView(R.id.cl_no_internet_connection) ConstraintLayout mClNoInternetConnection;

    //Variables
    private ArrayList<Show> mShows = new ArrayList<>();
    private ShowAdapter mAdapter;
    private ApiConnection mApiConnection;

    //Constants
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";
    private static final String API_PAGE_BUNDLE_KEY = "api_age";
    private static final String API_STARTING_SHOW_BUNDLE_KEY = "api_starting_show";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_shows);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer(this);

        //Ensures that the user's key has been fetched
        UserAccountUtilities.checkUserKey(this);

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
    protected void onDestroy() {
        super.onDestroy();

        //Cancels the AsyncTask if it is still running
        AsyncTaskUtilities.cancelAsyncTask(mApiConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_REQUEST_CODE){
            //Updates the RecyclerView if the user added/removed a Show from My Series on the SpecificShowActivity
            if(resultCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED){
                Show.markShowsInMySeries(this, mShows, new DataReceiver(new Handler()));
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

        switch (id){
            case R.id.mi_refresh:
                getRandomShows();
                return true;
             default:
                 return super.onOptionsItemSelected(item);
        }
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
    }

    /**
     * Fetches 20 random Shows
     */
    private void getRandomShows(){
        //Displays/hides Views based on Internet connection status
        boolean online = NetworkUtilities.isOnline(this);
        toggleNoInternetMessageVisibility(online);

        //Clears the previous Shows
        mShows.clear();
        mAdapter.notifyDataSetChanged();

        //Fetches the Shows if there is an Internet connection
        if(online){
            //Displays the ProgressBar
            mProgressBar.setVisibility(View.VISIBLE);

            //Fetches JSON from API (If a page on the API has already been determined, then it is fetched from the Bundle, otherwise the Math.random() method chooses a random page from the API to fetch)
            Bundle bundle = getIntent().getExtras();
            int page;
            if(bundle != null && bundle.getInt(API_PAGE_BUNDLE_KEY) != -1){
                page = bundle.getInt(API_PAGE_BUNDLE_KEY);
            }
            else{
                page = (int) (Math.random() * 100);
            }
            mApiConnection = new ApiConnection();
            mApiConnection.delegate = this;
            mApiConnection.execute(LinkUtilities.getMultipleShowPageLink(page));
        }
    }

    /**
     * Restores any saved data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            mShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides the ProgressBar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Fetches 20 random Shows
     */
    public void refreshActivity(View view){
        getRandomShows();
    }

    /**
     * Fetches the JSON from the ApiConnection class, and parses it
     */
    @Override
    public void parseJsonResponse(String response) {
        try{
            if(response != null){
                //JSONArray stores the JSON returned from the TVMaze API
                JSONArray jsonArray = new JSONArray(response);

                //Fetches previous list of Shows from the Bundle if a starting show has already been determined, otherwise Math.random() is used to choose a random starting point to fetch data from the API. This allows the app to fetch different shows each time it runs
                Bundle bundle = getIntent().getExtras();
                int startingShow;
                if(bundle != null && bundle.getInt(API_STARTING_SHOW_BUNDLE_KEY) != -1){
                    startingShow = bundle.getInt(API_STARTING_SHOW_BUNDLE_KEY);
                }
                else{
                    startingShow = (int) (Math.random() * 230 + 1);
                }

                //Loops through the 20 randomly chosen shows returned from the TVMaze API
                for(int i = 0; i < 20 && (startingShow + i) < jsonArray.length() - 1; i++){
                    //Creates new JSONObject to parse the data returned
                    JSONObject json = jsonArray.getJSONObject(startingShow + i);

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        mShows.add(JsonUtilities.parseShowJson(json, this, this, false, null));
                    }
                    else{
                        //Exits the loop if the JSON returned is null
                        break;
                    }
                }

                //Determines which Shows have been added to My Series by the user
                Show.markShowsInMySeries(this, mShows, new DataReceiver(new Handler()));
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
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Used to receive data from Services
     */
    private class DataReceiver
            extends ResultReceiver {

        /**
         * Constructor
         */
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == FirebaseService.ACTION_MARK_SHOWS_IN_MY_SERIES_RESULT_CODE){
                //Updates the mShows ArrayList with the new data
                if(resultData != null && resultData.containsKey(FirebaseService.EXTRA_SHOWS)){
                    mShows = resultData.getParcelableArrayList(FirebaseService.EXTRA_SHOWS);

                    //Refreshes the RecyclerView's data
                    mAdapter.setShows(mShows);
                    mAdapter.notifyDataSetChanged();
                }

                //Hides ProgressBar
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}