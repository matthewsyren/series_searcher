package com.matthewsyren.seriessearcher.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.ShowEpisode;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.network.ApiConnection.IApiConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.AsyncTaskUtilities;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.NetworkUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchByEpisodeActivity
        extends AppCompatActivity
        implements IApiConnectionResponse {
    //View bindings
    @BindView(R.id.text_show_title) TextView mTextShowTitle;
    @BindView(R.id.text_show_episode_name) TextView mTextShowEpisodeName;
    @BindView(R.id.text_show_air_date) TextView mTextShowAirDate;
    @BindView(R.id.text_show_runtime) TextView mTextShowRuntime;
    @BindView(R.id.text_show_summary) TextView mTextShowSummary;
    @BindView(R.id.text_show_season) EditText mTextShowSeason;
    @BindView(R.id.text_show_episode) EditText mTextShowEpisode;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.button_search) Button mButtonSearch;
    @BindView(R.id.ll_search_by_episode_information) LinearLayout mLlSearchByEpisodeInformation;
    @BindView(R.id.text_no_internet_connection) TextView mTextNoInternetConnection;
    @BindView(R.id.text_show_season_number) TextView mTextShowSeasonNumber;
    @BindView(R.id.text_show_episode_number) TextView mTextShowEpisodeNumber;

    //Variables
    private ShowEpisode mShowEpisode;
    private ApiConnection mApiConnection;

    //Constants
    private static final String SHOW_EPISODE_BUNDLE_KEY = "show_episode_bundle_key";
    public static final String SHOW_TITLE_BUNDLE_KEY = "show_title";
    public static final String SHOW_NUMBER_BUNDLE_KEY = "show_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_episode);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            restoreData(savedInstanceState);
        }

        //Displays Back button in ActionBar
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Hides ProgressBar
        mProgressBar.setVisibility(View.INVISIBLE);

        displayShowTitle();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mShowEpisode != null){
            outState.putParcelable(SHOW_EPISODE_BUNDLE_KEY, mShowEpisode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Cancels the AsyncTask if it is still running
        AsyncTaskUtilities.cancelAsyncTask(mApiConnection);
    }

    /**
     * Restores any saved data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOW_EPISODE_BUNDLE_KEY)){
            //Restores the ShowEpisode object
            mShowEpisode = savedInstanceState.getParcelable(SHOW_EPISODE_BUNDLE_KEY);

            if(mShowEpisode != null){
                //Displays the episode's information
                displayEpisodeInformation(mShowEpisode);
            }
        }
    }

    /**
     * Displays the title of the show
     */
    private void displayShowTitle(){
        //Fetches the show title from the Bundle
        Bundle bundle = getIntent().getExtras();
        String showTitle = "";
        if(bundle != null){
            showTitle = bundle.getString(SHOW_TITLE_BUNDLE_KEY);
        }

        mTextShowTitle.setText(showTitle);
        setTitle(getString(R.string.title_activity_search_by_episode));
    }

    /**
     * Takes the user back to the previous Activity when the back button is pressed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Takes the user back to the previous Activity if the button that was pressed was the back button
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Searches for the episode entered by the user
     */
    public void searchByEpisodeOnClick(View view){
        if(NetworkUtilities.isOnline(this)){
            try{
                //Displays ProgressBar and hides the search Button, episode information and no Internet connection message
                mLlSearchByEpisodeInformation.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mButtonSearch.setVisibility(View.INVISIBLE);
                mTextNoInternetConnection.setVisibility(View.GONE);

                //Hides the user's keyboard
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(inputMethodManager != null){
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                //Fetches the show title from the Bundle and assigns input values to the variables
                Bundle bundle = getIntent().getExtras();
                String showNumber = "";
                if(bundle != null){
                    showNumber = bundle.getString(SHOW_NUMBER_BUNDLE_KEY);
                }

                //Gets the user's input
                int season = Integer.parseInt(mTextShowSeason.getText().toString());
                int episode = Integer.parseInt(mTextShowEpisode.getText().toString());

                //Fetches information from the TVMaze API
                mApiConnection = new ApiConnection();
                mApiConnection.setApiConnectionResponse(this);
                mApiConnection.execute(LinkUtilities.getShowEpisodeInformationLink(showNumber, season, episode));
            }
            catch(NumberFormatException nfe){
                //Displays an error message to the user
                if(mTextShowEpisode.getText().toString().length() == 0 && mTextShowSeason.getText().toString().length() == 0){
                    Toast.makeText(getApplicationContext(), R.string.error_missing_season_and_episode_number, Toast.LENGTH_LONG).show();
                }
                else if(mTextShowSeason.getText().toString().length() == 0){
                    Toast.makeText(getApplicationContext(), R.string.error_missing_season_number, Toast.LENGTH_LONG).show();
                }
                else if(mTextShowEpisode.getText().toString().length() == 0){
                    Toast.makeText(getApplicationContext(), R.string.error_missing_episode_number, Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), R.string.error_enter_whole_number, Toast.LENGTH_LONG).show();
                }

                //Hides and displays the appropriate Views
                mProgressBar.setVisibility(View.INVISIBLE);
                mButtonSearch.setVisibility(View.VISIBLE);

                //Resets the search results
                resetSearchResults();
            }
        }
        else{
            //Hides the Show's information, displays the no Internet connection message and clears the previous episode data
            mLlSearchByEpisodeInformation.setVisibility(View.GONE);
            mTextNoInternetConnection.setVisibility(View.VISIBLE);
            mShowEpisode = null;
        }
    }

    /**
     * Parses the JSON returned from the TVMaze API and displays it
     */
    @Override
    public void parseJsonResponse(String response) {
        try{
            if(response != null){
                //Creates a JSONObject
                JSONObject jsonObject = new JSONObject(response);

                //Parses the JSON
                mShowEpisode = JsonUtilities.parseShowEpisode(jsonObject, this);

                //Displays the episode's information
                displayEpisodeInformation(mShowEpisode);
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.error_no_episode_information_found, Toast.LENGTH_LONG).show();

                //Resets the search results
                resetSearchResults();
            }

            //Hides ProgressBar and displays the search Button and episode information
            mProgressBar.setVisibility(View.INVISIBLE);
            mButtonSearch.setVisibility(View.VISIBLE);
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Resets the search results
     */
    private void resetSearchResults(){
        //Clears the TextViews and hides them
        mTextShowAirDate.setText("");
        mTextShowEpisodeName.setText("");
        mTextShowRuntime.setText("");
        mTextShowSummary.setText("");
        mLlSearchByEpisodeInformation.setVisibility(View.INVISIBLE);

        //Clears the mShowEpisode object
        mShowEpisode = null;
    }

    /**
     * Displays the data for the episode
     */
    private void displayEpisodeInformation(ShowEpisode showEpisode){
        //Displays values in TextViews
        mTextShowEpisodeName.setText(showEpisode.getEpisodeName());
        mTextShowAirDate.setText(showEpisode.getEpisodeAirDate());
        mTextShowSummary.setText(Html.fromHtml(showEpisode.getEpisodeSummary()));
        mTextShowSeasonNumber.setText(showEpisode.getSeasonNumber());
        mTextShowEpisodeNumber.setText(showEpisode.getEpisodeNumber());

        //Displays the appropriate unit for the runtime
        if(!showEpisode.getEpisodeRuntime().equals(getString(R.string.n_a))){
            mTextShowRuntime.setText(
                    getString(
                            R.string.text_minutes,
                            showEpisode.getEpisodeRuntime()));
        }
        else{
            mTextShowRuntime.setText(showEpisode.getEpisodeRuntime());
        }

        //Makes the Views visible
        mLlSearchByEpisodeInformation.setVisibility(View.VISIBLE);
    }
}