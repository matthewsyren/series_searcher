package syrenware.seriessearcher;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity implements IAPIConnectionResponse {
    //Declarations
    private APIConnection api = new APIConnection();
    private ArrayList<Show> lstShows =  new ArrayList<>();
    SearchListViewAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Hides ProgressBar
        toggleProgressBar(View.INVISIBLE);

        //Sets a custom adapter for the list_view_search_results ListView to display the search results
        adapter = new SearchListViewAdapter(this, lstShows, false);
        listView = findViewById(R.id.list_view_search_results);
        listView.setAdapter(adapter);

        final EditText txtSearch = findViewById(R.id.text_search_series);
        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchShows();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        super.onCreateDrawer();
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

    //Method retrieves the text that the user searches for in text_search, and then searches for that text using the API
    public void searchShows(){
        try{
            //Fetches user's input
            EditText txtSearch = findViewById(R.id.text_search_series);
            String show = txtSearch.getText().toString();

            //Displays ProgressBar
            toggleProgressBar(View.VISIBLE);

            //Connects to the TVMaze API using the specific URL for the selected show
            api.cancel(true);
            api = new APIConnection();
            api.delegate = this;
            api.execute("http://api.tvmaze.com/search/shows?q=" + show);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method parses the JSON returned from the API, and populates the list_view_search_results ListView with the data
    @Override
    public void parseJsonResponse(String response) {
        try{
            //JSONArray stores the JSON returned from the TVMaze API
            if(response != null){
                JSONArray jsonArray = new JSONArray(response);

                //Loops through all Shows returned from the TVMaze API search result
                for(int i = 0; i < jsonArray.length(); i++){
                    //Instantiates JSONObject to store the results returned from the API
                    JSONObject json = jsonArray.getJSONObject(i);
                    json = json.getJSONObject("show");

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
                        Show show = new Show(id, name, rating, status, imageUrl);
                        show.setShowRuntime(runtime);
                        lstShows.add(show);
                        adapter.notifyDataSetChanged();
                    }
                }

                //Sets an OnItemClickListener on the ListView, which will take the user to the SpecificShowActivity, where the user will be shown more information on the show that they clicked on
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                        Intent intent = new Intent(SearchActivity.this, SpecificShowActivity.class);
                        intent.putExtra("showNumber", "" + lstShows.get(pos).getShowId());
                        intent.putExtra("previousActivity", "SearchActivity");
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