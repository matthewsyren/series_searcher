package syrenware.seriessearcher;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecificShowActivity extends AppCompatActivity
                                  implements IAPIConnectionResponse{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_specific_show);

            displayShowInformation();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method takes the user back to the Activity that they used to get to this Activity
    @Override
    public void onBackPressed() {
        try{
            super.onBackPressed();
            Bundle bundle = getIntent().getExtras();
            String previousActivity = bundle.getString("previousActivity");
            Intent intent = null;

            if(previousActivity != null){
                switch(previousActivity){
                    case "HomeActivity":
                        intent = new Intent(SpecificShowActivity.this, HomeActivity.class);
                        break;
                    case "RandomShowsActivity":
                        intent = new Intent(SpecificShowActivity.this, RandomShowsActivity.class);
                        break;
                    case "SearchActivity":
                        intent = new Intent(SpecificShowActivity.this, SearchActivity.class);
                        break;
                }
            }
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the information on the Activity
    public void displayShowInformation(){
        try{
            //Fetches the link for the show that the user clicked on from the Bundle
            Bundle bundle = getIntent().getExtras();
            String showNumber = bundle.getString("showNumber");
            String showLink = "http://api.tvmaze.com/shows/" + showNumber;

            //Displays ProgressBar
            toggleProgressBar(View.VISIBLE);

            //Fetches data from the TVMaze API using the link
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute(showLink);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBar(int visibility){
        try{
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(visibility);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method parses the JSON returned from the API and displays the data
    @Override
    public void getJsonResponse(String response) {
        try{
            //Assigns JSON data to variables if there is a valid JSON response
            if(response != null){
                JSONObject json = new JSONObject(response);

                //If the JSON has a 'premiered' key, then it is used for the show's main information, and if it has a 'season' key, it has information about either the show's next or previous episode
                if(json.has("premiered")){
                    displayShowInformation(json);
                }
                else if(json.has("season")){
                    TextView txtLatestEpisode = (TextView) findViewById(R.id.text_show_latest_episode);
                    String season = json.getString("season");
                    String episode = json.getString("number");
                    if(txtLatestEpisode.getText().toString().length() == 0) {
                        txtLatestEpisode.setText("Latest Episode: Season: " + season + " Episode: " + episode);
                    }
                    else{
                        String airDate = json.getString("airdate");
                        if(airDate == null){
                            airDate = "";
                        }
                        else{
                            airDate = "(" + airDate + ")";
                        }
                        TextView txtNextEpisode = (TextView) findViewById(R.id.text_show_next_episode);
                        txtNextEpisode.setText("Next Episode: Season: " + season + " Episode: " + episode + " " + airDate);

                        //Hides ProgressBar
                        toggleProgressBar(View.INVISIBLE);
                    }
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Error fetching data, please try again", Toast.LENGTH_LONG).show();
            }
        }
        catch(JSONException jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches the main information for the show from the TVMaze API, and then calls another link for more specific information about the show
    public void displayShowInformation(JSONObject json){
        try{
            String name = json.getString("name");
            String premiered = json.getString("premiered");
            String language = json.getString("language");
            String status = json.getString("status");
            String runtime = json.getString("runtime");
            JSONArray arrGenres;

            if(!json.getString("genres").equals("[]")){
                arrGenres = json.getJSONArray("genres");
            }
            else{
                arrGenres = null;
            }

            String rating = json.getJSONObject("rating").getString("average");
            String summary = json.getString("summary");
            String imageUrl;

            if(!json.getString("image").equals("null")){
                imageUrl = json.getJSONObject("image").getString("medium");
            }
            else{
                imageUrl = null;
            }

            //Replaces null values/empty Strings with "N/A"
            if(premiered.equalsIgnoreCase("null") || premiered.length() == 0){
                premiered = "N/A";
            }
            if(language.equalsIgnoreCase("null") || language.length() == 0){
                language = "N/A";
            }
            if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                rating = "N/A";
            }
            if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
                runtime = "N/A";
            }
            if(status.equalsIgnoreCase("null") || status.length() == 0){
                status = "N/A";
            }
            if(summary.equalsIgnoreCase("null") || summary.length() == 0){
                summary = "N/A";
            }

            //Assigns GUI components to variables
            TextView txtName = (TextView) findViewById(R.id.text_show_title);
            TextView txtPremiered = (TextView) findViewById(R.id.text_show_premiered);
            TextView txtLanguage = (TextView) findViewById(R.id.text_show_language);
            TextView txtStatus = (TextView) findViewById(R.id.text_show_status);
            TextView txtRuntime = (TextView) findViewById(R.id.text_show_runtime);
            TextView txtGenres = (TextView) findViewById(R.id.text_show_genres);
            TextView txtRating = (TextView) findViewById(R.id.text_show_rating);
            TextView txtSummary = (TextView) findViewById(R.id.text_show_summary);
            TextView txtPreviousEpisode = (TextView) findViewById(R.id.text_show_latest_episode);
            TextView txtNextEpisode = (TextView) findViewById(R.id.text_show_next_episode);

            //Displays the JSON data in the GUI components
            Resources resources = this.getResources();
            txtName.setText(name);
            txtPremiered.setText(resources.getString(R.string.text_premiered, premiered));
            txtLanguage.setText(resources.getString(R.string.text_language, language));
            txtStatus.setText(resources.getString(R.string.text_status, status));
            txtRuntime.setText(resources.getString(R.string.text_runtime, runtime));
            txtRating.setText(resources.getString(R.string.text_rating, rating));

            if(arrGenres != null){
                txtGenres.setText(resources.getString(R.string.text_genres, arrGenres.get(0)));
                for(int i = 1; i < arrGenres.length(); i++){
                    txtGenres.setText(txtGenres.getText() + ", " + arrGenres.get(i).toString());
                }
            }
            else{
                txtGenres.setText("N/A");
            }
            summary = formatSummary(summary);
            txtSummary.setText(resources.getString(R.string.text_summary, summary));

            //Fetches image from the API
            ImageView imgSpecificShow = (ImageView) findViewById(R.id.image_view_specific_show);
            ImageLoad image = new ImageLoad(imageUrl, imgSpecificShow, 0);
            image.execute();

            //Fetches data about the show's previous and next episodes (which are accessed using different links)
            JSONObject links = json.getJSONObject("_links");
            if(links.has("previousepisode")){
                String previousEpisodeLink = links.getJSONObject("previousepisode").getString("href");

                //Fetches data from the TVMaze API using the link
                APIConnection api = new APIConnection();
                api.delegate = this;
                api.execute(previousEpisodeLink);
            }
            else{
                txtPreviousEpisode.setText(resources.getString(R.string.text_latest_episode, "N/A"));
            }
            if(links.has("nextepisode")){
                String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");

                //Fetches data from the TVMaze API using the link
                APIConnection api = new APIConnection();
                api.delegate = this;
                api.execute(nextEpisodeLink);
            }
            else{
                txtNextEpisode.setText(resources.getString(R.string.text_next_episode, "N/A"));
                toggleProgressBar(View.INVISIBLE);
            }
        }
        catch(JSONException jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method removes any HTML formatting from the summary field
    public String formatSummary(String summary){
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
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
        return summary;
    }

    //Method takes the user to the SearchByEpisodeActivity
    public void searchByEpisodeOnClick(View view) {
        try {
            //Fetches the show name
            TextView txtShowName = (TextView) findViewById(R.id.text_show_title);
            String showName = txtShowName.getText().toString();

            //Fetches the link for the show that the user clicked on from the Bundle
            Bundle bundle = getIntent().getExtras();
            String showNumber = bundle.getString("showNumber");
            String previousActivity = bundle.getString("previousActivity");

            Intent intent = new Intent(SpecificShowActivity.this, SearchByEpisodeActivity.class);
            intent.putExtra("showTitle", showName);
            intent.putExtra("showNumber", showNumber);
            intent.putExtra("previousActivity", previousActivity);
            startActivity(intent);
        }
        catch (Exception exc) {
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}