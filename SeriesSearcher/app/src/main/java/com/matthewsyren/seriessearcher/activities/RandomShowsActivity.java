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
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;
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
        implements IAPIConnectionResponse {
    //View bindings
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.list_view_random_shows) ListView mListViewRandomShows;
    @BindView(R.id.button_refresh) Button mButtonRefresh;
    @BindView(R.id.text_no_internet) TextView mTextNoInternet;

    //Declarations
    private ArrayList<Show> lstShows = new ArrayList<>();
    private ListViewAdapter adapter;
    private static final String SHOWS_BUNDLE_KEY = "shows_bundle_key";

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
                intent.putExtra("showNumber", "" + lstShows.get(pos).getShowId());
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
                toggleProgressBar(View.VISIBLE);

                //Fetches JSON from API (If a page on the API has already been determined, then it is fetched from the Bundle, otherwise the Math.random() method chooses a random page from the API to fetch)
                Bundle bundle = getIntent().getExtras();
                int page;
                if(bundle != null && bundle.getInt("apiPage") != -1){
                    page = bundle.getInt("apiPage");
                }
                else{
                    page = (int) (Math.random() * 100);
                }
                APIConnection api = new APIConnection();
                api.delegate = this;
                api.execute("http://api.tvmaze.com/shows?page=" + page);
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

    //Restores any saved data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOWS_BUNDLE_KEY)){
            lstShows = savedInstanceState.getParcelableArrayList(SHOWS_BUNDLE_KEY);

            //Hides the ProgressBar
            toggleProgressBar(View.GONE);
        }
    }

    //Refreshes the Activity
    public void refreshActivity(View view){
        //Restarts the Activity
        refreshActivity();
    }

    //Method toggles the visibility of the ProgressBar
    private void toggleProgressBar(int visibility){
        mProgressBar.setVisibility(visibility);
    }

    //Method fetches the JSON from the APIConnection class, and parses it
    @Override
    public void parseJsonResponse(String response) {
        try{
            if(response != null){
                //JSONArray stores the JSON returned from the TVMaze API
                JSONArray jsonArray = new JSONArray(response);

                //Fetches previous list of Shows from the Bundle if a starting show has already been determined, otherwise Math.random() is used to choose a random starting point to fetch data from the API. This allows the app to fetch different shows each time it runs
                Bundle bundle = getIntent().getExtras();
                int startingShow;
                if(bundle != null && bundle.getInt("apiStartingShow") != -1){
                    startingShow = bundle.getInt("apiStartingShow");
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

                        //Ensures that the data returned from the API is valid
                        if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                            rating = getString(R.string.n_a);
                        }
                        if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
                            runtime = getString(R.string.n_a);
                        }

                        //Instantiates a Show object and adds it to the list_view_random_shows ListView
                        Show show = new Show(id, name, rating, status, imageUrl);
                        show.setShowRuntime(runtime);
                        show.setShowAdded(null);
                        lstShows.add(show);
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

    //Method refreshes the Activity in order to fetch 20 new randomised series
    private void refreshActivity(){
        finish();
        startActivity(new Intent(this, RandomShowsActivity.class));
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