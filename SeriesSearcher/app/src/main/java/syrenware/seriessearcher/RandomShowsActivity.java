package syrenware.seriessearcher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RandomShowsActivity extends BaseActivity implements IAPIConnectionResponse {
    //Declarations
    private ArrayList<Show> lstShows;
    private SearchListViewAdapter adapter;
    private ListView listView;
    private int page;
    private int startingShow;

    //Setter method
    public void setLstShows(ArrayList<Show> lstShows){
        this.lstShows = lstShows;

        //Updates the Adapter
        adapter.notifyDataSetChanged();

        //Hides ProgressBar
        toggleProgressBar(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_shows);
        super.onCreateDrawer();
        super.setSelectedNavItem(R.id.nav_random_shows);

        //Sets up Adapter to ListView
        lstShows = new ArrayList<>();
        adapter = new SearchListViewAdapter(this, lstShows);
        listView = findViewById(R.id.list_view_random_shows);
        listView.setAdapter(adapter);

        //Displays the ProgressBar
        toggleProgressBar(View.VISIBLE);

        //Fetches JSON from API (If a page on the API has already been determined, then it is fetched from the Bundle, otherwise the Math.random() method chooses a random page from the API to fetch)
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getInt("apiPage") != -1){
            page = bundle.getInt("apiPage");
        }
        else{
            page = (int) (Math.random() * 100);
        }
        APIConnection api = new APIConnection();
        api.delegate = this;
        api.execute("http://api.tvmaze.com/shows?page=" + page);
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBar(int visibility){
        try{
            ProgressBar progressBar = findViewById(R.id.progress_bar);
            progressBar.setVisibility(visibility);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches the JSON from the APIConnection class, and parses it
    @Override
    public void parseJsonResponse(String response) {
        try{
            if(response != null){
                //JSONArray stores the JSON returned from the TVMaze API
                JSONArray jsonArray = new JSONArray(response);

                //Fetches previous list of Shows from the Bundle if a starting show has already been determined, otherwise Math.random() is used to choose a random starting point to fetch data from the API. This allows the app to fetch different shows each time it runs
                Bundle bundle = getIntent().getExtras();
                if(bundle != null && bundle.getInt("apiStartingShow") != -1){
                    startingShow = bundle.getInt("apiStartingShow");
                }
                else{
                    startingShow = (int) (Math.random() * 230 + 1);
                }
                int showCount = 0;

                //Loops through the 20 randomly chosen shows returned from the TVMaze API
                for(int i = 0; i < 20 && (startingShow + i) < jsonArray.length() - 1; i++){
                    //Creates new JSONObject to parse the data returned
                    JSONObject json = jsonArray.getJSONObject(startingShow + i);

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        int id = json.getInt("id");
                        String name = json.getString("name");
                        String status = json.getString("status");
                        String runtime = json.getString("runtime");
                        String rating = json.getJSONObject("rating").getString("average");
                        String imageUrl;

                        //Gets the image URL for the current show if there is a URL provided, otherwise sets the URL to null
                        if(!json.getString("image").equals("null")){
                            imageUrl = json.getJSONObject("image").getString("medium");
                        }
                        else{
                            imageUrl = null;
                        }

                        //Ensures that the data returned from the API is valid
                        if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                            rating = "N/A";
                        }
                        if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
                            runtime = "N/A";
                        }

                        //Instantiates a Show object and adds it to the list_view_random_shows ListView
                        Show show = new Show(id, name, rating, status, imageUrl);
                        show.setShowRuntime(runtime);
                        show.setShowAdded(null);
                        lstShows.add(show);
                        showCount++;
                    }
                    else{
                        //Exits the loop if the JSON returned is null
                        break;
                    }
                }
                //Determines which Shows have been added to My Series by the user
                Show.checkIfShowIsAdded(new User(this).getUserKey(), lstShows, null, this);

                //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                        Intent intent = new Intent(RandomShowsActivity.this, SpecificShowActivity.class);
                        intent.putExtra("showNumber", "" + lstShows.get(pos).getShowId());
                        intent.putExtra("previousActivity", "RandomShowsActivity");
                        intent.putExtra("apiPage", page);
                        intent.putExtra("apiStartingShow", startingShow);
                        startActivity(intent);
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(), "Error fetching data, please check your internet connection", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method refreshes the Activity in order to fetch 20 new randomised series
    public void refreshOnClick(View view){
        try{
            finish();
            startActivity(new Intent(this, RandomShowsActivity.class));
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}