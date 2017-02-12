package syrenware.seriessearcher;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RandomShowsActivity extends BaseActivity
        implements IAPIConnectionResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_shows);
        super.onCreateDrawer();
        super.setSelectedNavItem(R.id.nav_all_shows);

        //Fetches JSON from API
        int page = (int) (Math.random() * 100 + 1);
        APIConnection api = new APIConnection();
        api.delegate = this;
        api.execute("http://api.tvmaze.com/shows?page=" + page);
    }

    //Method fetches the JSON from the APIConnection class, and parses it
    @Override
    public void getJsonResponse(String response) {
        try{
            JSONArray jsonArray = new JSONArray(response);
            ArrayList<Show> lstShows = new ArrayList<Show>();

            int startingShow = (int) (Math.random() * 230 + 1);
            int showCount = 0;

            for(int i = 0; i < 20 && (startingShow + i) < jsonArray.length() - 1; i++){
                JSONObject json = jsonArray.getJSONObject(startingShow + i);
                if(json != null){
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
                    showCount++;
                }
                else{
                    break;
                }
            }

            ListViewAdapter adapter = new ListViewAdapter(this, lstShows);
            ListView listView = (ListView) findViewById(R.id.lstAllShows);
            listView.setAdapter(adapter);
        }
        catch(Exception jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
