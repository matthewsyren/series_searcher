package com.matthewsyren.seriessearcher.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
        implements IApiConnectionResponse {
    //View bindings
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.list_view_random_shows) ListView mListViewRandomShows;
    @BindView(R.id.button_refresh) Button mButtonRefresh;
    @BindView(R.id.text_no_internet) TextView mTextNoInternet;

    //Variables
    private ArrayList<Show> lstShows = new ArrayList<>();
    private ListViewAdapter adapter;

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
        super.onCreateDrawer();

        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }

        //Sets up Adapter to ListView
        adapter = new ListViewAdapter(this, lstShows, false);
        mListViewRandomShows.setAdapter(adapter);

        //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
        mListViewRandomShows.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                Intent intent = new Intent(RandomShowsActivity.this, SpecificShowActivity.class);
                intent.putExtra(SpecificShowActivity.SHOW_ID_KEY, "" + lstShows.get(pos).getShowId());
                ImageView imageView = v.findViewById(R.id.image_show_poster);
                Bundle bundle = ActivityOptions
                        .makeSceneTransitionAnimation(RandomShowsActivity.this, imageView, imageView.getTransitionName())
                        .toBundle();
                startActivity(intent, bundle);
            }
        });

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
            mTextNoInternet.setVisibility(View.VISIBLE);
            mButtonRefresh.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Sets the selected item in the NavigationDrawer to RandomShowsActivity
        super.setSelectedNavItem(R.id.nav_random_shows);
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
                int showCount = 0;

                //Loops through the 20 randomly chosen shows returned from the TVMaze API
                for(int i = 0; i < 20 && (startingShow + i) < jsonArray.length() - 1; i++){
                    //Creates new JSONObject to parse the data returned
                    JSONObject json = jsonArray.getJSONObject(startingShow + i);

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        lstShows.add(JsonUtilities.parseShowJson(json, this, this, false));
                        showCount++;
                    }
                    else{
                        //Exits the loop if the JSON returned is null
                        break;
                    }
                }

                //Determines which Shows have been added to My Series by the user
                Show.checkIfShowIsAdded(UserAccountUtilities.getUserKey(this), lstShows, null, this);
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
     * Refreshes the Activity in order to fetch 20 new randomised series
     */
    private void refreshActivity(){
        finish();
        startActivity(new Intent(this, RandomShowsActivity.class));
    }

    /**
     * Setter method
     */
    public void setLstShows(ArrayList<Show> lstShows){
        this.lstShows = lstShows;

        //Updates the Adapter
        adapter.notifyDataSetChanged();

        //Hides ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}