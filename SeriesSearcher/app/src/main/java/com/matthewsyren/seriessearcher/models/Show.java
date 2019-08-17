package com.matthewsyren.seriessearcher.models;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.services.FirebaseService;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class holds the details that are retrieved for the TV Shows
 */

public class Show
        implements Parcelable {
    //Declarations
    private int showId;
    private String showImageUrl;
    private String showTitle;
    private String showRating;
    private String showStatus;
    private String showNextEpisode;
    private String showRuntime;
    private Boolean showAdded;
    private String showPremieredDate;
    private String showLanguages;
    private String showGenres;
    private String showSummary;
    private String showPreviousEpisode;

    /**
     * Constructor (used for the RecyclerViews)
     * @param showId The ID of the Show
     * @param showTitle The title of the Show
     * @param showRating The rating of the Show (out of 10)
     * @param showStatus The status of the Show (whether it is still running or not)
     * @param showImageUrl The URL of the poster for the Show
     * @param showRuntime The runtime of the Show in minutes
     * @param showAdded True if the Show has been added to My Series, otherwise false
     * @param showNextEpisode The episode number and date of the next episode for the Show
     */
    public Show(int showId, String showTitle, String showRating, String showStatus, String showImageUrl, String showRuntime, Boolean showAdded, String showNextEpisode) {
        this.showId = showId;
        this.showTitle = showTitle;
        this.showRating = showRating;
        this.showStatus = showStatus;
        this.showImageUrl = showImageUrl;
        this.showRuntime = showRuntime;
        this.showAdded = showAdded;
        this.showNextEpisode = showNextEpisode;
    }

    /**
     * Constructor (used for more detailed information on the Show)
     * @param showId The ID of the Show
     * @param showImageUrl The URL of the poster for the Show
     * @param showTitle The title of the Show
     * @param showRating The rating of the Show (out of 10)
     * @param showStatus The status of the Show (whether it is still running or not)
     * @param showNextEpisode The episode number and date of the next episode of the Show
     * @param showRuntime The runtime of the Show in minutes
     * @param showAdded True if the Show has been added to My Series, otherwise false
     * @param showPremieredDate The date that the Show premiered
     * @param showLanguages The languages used in the Show (separated by commas)
     * @param showGenres The genres of the Show (separated by commas)
     * @param showSummary The summary of the Show's plot
     * @param showPreviousEpisode The episode number and date of the previous episode for the Show
     */
    public Show(int showId, String showImageUrl, String showTitle, String showRating, String showStatus, String showNextEpisode, String showRuntime, Boolean showAdded, String showPremieredDate, String showLanguages, String showGenres, String showSummary, String showPreviousEpisode) {
        this.showId = showId;
        this.showImageUrl = showImageUrl;
        this.showTitle = showTitle;
        this.showRating = showRating;
        this.showStatus = showStatus;
        this.showNextEpisode = showNextEpisode;
        this.showRuntime = showRuntime;
        this.showAdded = showAdded;
        this.showPremieredDate = showPremieredDate;
        this.showLanguages = showLanguages;
        this.showGenres = showGenres;
        this.showSummary = showSummary;
        this.showPreviousEpisode = showPreviousEpisode;
    }

    /**
     * Getter method
     * @return The ID of the Show
     */
    public int getShowId(){
        return showId;
    }

    /**
     * Getter method
     * @return The title of the Show
     */
    public String getShowTitle() {
        return showTitle;
    }

    /**
     * Getter method
     * @return The rating of the Show (out of 10)
     */
    public String getShowRating() {
        return showRating;
    }

    /**
     * Getter method
     * @return The status of the Show (e.g. running, ended etc.)
     */
    public String getShowStatus() {
        return showStatus;
    }

    /**
     * Getter method
     * @return The episode number and date of the Show's next episode)
     */
    public String getShowNextEpisode() {
        return showNextEpisode;
    }

    /**
     * Getter method
     * @return The URL of the Show's poster
     */
    public String getShowImageUrl(){
        return showImageUrl;
    }

    /**
     * Getter method
     * @return The runtime of the Show (in minutes)
     */
    public String getShowRuntime() {
        return showRuntime;
    }

    /**
     * Getter method
     * @return The date that the Show premiered
     */
    public String getShowPremieredDate() {
        return showPremieredDate;
    }

    /**
     * Getter method
     * @return The languages used in the Show (separated by commas)
     */
    public String getShowLanguages() {
        return showLanguages;
    }

    /**
     * Getter method
     * @return The genres of the Show (separated by commas)
     */
    public String getShowGenres() {
        return showGenres;
    }

    /**
     * Getter method
     * @return The summary of the Show's plot
     */
    public String getShowSummary() {
        return showSummary;
    }

    /**
     * Getter method
     * @return The episode number and date of the Show's latest episode
     */
    public String getShowPreviousEpisode() {
        return showPreviousEpisode;
    }

    /**
     * Getter method
     * @return True if the Show has been added to My Series, otherwise false
     */
    public Boolean isShowAdded() {
        return showAdded;
    }

    /**
     * Setter method
     * @param showNextEpisode The episode number and date of the Show's next episode
     */
    public void setShowNextEpisode(String showNextEpisode) {
        this.showNextEpisode = showNextEpisode;
    }

    /**
     * Setter method
     * @param showAdded True if the Show has been added to My Series, otherwise false
     */
    public void setShowAdded(Boolean showAdded) {
        this.showAdded = showAdded;
    }

    /**
     * Setter method
     * @param showPreviousEpisode The episode number and date of the Show's latest episode
     */
    public void setShowPreviousEpisode(String showPreviousEpisode) {
        this.showPreviousEpisode = showPreviousEpisode;
    }

    /**
     * Starts a Service action that will loop through all Shows that the user has added to My Series, and set showAdded to true for each Show in shows that have been added to My Series
     * @param context The Context of the calling Activity
     * @param shows The ArrayList of Shows that must be marked if they have been added to My Series
     * @param resultReceiver The ResultReceiver to which data from the Service must be sent
     */
    public static void markShowsInMySeries(Context context, final ArrayList<Show> shows, ResultReceiver resultReceiver){
        //Sets up the Service
        Intent intent = new Intent(context, FirebaseService.class);
        Bundle bundle = new Bundle();
        intent.setAction(FirebaseService.ACTION_MARK_SHOWS_IN_MY_SERIES);
        intent.putExtra(FirebaseService.RESULT_RECEIVER, resultReceiver);
        bundle.putParcelableArrayList(FirebaseService.EXTRA_SHOWS, shows);
        bundle.putString(FirebaseService.EXTRA_USER_KEY, UserAccountUtilities.getUserKey(context));
        intent.putExtras(bundle);

        //Starts the Service
        context.startService(intent);
    }

    /**
     * Starts a Service action that will update the Show that the user has either added to or deleted from My Series in the Firebase Database
     * @param showAdded True if the user added the Show, otherwise false
     * @param context The Context of the calling Activity
     */
    public void updateShowInDatabase(boolean showAdded, Context context){
        //Sets up the Service
        Intent intent = new Intent(context, FirebaseService.class);
        Bundle bundle = new Bundle();
        intent.setAction(FirebaseService.ACTION_UPDATE_SHOW_IN_DATABASE);
        bundle.putString(FirebaseService.EXTRA_USER_KEY, UserAccountUtilities.getUserKey(context));
        bundle.putInt(FirebaseService.EXTRA_SHOW_ID, this.showId);
        bundle.putBoolean(FirebaseService.EXTRA_IS_SHOW_ADDED, showAdded);
        intent.putExtras(bundle);

        //Starts the Service
        context.startService(intent);

        //Displays a message and updates the Show object
        String message = showAdded ? context.getString(R.string.added_to_my_series, showTitle) : context.getString(R.string.removed_from_my_series, showTitle);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        setShowAdded(showAdded);
    }

    /**
     * Starts a Service action that fetches the Show IDs of Shows that the user has added to My Series
     * @param context The Context of the calling Activity
     * @param resultReceiver The ResultReceiver to which data from the Service must be sent
     */
    public static void getShowIdsInMySeries(Context context, ResultReceiver resultReceiver){
        //Sets up the Service
        Intent intent = new Intent(context, FirebaseService.class);
        Bundle bundle = new Bundle();
        intent.setAction(FirebaseService.ACTION_GET_SHOW_IDS);
        intent.putExtra(FirebaseService.RESULT_RECEIVER, resultReceiver);
        bundle.putString(FirebaseService.EXTRA_USER_KEY, UserAccountUtilities.getUserKey(context));
        intent.putExtras(bundle);

        //Starts the Service
        context.startService(intent);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(showId);
        dest.writeString(showImageUrl);
        dest.writeString(showTitle);
        dest.writeString(showRating);
        dest.writeString(showStatus);
        dest.writeString(showNextEpisode);
        dest.writeString(showRuntime);
        dest.writeByte((byte) ((showAdded != null && showAdded) ? 1 : 0));
        dest.writeString(showPremieredDate);
        dest.writeString(showLanguages);
        dest.writeString(showGenres);
        dest.writeString(showSummary);
        dest.writeString(showPreviousEpisode);
    }

    protected Show(Parcel in) {
        showId = in.readInt();
        showImageUrl = in.readString();
        showTitle = in.readString();
        showRating = in.readString();
        showStatus = in.readString();
        showNextEpisode = in.readString();
        showRuntime = in.readString();
        showAdded = in.readByte() == 1;
        showPremieredDate = in.readString();
        showLanguages = in.readString();
        showGenres = in.readString();
        showSummary = in.readString();
        showPreviousEpisode = in.readString();
    }

    public static final Creator<Show> CREATOR = new Creator<Show>() {
        @Override
        public Show createFromParcel(Parcel in) {
            return new Show(in);
        }

        @Override
        public Show[] newArray(int size) {
            return new Show[size];
        }
    };

    /**
     * Used to sort a List of Shows alphabetically by Show Title
     */
    public static class ShowTitleComparator
            implements Comparator<Show>{
        /**
         * Sorts by Show Title
         */
        @Override
        public int compare(Show o1, Show o2) {
            return o1.getShowTitle().compareToIgnoreCase(o2.getShowTitle());
        }
    }
}