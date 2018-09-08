package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
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
    @BindView(R.id.image_view_specific_show) ImageView mImageViewSpecificShow;
    @BindView(R.id.text_show_premiered) TextView mTextShowPremiered;
    @BindView(R.id.text_show_language) TextView mTextShowLanguage;
    @BindView(R.id.text_show_status) TextView mTextShowStatus;
    @BindView(R.id.text_show_genres) TextView mTextShowGenres;
    @BindView(R.id.text_show_runtime) TextView mTextShowRuntime;
    @BindView(R.id.text_show_rating) TextView mTextShowRating;
    @BindView(R.id.text_show_summary) TextView mTextShowSummary;
    @Nullable
    @BindView(R.id.app_bar) AppBarLayout mAppBar;
    @Nullable
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.button_search_by_episode) FloatingActionButton mButtonSearchByEpisode;

    //Variables
    private static final String SHOW_BUNDLE_KEY = "show_bundle_key";
    private static final String SHOW_ID_BUNDLE_KEY = "show_id_bundle_key";
    private Show mShow;
    private static String sShowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_show);
        ButterKnife.bind(this);

        //Displays the back arrow icon
        setUpBackArrowIcon();

        //Fetches the Show's information if it hasn't been fetched already, otherwise restores it
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
        else{
            getShowInformation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Displays the ProgressBar if the show's information hasn't been fetched yet
        if(mToolbar != null && mToolbar.getTitle() == null){
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mShow != null){
            outState.putParcelable(SHOW_BUNDLE_KEY, mShow);
        }

        if(sShowId != null){
            outState.putString(SHOW_ID_BUNDLE_KEY, sShowId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Hides the FloatingActionButton
        mButtonSearchByEpisode.setVisibility(View.GONE);
    }

    //Restores the Activity's data
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOW_BUNDLE_KEY)){
            mShow = savedInstanceState.getParcelable(SHOW_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(SHOW_ID_BUNDLE_KEY)){
            sShowId = savedInstanceState.getString(SHOW_ID_BUNDLE_KEY);
        }

        displayShowInformation(mShow);
    }

    //Displays a back arrow icon and adds functionality to the icon
    private void setUpBackArrowIcon(){
        if(mToolbar != null){
            //Displays a back arrow icon
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_shadow);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            if(mAppBar != null){
                /*
                 * Adds OffsetChangedListener to the AppBarLayout, which will display the appropriate icons when the AppBar is collapsed or expanded
                 * Adapted from https://stackoverflow.com/questions/31682310/android-collapsingtoolbarlayout-collapse-listener?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                 */
                mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                            //Displays a back Button icon with no shadow when the AppBar is collapsed
                            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
                        }
                        else {
                            //Displays a back Button icon with a shadow when the AppBar is expanded (in order to make the icon visible regardless of the image behind it)
                            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_shadow);
                        }
                    }
                });
            }
        }
    }

    //Sets the appropriate theme based on the device's orientation
    private void setTheme(){
        int orientation = getResources().getConfiguration().orientation;

        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            setTheme(R.style.AppTheme);

            //Displays back button
            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        else{
            setTheme(R.style.AppTheme_NoActionBar);
        }
    }

    //Method fetches the information for the Show
    private void getShowInformation(){
        //Fetches the link for the show that the user clicked on from the Bundle
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            //Gets the link to the Show
            sShowId = bundle.getString("showNumber");
            String showLink = LinkUtilities.getShowInformationLink(sShowId);

            //Displays ProgressBar
            mProgressBar.setVisibility(View.VISIBLE);

            //Fetches data from the TVMaze API using the link
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute(showLink);
        }
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
                    parseJson(json);
                }
                else if(json.has("season")){
                    String season = json.getString("season");
                    String episode = json.getString("number");

                    if(mTextShowLatestEpisode.getText().toString().length() == 0) {
                        String display = "Season " + season + ", Episode " + episode;
                        mTextShowLatestEpisode.setText(getResources().getString(R.string.text_latest_episode,display));
                        mShow.setShowPreviousEpisode(display);
                    }
                    else{
                        String airDate = json.getString("airdate");

                        if(airDate == null){
                            airDate = "";
                        }
                        else{
                            airDate = "(" + airDate + ")";
                        }
                        String display = "Season " + season + ", Episode " + episode + " " + airDate;
                        mTextShowNextEpisode.setText(getResources().getString(R.string.text_next_episode,display));
                        mShow.setShowNextEpisode(display);

                        //Hides ProgressBar
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    //Displays the Show's information
                    displayShowInformation(mShow);
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
    private void parseJson(JSONObject json){
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

            //Displays the genres separated by a comma
            String genres = "";
            if(arrGenres != null){
                mTextShowGenres.setText(getString(R.string.text_genres, arrGenres.get(0)));
                genres += arrGenres.get(0).toString();
                for(int i = 1; i < arrGenres.length(); i++){
                    mTextShowGenres.setText(getString(R.string.text_genres_sections, mTextShowGenres.getText(), arrGenres.get(i).toString()));
                    genres = getString(R.string.text_genres_sections, genres, arrGenres.get(i).toString());
                }
            }
            else{
                mTextShowGenres.setText(getString(R.string.n_a));
                genres = getString(R.string.n_a);
            }

            //Gets the summary
            summary = Show.formatSummary(summary);
            mTextShowSummary.setText(getString(R.string.text_summary, summary));

            //Creates a Show object with the appropriate information
            mShow = new Show(imageUrl,
                    name,
                    rating,
                    status,
                    null,
                    runtime,
                    null,
                    premiered,
                    language,
                    genres,
                    summary,
                    null);

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
                mTextShowLatestEpisode.setText(getString(R.string.text_latest_episode, getString(R.string.n_a)));
                mShow.setShowPreviousEpisode(getString(R.string.n_a));
            }

            if(links.has("nextepisode")){
                String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");

                //Fetches data from the TVMaze API using the link
                APIConnection api = new APIConnection();
                api.delegate = this;
                api.execute(nextEpisodeLink);
            }
            else{
                mTextShowNextEpisode.setText(getString(R.string.text_next_episode, getString(R.string.n_a)));
                mShow.setShowNextEpisode(getString(R.string.n_a));
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            displayShowInformation(mShow);
        }
        catch(JSONException jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Displays the Show's information
    private void displayShowInformation(Show show){
        if(show != null){
            //Displays the information
            mTextShowPremiered.setText(getString(R.string.text_premiered, show.getShowPremieredDate()));
            mTextShowLanguage.setText(getString(R.string.text_language, show.getShowLanguages()));
            mTextShowStatus.setText(getString(R.string.text_status, show.getShowStatus()));
            mTextShowRuntime.setText(getString(R.string.text_runtime, show.getShowRuntime()));
            mTextShowRating.setText(getString(R.string.text_rating, show.getShowRating()));
            mTextShowGenres.setText(getString(R.string.text_genres, show.getShowGenres()));
            mTextShowSummary.setText(getString(R.string.text_summary, show.getShowSummary()));

            if(show.getShowPreviousEpisode() != null){
                mTextShowLatestEpisode.setText(getString(R.string.text_latest_episode, show.getShowPreviousEpisode()));
            }

            if(show.getShowNextEpisode() != null){
                mTextShowNextEpisode.setText(getString(R.string.text_next_episode, show.getShowNextEpisode()));
            }

            //Displays the image for the Show
            displayImage(show.getShowImageUrl());

            //Displays the title for the Activity
            if(mToolbar != null){
                mToolbar.setTitle(show.getShowTitle());
            }
            else{
                if(getSupportActionBar() != null){
                    getSupportActionBar().setTitle(show.getShowTitle());
                }
            }
        }
    }

    //Displays the image for the Show
    private void displayImage(String imageUrl){
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
    }

    //Method takes the user to the SearchByEpisodeActivity
    public void searchByEpisodeOnClick(View view) {
        //Fetches the show name
        String showName = null;

        if(mToolbar != null && mToolbar.getTitle() != null){
            showName = mToolbar.getTitle().toString();
        }
        else{
            if(getSupportActionBar() != null && getSupportActionBar().getTitle() != null){
                showName = getSupportActionBar().getTitle().toString();
            }
        }

        //Fetches the link for the show that the user clicked on from the Bundle
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            String showNumber = bundle.getString("showNumber");
            String previousActivity = bundle.getString("previousActivity");

            Intent intent = new Intent(SpecificShowActivity.this, SearchByEpisodeActivity.class);
            intent.putExtra("showTitle", showName);
            intent.putExtra("showNumber", showNumber);
            intent.putExtra("previousActivity", previousActivity);
            startActivity(intent);
        }
    }
}