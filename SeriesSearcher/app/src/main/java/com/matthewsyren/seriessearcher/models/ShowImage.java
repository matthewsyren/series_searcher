package com.matthewsyren.seriessearcher.models;

import android.content.Context;
import android.content.res.Configuration;
import android.view.WindowManager;

import com.matthewsyren.seriessearcher.utilities.DeviceUtilities;

/**
 * Class used to store the width and height that the image for a Show must be resized to
 */

public class ShowImage {
    //Declarations
    private int height = 0;
    private int width = 0;

    /**
     * Constructor used to calculate the width and height that the image must be resized to
     * @param windowManager The WindowManager for the calling Activity
     * @param context The Context of the calling Activity
     */
    public ShowImage(WindowManager windowManager, Context context){
        //Fetches the device's orientation
        int orientation = context.getResources().getConfiguration().orientation;

        //Sets the appropriate dimensions for resizing the image (based on the device's orientation and dimensions)
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            height = DeviceUtilities.getDeviceHeight(windowManager);
        }
        else{
            width = DeviceUtilities.getDeviceWidth(windowManager);
        }
    }

    /**
     * Getter method
     */
    public int getHeight(){
        return height;
    }

    /**
     * Getter method
     */
    public int getWidth(){
        return width;
    }
}