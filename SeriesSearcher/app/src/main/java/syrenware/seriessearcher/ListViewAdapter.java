package syrenware.seriessearcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by matthew on 2017/01/28.
 * Class populates a ListView with the data that is passed into the constructor
 */

public class ListViewAdapter extends ArrayAdapter
                             implements IAPIImage {
    ArrayList<Show> shows = null;
    Context context;
    ImageView image;
    TextView title;
    TextView rating;
    TextView latestEpisode;
    TextView nextEpisode;


    public ListViewAdapter(Context context, ArrayList<Show> resource)
    {
        super(context, R.layout.list_row,resource);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.shows = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        // TODO Auto-generated method stub
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_row, parent, false);

        //Component assignments
        image = (ImageView) convertView.findViewById(R.id.show_poster);
        title = (TextView) convertView.findViewById(R.id.show_title);
        rating = (TextView) convertView.findViewById(R.id.show_rating);
        latestEpisode = (TextView) convertView.findViewById((R.id.show_latest_episode));
        nextEpisode = (TextView) convertView.findViewById((R.id.show_next_episode_date));


        if(shows.get(position).getShowImageUrl() != null){
            if(shows.get(position).getShowImage() == null){
                ImageLoadClass loadClass = new ImageLoadClass(shows.get(position).getShowImageUrl(), image, position);
                loadClass.delegate = this;
                loadClass.execute();
            }
            else{
                image.setImageBitmap(shows.get(position).getShowImage());
            }
        }

        title.setText(shows.get(position).getShowTitle());
        rating.setText("Rating: " + shows.get(position).getShowRating());
        latestEpisode.setText("Status: " + shows.get(position).getShowStatus());
        nextEpisode.setText("Runtime: " + shows.get(position).getShowRuntime());
        return convertView;
    }

    //Method assigns the downloaded to the Show object, in order to prevent downloading the same image more than once
    @Override
    public void getJsonImage(Bitmap bitmap, int position) {
        shows.get(position).setShowImage(bitmap);
    }
}
