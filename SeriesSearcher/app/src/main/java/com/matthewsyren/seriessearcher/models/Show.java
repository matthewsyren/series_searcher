package com.matthewsyren.seriessearcher.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    //Constructor
    public Show(int showId, String showTitle, String showRating, String showStatus, String showImageUrl) {
        this.showId = showId;
        this.showTitle = showTitle;
        this.showRating = showRating;
        this.showStatus = showStatus;
        this.showImageUrl = showImageUrl;
    }

    //Accessor Methods
    public int getShowId(){
        return showId;
    }

    public String getShowTitle() {
        return showTitle;
    }

    public String getShowRating() {
        return showRating;
    }

    public String getShowStatus() {
        return showStatus;
    }

    public String getShowNextEpisode() {
        return showNextEpisode;
    }

    public String getShowImageUrl(){
        return showImageUrl;
    }

    public String getShowRuntime() {
        return showRuntime;
    }

    public Boolean isShowAdded() {
        return showAdded;
    }

    //Mutator methods
    public void setShowNextEpisode(String showNextEpisode) {
        this.showNextEpisode = showNextEpisode;
    }

    public void setShowRuntime(String showRuntime) {
        this.showRuntime = showRuntime;
    }

    public void setShowAdded(Boolean showAdded) {
        this.showAdded = showAdded;
    }

    //Method removes any HTML formatting from the summary field
    public static String formatSummary(Context context, String summary){
        boolean htmlIncluded = summary.contains("<");

        while(htmlIncluded){
            String beforeHTML = summary.substring(0, summary.indexOf("<"));
            String afterHTML = summary.substring(summary.indexOf(">") + 1);
            summary = beforeHTML + afterHTML;
            htmlIncluded = summary.contains("<");
        }

        return summary;
    }

    //Method loops through all Shows that the user has added to My Series, and sets showAdded to true if the Show that is passed into the method has been added to My Series
    public static void checkIfShowIsAdded(String userKey, final ArrayList<Show> lstShows, final SearchActivity searchActivity, final RandomShowsActivity randomShowsActivity){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all shows and sets the text of the Button to - if the user has added the show to 'My Series' already
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : lstSnapshots){
                    String showKey = snapshot.getKey();
                    for(int i = 0; i < lstShows.size(); i++){
                        if(showKey.equals("" + lstShows.get(i).getShowId()) && (boolean) snapshot.getValue()){
                            lstShows.get(i).setShowAdded(true);
                            break;
                        }
                    }
                }

                //Sets the Shows that haven't been found to false
                for(int i = 0; i < lstShows.size(); i++){
                    if(lstShows.get(i).isShowAdded() == null){
                        lstShows.get(i).setShowAdded(false);
                    }
                }
                //Removes DatabaseListener
                databaseReference.removeEventListener(this);

                //Updates the appropriate Activity's data
                if(searchActivity != null){
                    searchActivity.setLstShows(lstShows);
                }
                else if(randomShowsActivity != null){
                    randomShowsActivity.setLstShows(lstShows);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
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