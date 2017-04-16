package syrenware.seriessearcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Matthew Syrén on 2017/04/10.
 */

public class User {
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
    public void setUserKey(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("Users");

        //Adds Listeners for when the data is changed
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all children of the Users key in the Firebase database, and fetches the correct key for the user that is signed in (using the user's email address)
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : lstSnapshots){
                    String key = snapshot.getKey();
                    String email = (String) snapshot.getValue();

                    //Sets the user's key once the key has been located
                    if(email.equals(userEmailAddress)){
                        userKey = key;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("Data", "Failed to read data");
            }
        });
    }
}
