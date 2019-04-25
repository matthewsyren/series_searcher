package com.matthewsyren.seriessearcher.utilities;

import android.content.Context;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.models.ShowEpisode;
import com.matthewsyren.seriessearcher.network.ApiConnection;
import com.matthewsyren.seriessearcher.network.ApiConnection.IApiConnectionResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtilities {
    /**
     * Returns a Show object parsed from the JSONObject passed in
     * @param json The JSON to be parsed
     * @param context The Context of the calling Activity
     * @param iApiConnectionResponse The class that implements IApiConnectionResponse
     * @param fetchNextEpisode Set as true to fetch information about the Show's next episode
     * @return A Show object with the parsed JSON as values
     */
    public static Show parseShowJson(JSONObject json, Context context, IApiConnectionResponse iApiConnectionResponse, boolean fetchNextEpisode, Boolean showAdded) throws JSONException{
        //Fetches values
        int id = json.getInt("id");
        String name = json.getString("name");
        String status = json.getString("status");
        String rating = json.getJSONObject("rating").getString("average");
        String runtime = json.getString("runtime");
        String imageUrl = null;

        //Gets the image URL for the current show if there is a URL provided
        if(!json.getString("image").equals("null")){
            JSONObject imageJson = json.getJSONObject("image");

            if(!imageJson.getString("original").equals("null")){
                imageUrl = imageJson.getString("original");
            }
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
            ApiConnection api = new ApiConnection();
            api.setApiConnectionResponse(iApiConnectionResponse);
            api.execute(nextEpisodeLink);
        }

        //Instantiates a Show object and returns it
        return new Show(
                id,
                name,
                rating,
                status,
                imageUrl,
                runtime,
                showAdded,
                context.getString(R.string.n_a));
    }

    /**
     * Returns a Show object parsed from the JSONObject passed in
     * @param json The JSON to be parsed
     * @param context The Context of the calling Activity
     * @param iApiConnectionResponse The class that implements IApiConnectionResponse
     * @return A Show object with the parsed JSON as values
     */
    public static Show parseFullShowJson(JSONObject json, Context context, IApiConnectionResponse iApiConnectionResponse, Boolean showAdded) throws JSONException{
        //Parses the small version of the Show
        Show show = parseShowJson(json, context, iApiConnectionResponse, false, showAdded);

        //Parses the rest of the Show
        String premiered = json.getString("premiered");
        String language = json.getString("language");
        String summary = json.getString("summary");

        //Gets all genres
        JSONArray arrGenres;

        if(!json.getString("genres").equals("[]")){
            arrGenres = json.getJSONArray("genres");
        }
        else{
            arrGenres = null;
        }

        //Replaces null values/empty Strings with "N/A"
        if(premiered.equalsIgnoreCase("null") || premiered.length() == 0){
            premiered = context.getString(R.string.n_a);
        }
        if(language.equalsIgnoreCase("null") || language.length() == 0){
            language = context.getString(R.string.n_a);
        }
        if(summary.equalsIgnoreCase("null") || summary.length() == 0){
            summary = context.getString(R.string.no_summary_is_available);
        }

        //Displays the genres separated by a comma
        String genres = "";
        if(arrGenres != null){
            genres += arrGenres.get(0).toString();
            for(int i = 1; i < arrGenres.length(); i++){
                genres = context.getString(R.string.text_genres_sections, genres, arrGenres.get(i).toString());
            }
        }
        else{
            genres = context.getString(R.string.n_a);
        }

        //Initialises variables
        String nextEpisode = null;
        String previousEpisode = null;

        //Fetches data about the show's previous and next episodes (which are accessed using different links)
        JSONObject links = json.getJSONObject("_links");
        if(links.has("previousepisode")){
            String previousEpisodeLink = links.getJSONObject("previousepisode").getString("href");

            //Fetches data from the TVMaze API using the link
            ApiConnection api = new ApiConnection();
            api.setApiConnectionResponse(iApiConnectionResponse);
            api.execute(previousEpisodeLink);
        }
        else{
            previousEpisode = context.getString(R.string.n_a);
        }

        if(links.has("nextepisode")){
            String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");

            //Fetches data from the TVMaze API using the link
            ApiConnection api = new ApiConnection();
            api.setApiConnectionResponse(iApiConnectionResponse);
            api.execute(nextEpisodeLink);
        }
        else{
            nextEpisode = context.getString(R.string.n_a);
        }

        //Returns a Show object with the appropriate information
        return new Show(
                show.getShowId(),
                show.getShowImageUrl(),
                show.getShowTitle(),
                show.getShowRating(),
                show.getShowStatus(),
                nextEpisode,
                show.getShowRuntime(),
                showAdded,
                premiered,
                language,
                genres,
                summary,
                previousEpisode);
    }

    /**
     * Parses information about an episode of the Show
     * @param json The JSON to be parsed
     * @return The episode date
     */
    public static String parseShowEpisodeDate(JSONObject json, Context context, boolean addDateOnNextLine) throws JSONException{
        String season = json.getString("season");
        String episode = json.getString("number");
        String airDate = json.getString("airdate");
        String nextLine = addDateOnNextLine ? "\n" : "";

        //Sets the appropriate airDate
        if(airDate == null){
            airDate = "";
        }
        else{
            airDate = nextLine + "(" + airDate + ")";
        }

        return (context.getString(R.string.season_episode,
                context.getString(R.string.season),
                season,
                context.getString(R.string.episode),
                episode,
                airDate));
    }

    /**
     * Parses information about an episode of the Show
     * @param json The JSON to be parsed
     * @param context The Context of the calling Activity
     * @return The episode information in the form of a ShowEpisode object
     */
    public static ShowEpisode parseShowEpisode(JSONObject json, Context context) throws JSONException{
        //Parses the JSON
        String episodeName = json.getString("name");
        String episodeAirDate = json.getString("airdate");
        String episodeRuntime = json.getString("runtime");
        String episodeSummary = json.getString("summary");
        String seasonNumber = json.getString("season");
        String episodeNumber = json.getString("number");

        //Replaces any empty data with "N/A"
        if(episodeName.equalsIgnoreCase("null") || episodeName.length() == 0) {
            episodeName = context.getString(R.string.n_a);
        }
        if(episodeAirDate.equalsIgnoreCase("null") || episodeAirDate.length() == 0){
            episodeAirDate = context.getString(R.string.n_a);
        }
        if(episodeRuntime.equalsIgnoreCase("null") || episodeRuntime.length() == 0){
            episodeRuntime = context.getString(R.string.n_a);
        }
        if(episodeSummary.equalsIgnoreCase("null") || episodeSummary.length() == 0){
            episodeSummary = context.getString(R.string.no_summary_is_available);
        }
        if(seasonNumber.equalsIgnoreCase("null") || seasonNumber.length() == 0){
            seasonNumber = context.getString(R.string.n_a);
        }
        if(episodeNumber.equalsIgnoreCase("null") || episodeNumber.length() == 0){
            episodeNumber = context.getString(R.string.n_a);
        }

        //Creates a ShowEpisode object
        return new ShowEpisode(
                episodeName,
                episodeAirDate,
                episodeRuntime,
                episodeSummary,
                seasonNumber,
                episodeNumber);
    }
}