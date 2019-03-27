package com.matthewsyren.seriessearcher.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.RandomShowsActivity;
import com.matthewsyren.seriessearcher.activities.SearchActivity;

import java.util.ArrayList;

/**
 * Created by matthew on 2017/01/28.
 * Class holds the details that are retrieved for the TV Shows
 */

@SuppressWarnings("WeakerAccess")
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
     * Constructor (used for the ListViews)
     */
    public Show(int showId, String showTitle, String showRating, String showStatus, String showImageUrl, String showRuntime) {
        this.showId = showId;
        this.showTitle = showTitle;
        this.showRating = showRating;
        this.showStatus = showStatus;
        this.showImageUrl = showImageUrl;
        this.showRuntime = showRuntime;
    }

    /**
     * Constructor (used for more detailed information on the Show)
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
     */
    public int getShowId(){
        return showId;
    }

    /**
     * Getter method
     */
    public String getShowTitle() {
        return showTitle;
    }

    /**
     * Getter method
     */
    public String getShowRating() {
        return showRating;
    }

    /**
     * Getter method
     */
    public String getShowStatus() {
        return showStatus;
    }

    /**
     * Getter method
     */
    public String getShowNextEpisode() {
        return showNextEpisode;
    }

    /**
     * Getter method
     */
    public String getShowImageUrl(){
        return showImageUrl;
    }

    /**
     * Getter method
     */
    public String getShowRuntime() {
        return showRuntime;
    }

    /**
     * Getter method
     */
    public String getShowPremieredDate() {
        return showPremieredDate;
    }

    /**
     * Getter method
     */
    public String getShowLanguages() {
        return showLanguages;
    }

    /**
     * Getter method
     */
    public String getShowGenres() {
        return showGenres;
    }

    /**
     * Getter method
     */
    public String getShowSummary() {
        return showSummary;
    }

    /**
     * Getter method
     */
    public String getShowPreviousEpisode() {
        return showPreviousEpisode;
    }

    /**
     * Getter method
     */
    public Boolean isShowAdded() {
        return showAdded;
    }

    /**
     * Setter method
     */
    public void setShowNextEpisode(String showNextEpisode) {
        this.showNextEpisode = showNextEpisode;
    }

    /**
     * Setter method
     */
    public void setShowAdded(Boolean showAdded) {
        this.showAdded = showAdded;
    }

    /**
     * Setter method
     */
    public void setShowPreviousEpisode(String showPreviousEpisode) {
        this.showPreviousEpisode = showPreviousEpisode;
    }

    /**
     * Loops through all Shows that the user has added to My Series, and sets showAdded to true for each Show in lstShows that has been added to My Series
     */
    public static void markShowsThatAreAddedToMySeries(String userKey, final ArrayList<Show> lstShows, final SearchActivity searchActivity, final RandomShowsActivity randomShowsActivity){
        if(userKey != null){
            //Establishes a connection to Firebase
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

            //Sets the previous values for showAdded in lstShows to false
            for(int i = 0; i < lstShows.size(); i++){
                lstShows.get(i).setShowAdded(false);
            }

            //Fetches the user's Shows
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Gets the children of the Snapshot from the database
                    Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();

                    //Loops through all Shows and sets showAdded to true if the showIds match and the boolean is set to true
                    for(DataSnapshot snapshot : lstSnapshots){
                        //Fetches the showId
                        String showId = snapshot.getKey();

                        //Loops through lstShows to find a match in showId
                        for(int i = 0; i < lstShows.size(); i++){
                            //Sets showAdded to true if the showIds match and the boolean is set to true
                            if(showId != null && showId.equals("" + lstShows.get(i).getShowId()) && (boolean) snapshot.getValue()){
                                lstShows.get(i).setShowAdded(true);
                                break;
                            }
                        }
                    }

                    //Updates the appropriate Activity's data
                    if(searchActivity != null){
                        searchActivity.setLstShows(lstShows);
                    }
                    else if(randomShowsActivity != null){
                        randomShowsActivity.setLstShows(lstShows);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    /**
     * Updates the Shows that the user has added to My Series in the Firebase database
     */
    public void pushUserShowSelection(String userKey, boolean showAdded, Context context){
        //Establishes a connection to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(userKey);
        String message = showAdded ? context.getString(R.string.added_to_my_series, showTitle) : context.getString(R.string.removed_from_my_series, showTitle);

        //Saves the updated data to the Firebase database
        databaseReference.child("" + this.showId).setValue(showAdded);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        setShowAdded(showAdded);
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
}