package com.matthewsyren.seriessearcher.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.adapters.ListViewAdapter;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.network.IApiConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity
        extends BaseActivity
        implements IApiConnectionResponse {
    //View bindings
    @BindView(R.id.list_view_search_results) ListView mListViewSearchResults;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_search_series) EditText mTextSearchSeries;
    @BindView(R.id.tv_no_series_found) TextView mTvNoSeriesFound;

    //Declarations
    private ApiConnection api = new ApiConnection();
    private ArrayList<Show> lstShows =  new ArrayList<>();
    private ListViewAdapter adapter;
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer();

        //Ensures that the user's key has been fetched
        UserAccountUtilities.checkUserKey(this);

        //Hides ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        //Sets a custom adapter for the list_view_search_results ListView to display the search results
        adapter = new ListViewAdapter(this, lstShows, false);
        mListViewSearchResults.setAdapter(adapter);

        //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
        mListViewSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Intent intent = new Intent(SearchActivity.this, SpecificShowActivity.class);
                intent.putExtra(SpecificShowActivity.SHOW_ID_KEY, "" + lstShows.get(pos).getShowId());
                ImageView imageView = v.findViewById(R.id.image_show_poster);
                Bundle bundle = ActivityOptions
                        .makeSceneTransitionAnimation(SearchActivity.this, imageView, imageView.getTransitionName())
                        .toBundle();
                startActivity(intent, bundle);
            }
        });

        mTextSearchSeries.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Performs a search using the text the user has entered
                searchShows();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to SearchActivity
        super.setSelectedNavItem(R.id.nav_search);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(lstShows.size() > 0){
            outState.putParcelableArrayList(SHOWS_BUNDLE_KEY, lstShows);
        }
    }

    /**
     * Restores any saved data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            lstShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides ProgressBar
            mProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Retrieves the text that the user searches for in text_search, and then searches for that text using the API
     */
    private void searchShows(){
        //Fetches user's input
        String searchText = mTextSearchSeries.getText().toString();

        //Displays ProgressBar and hides the TextView
        mTvNoSeriesFound.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        //Cancels any previous requests and clears the previous results
        api.cancel(true);
        lstShows.clear();
        adapter.notifyDataSetChanged();

        //Connects to the TVMaze API using the specific URL for the selected show
        api = new ApiConnection();
        api.delegate = this;
        api.execute(LinkUtilities.getSearchLink(searchText));
    }

    /**
     * Parses the JSON returned from the API, and populates the list_view_search_results ListView with the data
     */
    @Override
    public void parseJsonResponse(String response) {
        try{
            //JSONArray stores the JSON returned from the TVMaze API
            if(response != null){
                JSONArray jsonArray = new JSONArray(response);
                lstShows.clear();

                //Loops through all Shows returned from the TVMaze API search result
                for(int i = 0; i < jsonArray.length(); i++){
                    //Instantiates JSONObject to store the results returned from the API
                    JSONObject json = jsonArray.getJSONObject(i);
                    json = json.getJSONObject("show");

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        lstShows.add(JsonUtilities.parseShowJson(json, this, this, false));
                    }
                }
                //Determines which Shows have been added to My Series by the user
                Show.checkIfShowIsAdded(UserAccountUtilities.getUserKey(this), lstShows, this, null);
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.error_fetching_data_no_internet_connection, Toast.LENGTH_LONG).show();
            }
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Setter method
     */
    public void setLstShows(ArrayList<Show> lstShows){
        this.lstShows = lstShows;

        //Updates the Adapter
        adapter.notifyDataSetChanged();

        //Displays a message if no series are found
        if(lstShows.size() == 0){
            mTvNoSeriesFound.setVisibility(View.VISIBLE);
        }
        else{
            mTvNoSeriesFound.setVisibility(View.INVISIBLE);
        }

        //Hides ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}