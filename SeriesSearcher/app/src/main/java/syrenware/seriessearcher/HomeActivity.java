package syrenware.seriessearcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity
                          implements IAPIConnectionResponse {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
        super.onCreateDrawer();
        super.setSelectedNavItem(R.id.nav_home);

        toggleProgressBar(View.VISIBLE);

        //Toggles the views visible based on whether the user has added shows to 'My Shows'
        toggleViewVisibility(View.VISIBLE,View.INVISIBLE);

        //Gets the unique key used by Firebase to store information about the user signed in, and fetches data based on the keys fetched
        User user = new User(this);
        getUserShowKeys(user.getUserKey());
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

    //Method takes user to All Shows activity
    public void openAllShows(View view) {
        Intent intent = new Intent(this, RandomShowsActivity.class);
        startActivity(intent);
    }

    //Method fetches all show keys (show ID's) associated with the user's key, and adds them to an ArrayList. The ArrayList is then passed to the getUserShowData method, which fetches the JSON data for each show from the TVMAze API
    public void getUserShowKeys(String userKey){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

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
                getUserShowData(lstShows);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("Data", "Failed to read data, please check your internet connection");
            }
        });
    }

    //Method fetches the shows the user has added to 'My Shows' using the keys passed in with the ArrayList
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
        }
    }

    //Method sets the visibility of the views based on the parameters passed in
    public void toggleViewVisibility(int listViewVisibility, int otherViewVisibility){
        try{
            TextView txtNoShows = (TextView) findViewById(R.id.text_no_shows);
            Button btnAddShows = (Button) findViewById(R.id.button_add_shows);
            ListView lstMyShows = (ListView) findViewById(R.id.list_view_my_shows);

            txtNoShows.setVisibility(otherViewVisibility);
            btnAddShows.setVisibility(otherViewVisibility);
            lstMyShows.setVisibility(listViewVisibility);
        }
        catch(Exception exc){
            displayToast(exc.getMessage());
        }
    }

    //Method displays a Toast message with the String parameter as its value
    public void displayToast(String message){
        try{
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
        catch(Exception exc){
            displayToast(exc.getMessage());
        }
    }

    //Method parses the JSON returned from the API and displays the information in the list_view_my_shows ListView
    @Override
    public void getJsonResponse(String response) {
        try{
            //JSONArray stores the JSON returned from the TVMaze API
            if(response != null){
                response = "[\n" + response + "\n]";
                JSONArray jsonArray = new JSONArray(response);
                final ArrayList<Show> lstShows = new ArrayList<>();

                //Loops through all Shows returned from the TVMaze API search result
                for(int i = 0; i < jsonArray.length(); i++){
                    //Instantiates JSONObject to store the results returned from the API
                    JSONObject json = jsonArray.getJSONObject(i);

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

                        //Ensures that the data returned in the JSON is valid
                        if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                            rating = "N/A";
                        }
                        if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
                            runtime = "N/A";
                        }

                        //Instantiates a Show object and adds it to the lstShows ArrayList
                        Show show = new Show(id, name, rating, status, runtime, imageUrl);
                        lstShows.add(show);
                    }
                }

                //Sets a custom adapter for the list_view_search_results ListView to display the search results
                final ListViewAdapter adapter = new ListViewAdapter(this, lstShows);
                ListView listView = (ListView) findViewById(R.id.list_view_my_shows);
                listView.setAdapter(adapter);

                //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                        Intent intent = new Intent(HomeActivity.this, SpecificShowActivity.class);
                        intent.putExtra("specificShowLink", "http://api.tvmaze.com/shows/" + lstShows.get(pos).getShowId());
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