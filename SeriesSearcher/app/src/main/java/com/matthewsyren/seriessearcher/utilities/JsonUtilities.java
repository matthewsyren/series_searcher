package com.matthewsyren.seriessearcher.utilities;

import android.content.Context;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.models.ShowEpisode;
import com.matthewsyren.seriessearcher.network.APIConnection;
import com.matthewsyren.seriessearcher.network.IAPIConnectionResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtilities {
    /**
     * Returns a Show object parsed from the JSONObject passed in
     * @param json The JSON to be parsed
     * @param context The Context of the calling Activity
     * @param delegate The class that implements IAPIConnectionResponse
     * @param fetchNextEpisode Set as true to fetch information about the Show's next episode
     * @return A Show object with the parsed JSON as values
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

    /**
     * Returns a Show object parsed from the JSONObject passed in
     * @param json The JSON to be parsed
     * @param context The Context of the calling Activity
     * @param delegate The class that implements IAPIConnectionResponse
     * @return A Show object with the parsed JSON as values
     */
    public static Show parseFullShowJson(JSONObject json, Context context, IAPIConnectionResponse delegate) throws JSONException{
        //Parses the small version of the Show
        Show show = parseShowJson(json, context, delegate, false);

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
            summary = context.getString(R.string.n_a);
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

        //Gets the summary
        summary = Show.formatSummary(summary);

        //Initialises variables
        String nextEpisode = null;
        String previousEpisode = null;

        //Fetches data about the show's previous and next episodes (which are accessed using different links)
        JSONObject links = json.getJSONObject("_links");
        if(links.has("previousepisode")){
            String previousEpisodeLink = links.getJSONObject("previousepisode").getString("href");

            //Fetches data from the TVMaze API using the link
            APIConnection api = new APIConnection();
            api.delegate = delegate;
            api.execute(previousEpisodeLink);
        }
        else{
            previousEpisode = context.getString(R.string.n_a);
        }

        if(links.has("nextepisode")){
            String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");

            //Fetches data from the TVMaze API using the link
            APIConnection api = new APIConnection();
            api.delegate = delegate;
            api.execute(nextEpisodeLink);
        }
        else{
            nextEpisode = context.getString(R.string.n_a);
        }

        //Creates a Show object with the appropriate information
        show = new Show(
                show.getShowImageUrl(),
                show.getShowTitle(),
                show.getShowRating(),
                show.getShowStatus(),
                nextEpisode,
                show.getShowRuntime(),
                null,
                premiered,
                language,
                genres,
                summary,
                previousEpisode);

        return show;
    }

    /**
     * Parses information about an episode of the Show
     * @param json The JSON to be parsed
     * @return The episode date
     */
    public static String parseShowEpisodeDate(JSONObject json) throws JSONException{
        String season = json.getString("season");
        String episode = json.getString("number");

        String airDate = json.getString("airdate");

        if(airDate == null){
            airDate = "";
        }
        else{
            airDate = "(" + airDate + ")";
        }
        return ("Season " + season + ", Episode " + episode + " " + airDate);
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
        episodeSummary = Show.formatSummary(episodeSummary);

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
            episodeSummary = context.getString(R.string.n_a);
        }

        //Creates a ShowEpisode object
        return new ShowEpisode(episodeName, episodeAirDate, episodeRuntime, episodeSummary);
    }
}