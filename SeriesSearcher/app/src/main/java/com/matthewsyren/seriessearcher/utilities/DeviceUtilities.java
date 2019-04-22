package com.matthewsyren.seriessearcher.utilities;

import android.content.Context;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

public class DeviceUtilities {
    /**
     * Hides the device's keyboard
     * @param context The Context of the calling Activity
     * @param window The Window of the calling Activity (retrieved using the getWindow() method)
     */
    public static void hideKeyboard(Context context, Window window){
        //Fetches the InputMethodManager
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        //Hides the user's keyboard
        if(inputMethodManager != null){
            inputMethodManager.hideSoftInputFromWindow(window.getDecorView().getWindowToken(), 0);
        }
    }
}