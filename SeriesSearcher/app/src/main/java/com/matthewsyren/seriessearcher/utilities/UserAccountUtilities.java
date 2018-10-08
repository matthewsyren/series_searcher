package com.matthewsyren.seriessearcher.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.HomeActivity;

public class UserAccountUtilities {
    //Constants
    private static final String USER_KEY = "user_key";
    public static final String DATA_SAVING_MODE_KEY = "dataSavingMode";

    /**
     * Returns the user's unique key (which is stored in SharedPreferences)
     */
    public static String getUserKey(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(USER_KEY, null);
    }

    /**
     * Saves the user's unique key in SharedPreferences
     * @param key The user's unique key for Firebase
     */
    public static void setUserKey(Context context, String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(USER_KEY, key);
        sharedPreferencesEditor.apply();
    }

    /**
     * Redirects the user to HomeActivity if their key is null
     */
    public static void checkUserKey(Context context){
        if(getUserKey(context) == null){
            Intent intent = new Intent(context, HomeActivity.class);
            context.startActivity(intent);
        }
    }

    /**
     * Returns the user's email address
     */
    public static String getUserEmailAddress(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance()
                .getCurrentUser();

        if(firebaseUser != null){
            return firebaseUser.getEmail();
        }
        return null;
    }

    /**
     * Returns the value which states whether the user has chosen Data Saving Mode
     */
    public static boolean getDataSavingPreference(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(DATA_SAVING_MODE_KEY, false);
    }

    /**
     * Changes the user's preferences for Data Saving Mode
     */
    public static void toggleDataSavingPreference(Context context){
        //Fetches the user's current preferences for Data Saving Mode
        boolean currentPreference = UserAccountUtilities.getDataSavingPreference(context);

        //Saves the user's new preference for Data Saving Mode
        SharedPreferences preferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(UserAccountUtilities.DATA_SAVING_MODE_KEY, !currentPreference);
        editor.apply();

        //Displays a message confirming the user's action
        if(!currentPreference){
            Toast.makeText(context, R.string.data_saving_mode_activated, Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(context, R.string.data_saving_mode_deactivated, Toast.LENGTH_LONG).show();
        }
    }
}