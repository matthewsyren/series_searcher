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
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.adapters.SearchListViewAdapter;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity
        extends BaseActivity
        implements IAPIConnectionResponse {
    //View bindings
    @BindView(R.id.list_view_search_results) ListView mListViewSearchResults;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_search_series) EditText mTextSearchSeries;

    //Declarations
    private APIConnection api = new APIConnection();
    private ArrayList<Show> lstShows =  new ArrayList<>();
    private SearchListViewAdapter adapter;
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        //Sets the NavigationDrawer for the Activity
        super.onCreateDrawer();

        //Hides ProgressBar
        toggleProgressBar(View.INVISIBLE);

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        //Sets a custom adapter for the list_view_search_results ListView to display the search results
        adapter = new SearchListViewAdapter(this, lstShows);
        mListViewSearchResults.setAdapter(adapter);

        //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
        mListViewSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Intent intent = new Intent(SearchActivity.this, SpecificShowActivity.class);
                intent.putExtra("showNumber", "" + lstShows.get(pos).getShowId());
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

    //Restores any saved data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            lstShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides ProgressBar
            toggleProgressBar(View.GONE);
        }
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBar(int visibility){
        mProgressBar.setVisibility(visibility);
    }

    //Method retrieves the text that the user searches for in text_search, and then searches for that text using the API
    public void searchShows(){
        //Fetches user's input
        String searchText = mTextSearchSeries.getText().toString();

        //Displays ProgressBar
        toggleProgressBar(View.VISIBLE);

        //Connects to the TVMaze API using the specific URL for the selected show
        api.cancel(true);
        lstShows.clear();
        adapter.notifyDataSetChanged();
        api = new APIConnection();
        api.delegate = this;
        api.execute("http://api.tvmaze.com/search/shows?q=" + searchText);
    }

    //Method parses the JSON returned from the API, and populates the list_view_search_results ListView with the data
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
                        int id = json.getInt("id");
                        String name = json.getString("name");
                        String status = json.getString("status");
                        String runtime = json.getString("runtime");
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
                        if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
                            runtime = getString(R.string.n_a);
                        }

                        //Instantiates a Show object and adds it to the lstShows ArrayList
                        Show show = new Show(id, name, rating, status, imageUrl);
                        show.setShowRuntime(runtime);
                        show.setShowAdded(null);
                        lstShows.add(show);
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

    //Setter method
    public void setLstShows(ArrayList<Show> lstShows){
        this.lstShows = lstShows;

        //Updates the Adapter
        adapter.notifyDataSetChanged();

        //Hides ProgressBar
        toggleProgressBar(View.INVISIBLE);
    }
}