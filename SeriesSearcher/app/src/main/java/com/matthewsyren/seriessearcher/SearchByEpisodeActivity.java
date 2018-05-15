package com.matthewsyren.seriessearcher;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class SearchByEpisodeActivity extends AppCompatActivity implements IAPIConnectionResponse {
    private static final String EPISODE_NAME_BUNDLE_KEY = "episode_information_bundle_key";
    private static final String EPISODE_AIR_DATE_BUNDLE_KEY = "episode_air_date_bundle_key";
    private static final String EPISODE_RUNTIME_BUNDLE_KEY = "episode_runtime_bundle_key";
    private static final String EPISODE_SUMMARY_BUNDLE_KEY = "episode_summary_bundle_key";
    private static String sEpisodeName;
    private static String sEpisodeAirDate;
    private static String sEpisodeRuntime;
    private static String sEpisodeSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_search_by_episode);

            if (savedInstanceState != null) {
                restoreData(savedInstanceState);
            }

            //Displays Back button in ActionBar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            //Hides ProgressBar
            toggleProgressBar(View.INVISIBLE);

            displayShowTitle();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(!TextUtils.isEmpty(sEpisodeName)){
            outState.putString(EPISODE_NAME_BUNDLE_KEY, sEpisodeName);
        }

        if(!TextUtils.isEmpty(sEpisodeAirDate)){
            outState.putString(EPISODE_AIR_DATE_BUNDLE_KEY, sEpisodeAirDate);
        }

        if(!TextUtils.isEmpty(sEpisodeRuntime)){
            outState.putString(EPISODE_RUNTIME_BUNDLE_KEY, sEpisodeRuntime);
        }

        if(!TextUtils.isEmpty(sEpisodeSummary)){
            outState.putString(EPISODE_SUMMARY_BUNDLE_KEY, sEpisodeSummary);
        }
    }

    //Restores any saved data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(EPISODE_NAME_BUNDLE_KEY)){
            sEpisodeName = savedInstanceState.getString(EPISODE_NAME_BUNDLE_KEY);
            TextView txtEpisodeName = findViewById(R.id.text_show_episode_name);
            txtEpisodeName.setText(getString(R.string.text_episode_name, sEpisodeName));
        }

        if(savedInstanceState.containsKey(EPISODE_AIR_DATE_BUNDLE_KEY)){
            sEpisodeAirDate = savedInstanceState.getString(EPISODE_AIR_DATE_BUNDLE_KEY);
            TextView txtEpisodeAirDate = findViewById(R.id.text_show_air_date);
            txtEpisodeAirDate.setText(getString(R.string.text_episode_air_date, sEpisodeAirDate));
        }

        if(savedInstanceState.containsKey(EPISODE_RUNTIME_BUNDLE_KEY)){
            sEpisodeRuntime = savedInstanceState.getString(EPISODE_RUNTIME_BUNDLE_KEY);
            TextView txtEpisodeRuntime = findViewById(R.id.text_show_runtime);
            txtEpisodeRuntime.setText(getString(R.string.text_episode_runtime, sEpisodeRuntime));
        }

        if(savedInstanceState.containsKey(EPISODE_SUMMARY_BUNDLE_KEY)){
            sEpisodeSummary = savedInstanceState.getString(EPISODE_SUMMARY_BUNDLE_KEY);
            TextView txtEpisodeSummary = findViewById(R.id.text_show_summary);
            txtEpisodeSummary.setText(getString(R.string.text_episode_summary, sEpisodeSummary));
        }
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBar(int visibility){
        try{
            ProgressBar progressBar = findViewById(R.id.progress_bar);
            progressBar.setVisibility(visibility);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the title of the show
    public void displayShowTitle(){
        try{
            //Fetches the show title from the Bundle
            Bundle bundle = getIntent().getExtras();
            String showTitle = bundle.getString("showTitle");

            TextView txtShowTitle = findViewById(R.id.text_show_title);
            txtShowTitle.setText(showTitle);
            setTitle("Search for episode");
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Takes the user back to the DeliveryControlActivity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the DeliveryControlActivity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                onBackPressed();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Searches for the episode entered by the user
    public void searchByEpisodeOnClick(View view){
        try{
            //Displays ProgressBar
            toggleProgressBar(View.VISIBLE);

            //Hides the user's keyboard
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

            //Gets user's input
            EditText txtSeason = findViewById(R.id.text_show_season);
            EditText txtEpisode = findViewById(R.id.text_show_episode);

            //Fetches the show title from the Bundle and assigns input values to the variables
            Bundle bundle = getIntent().getExtras();
            String showNumber = bundle.getString("showNumber");
            int season = Integer.parseInt(txtSeason.getText().toString());
            int episode = Integer.parseInt(txtEpisode.getText().toString());

            //Fetches information from the TVMaze API
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute("http://api.tvmaze.com/shows/" + showNumber + "/episodebynumber?season=" + season + "&number="  + episode);
        }
        catch(NumberFormatException nfe){
            Toast.makeText(getApplicationContext(), "Please only enter whole numbers, and don't leave any fields empty", Toast.LENGTH_LONG).show();
            toggleProgressBar(View.INVISIBLE);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
            toggleProgressBar(View.INVISIBLE);
        }
    }

    //Method parses the JSON returned from the TVMaze API and displays it
    @Override
    public void parseJsonResponse(String response) {
        try{
            //Assigns Views to variables
            TextView txtEpisodeName = findViewById(R.id.text_show_episode_name);
            TextView txtEpisodeAirDate = findViewById(R.id.text_show_air_date);
            TextView txtEpisodeRuntime = findViewById(R.id.text_show_runtime);
            TextView txtEpisodeSummary = findViewById(R.id.text_show_summary);

            if(response != null){
                //Assigns JSON data to variables
                JSONObject jsonObject = new JSONObject(response);
                String episodeName = jsonObject.getString("name");
                String episodeAirDate = jsonObject.getString("airdate");
                String episodeRuntime = jsonObject.getString("runtime");
                String episodeSummary = jsonObject.getString("summary");
                episodeSummary = Show.formatSummary(this, episodeSummary);

                //Replaces any empty data with "N/A"
                if(episodeName.equalsIgnoreCase("null") || episodeName.length() == 0) {
                    episodeName = "N/A";
                }
                if(episodeAirDate.equalsIgnoreCase("null") || episodeAirDate.length() == 0){
                    episodeAirDate = "N/A";
                }
                if(episodeRuntime.equalsIgnoreCase("null") || episodeRuntime.length() == 0){
                    episodeRuntime = "N/A";
                }
                if(episodeSummary.equalsIgnoreCase("null") || episodeSummary.length() == 0){
                    episodeSummary = "N/A";
                }

                //Displays values in TextViews
                txtEpisodeName.setText(getString(R.string.text_episode_name, episodeName));
                txtEpisodeAirDate.setText(getString(R.string.text_episode_air_date, episodeAirDate));
                txtEpisodeRuntime.setText(getString(R.string.text_episode_runtime, episodeRuntime));
                txtEpisodeSummary.setText(getString(R.string.text_episode_summary, episodeSummary));

                //Assigns the variables to global variables for data restoration purposes
                sEpisodeName = episodeName;
                sEpisodeAirDate = episodeAirDate;
                sEpisodeRuntime = episodeRuntime;
                sEpisodeSummary = episodeSummary;
            }
            else{
                Toast.makeText(getApplicationContext(), "No information about that episode was found...", Toast.LENGTH_LONG).show();
                txtEpisodeAirDate.setText("");
                txtEpisodeName.setText("");
                txtEpisodeRuntime.setText("");
                txtEpisodeSummary.setText("");
            }

            //Hides ProgressBar
            toggleProgressBar(View.INVISIBLE);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}