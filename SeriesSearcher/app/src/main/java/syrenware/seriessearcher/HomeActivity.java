package syrenware.seriessearcher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends BaseActivity implements IAPIConnectionResponse {
    //Declarations
    private ArrayList<Show> lstShows = new ArrayList<>();
    private HomeListViewAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
        super.onCreateDrawer();

        //Displays ProgressBar
        toggleProgressBar(View.VISIBLE);

        //Sets a custom adapter for the list_view_search_results ListView to display the search results
        listView = findViewById(R.id.list_view_my_shows);
        adapter = new HomeListViewAdapter(this, lstShows);
        listView.setAdapter(adapter);

        //Toggles the views visible based on whether the user has added shows to 'My Series'
        toggleViewVisibility(View.VISIBLE,View.INVISIBLE);

        //Gets the unique key used by Firebase to store information about the user signed in, and fetches data based on the keys fetched
        User user = new User(this);
        getUserShowKeys(user.getUserKey());
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

    //Method takes user to SearchActivity
    public void openSearchShows(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    //Method fetches all show keys (show ID's) associated with the user's key, and adds them to an ArrayList. The ArrayList is then passed to the getUserShowData method, which fetches the JSON data for each show from the TVMAze API
    public void getUserShowKeys(String userKey){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all shows and adds each show key to the lstShows ArrayList
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                ArrayList<String> lstShows = new ArrayList<>();
                for(DataSnapshot snapshot : lstSnapshots){
                    String showKey = snapshot.getKey();
                    if((boolean) snapshot.getValue()){
                        lstShows.add(showKey);
                    }
                }
                //Removes EventListener from the Firebase Database and fetches the data for the Shows
                databaseReference.removeEventListener(this);
                getUserShowData(lstShows);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    //Method fetches the shows the user has added to 'My Series' using the keys passed in with the ArrayList
    public void getUserShowData(ArrayList<String> lstShows){
        if(lstShows.size() > 0){
            //Transfers the data from lstShows to an array containing the necessary links to the API (an array can be passed in to the APIConnection class to fetch data from the API)
            String[] arrShows = new String[lstShows.size()];
            for(int i = 0; i < lstShows.size(); i++){
                arrShows[i] = "http://api.tvmaze.com/shows/" + lstShows.get(i);
            }

            //Fetches the data from the TVMaze API
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute(arrShows);
        }
        else{
            toggleViewVisibility(View.INVISIBLE,View.VISIBLE);
            toggleProgressBar(View.INVISIBLE);
        }
    }

    //Method sets the visibility of the views based on the parameters passed in
    public void toggleViewVisibility(int listViewVisibility, int otherViewVisibility){
        try{
            TextView txtNoShows = findViewById(R.id.text_no_shows);
            Button btnAddShows = findViewById(R.id.button_add_shows);
            ListView lstMyShows = findViewById(R.id.list_view_my_shows);

            txtNoShows.setVisibility(otherViewVisibility);
            btnAddShows.setVisibility(otherViewVisibility);
            lstMyShows.setVisibility(listViewVisibility);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method parses the JSON returned from the API and displays the information in the list_view_my_shows ListView
    @Override
    public void parseJsonResponse(String response) {
        try{
            //JSONArray stores the JSON returned from the TVMaze API
            if(response != null){
                //Creates JSONArray from the response
                response = "[\n" + response + "\n]";
                JSONArray jsonArray = new JSONArray(response);

                //Loops through all Shows returned from the TVMaze API search result
                for(int i = 0; i < jsonArray.length(); i++){
                    //Instantiates JSONObject to store the results returned from the API
                    JSONObject json = jsonArray.getJSONObject(i);

                    //Assigns values to the JSONObject if the JSON returned from the API is not null
                    if(json != null){
                        String url = json.getString("url");
                        if(url.contains("shows")){
                            int id = json.getInt("id");
                            String name = json.getString("name");
                            String status = json.getString("status");
                            String rating = json.getJSONObject("rating").getString("average");
                            String imageUrl;

                            //Gets the image URL for the current show if there is a URL provided, otherwise sets the URL to null
                            if(!json.getString("image").equals("null")){
                                imageUrl = json.getJSONObject("image").getString("medium");
                            }
                            else{
                                imageUrl = null;
                            }

                            //Ensures that the data returned in the JSON is valid
                            if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                                rating = "N/A";
                            }

                            JSONObject links = json.getJSONObject("_links");
                            if(links.has("nextepisode")){
                                String nextEpisodeLink = links.getJSONObject("nextepisode").getString("href");
                                //Fetches data from the TVMaze API using the link
                                APIConnection api = new APIConnection();
                                api.delegate = this;
                                api.execute(nextEpisodeLink);
                            }

                            //Instantiates a Show object and adds it to the lstShows ArrayList
                            Show show = new Show(id, name, rating, status, imageUrl);
                            show.setShowNextEpisode("N/A");
                            lstShows.add(show);
                            adapter.notifyDataSetChanged();
                        }
                        else if(url.contains("episodes")){
                            String season = json.getString("season");
                            String episode = json.getString("number");
                            String airDate = json.getString("airdate");

                            //Sets the next episode information
                            if(airDate == null){
                                airDate = "N/A";
                            }
                            else{
                                airDate = airDate + " (S: " + season + " E: " + episode + ")";
                            }

                            //Sets the next episode date for the appropriate series
                            for(int s = 0; s < lstShows.size(); s++){
                                String seriesName = lstShows.get(s).getShowTitle().toLowerCase();
                                seriesName = seriesName.replaceAll("[^a-z A-Z]", "");
                                seriesName = seriesName.replaceAll(" ", "-");
                                if(url.toLowerCase().contains(seriesName)){
                                    lstShows.get(s).setShowNextEpisode(airDate);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }

                //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                        Intent intent = new Intent(HomeActivity.this, SpecificShowActivity.class);
                        intent.putExtra("showNumber", "" + lstShows.get(pos).getShowId());
                        intent.putExtra("previousActivity", "HomeActivity");
                        startActivity(intent);
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(), "Error fetching data, please check your internet connection", Toast.LENGTH_LONG).show();
            }

            //Hides ProgressBar
            toggleProgressBar(View.INVISIBLE);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}