package com.matthewsyren.seriessearcher.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchByEpisodeActivity
        extends AppCompatActivity
        implements IAPIConnectionResponse {
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

    //Variables
    private static final String SHOW_EPISODE_BUNDLE_KEY = "show_episode_bundle_key";
    private ShowEpisode mShowEpisode;

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

    /**
     * Restores any saved data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOW_EPISODE_BUNDLE_KEY)){
            //Restores the ShowEpisode object
            mShowEpisode = savedInstanceState.getParcelable(SHOW_EPISODE_BUNDLE_KEY);

            //Displays the episode's information
            displayEpisodeInformation(mShowEpisode);
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
            showTitle = bundle.getString("showTitle");
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
        try{
            //Displays ProgressBar and hides the search Button and episode information
            mLlSearchByEpisodeInformation.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mButtonSearch.setVisibility(View.INVISIBLE);

            //Hides the user's keyboard
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager != null){
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            //Fetches the show title from the Bundle and assigns input values to the variables
            Bundle bundle = getIntent().getExtras();
            String showNumber = "";
            if(bundle != null){
                showNumber = bundle.getString("showNumber");
            }

            //Gets the user's input
            int season = Integer.parseInt(mTextShowSeason.getText().toString());
            int episode = Integer.parseInt(mTextShowEpisode.getText().toString());

            //Fetches information from the TVMaze API
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute(LinkUtilities.getShowEpisodeInformationLink(showNumber, season, episode));
        }
        catch(NumberFormatException nfe){
            //Displays error message to the user
            Toast.makeText(getApplicationContext(), R.string.error_enter_whole_number, Toast.LENGTH_LONG).show();

            //Hides and displays the appropriate Views
            mProgressBar.setVisibility(View.INVISIBLE);
            mButtonSearch.setVisibility(View.VISIBLE);
            mLlSearchByEpisodeInformation.setVisibility(View.VISIBLE);
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

                //Clears the TextViews
                mTextShowAirDate.setText("");
                mTextShowEpisodeName.setText("");
                mTextShowRuntime.setText("");
                mTextShowSummary.setText("");
            }

            //Hides ProgressBar and displays the search Button and episode information
            mProgressBar.setVisibility(View.INVISIBLE);
            mButtonSearch.setVisibility(View.VISIBLE);
            mLlSearchByEpisodeInformation.setVisibility(View.VISIBLE);
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Displays the data for the episode
     */
    private void displayEpisodeInformation(ShowEpisode showEpisode){
        //Displays values in TextViews
        mTextShowEpisodeName.setText(getString(R.string.text_episode_name, showEpisode.getEpisodeName()));
        mTextShowAirDate.setText(getString(R.string.text_episode_air_date, showEpisode.getEpisodeAirDate()));
        mTextShowRuntime.setText(getString(R.string.text_episode_runtime, showEpisode.getEpisodeRuntime()));
        mTextShowSummary.setText(getString(R.string.text_episode_summary, showEpisode.getEpisodeSummary()));
    }
}