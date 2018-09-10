package com.matthewsyren.seriessearcher.utilities;

import android.content.Context;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtilities {
    /**
     * Returns a Show object parsed from the JSONObject passed in
     * @param json The JSON to be parsed
     */
    public static Show parseShowJson(JSONObject json, Context context, IAPIConnectionResponse delegate, boolean fetchNextEpisode) throws JSONException{
        //Fetches values
        int id = json.getInt("id");
        String name = json.getString("name");
        String status = json.getString("status");
        String rating = json.getJSONObject("rating").getString("average");
        String runtime = json.getString("runtime");
        String imageUrl;

        //Gets the image URL for the current show if there is a URL provided, otherwise sets the URL to null
        if(!json.getString("image").equals("null")){
            imageUrl = json.getJSONObject("image").getString("medium");
        }
        else{
            imageUrl = null;
        }

        //Adds N/A if the data is not found
        if(rating.equalsIgnoreCase("null") || rating.length() == 0){
            rating = context.getString(R.string.n_a);
        }

        if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
            runtime = context.getString(R.string.n_a);
        }

        JSONObject links = json.getJSONObject("_links");

        //Fetches information about the next episode
        if(fetchNextEpisode && links.has("nextepisode")){
            String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");

            //Fetches data from the TVMaze API using the link
            APIConnection api = new APIConnection();
            api.delegate = delegate;
            api.execute(nextEpisodeLink);
        }

        //Instantiates a Show object and adds it to the lstShows ArrayList
        Show show = new Show(id, name, rating, status, imageUrl, runtime);
        show.setShowNextEpisode(context.getString(R.string.n_a));
        show.setShowAdded(null);

        return show;
    }
}