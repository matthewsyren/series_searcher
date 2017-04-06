package syrenware.seriessearcher;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecificShowActivity extends AppCompatActivity
                                  implements IAPIConnectionResponse, IAPIImage{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_specific_show);

            //Fetches the link for the show that the user clicked on from the Bundle
            Bundle bundle = getIntent().getExtras();
            String showLink = bundle.getString("specificShowLink");

            //Fetches data from the TVMaze API using the link
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute(showLink);
        }
        catch(Exception exc){
            displayToastMessage(exc.getMessage());
        }
    }

    //Method parses the JSON returned from the API and displays the data
    @Override
    public void getJsonResponse(String response) {
        try{
            //Assigns JSON data to variables if there is a valid JSON response
            if(response != null){
                JSONObject json = new JSONObject(response);
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

                //Ensures that no null values are displayed
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

                //Displays the JSON data in the GUI components
                txtName.setText(name);
                txtPremiered.setText("Premiered: " + premiered);
                txtLanguage.setText("Language: " + language);
                txtStatus.setText("Status: " + status);
                txtRuntime.setText("Runtime: " + runtime);
                txtRating.setText("Rating: " + rating);
                if(arrGenres != null){
                    txtGenres.setText("Genres: " + arrGenres.get(0));
                    for(int i = 1; i < arrGenres.length(); i++){
                        txtGenres.setText(txtGenres.getText() + ", " + arrGenres.get(i).toString());
                    }
                }
                else{
                    txtGenres.setText("N/A");
                }
                txtSummary.setText("Summary: " + summary);

                //Fetches image from the API
                ImageLoad image = new ImageLoad(imageUrl, 0);
                image.delegate = this;
                image.execute();
            }
            else{
                displayToastMessage("Error fetching data, please try again");
            }
        }
        catch(JSONException jse){
            displayToastMessage(jse.getMessage());
        }
        catch(Exception exc){
            displayToastMessage(exc.getMessage());
        }
    }

    //Method retrieves the image from the API and assigns it to the ImageView on this Activity
    @Override
    public void getJsonImage(Bitmap bitmap, int position) {
        ImageView imgSpecificShow = (ImageView) findViewById(R.id.image_view_specific_show);
        imgSpecificShow.setImageBitmap(bitmap);
    }

    //Method displays the message that is passed in using a Toast alert
    public void displayToastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}