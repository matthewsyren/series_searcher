package syrenware.seriessearcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

/**
 * Created by matthew on 2017/01/28.
 * Class holds the details that are retrieved for the TV Shows
 * showImage is stored in order to decrease data usage. When the user scrolls through a ListView that displays shows, the image would have to be fetched
 *           from the API multiple times, hence using lots of data. If it is stored in a variable, however, the data needs to be retrieved from the API only once.
 */

@SuppressWarnings("WeakerAccess")
public class Show {
    //Declarations
    private int showId;
    private String showImageUrl;
    private String showTitle;
    private String showRating;
    private String showStatus;
    private String showRuntime;
    private Bitmap showImage = null;

    //Constructor
    public Show(int id, String title, String rating, String status, String runtime, String imageUrl)
    {
        showId = id;
        showTitle = title;
        showRating = rating;
        showStatus = status;
        showRuntime = runtime;
        showImageUrl = imageUrl;
        showImage = null;
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

    public String getShowRuntime() {
        return showRuntime;
    }

    public String getShowImageUrl(){
        return showImageUrl;
    }

    public Bitmap getShowImage() {
        return showImage;
    }

    //Mutator Methods
    public void setShowImage(Bitmap map){
        showImage = map;
    }

    //Method removes any HTML formatting from the summary field
    public static String formatSummary(Context context, String summary){
        try{
            boolean htmlIncluded = summary.contains("<");
            while(htmlIncluded){
                String beforeHTML = summary.substring(0, summary.indexOf("<"));
                String afterHTML = summary.substring(summary.indexOf(">") + 1);
                summary = beforeHTML + afterHTML;
                htmlIncluded = summary.contains("<");
            }
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
        return summary;
    }
}