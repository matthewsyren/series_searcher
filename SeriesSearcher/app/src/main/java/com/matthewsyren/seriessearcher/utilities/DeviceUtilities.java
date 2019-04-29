package com.matthewsyren.seriessearcher.utilities;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
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

    /**
     * Returns the device's height in pixels
     * @param windowManager The WindowManager of the calling Activity (retrieved using the getWindowManager() method)
     */
    public static int getDeviceHeight(WindowManager windowManager){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}