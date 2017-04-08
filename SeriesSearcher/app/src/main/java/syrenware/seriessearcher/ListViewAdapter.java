package syrenware.seriessearcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by matthew on 2017/01/28.
 * Class populates a ListView with the data that is passed into the constructor
 */

public class ListViewAdapter extends ArrayAdapter
                             implements IAPIImage {
    //Declarations
    ArrayList<Show> shows = null;
    Context context;
    ImageView image;
    TextView title;
    TextView rating;
    TextView latestEpisode;
    TextView nextEpisode;
    Button btnToggleShow;

    //Constructor
    public ListViewAdapter(Context context, ArrayList<Show> shows)
    {
        super(context, R.layout.list_row,shows);
        this.context = context;
        this.shows = shows;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_row, parent, false);

        //Component assignments
        image = (ImageView) convertView.findViewById(R.id.show_poster);
        title = (TextView) convertView.findViewById(R.id.show_title);
        rating = (TextView) convertView.findViewById(R.id.show_rating);
        latestEpisode = (TextView) convertView.findViewById((R.id.show_latest_episode));
        nextEpisode = (TextView) convertView.findViewById((R.id.show_next_episode_date));
        btnToggleShow = (Button) convertView.findViewById(R.id.button_toggle_show);

        //Populates ImageView from URL if image hasn't been stored in the Show object yet. If the image has been stored, the ImageView is populated with the stored image from the Show object
        if(shows.get(position).getShowImageUrl() != null){
            if(shows.get(position).getShowImage() == null){
                ImageLoad loadClass = new ImageLoad(shows.get(position).getShowImageUrl(), image, position);

                //The delegate variable is used to pass the data from the IAPIImage class to the getJsonImage method in this class
                loadClass.delegate = this;
                loadClass.execute();
            }
            else{
                image.setImageBitmap(shows.get(position).getShowImage());
            }
        }

        //Displays the data in the appropriate Views
        title.setText(shows.get(position).getShowTitle());
        rating.setText("Rating: " + shows.get(position).getShowRating());
        latestEpisode.setText("Status: " + shows.get(position).getShowStatus());
        nextEpisode.setText("Runtime: " + shows.get(position).getShowRuntime());

        //Sets onCLickListener for the buttons contained in each row of the ListView
        btnToggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, shows.get(position).getShowTitle(), Toast.LENGTH_LONG).show();
            }
        });
        return convertView;
    }

    //Method assigns the downloaded image to the Show object's showImage attribute, in order to prevent downloading the same image more than once
    @Override
    public void getJsonImage(Bitmap bitmap, int position) {
        shows.get(position).setShowImage(bitmap);
    }
}