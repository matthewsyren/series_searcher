package syrenware.seriessearcher;

import android.graphics.Bitmap;

/**
 * Created by matthew on 2017/01/28.
 * Class holds the details that are retrieved for the TV Shows
 */

public class Show {
    //Declarations
    public Bitmap showImage = null;
    public String showImageUrl;
    public String showTitle;
    public String showRating;
    public String showStatus;
    public String showRuntime;
    public boolean changed = false;

    //Constructor
    public Show(String title, String rating, String status, String runtime, String imageUrl)
    {
        showImage = null;
        showTitle = title;
        showRating = rating;
        showStatus = status;
        showRuntime = runtime;
        showImageUrl = imageUrl;
    }

    //Accessor Methods
    public Bitmap getShowImage() {

        return showImage;
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

    public String getShowRuntime() {

        return showRuntime;
    }

    public String getShowImageUrl(){

        return showImageUrl;
    }

    public void setShowImage(Bitmap map){
        showImage = map;
        changed = true;
    }

    public void setShowTitle(String title){
        showTitle = title;
    }

    public boolean getChanged(){
        return changed;
    }
}
