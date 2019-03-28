package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class RandomShowsActivity
        extends BaseActivity
        implements IApiConnectionResponse,
        IOnDataSavingPreferenceChangedListener,
        IShowUpdatedListener {
    //View bindings
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.recycler_view_random_shows) RecyclerView mRecyclerViewRandomShows;
    @BindView(R.id.button_refresh) Button mButtonRefresh;
    @BindView(R.id.text_no_internet) TextView mTextNoInternet;

    //Variables
    private ArrayList<Show> lstShows = new ArrayList<>();
    private ShowAdapter adapter;

    //Constants
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";
    private static final String API_PAGE_BUNDLE_KEY = "apiPage";
    private static final String API_STARTING_SHOW_BUNDLE_KEY = "apiStartingShow";

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

        //Sets up Adapter to RecyclerView
        adapter = new ShowAdapter(this, lstShows, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewRandomShows.setLayoutManager(linearLayoutManager);
        mRecyclerViewRandomShows.setAdapter(adapter);

        if(NetworkUtilities.isOnline(this)){
            if(lstShows.size() == 0){
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
                ApiConnection api = new ApiConnection();
                api.delegate = this;
                api.execute(LinkUtilities.getMultipleShowPageLink(page));
            }
        }
        else{
            //Displays a refresh Button
            displayRefreshButton();
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
                Show.markShowsThatAreAddedToMySeries(
                        UserAccountUtilities.getUserKey(this),
                        lstShows,
                        this);
            }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_random_shows, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.mi_refresh:
                refreshActivity();
                return true;
             default:
                 return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Restores any saved data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            lstShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides the ProgressBar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Refreshes the Activity
     */
    public void refreshActivity(View view){
        //Restarts the Activity
        refreshActivity();
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
                        lstShows.add(JsonUtilities.parseShowJson(json, this, this, false, null));
                    }
                    else{
                        //Exits the loop if the JSON returned is null
                        break;
                    }
                }

                //Determines which Shows have been added to My Series by the user
                Show.markShowsThatAreAddedToMySeries(
                        UserAccountUtilities.getUserKey(this),
                        lstShows,
                        this);
            }
            else{
                //Displays a refresh Button
                displayRefreshButton();
            }
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showsUpdated() {
        //Refreshes the RecyclerView's data
        adapter.notifyDataSetChanged();

        //Hides ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Refreshes the Activity in order to fetch 20 new randomised series
     */
    private void refreshActivity(){
        finish();
        startActivity(new Intent(this, RandomShowsActivity.class));
    }

    /**
     * Displays the refresh Button and a no Internet connection message
     */
    private void displayRefreshButton(){
        mTextNoInternet.setVisibility(View.VISIBLE);
        mButtonRefresh.setVisibility(View.VISIBLE);

        lstShows.clear();
    }

    @Override
    public void onDataSavingPreferenceChanged() {
        //Updates the images in the RecyclerView
        adapter.notifyDataSetChanged();
    }
}