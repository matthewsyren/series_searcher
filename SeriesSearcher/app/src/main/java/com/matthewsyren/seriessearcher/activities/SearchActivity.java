package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.adapters.ShowAdapter;
import com.matthewsyren.seriessearcher.models.IShowUpdatedListener;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.network.IApiConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.IOnDataSavingPreferenceChangedListener;
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

public class SearchActivity
        extends BaseActivity
        implements IApiConnectionResponse,
        IOnDataSavingPreferenceChangedListener,
        IShowUpdatedListener {
    //View bindings
    @BindView(R.id.recycler_view_search_results) RecyclerView mRecyclerViewSearchResults;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_search_series) EditText mTextSearchSeries;
    @BindView(R.id.tv_no_series_found) TextView mTvNoSeriesFound;
    @BindView(R.id.text_no_internet_connection) TextView mTextNoInternetConnection;

    //Declarations
    private ApiConnection mApiConnection = new ApiConnection();
    private ArrayList<Show> mShows =  new ArrayList<>();
    private ShowAdapter mAdapter;
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";
    private boolean mRestored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer(this);

        //Ensures that the user's key has been fetched
        UserAccountUtilities.checkUserKey(this);

        //Hides ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        //Sets up the Adapter and typing listener
        setUpAdapter();
        setUpTypingListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to SearchActivity
        super.setSelectedNavItem(R.id.nav_search);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Cancels the AsyncTask if it is still running
        if(mApiConnection != null && mApiConnection.getStatus() == AsyncTask.Status.RUNNING && !mApiConnection.isCancelled()){
            mApiConnection.cancel(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_REQUEST_CODE){
            //Updates the RecyclerView if the user added/removed a Show from My Series on the SpecificShowActivity
            if(resultCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED){
                Show.markShowsThatAreAddedToMySeries(
                        UserAccountUtilities.getUserKey(this),
                        mShows,
                        this);
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

    /**
     * Restores any saved data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            mShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);
            mRestored = true;

            //Hides ProgressBar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up the Adapter
     */
    private void setUpAdapter(){
        //Sets up Adapter to RecyclerView
        mAdapter = new ShowAdapter(this, mShows, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewSearchResults.setLayoutManager(linearLayoutManager);
        mRecyclerViewSearchResults.setAdapter(mAdapter);
    }

    /**
     * Sets up the typing listener
     */
    private void setUpTypingListener(){
        mTextSearchSeries.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!mRestored){
                    //Performs a search using the text the user has entered
                    searchShows();
                }
                mRestored = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Retrieves the text that the user searches for in text_search, and then searches for that text using the API
     */
    private void searchShows(){
        //Fetches user's input
        String searchText = mTextSearchSeries.getText().toString();

        //Displays ProgressBar and hides the TextViews
        mTvNoSeriesFound.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mTextNoInternetConnection.setVisibility(View.GONE);

        //Cancels any previous requests and clears the previous results
        mApiConnection.cancel(true);
        mShows.clear();
        mAdapter.notifyDataSetChanged();

        if(NetworkUtilities.isOnline(this)){
            //Connects to the TVMaze API using the specific URL for the selected show
            mApiConnection = new ApiConnection();
            mApiConnection.delegate = this;
            mApiConnection.execute(LinkUtilities.getSearchLink(searchText));
        }
        else{
            //Displays no Internet connection message and hides the ProgressBar
            mTextNoInternetConnection.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Parses the JSON returned from the API, and populates the recycler_view_search_results RecyclerView with the data
     */
    @Override
    public void parseJsonResponse(String response) {
        try{
            //JSONArray stores the JSON returned from the TVMaze API
            if(response != null){
                JSONArray jsonArray = new JSONArray(response);
                mShows.clear();

                //Loops through all Shows returned from the TVMaze API search result
                for(int i = 0; i < jsonArray.length(); i++){
                    //Instantiates JSONObject to store the results returned from the API
                    JSONObject json = jsonArray.getJSONObject(i);
                    json = json.getJSONObject("show");

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        mShows.add(JsonUtilities.parseShowJson(json, this, this, false, null));
                    }
                }
                //Determines which Shows have been added to My Series by the user
                Show.markShowsThatAreAddedToMySeries(
                        UserAccountUtilities.getUserKey(this),
                        mShows,
                        this);
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.error_fetching_data_no_internet_connection, Toast.LENGTH_LONG).show();
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

    @Override
    public void showsUpdated() {
        //Refreshes the RecyclerView's data
        mAdapter.notifyDataSetChanged();

        //Displays a message if no series are found
        if(mShows.size() == 0){
            mTvNoSeriesFound.setVisibility(View.VISIBLE);
        }
        else{
            mTvNoSeriesFound.setVisibility(View.INVISIBLE);
        }

        //Hides ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}