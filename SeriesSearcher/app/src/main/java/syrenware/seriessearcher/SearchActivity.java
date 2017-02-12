package syrenware.seriessearcher;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity implements IAPIConnectionResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        super.onCreateDrawer();
        super.setSelectedNavItem(R.id.nav_search);
    }

    //Method retrieves the text that the user searches for in txtSearch, and then searches for that text using the API
    public void searchShows(View view){
        try{
            EditText txtSearch = (EditText) findViewById(R.id.txtSearch);
            String show = txtSearch.getText().toString();

            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute("http://api.tvmaze.com/search/shows?q=" + show);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method parses the JSON returned from the API, and populates the lstShows ListView with the data
    @Override
    public void getJsonResponse(String response) {
        try{
            JSONArray jsonArray = new JSONArray(response);
            ArrayList<Show> lstShows = new ArrayList<Show>();
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject json = jsonArray.getJSONObject(i);
                json = json.getJSONObject("show");
                String name = json.getString("name");
                String status = json.getString("status");
                String runtime = json.getString("runtime");
                String rating = json.getJSONObject("rating")
                        .getString("average");
                String imageUrl;
                if(json.getString("image") != "null"){
                    imageUrl = json.getJSONObject("image")
                                   .getString("medium");
                }
                else{
                    imageUrl = null;
                }

                if(rating.equalsIgnoreCase("null") || rating.length() == 0){
                    rating = "N/A";
                }
                if(runtime.equalsIgnoreCase("null") || runtime.length() == 0){
                    runtime = "N/A";
                }

                Show show = new Show(name, rating, status, runtime, imageUrl);
                lstShows.add(show);
            }

            ListViewAdapter adapter = new ListViewAdapter(this, lstShows);
            ListView listView = (ListView) findViewById(R.id.lstSearchResults);
            listView.setAdapter(adapter);
        }
        catch(Exception jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
