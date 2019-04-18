package com.matthewsyren.seriessearcher.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.fragments.RemoveShowFromMySeriesFragment;
import com.matthewsyren.seriessearcher.fragments.RemoveShowFromMySeriesFragment.IRemoveShowFromMySeriesFragmentOnClickListener;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.network.ApiConnection.IApiConnectionResponse;
import com.matthewsyren.seriessearcher.utilities.AsyncTaskUtilities;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.NetworkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpecificShowActivity
        extends AppCompatActivity
        implements IApiConnectionResponse,
        IRemoveShowFromMySeriesFragmentOnClickListener {
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
    @BindView(R.id.cl_no_internet_connection) ConstraintLayout mClNoInternetConnection;
    @Nullable
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout mCollapsingToolbarLayout;

    //Variables
    private Show mShow;
    private String mShowId;
    private String mShowTitle;
    private boolean mIsShowAdded;
    private Boolean mChanged;
    private boolean mShadowIcon = false;
    private ApiConnection mApiConnection;
    private RemoveShowFromMySeriesFragment mRemoveShowFromMySeriesFragment;

    //Constants
    public static final String SHOW_ID_KEY = "show_id_key";
    public static final String SHOW_TITLE_KEY = "show_title_key";
    public static final String SHOW_IS_ADDED_KEY = "show_is_added_key";
    private static final String SHOW_BUNDLE_KEY = "show_bundle_key";
    private static final String SHOW_ID_BUNDLE_KEY = "show_id_bundle_key";
    private static final String SHOW_TITLE_BUNDLE_KEY = "show_title_bundle_key";
    private static final String CHANGED_BUNDLE_KEY = "changed_bundle_key";

    //Codes
    public static final int SPECIFIC_SHOW_ACTIVITY_REQUEST_CODE = 1000;
    public static final int SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_show);
        ButterKnife.bind(this);

        //Sets the SupportActionBar if it hasn't been set already
        if(getSupportActionBar() == null && mToolbar != null){
            setSupportActionBar(mToolbar);
        }

        //Displays the back arrow icon
        setUpBackArrowIcon();

        //Fetches the Show's information if it hasn't been fetched already, otherwise restores it
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
        else{
            getShowInformation();
        }

        //Displays the Activity's title
        displayActivityTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Displays the ProgressBar if the show's information hasn't been fetched yet
        if(mShow == null && NetworkUtilities.isOnline(this) && AsyncTaskUtilities.isAsyncTaskRunning(mApiConnection)){
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Cancels the AsyncTask if it is still running
        AsyncTaskUtilities.cancelAsyncTask(mApiConnection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mShow != null){
            outState.putParcelable(SHOW_BUNDLE_KEY, mShow);
        }

        if(mShowId != null){
            outState.putString(SHOW_ID_BUNDLE_KEY, mShowId);
        }

        if(mChanged != null){
            outState.putBoolean(CHANGED_BUNDLE_KEY, mChanged);
        }

        if(mShowTitle != null){
            outState.putString(SHOW_TITLE_BUNDLE_KEY, mShowTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        else if(id == R.id.mi_specific_show_activity_toggle_show_added){
            if(NetworkUtilities.isOnline(this)){
                //Updates the data in the Firebase database (toggles showAdded from true to false, or vice versa)
                if(mShow.isShowAdded()){
                    //Initialises a DialogFragment that makes the user confirm their decision to remove a Show from My Series
                    FragmentManager fragmentManager = getFragmentManager();
                    mRemoveShowFromMySeriesFragment = new RemoveShowFromMySeriesFragment();

                    //Sends data to the DialogFragment
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(RemoveShowFromMySeriesFragment.SHOW_KEY, mShow);
                    mRemoveShowFromMySeriesFragment.setRemoveShowFromMySeriesFragmentOnClickListener(this);
                    mRemoveShowFromMySeriesFragment.setArguments(arguments);

                    //Displays the DialogFragment
                    mRemoveShowFromMySeriesFragment.show(fragmentManager, RemoveShowFromMySeriesFragment.REMOVE_SHOW_FROM_MY_SERIES_FRAGMENT_TAG);
                }
                else{
                    //Adds the Show to My Series
                    mShow.updateShowInDatabase(true, this);
                    showIsAddedStatusChanged();
                }
                return true;
            }
            else{
                //Displays a message telling the user to connect to the Internet
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Sets the result for the Activity if the user toggled the showAdded value of the Show (by using the menu)
        if(mChanged != null && mChanged){
            setResult(SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED);
        }

        //Resets mChanged
        mChanged = null;

        //Hides the FloatingActionButton
        mButtonSearchByEpisode.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mShow != null){
            //Inflates the Menu
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.activity_specific_show, menu);
            MenuItem menuItem = menu.findItem(R.id.mi_specific_show_activity_toggle_show_added);

            //Shows the appropriate icon for the Menu (a delete icon if the Show has been added to My Series, otherwise an add icon. A shadow is added to the icon if the AppBar is open in order to allow the icon to be visible over white images)
            if(mShow != null && mShow.isShowAdded() != null){
                if(mShow.isShowAdded()){
                    if(mShadowIcon){
                        menuItem.setIcon(R.drawable.ic_delete_white_shadow);
                    }
                    else{
                        menuItem.setIcon(R.drawable.ic_delete_white_24dp);
                    }
                }
                else{
                    if(mShadowIcon){
                        menuItem.setIcon(R.drawable.ic_add_white_shadow);
                    }
                    else{
                        menuItem.setIcon(R.drawable.ic_add_white_24dp);
                    }
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Marks the Show isAdded status to changed and updates the Menu icon
     */
    private void showIsAddedStatusChanged(){
        //Sets mChanged to true and refreshes the options menu
        mChanged = true;
        invalidateOptionsMenu();
    }

    /**
     * Restores the Activity's data
     */
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(SHOW_BUNDLE_KEY)){
            mShow = savedInstanceState.getParcelable(SHOW_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(SHOW_ID_BUNDLE_KEY)){
            mShowId = savedInstanceState.getString(SHOW_ID_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(CHANGED_BUNDLE_KEY)){
            mChanged = savedInstanceState.getBoolean(CHANGED_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(SHOW_TITLE_BUNDLE_KEY)){
            mShowTitle = savedInstanceState.getString(SHOW_TITLE_BUNDLE_KEY);
        }

        //Fetches the RemoveShowFromMySeriesFragment
        mRemoveShowFromMySeriesFragment = (RemoveShowFromMySeriesFragment) getFragmentManager()
                .findFragmentByTag(RemoveShowFromMySeriesFragment.REMOVE_SHOW_FROM_MY_SERIES_FRAGMENT_TAG);

        //Ensures updates from RemoveShowFromMySeriesFragment are sent to this Activity
        if(mRemoveShowFromMySeriesFragment != null){
            mRemoveShowFromMySeriesFragment.setRemoveShowFromMySeriesFragmentOnClickListener(this);
        }

        if(mShow != null){
            //Displays the Show's information
            displayShowInformation(mShow);
        }
        else{
            //Fetches the Show's information if it hasn't been fetched already
            getShowInformation();
        }
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
                            mShadowIcon = false;
                        }
                        else {
                            //Displays a back Button icon with a shadow when the AppBar is expanded (in order to make the icon visible regardless of the image behind it)
                            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_shadow);
                            mShadowIcon = true;
                        }

                        //Updates the Menu icons
                        invalidateOptionsMenu();
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
            //Gets the data from the Bundle and the link to the Show
            mShowId = bundle.getString(SHOW_ID_KEY);
            mIsShowAdded = bundle.getBoolean(SHOW_IS_ADDED_KEY);
            mShowTitle = bundle.getString(SHOW_TITLE_KEY);
            String showLink = LinkUtilities.getShowInformationLink(mShowId);

            //Displays ProgressBar
            mProgressBar.setVisibility(View.VISIBLE);

            //Displays/hides Views based on Internet connection status
            boolean online = NetworkUtilities.isOnline(this);
            toggleNoInternetMessageVisibility(online);

            if(online){
                //Fetches data from the TVMaze API using the link
                mApiConnection = new ApiConnection();
                mApiConnection.setApiConnectionResponse(this);
                mApiConnection.execute(showLink);
            }
        }
    }

    /**
     * Toggles the visibility of a no Internet connection message
     */
    private void toggleNoInternetMessageVisibility(boolean online){
        if(online){
            mClNoInternetConnection.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mButtonSearchByEpisode.setVisibility(View.VISIBLE);
        }
        else{
            mClNoInternetConnection.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mButtonSearchByEpisode.setVisibility(View.GONE);
        }
    }

    /**
     * Fetches the Show's information
     */
    public void refreshActivity(View view){
        getShowInformation();
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
                    mShow = JsonUtilities.parseFullShowJson(json, this, this, mIsShowAdded);
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
                    invalidateOptionsMenu();
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

            //Displays the FloatingActionButton
            mButtonSearchByEpisode.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets the title for the Activity
     */
    private void displayActivityTitle(){
        if(mShowTitle != null){
            if(mCollapsingToolbarLayout != null){
                mCollapsingToolbarLayout.setTitle(mShowTitle);
            }
            else if(getSupportActionBar() != null){
                getSupportActionBar().setTitle(mShowTitle);
            }
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

            //Sets the maxWidth of the ImageView to a quarter of the user's screen
            Display display = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            mImageViewSpecificShow.setMaxWidth(point.x / 4);
        }
        else{
            //Displays the show's poster
            Picasso.with(this)
                    .load(imageUrl)
                    .error(R.color.colorGray)
                    .placeholder(R.color.colorGray)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            //Displays the image
                            mImageViewSpecificShow.setImageBitmap(bitmap);

                            //Sets the image background to either the dominant or muted swatch (depending on what's available)
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@NonNull Palette palette) {
                                    //Gets the dominant swatch
                                    Palette.Swatch swatch = palette.getDominantSwatch();

                                    //Gets the muted swatch if the dominant swatch is null
                                    if (swatch == null) {
                                        swatch = palette.getMutedSwatch();
                                    }

                                    //Sets the image background to the swatch (if it is not null)
                                    if(swatch != null){
                                        mImageViewSpecificShow.setBackgroundColor((swatch.getRgb()));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
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

    @Override
    public void onRemoveShowFromMySeriesFragmentClick(boolean removed, Show show) {
        //Updates the options menu if the user has removed the Show from My Series
        if(removed){
            showIsAddedStatusChanged();
        }
    }
}