package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpecificShowActivity
        extends AppCompatActivity
        implements IAPIConnectionResponse{
    //View bindings
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_show_next_episode) TextView mTextShowNextEpisode;
    @BindView(R.id.text_show_latest_episode) TextView mTextShowLatestEpisode;
    @BindView(R.id.text_show_title) TextView mTextShowTitle;
    @BindView(R.id.image_view_specific_show) ImageView mImageViewSpecificShow;
    @BindView(R.id.text_show_premiered) TextView mTextShowPremiered;
    @BindView(R.id.text_show_language) TextView mTextShowLanguage;
    @BindView(R.id.text_show_status) TextView mTextShowStatus;
    @BindView(R.id.text_show_genres) TextView mTextShowGenres;
    @BindView(R.id.text_show_runtime) TextView mTextShowRuntime;
    @BindView(R.id.text_show_rating) TextView mTextShowRating;
    @BindView(R.id.text_show_summary) TextView mTextShowSummary;
    @BindView(R.id.app_bar) AppBarLayout mAppBar;
    @BindView(R.id.cl_specific_show) CoordinatorLayout mClSpecificShow;
    @BindView(R.id.button_search_by_episode) FloatingActionButton mButtonSearchByEpisode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_show);
        ButterKnife.bind(this);

        displayShowInformation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Displays the ProgressBar if the show's information hasn't been fetched yet
        if(TextUtils.isEmpty(mTextShowTitle.getText().toString())){
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Hides the FloatingActionButton
        mButtonSearchByEpisode.setVisibility(View.GONE);
    }

    //Method displays the information on the Activity
    public void displayShowInformation(){
        //Fetches the link for the show that the user clicked on from the Bundle
        Bundle bundle = getIntent().getExtras();
        String showNumber = bundle.getString("showNumber");
        String showLink = "http://api.tvmaze.com/shows/" + showNumber;

        //Displays ProgressBar
        toggleProgressBar(View.VISIBLE);

        //Fetches data from the TVMaze API using the link
        APIConnection api = new APIConnection();
        api.delegate = this;
        api.execute(showLink);
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBar(int visibility){
        mProgressBar.setVisibility(visibility);
    }

    //Method parses the JSON returned from the API and displays the data
    @Override
    public void parseJsonResponse(String response) {
        try{
            //Assigns JSON data to variables if there is a valid JSON response
            if(response != null){
                JSONObject json = new JSONObject(response);

                //If the JSON has a 'premiered' key, then it is used for the show's main information, and if it has a 'season' key, it has information about either the show's next or previous episode
                if(json.has("premiered")){
                    displayShowInformation(json);
                }
                else if(json.has("season")){
                    String season = json.getString("season");
                    String episode = json.getString("number");

                    if(mTextShowLatestEpisode.getText().toString().length() == 0) {
                        mTextShowLatestEpisode.setText(getResources().getString(R.string.text_latest_episode,"Season " + season + ", Episode " + episode));
                    }
                    else{
                        String airDate = json.getString("airdate");

                        if(airDate == null){
                            airDate = "";
                        }
                        else{
                            airDate = "(" + airDate + ")";
                        }
                        mTextShowNextEpisode.setText(getResources().getString(R.string.text_next_episode,"Season " + season + ", Episode " + episode + " " + airDate));

                        //Hides ProgressBar
                        toggleProgressBar(View.INVISIBLE);
                    }
                }
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.error_fetching_data_no_internet_connection, Toast.LENGTH_LONG).show();
            }
        }
        catch(JSONException j){
            Toast.makeText(getApplicationContext(), R.string.error_occurred, Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches the main information for the show from the TVMaze API, and then calls another link for more specific information about the show
    public void displayShowInformation(JSONObject json){
        try{
            String name = json.getString("name");
            String premiered = json.getString("premiered");
            String language = json.getString("language");
            String status = json.getString("status");
            String runtime = json.getString("runtime");
            JSONArray arrGenres;

            if(!json.getString("genres").equals("[]")){
                arrGenres = json.getJSONArray("genres");
            }
            else{
                arrGenres = null;
            }

            String rating = json.getJSONObject("rating").getString("average");
            String summary = json.getString("summary");
            String imageUrl;

            if(!json.getString("image").equals("null")){
                imageUrl = json.getJSONObject("image").getString("medium");
            }
            else{
                imageUrl = null;
            }

            //Replaces null values/empty Strings with "N/A"
            if(premiered.equalsIgnoreCase("null") || premiered.length() == 0){
                premiered = getString(R.string.n_a);
            }
            if(language.equalsIgnoreCase("null") || language.length() == 0){
                language = getString(R.string.n_a);
            }
            if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                rating = getString(R.string.n_a);
            }
            if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
                runtime = getString(R.string.n_a);
            }
            if(status.equalsIgnoreCase("null") || status.length() == 0){
                status = getString(R.string.n_a);
            }
            if(summary.equalsIgnoreCase("null") || summary.length() == 0){
                summary = getString(R.string.n_a);
            }

            //Displays the JSON data in the GUI components
            Resources resources = this.getResources();
            mTextShowTitle.setText(name);
            mTextShowPremiered.setText(resources.getString(R.string.text_premiered, premiered));
            mTextShowLanguage.setText(resources.getString(R.string.text_language, language));
            mTextShowStatus.setText(resources.getString(R.string.text_status, status));
            mTextShowRuntime.setText(resources.getString(R.string.text_runtime, runtime));
            mTextShowRating.setText(resources.getString(R.string.text_rating, rating));

            //Displays the genres separated by a comma
            if(arrGenres != null){
                mTextShowGenres.setText(resources.getString(R.string.text_genres, arrGenres.get(0)));
                for(int i = 1; i < arrGenres.length(); i++){
                    mTextShowGenres.setText(resources.getString(R.string.text_genres_sections, mTextShowGenres.getText(), arrGenres.get(i).toString()));
                }
            }
            else{
                mTextShowGenres.setText(getString(R.string.n_a));
            }
            summary = Show.formatSummary(summary);
            mTextShowSummary.setText(resources.getString(R.string.text_summary, summary));

            //Displays a default image if the show doesn't have a poster or the user has enabled data saving mode, otherwise displays a default image
            if(UserAccountUtilities.getDataSavingPreference(this) || imageUrl == null){
                //Displays a default image
                mImageViewSpecificShow.setScaleType(ImageView.ScaleType.CENTER);
                mImageViewSpecificShow.setImageResource(R.mipmap.ic_launcher);
            }
            else{
                //Displays the show's poster
                Picasso.with(this)
                        .load(imageUrl)
                        .error(R.color.colorGray)
                        .placeholder(R.color.colorGray)
                        .into(mImageViewSpecificShow);
                mImageViewSpecificShow.setBackgroundColor(getResources().getColor(R.color.colorImageBackground));
            }

            //Fetches data about the show's previous and next episodes (which are accessed using different links)
            JSONObject links = json.getJSONObject("_links");
            if(links.has("previousepisode")){
                String previousEpisodeLink = links.getJSONObject("previousepisode").getString("href");

                //Fetches data from the TVMaze API using the link
                APIConnection api = new APIConnection();
                api.delegate = this;
                api.execute(previousEpisodeLink);
            }
            else{
                mTextShowLatestEpisode.setText(resources.getString(R.string.text_latest_episode, "N/A"));
            }
            if(links.has("nextepisode")){
                String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");

                //Fetches data from the TVMaze API using the link
                APIConnection api = new APIConnection();
                api.delegate = this;
                api.execute(nextEpisodeLink);
            }
            else{
                mTextShowNextEpisode.setText(resources.getString(R.string.text_next_episode, "N/A"));
                toggleProgressBar(View.INVISIBLE);
            }
        }
        catch(JSONException jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method takes the user to the SearchByEpisodeActivity
    public void searchByEpisodeOnClick(View view) {
        //Fetches the show name
        String showName = mTextShowTitle.getText().toString();

        //Fetches the link for the show that the user clicked on from the Bundle
        Bundle bundle = getIntent().getExtras();
        String showNumber = bundle.getString("showNumber");
        String previousActivity = bundle.getString("previousActivity");

        Intent intent = new Intent(SpecificShowActivity.this, SearchByEpisodeActivity.class);
        intent.putExtra("showTitle", showName);
        intent.putExtra("showNumber", showNumber);
        intent.putExtra("previousActivity", previousActivity);
        startActivity(intent);
    }
}