package syrenware.seriessearcher;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

            Bundle bundle = getIntent().getExtras();
            String showLink = bundle.getString("specificShowLink");
            Toast.makeText(getApplicationContext(), "Response is " + showLink, Toast.LENGTH_LONG).show();
            APIConnection api = new APIConnection();
            api.delegate = this;
            api.execute(showLink);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method parses the JSON returned from the API and displays the data
    @Override
    public void getJsonResponse(String response) {
        try{
            //Assigns JSON data to variables
            JSONObject json = new JSONObject(response);
            String name = json.getString("name");
            String premiered = json.getString("premiered");
            String language = json.getString("language");
            String status = json.getString("status");
            String runtime = json.getString("runtime");
            JSONArray arrGenres = json.getJSONArray("genres");
            String rating = json.getJSONObject("rating")
                                .getString("average");
            String summary = json.getString("summary");
            String imageUrl;
            if(json.getString("image") != "null"){
                imageUrl = json.getJSONObject("image")
                        .getString("medium");
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
            TextView txtName = (TextView) findViewById(R.id.txtShowTitle);
            TextView txtPremiered = (TextView) findViewById(R.id.txtShowPremiered);
            TextView txtLanguage = (TextView) findViewById(R.id.txtShowLanguage);
            TextView txtStatus = (TextView) findViewById(R.id.txtShowStatus);
            TextView txtRuntime = (TextView) findViewById(R.id.txtShowRuntime);
            TextView txtGenres = (TextView) findViewById(R.id.txtShowGenres);
            TextView txtRating = (TextView) findViewById(R.id.txtShowRating);
            TextView txtSummary = (TextView) findViewById(R.id.txtShowSummary);
            ImageView imgSpecificShow = (ImageView) findViewById(R.id.imgSpecificShow);

            //Displays the JSON data in the GUI components
            txtName.setText(name);
            txtPremiered.setText("Premiered: " + premiered);
            txtLanguage.setText("Language: " + language);
            txtStatus.setText("Status: " + status);
            txtRuntime.setText("Runtime: " + runtime);
            txtRating.setText("Rating: " + rating);
            txtGenres.setText("Genres: " + arrGenres.get(0));
            for(int i = 1; i < arrGenres.length(); i++){
                txtGenres.setText(txtGenres.getText() + ", " + arrGenres.get(i).toString());
            }
            txtSummary.setText("Summary: " + summary);

            ImageLoadClass image = new ImageLoadClass(imageUrl, imgSpecificShow, 0);
            image.delegate = this;
            image.execute();
        }
        catch(JSONException jse){
            Toast.makeText(getApplicationContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method retrieves the image from the API and assigns it to the ImageView on this Activity
    @Override
    public void getJsonImage(Bitmap bitmap, int position) {

    }
}
