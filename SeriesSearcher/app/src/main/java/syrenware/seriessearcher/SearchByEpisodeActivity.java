package syrenware.seriessearcher;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class SearchByEpisodeActivity extends AppCompatActivity
                                     implements IAPIConnectionResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_search_by_episode);

            //Displays Back button in ActionBar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            //Hides ProgressBar
            toggleProgressBar(View.INVISIBLE);

            displayShowTitle();
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

    //Method displays the title of the show
    public void displayShowTitle(){
        try{
            //Fetches the show title from the Bundle
            Bundle bundle = getIntent().getExtras();
            String showTitle = bundle.getString("showTitle");

            TextView txtShowTitle = (TextView) findViewById(R.id.text_show_title);
            txtShowTitle.setText(showTitle);
            setTitle("Search for episode");
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Takes the user back to the DeliveryControlActivity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the DeliveryControlActivity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                //Fetches the show number for the show that the user clicked on from the Bundle
                Bundle bundle = getIntent().getExtras();
                String showNumber = bundle.getString("showNumber");
                String previousActivity = bundle.getString("previousActivity");

                Intent intent = new Intent(SearchByEpisodeActivity.this, SpecificShowActivity.class);
                intent.putExtra("showNumber", showNumber);
                intent.putExtra("previousActivity", previousActivity);
                startActivity(intent);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Searches for the episode entered by the user
    public void searchByEpisodeOnClick(View view){
        try{
            //Displays ProgressBar
            toggleProgressBar(View.VISIBLE);

            EditText txtSeason = (EditText) findViewById(R.id.text_show_season);
            EditText txtEpisode = (EditText) findViewById(R.id.text_show_episode);

            //Fetches the show title from the Bundle and assigns input values to the variables
            Bundle bundle = getIntent().getExtras();
            String showNumber = bundle.getString("showNumber");
            int season = Integer.parseInt(txtSeason.getText().toString());
            int episode = Integer.parseInt(txtEpisode.getText().toString());

            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute("http://api.tvmaze.com/shows/" + showNumber + "/episodebynumber?season=" + season + "&number="  + episode);
        }
        catch(NumberFormatException nfe){
            Toast.makeText(getApplicationContext(), "Please only enter whole numbers, and don't leave any fields empty", Toast.LENGTH_LONG).show();
            toggleProgressBar(View.INVISIBLE);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
            toggleProgressBar(View.INVISIBLE);
        }
    }

    //Method parses the JSON returned from the TVMaze API and displays it
    @Override
    public void getJsonResponse(String response) {
        try{
            //Assigns Views to variables
            TextView txtEpisodeName = (TextView) findViewById(R.id.text_show_episode_name);
            TextView txtEpisodeAirDate = (TextView) findViewById(R.id.text_show_air_date);
            TextView txtEpisodeRuntime = (TextView) findViewById(R.id.text_show_runtime);
            TextView txtEpisodeSummary = (TextView) findViewById(R.id.text_show_summary);

            if(response != null){
                //Assigns JSON data to variables
                JSONObject jsonObject = new JSONObject(response);
                String episodeName = jsonObject.getString("name");
                String episodeAirDate = jsonObject.getString("airdate");
                String episodeRuntime = jsonObject.getString("runtime");
                String episodeSummary = jsonObject.getString("summary");
                episodeSummary = Show.formatSummary(this, episodeSummary);

                //Replaces any empty data with "N/A"
                if(episodeName.equalsIgnoreCase("null") || episodeName.length() == 0) {
                    episodeName = "N/A";
                }
                if(episodeAirDate.equalsIgnoreCase("null") || episodeAirDate.length() == 0){
                    episodeAirDate = "N/A";
                }
                if(episodeRuntime.equalsIgnoreCase("null") || episodeRuntime.length() == 0){
                    episodeRuntime = "N/A";
                }
                if(episodeSummary.equalsIgnoreCase("null") || episodeSummary.length() == 0){
                    episodeSummary = "N/A";
                }

                //Displays values in TextViews
                Resources resources = this.getResources();
                txtEpisodeName.setText(resources.getString(R.string.text_episode_name, episodeName));
                txtEpisodeAirDate.setText(resources.getString(R.string.text_episode_air_date, episodeAirDate));
                txtEpisodeRuntime.setText(resources.getString(R.string.text_episode_runtime, episodeRuntime));
                txtEpisodeSummary.setText(resources.getString(R.string.text_episode_summary, episodeSummary));
            }
            else{
                Toast.makeText(getApplicationContext(), "No information about that episode was found...", Toast.LENGTH_LONG).show();
                txtEpisodeAirDate.setText("");
                txtEpisodeName.setText("");
                txtEpisodeRuntime.setText("");
                txtEpisodeSummary.setText("");
            }

            //Hides ProgressBar
            toggleProgressBar(View.INVISIBLE);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}