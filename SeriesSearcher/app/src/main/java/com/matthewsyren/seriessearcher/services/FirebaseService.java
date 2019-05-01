package com.matthewsyren.seriessearcher.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matthewsyren.seriessearcher.models.Show;

import java.util.ArrayList;

public class FirebaseService
        extends IntentService {
    //ResultReceiver
    public static final String RESULT_RECEIVER = "result_receiver";

    //Actions and their result codes
    public static final String ACTION_GET_SHOW_IDS = "action_get_show_ids";
    public static final int ACTION_GET_SHOW_IDS_RESULT_CODE = 101;
    public static final String ACTION_MARK_SHOWS_IN_MY_SERIES = "action_mark_shows_in_my_series";
    public static final int ACTION_MARK_SHOWS_IN_MY_SERIES_RESULT_CODE = 102;
    public static final String ACTION_UPDATE_SHOW_IN_DATABASE = "action_update_show_in_database";

    //Extras
    public static final String EXTRA_SHOW_IDS = "extra_show_ids";
    public static final String EXTRA_USER_KEY = "extra_user_key";
    public static final String EXTRA_SHOWS = "extra_shows";
    public static final String EXTRA_IS_SHOW_ADDED = "extra_is_show_added";
    public static final String EXTRA_SHOW_ID = "extra_show_id";

    /**
     * Constructor
     */
    public FirebaseService() {
        super("FirebaseService");
    }

    //Calls the appropriate method based on the action passed in the Intent
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null){
            //Fetches data from the Intent
            ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
            String action = intent.getAction();
            String userKey = intent.getStringExtra(EXTRA_USER_KEY);

            if(action != null && userKey != null){
                switch(action){
                    case ACTION_GET_SHOW_IDS:
                        //Fetches the Show IDs that the user has added to My Series
                        getShowIds(userKey, resultReceiver);
                        break;
                    case ACTION_MARK_SHOWS_IN_MY_SERIES:
                        //Fetches data from the Intent
                        ArrayList<Show> shows = intent.getParcelableArrayListExtra(EXTRA_SHOWS);

                        //Marks Shows that have been added to My Series
                        if(shows != null){
                            markShowsInMySeries(userKey, shows, resultReceiver);
                        }
                        break;
                    case ACTION_UPDATE_SHOW_IN_DATABASE:
                        //Fetches data from the Intent
                        boolean isShowAdded = intent.getBooleanExtra(EXTRA_IS_SHOW_ADDED, false);
                        int showId = intent.getIntExtra(EXTRA_SHOW_ID, 0);

                        //Pushes the user's update to Firebase
                        if(showId > 0){
                            updateShowInDatabase(userKey, isShowAdded, showId);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Returns a DatabaseReference to the user's data in the Firebase Database
     */
    private DatabaseReference getFirebaseDatabaseReference(String userKey){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        return firebaseDatabase.getReference()
                .child(userKey);
    }

    /**
     * Fetches all Show IDs associated with the user's key, and adds them to an ArrayList
     */
    private void getShowIds(String userKey, final ResultReceiver resultReceiver){
        //Adds a Listener to fetch the data from the Firebase Database
        getFirebaseDatabaseReference(userKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Fetches the Shows from the Database
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();

                //Initialises the shows ArrayList
                ArrayList<String> shows = new ArrayList<>();

                //Loops through all Shows and adds each Show key that has a value of true to the shows ArrayList
                for(DataSnapshot snapshot : snapshots){
                    String showKey = snapshot.getKey();

                    if((boolean) snapshot.getValue()){
                        shows.add(showKey);
                    }
                }

                //Sends the data to the appropriate Activity
                returnShowIds(shows, resultReceiver);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Returns the user's Show IDs to the appropriate Activity
     */
    private void returnShowIds(ArrayList<String> showIds, ResultReceiver resultReceiver){
        if(resultReceiver != null){
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(EXTRA_SHOW_IDS, showIds);
            resultReceiver.send(ACTION_GET_SHOW_IDS_RESULT_CODE, bundle);
        }
    }

    /**
     * Loops through all Shows that the user has added to My Series, and sets showAdded to true for each Show in shows that have been added to My Series
     */
    private void markShowsInMySeries(String userKey, final ArrayList<Show> shows, final ResultReceiver resultReceiver){
        //Sets the previous values for showAdded in shows to false
        for(int i = 0; i < shows.size(); i++){
            shows.get(i).setShowAdded(false);
        }

        //Adds a Listener to fetch the data from the Firebase Database
        getFirebaseDatabaseReference(userKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Fetches the Shows from the Database
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();

                //Loops through all Shows and sets showAdded to true if the showIds match and the boolean is set to true
                for(DataSnapshot snapshot : snapshots){
                    //Fetches the showId
                    String showId = snapshot.getKey();

                    //Loops through shows to find a match in showId
                    for(int i = 0; i < shows.size(); i++){
                        //Sets showAdded to true if the showIds match and the boolean is set to true
                        if(showId != null && showId.equals("" + shows.get(i).getShowId()) && (boolean) snapshot.getValue()){
                            shows.get(i).setShowAdded(true);
                            break;
                        }
                    }
                }

                //Sends the updated Shows to the appropriate Activity
                returnMarkedShows(shows, resultReceiver);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Sends the shows ArrayList to the appropriate Activity
     */
    private void returnMarkedShows(ArrayList<Show> shows, ResultReceiver resultReceiver){
        if(resultReceiver != null && shows != null){
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(EXTRA_SHOWS, shows);
            resultReceiver.send(ACTION_MARK_SHOWS_IN_MY_SERIES_RESULT_CODE, bundle);
        }
    }

    /**
     * Updates the Show that the user has added to or removed from My Series in the Firebase Database
     */
    private void updateShowInDatabase(String userKey, boolean showAdded, int showId){
        //Saves the updated data to the Firebase database
        getFirebaseDatabaseReference(userKey).child("" + showId)
                .setValue(showAdded);
    }
}