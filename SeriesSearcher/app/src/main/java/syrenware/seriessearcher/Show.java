package syrenware.seriessearcher;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    private String showNextEpisode;
    private String showRuntime;

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

    //Mutator methods
    public void setShowNextEpisode(String showNextEpisode) {
        this.showNextEpisode = showNextEpisode;
    }

    public void setShowRuntime(String showRuntime) {
        this.showRuntime = showRuntime;
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