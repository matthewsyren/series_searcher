package com.matthewsyren.seriessearcher.activities;

import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.fragments.RemoveShowFromMySeriesFragment;
import com.matthewsyren.seriessearcher.fragments.RemoveShowFromMySeriesFragment.IRemoveShowFromMySeriesFragmentOnClickListener;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.models.ShowImage;
import com.matthewsyren.seriessearcher.utilities.DeviceUtilities;
import com.matthewsyren.seriessearcher.utilities.JsonUtilities;
import com.matthewsyren.seriessearcher.utilities.LinkUtilities;
import com.matthewsyren.seriessearcher.utilities.NetworkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.matthewsyren.seriessearcher.viewmodels.ShowViewModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity displays information about a specific Show
 */

public class SpecificShowActivity
        extends AppCompatActivity
        implements IRemoveShowFromMySeriesFragmentOnClickListener {
    //View bindings
    @BindView(R.id.pb_specific_show) ProgressBar mPbSpecificShow;
    @BindView(R.id.tv_show_next_episode) TextView mTvShowNextEpisode;
    @BindView(R.id.tv_show_latest_episode) TextView mTvShowLatestEpisode;
    @BindView(R.id.iv_specific_show) ImageView mIvSpecificShow;
    @BindView(R.id.tv_show_premiered) TextView mTvShowPremiered;
    @BindView(R.id.tv_show_language) TextView mTvShowLanguage;
    @BindView(R.id.tv_show_status) TextView mTvShowStatus;
    @BindView(R.id.tv_show_genres) TextView mTvShowGenres;
    @BindView(R.id.tv_show_runtime) TextView mTvShowRuntime;
    @BindView(R.id.tv_show_rating) TextView mTvShowRating;
    @BindView(R.id.tv_show_summary) TextView mTvShowSummary;
    @BindView(R.id.ll_specific_show) LinearLayout mLlSpecificShow;
    @Nullable
    @BindView(R.id.abl_specific_show) AppBarLayout mAblSpecificShow;
    @Nullable
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fab_search_by_episode) FloatingActionButton mFabSearchByEpisode;
    @BindView(R.id.cl_no_internet_connection) ConstraintLayout mClNoInternetConnection;
    @Nullable
    @BindView(R.id.ctl_specific_show) CollapsingToolbarLayout mCtlSpecificShow;

    //Variables
    private Show mShow;
    private String mShowId;
    private String mShowTitle;
    private boolean mIsShowAdded;
    private Boolean mChanged;
    private boolean mShadowIcon = false;
    private RemoveShowFromMySeriesFragment mRemoveShowFromMySeriesFragment;
    private boolean mIsImageLoaded = false;
    private ShowViewModel mShowViewModel;

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

        //Registers Observers for the ShowViewModel
        registerShowViewModelObservers();

        //Restores data if possible, otherwise fetches the Show's information
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
        //Sets the result for the Activity if the user toggled the showAdded value of the Show (by using the menu)
        if(mChanged != null && mChanged){
            //Adds data to the Intent
            Intent intent = new Intent();
            intent.putExtra(SHOW_ID_KEY, mShowId);
            intent.putExtra(SHOW_IS_ADDED_KEY, mShow.isShowAdded());

            //Sends the result back to the previous Activity
            setResult(SPECIFIC_SHOW_ACTIVITY_RESULT_CHANGED, intent);
        }

        //Resets mChanged
        mChanged = null;

        //Hides the FloatingActionButton
        mFabSearchByEpisode.setVisibility(View.GONE);

        super.onBackPressed();
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
     * Registers Observers for the ShowViewModel
     */
    private void registerShowViewModelObservers(){
        //Initialises the ShowViewModel
        mShowViewModel = ViewModelProviders.of(this).get(ShowViewModel.class);

        //Registers an Observer to keep track of whether an operation is ongoing or not
        mShowViewModel.getObservableOngoingOperation().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean ongoingOperation) {
                if(ongoingOperation != null){
                    if(ongoingOperation){
                        //Displays the ProgressBar
                        mPbSpecificShow.setVisibility(View.VISIBLE);
                    }
                    else{
                        //Hides the ProgressBar
                        mPbSpecificShow.setVisibility(View.GONE);
                    }
                }
            }
        });

        //Registers an Observer to retrieve responses from the TVMaze API
        mShowViewModel.getObservableResponse().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String response) {
                if(response != null){
                    //Parses the JSON response
                    parseJsonResponse(response);

                    //Resets the observable variable
                    mShowViewModel.getObservableResponse().setValue(null);
                }
            }
        });
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
     * @param savedInstanceState The Bundle containing the Activity's data
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

            if(mAblSpecificShow != null){
                /*
                 * Adds OffsetChangedListener to the AppBarLayout, which will display the appropriate icons when the AppBar is collapsed or expanded
                 * Adapted from https://stackoverflow.com/questions/31682310/android-collapsingtoolbarlayout-collapse-listener?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                 */
                mAblSpecificShow.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
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

            //Displays/hides Views based on Internet connection status
            boolean online = NetworkUtilities.isOnline(this);
            toggleNoInternetMessageVisibility(online);

            if(online){
                //Fetches data from the TVMaze API using the link
                mShowViewModel.requestJsonResponse(showLink);
            }
        }
    }

    /**
     * Toggles the visibility of a no Internet connection message
     * @param online A boolean indicating whether there is an Internet connection or not
     */
    private void toggleNoInternetMessageVisibility(boolean online){
        if(online){
            mClNoInternetConnection.setVisibility(View.GONE);
            mFabSearchByEpisode.setVisibility(View.VISIBLE);
        }
        else{
            mClNoInternetConnection.setVisibility(View.VISIBLE);
            mFabSearchByEpisode.setVisibility(View.GONE);

            //Scrolls up to make the Views more visible
            scrollUp(false);
        }
    }

    /**
     * Fetches the Show's information
     * @param view The View that was clicked on
     */
    public void refreshActivity(View view){
        getShowInformation();
    }

    /**
     * Parses the JSON returned from the API and displays the data
     * @param response The JSON response retrieved from the API
     */
    private void parseJsonResponse(String response) {
        try{
            //Assigns JSON data to variables if there is a valid JSON response
            if(response != null){
                JSONObject json = new JSONObject(response);
                String url = json.getString("url");

                //Parses the JSON based on the URL of the response
                if(url.startsWith(LinkUtilities.SHOW_LINK)){
                    //Parses the Show's main information and displays it
                    mShow = JsonUtilities.parseFullShowJson(json, this, mIsShowAdded, mShowViewModel);
                    displayShowInformation(mShow);
                }
                else if(url.startsWith(LinkUtilities.EPISODE_LINK)){
                    //Parses the episode information about the Show
                    String displayText = JsonUtilities.parseShowEpisodeDate(json, this, true);

                    //Displays the text in the appropriate TextView
                    if(mTvShowLatestEpisode.getText().toString().length() == 0) {
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
     * @param show A Show object with information about the Show that is to be displayed
     */
    private void displayShowInformation(Show show){
        if(show != null){
            //Displays the information for the Show
            mTvShowPremiered.setText(show.getShowPremieredDate());
            mTvShowLanguage.setText(show.getShowLanguages());
            mTvShowStatus.setText(show.getShowStatus());
            mTvShowRating.setText(show.getShowRating());
            mTvShowGenres.setText(show.getShowGenres());
            mTvShowSummary.setText(Html.fromHtml(show.getShowSummary()));

            //Displays the appropriate unit for the runtime
            if(!show.getShowRuntime().equals(getString(R.string.n_a))){
                mTvShowRuntime.setText(
                        getString(
                                R.string.text_minutes,
                                show.getShowRuntime()));
            }
            else{
                mTvShowRuntime.setText(show.getShowRuntime());
            }

            if(show.getShowPreviousEpisode() != null){
                mTvShowLatestEpisode.setText(show.getShowPreviousEpisode());
            }

            if(show.getShowNextEpisode() != null){
                mTvShowNextEpisode.setText(show.getShowNextEpisode());
            }

            if(show.getShowPreviousEpisode() != null && show.getShowNextEpisode() != null && show.getShowTitle() != null){
                //Displays data
                mLlSpecificShow.setVisibility(View.VISIBLE);
            }

            //Displays the image for the Show
            displayImage(show.getShowImageUrl());

            //Displays the FloatingActionButton
            mFabSearchByEpisode.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets the title for the Activity
     */
    private void displayActivityTitle(){
        if(mShowTitle != null){
            if(mCtlSpecificShow != null){
                mCtlSpecificShow.setTitle(mShowTitle);
            }
            else if(getSupportActionBar() != null){
                getSupportActionBar().setTitle(mShowTitle);
            }
        }
    }

    /**
     * Displays the image for the Show
     * @param imageUrl The URL of the image to be displayed for the Show
     */
    private void displayImage(String imageUrl){
        //Displays a default image if the show doesn't have a poster or the user has enabled data saving mode, otherwise displays a default image
        if((UserAccountUtilities.getDataSavingPreference(this) || imageUrl == null) && !mIsImageLoaded){
            //Displays a default image
            mIvSpecificShow.setScaleType(ImageView.ScaleType.CENTER);
            mIvSpecificShow.setImageResource(R.mipmap.ic_launcher);

            //Sets the ImageView's background to the colorPrimary colour
            mIvSpecificShow.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            //Sets the maxWidth and maxHeight of the ImageView to a quarter of the user's screen
            Display display = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            mIvSpecificShow.setMaxWidth(point.x / 4);
            mIvSpecificShow.setMaxHeight(point.y / 4);

            //Marks the image as loaded
            mIsImageLoaded = true;
        }
        else if(!mIsImageLoaded){
            //Sets the scroll offset to 0
            scrollToOffset(0, 0);

            try{
                //Initialises ShowImage
                ShowImage showImage = new ShowImage(getWindowManager(), this);

                //Displays the show's poster
                Picasso.with(this)
                        .load(imageUrl)
                        .resize(showImage.getWidth(), showImage.getHeight())
                        .onlyScaleDown()
                        .error(R.color.colorGray)
                        .placeholder(R.color.colorGray)
                        .into(mIvSpecificShow, new Callback() {
                            @Override
                            public void onSuccess() {
                                //Marks the image as loaded
                                mIsImageLoaded = true;
                                Bitmap bitmap = ((BitmapDrawable) mIvSpecificShow.getDrawable()).getBitmap();

                                //Applies the Show poster's swatch
                                applyShowPosterSwatch(bitmap);

                                //Scrolls up
                                scrollUp(true);
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(getApplicationContext(), R.string.error_show_poster_not_loaded, Toast.LENGTH_LONG).show();
                            }
                        });
            }
            catch(Exception exc){
                Toast.makeText(this, R.string.error_show_poster_not_loaded, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Used to set the colour of various Views based on the swatch from the Show's poster
     * @param bitmap The Show's poster as a Bitmap
     */
    private void applyShowPosterSwatch(Bitmap bitmap){
        //Sets the colours of certain Views to either the dark vibrant or dark muted swatch (depending on what's available)
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@NonNull Palette palette) {
                //Gets the dark vibrant swatch
                Palette.Swatch swatch = palette.getDarkVibrantSwatch();

                //Gets the dark muted swatch if the dark vibrant swatch is null
                if (swatch == null) {
                    swatch = palette.getDarkMutedSwatch();
                }

                //Sets the colour for the CollapsingToolbarLayout/ActionBar, status bar and ImageView to the swatch's color
                if(swatch != null){
                    //Gets the swatch colour
                    int swatchColour = swatch.getRgb();

                    //Sets the status bar colour to a slightly darker colour than the swatch (to make it distinguishable from the CollapsingToolbarLayout/ActionBar)
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    int darkSwatchColour = ColorUtils.blendARGB(swatchColour, Color.BLACK, 0.2f);
                    window.setStatusBarColor(darkSwatchColour);

                    //Sets the ImageView's background colour
                    mIvSpecificShow.setBackgroundColor(swatchColour);

                    //Sets the CollapsingToolbarLayout/ActionBar's colour
                    if(mCtlSpecificShow != null){
                        mCtlSpecificShow.setContentScrimColor(swatchColour);
                    }
                    else if(getSupportActionBar() != null && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(swatchColour));
                    }
                }
            }
        });
    }

    /**
     * Scrolls up by half the ImageView's height
     * @param considerDeviceHeight Set to true if the device height must be considered when deciding to scroll, otherwise set to false
     */
    private void scrollUp(boolean considerDeviceHeight){
        if(mIvSpecificShow.getDrawable() != null){
            //Fetches the heights of the device and image
            int deviceHeight = DeviceUtilities.getDeviceHeight(getWindowManager());
            final int imageHeight = mIvSpecificShow.getDrawable().getIntrinsicHeight();

            //Scrolls up if the ImageView's image takes up more than half the device's height, or if considerDeviceHeight is false
            if(!considerDeviceHeight || (imageHeight > (deviceHeight / 2))){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollToOffset(-(imageHeight / 2), 400);
                    }
                }, 500);
            }
        }
    }

    /**
     * Scrolls to the specified offset
     * @param offset The offset in pixels to scroll to
     * @param duration The duration in milliseconds of the scroll
     */
    private void scrollToOffset(int offset, int duration){
        if(mAblSpecificShow != null) {
            //Fetches the AppBarLayout's Behavior
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAblSpecificShow.getLayoutParams();
            final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();

            //Performs an animated scroll
            if (behavior != null) {
                //Sets up the animation
                ValueAnimator valueAnimator = ValueAnimator.ofInt();
                valueAnimator.setInterpolator(new DecelerateInterpolator());
                valueAnimator.setIntValues(0, offset);
                valueAnimator.setDuration(duration);

                //Sets the AnimatorUpdateListener for the ValueAnimator
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        //Sets the offset for the AppBar
                        behavior.setTopAndBottomOffset((Integer) animation.getAnimatedValue());
                        mAblSpecificShow.requestLayout();
                    }
                });

                //Starts the scrolling animation
                valueAnimator.start();
            }
        }
    }

    /**
     * Takes the user to the SearchByEpisodeActivity
     * @param view The View that was clicked on
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