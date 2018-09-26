package com.matthewsyren.seriessearcher.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.network.IApiConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpecificShowActivity
        extends AppCompatActivity
        implements IApiConnectionResponse {
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
    @BindView(R.id.ll_specific_show) LinearLayout mLlSpecificShow;
    @Nullable
    @BindView(R.id.app_bar) AppBarLayout mAppBar;
    @Nullable
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.button_search_by_episode) FloatingActionButton mButtonSearchByEpisode;

    //Variables
    private Show mShow;
    private static String sShowId;

    //Constants
    public static final String SHOW_ID_KEY = "show_id_key";
    private static final String SHOW_BUNDLE_KEY = "show_bundle_key";
    private static final String SHOW_ID_BUNDLE_KEY = "show_id_bundle_key";

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
        if(mShow == null){
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

    /**
     * Restores the Activity's data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOW_BUNDLE_KEY)){
            mShow = savedInstanceState.getParcelable(SHOW_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(SHOW_ID_BUNDLE_KEY)){
            sShowId = savedInstanceState.getString(SHOW_ID_BUNDLE_KEY);
        }

        displayShowInformation(mShow);
    }

    /**
     * Displays a back arrow icon and adds functionality to the icon
     */
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

    /**
     * Sets the appropriate theme based on the device's orientation
     */
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

    /**
     * Fetches the information for the Show
     */
    private void getShowInformation(){
        //Fetches the link for the show that the user clicked on from the Bundle
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            //Gets the link to the Show
            sShowId = bundle.getString(SHOW_ID_KEY);
            String showLink = LinkUtilities.getShowInformationLink(sShowId);

            //Displays ProgressBar
            mProgressBar.setVisibility(View.VISIBLE);

            //Fetches data from the TVMaze API using the link
            ApiConnection api = new ApiConnection();
            api.delegate = this;
            api.execute(showLink);
        }
    }

    /**
     * Parses the JSON returned from the API and displays the data
     */
    @Override
    public void parseJsonResponse(String response) {
        try{
            //Assigns JSON data to variables if there is a valid JSON response
            if(response != null){
                JSONObject json = new JSONObject(response);
                String url = json.getString("url");

                //Parses the JSON based on the URL of the response
                if(url.startsWith(LinkUtilities.SHOW_LINK)){
                    //Parses the Show's main information and displays it
                    mShow = JsonUtilities.parseFullShowJson(json, this, this);
                    displayShowInformation(mShow);
                }
                else if(url.startsWith(LinkUtilities.EPISODE_LINK)){
                    //Parses the episode information about the Show
                    String displayText = JsonUtilities.parseShowEpisodeDate(json, this, true);

                    //Displays the text in the appropriate TextView
                    if(mTextShowLatestEpisode.getText().toString().length() == 0) {
                        mShow.setShowPreviousEpisode(displayText);
                    }
                    else{
                        mShow.setShowNextEpisode(displayText);
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

    /**
     * Displays the Show's information
     */
    private void displayShowInformation(Show show){
        if(show != null){
            //Displays the information
            mTextShowPremiered.setText(show.getShowPremieredDate());
            mTextShowLanguage.setText(show.getShowLanguages());
            mTextShowStatus.setText(show.getShowStatus());
            mTextShowRating.setText(show.getShowRating());
            mTextShowGenres.setText(show.getShowGenres());
            mTextShowSummary.setText(Html.fromHtml(show.getShowSummary()));

            //Displays the appropriate unit for the runtime
            if(!show.getShowRuntime().equals(getString(R.string.n_a))){
                mTextShowRuntime.setText(
                        getString(
                                R.string.text_minutes,
                                show.getShowRuntime()));
            }
            else{
                mTextShowRuntime.setText(show.getShowRuntime());
            }

            if(show.getShowPreviousEpisode() != null){
                mTextShowLatestEpisode.setText(show.getShowPreviousEpisode());
            }

            if(show.getShowNextEpisode() != null){
                mTextShowNextEpisode.setText(show.getShowNextEpisode());
            }

            if(show.getShowPreviousEpisode() != null && show.getShowNextEpisode() != null && show.getShowTitle() != null){
                //Hides ProgressBar and displays data
                mLlSpecificShow.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
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

            //Displays the FloatingActionButton
            mButtonSearchByEpisode.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Displays the image for the Show
     */
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

    /**
     * Takes the user to the SearchByEpisodeActivity
     */
    public void searchByEpisodeOnClick(View view) {
        //Fetches the link for the show that the user clicked on from the Bundle
        Bundle bundle = getIntent().getExtras();

        if(bundle != null && mShow != null){
            //Opens the SpecificShowActivity
            String showNumber = bundle.getString(SHOW_ID_KEY);
            Intent intent = new Intent(SpecificShowActivity.this, SearchByEpisodeActivity.class);
            intent.putExtra(SearchByEpisodeActivity.SHOW_TITLE_BUNDLE_KEY, mShow.getShowTitle());
            intent.putExtra(SearchByEpisodeActivity.SHOW_NUMBER_BUNDLE_KEY, showNumber);
            startActivity(intent);
        }
    }
}