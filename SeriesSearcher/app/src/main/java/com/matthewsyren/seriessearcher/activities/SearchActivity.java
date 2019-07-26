package com.matthewsyren.seriessearcher.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity allows the user to search for Shows
 */

public class SearchActivity
        extends BaseActivity
        implements IOnDataSavingPreferenceChangedListener{
    //View bindings
    @BindView(R.id.rv_search_results) RecyclerView mRvSearchResults;
    @BindView(R.id.pb_search) ProgressBar mPbSearch;
    @BindView(R.id.et_search_series) EditText mEtSearchSeries;
    @BindView(R.id.tv_no_series_found) TextView mTvNoSeriesFound;
    @BindView(R.id.tv_no_internet_connection) TextView mTextNoInternetConnection;

    //Variables
    private ArrayList<Show> mShows =  new ArrayList<>();
    private ShowAdapter mAdapter;
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";
    private boolean mRestored = false;
    private ShowViewModel mShowViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer(this);

        //Ensures that the user's key has been fetched
        UserAccountUtilities.checkUserKey(this);

        //Registers the Observers for ShowViewModel
        registerShowViewModelObservers();

        //Restores data if possible
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        //Sets up the Adapter
        setUpAdapter();

        //Sets up the typing listener
        setUpTypingListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to SearchActivity
        super.setSelectedNavItem(R.id.nav_search);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_REQUEST_CODE){
            //Updates the RecyclerView if the user added/removed a Show from My Series on the SpecificShowActivity
            if(resultCode == SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED){
                //Updates the showAdded attribute of the Show that has been changed
                if(data != null){
                    //Fetches data from the Intent
                    String showId = data.getStringExtra(SpecificShowActivity.SHOW_ID_KEY);
                    boolean isShowAdded = data.getBooleanExtra(SpecificShowActivity.SHOW_IS_ADDED_KEY, false);

                    //Updates the appropriate Show's showAdded attribute
                    if(showId != null){
                        for(int i = 0; i < mShows.size(); i++){
                            if((String.valueOf(mShows.get(i).getShowId())).equals(showId)){
                                mShows.get(i).setShowAdded(isShowAdded);
                                mAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                }
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
     * Registers Observers for the ShowViewModel
     */
    private void registerShowViewModelObservers(){
        //Initialises the ShowViewModel
        mShowViewModel = ViewModelProviders.of(this).get(ShowViewModel.class);

        //Registers an Observer to keep track of changes to the shows ArrayList
        mShowViewModel.getObservableShows().observe(this, new Observer<ArrayList<Show>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Show> shows) {
                if(shows != null && shows.size() > 0){
                    //Updates the mShows variable
                    mShows = shows;

                    //Displays the data
                    if(mAdapter != null){
                        mAdapter.setShows(mShows);
                    }
                }
            }
        });

        //Registers an Observer to keep track of whether an operation is ongoing or not
        mShowViewModel.getObservableOngoingOperation().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean ongoingOperation) {
                if(ongoingOperation != null){
                    if(ongoingOperation){
                        //Hides the RecyclerView and displays the ProgressBar
                        mPbSearch.setVisibility(View.VISIBLE);
                        mRvSearchResults.setVisibility(View.GONE);
                    }
                    else{
                        //Hides the ProgressBar and displays the RecyclerView
                        mPbSearch.setVisibility(View.GONE);
                        mRvSearchResults.setVisibility(View.VISIBLE);

                        //Displays a message if no series are found
                        if(mShows.size() == 0){
                            mTvNoSeriesFound.setVisibility(View.VISIBLE);
                        }
                        else{
                            mTvNoSeriesFound.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        });

        //Registers an Observer to retrieve responses from the TVMaze API
        mShowViewModel.getObservableResponse().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String response) {
                if(response != null){
                    //Parses the JSON response
                    parseJsonResponse(response);

                    //Resets the observable variable
                    mShowViewModel.getObservableResponse().setValue(null);
                }
            }
        });
    }

    /**
     * Restores any saved data
     * @param savedInstanceState The Bundle containing the Activity's data
     */
    private void restoreData(Bundle savedInstanceState){
        mRestored = true;

        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            mShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);
        }
    }

    /**
     * Sets up the Adapter
     */
    private void setUpAdapter(){
        //Sets up Adapter to RecyclerView
        mAdapter = new ShowAdapter(this, mShows, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvSearchResults.setLayoutManager(linearLayoutManager);
        mRvSearchResults.setAdapter(mAdapter);
        mRvSearchResults.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

    /**
     * Sets up the typing listener
     */
    private void setUpTypingListener(){
        mEtSearchSeries.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!mRestored){
                    //Performs a search using the text the user has entered
                    searchShows();
                }

                //Resets variable
                mRestored = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Retrieves the text that the user searches for, and then searches for that text using the API
     */
    private void searchShows(){
        //Fetches user's input
        String searchText = mEtSearchSeries.getText().toString();

        //Hides the TextViews
        mTvNoSeriesFound.setVisibility(View.INVISIBLE);
        mTextNoInternetConnection.setVisibility(View.GONE);

        //Cancels any previous requests and clears the previous results
        mShowViewModel.cancelAsyncTasks();
        mShows.clear();
        mAdapter.notifyDataSetChanged();

        if(NetworkUtilities.isOnline(this)){
            //Connects to the TVMaze API using the specific URL for the selected show
            mShowViewModel.requestJsonResponse(LinkUtilities.getSearchLink(searchText));
        }
        else{
            //Displays no Internet connection message
            mTextNoInternetConnection.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Parses the JSON returned from the API, and populates the RecyclerView with the data
     * @param response The JSON response retrieved from the API
     */
    private void parseJsonResponse(String response) {
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

                    //Parses the JSON and creates a Show object to add to the mShows ArrayList
                    if(json != null){
                        mShows.add(JsonUtilities.parseShowJson(json, this, false, null, mShowViewModel));
                    }
                }

                //Determines which Shows have been added to My Series by the user
                mShowViewModel.markShowsInMySeries(mShows);
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
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }
}