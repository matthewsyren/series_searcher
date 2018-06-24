package com.matthewsyren.seriessearcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by matthew on 2017/01/28.
 * Class is used as a basis to hold the data for the users of the program
 */

@SuppressWarnings("WeakerAccess")
public class User {
    //Declarations
    private String userEmailAddress;
    private String userKey;
    private String userPassword;

    //Constructor (used when the user is creating an account)
    public User(String email, String password){
        userEmailAddress = email;
        userPassword = password;
    }

    //Default constructor (used when the user has already signed in, context is used to get the user's key from SharedPreferences)
    public User(Context context){
        //Fetches the user's email address and key from SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
        userEmailAddress = preferences.getString("userEmail", null);
        userKey = preferences.getString("userKey", null);
    }

    //Accessor methods
    public String getUserEmailAddress() {
        return userEmailAddress;
    }

    public String getUserKey() {
        return userKey;
    }

    public String getUserPassword() {
        return userPassword;
    }

    //Method gets the unique key used by Firebase to store information about the user signed in
    public void setUserKey(final Context context){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference().child("Users");

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all children of the Users key in the Firebase database, and fetches the correct key for the user that is signed in (using the user's email address)
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                boolean found = false;
                for(DataSnapshot snapshot : lstSnapshots){
                    String key = snapshot.getKey();
                    String email = (String) snapshot.getValue();

                    //Sets the user's key once the key has been located, and calls the method to log the user in
                    if(email.equals(userEmailAddress.replace("@googlemail.com", "@gmail.com"))){
                        userKey = key;
                        writeDataToSharedPreferences(userKey, context);
                        found = true;
                        databaseReference.removeEventListener(this);
                        break;
                    }
                }
                if(!found){
                    pushUser(context);
                    databaseReference.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("Data", "Failed to read data");
            }
        });
    }

    //Method returns the value which states whether the user has chosen Data Saving Mode
    public static boolean getDataSavingPreference(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("dataSavingMode", false);
    }

    //Method generates a unique key for the created user, and writes the key and its value (the user's email) to the 'Users' child in the Firebase database
    public void pushUser(Context context){
        //Establishes a connection to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child("Users");

        //Generates the user's key and saves the value (the user's email address) to the Firebase database
        String key =  databaseReference.push().getKey();
        databaseReference.child(key).setValue(userEmailAddress);

        //Saves the user's email and key to the device's SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userEmail", userEmailAddress.replace("@googlemail.com", "@gmail.com"));
        editor.putString("userKey", key);
        editor.putBoolean("dataSavingMode", false);
        editor.apply();

        Toast.makeText(context, "Account successfully created!", Toast.LENGTH_LONG).show();

        //Takes the user to the next activity
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    //Method writes the user's data to SharedPreferences and then takes the user to the HomeActivity
    public void writeDataToSharedPreferences(String key, Context context){
        //Saves the user's data in SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userEmail", userEmailAddress);
        editor.putString("userKey", key);
        editor.apply();

        //Takes the user to the HomeActivity
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }
}