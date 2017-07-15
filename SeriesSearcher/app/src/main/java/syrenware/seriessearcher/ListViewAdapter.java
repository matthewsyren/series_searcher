package syrenware.seriessearcher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by matthew on 2017/01/28.
 * Class populates a ListView with the data that is passed into the constructor
 */

@SuppressWarnings("WeakerAccess")
public class ListViewAdapter extends ArrayAdapter
                             implements IAPIImage {
    //Declarations
    private ArrayList<Show> shows = null;
    private Context context;

    //Constructor
    public ListViewAdapter(Context context, ArrayList<Show> shows)
    {
        super(context, R.layout.list_row,shows);
        this.context = context;
        this.shows = shows;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent)
    {
        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_row, parent, false);

        //Component assignments
        final ImageView image = (ImageView) convertView.findViewById(R.id.show_poster);
        final TextView title = (TextView) convertView.findViewById(R.id.show_title);
        final TextView rating = (TextView) convertView.findViewById(R.id.show_rating);
        final TextView latestEpisode = (TextView) convertView.findViewById((R.id.show_latest_episode));
        final TextView nextEpisode = (TextView) convertView.findViewById((R.id.show_next_episode_date));
        final Button btnToggleShow = (Button) convertView.findViewById(R.id.button_toggle_show);

        //Populates ImageView from URL if image hasn't been stored in the Show object yet. If the image has been stored, the ImageView is populated with the stored image from the Show object
        if(shows.get(position).getShowImageUrl() != null){
            if(shows.get(position).getShowImage() == null){
                ImageLoad imageLoad = new ImageLoad(shows.get(position).getShowImageUrl(), image, position);

                //The delegate variable is used to pass the data from the IAPIImage class to the getJsonImage method in this class
                imageLoad.delegate = this;
                imageLoad.execute();
            }
            else{
                image.setImageBitmap(shows.get(position).getShowImage());
                notifyDataSetChanged();
            }
        }

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        title.setText(shows.get(position).getShowTitle());
        rating.setText(resources.getString(R.string.text_rating, shows.get(position).getShowRating()));
        latestEpisode.setText(resources.getString(R.string.text_status, shows.get(position).getShowStatus()));
        nextEpisode.setText(resources.getString(R.string.text_runtime, shows.get(position).getShowRuntime()));

        final User user = new User(context);
        displayButtonText(user.getUserKey(), "" + shows.get(position).getShowId(), btnToggleShow);

        //Sets onCLickListener for the buttons contained in each row of the ListView
        btnToggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnToggleShow.getText().toString().equals("+")){
                    btnToggleShow.setText("-");
                    pushUserShowSelection(user.getUserKey(), "" + shows.get(position).getShowId(), true);
                }
                else{
                    btnToggleShow.setText("+");
                    pushUserShowSelection(user.getUserKey(), "" + shows.get(position).getShowId(), false);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    //Method assigns the downloaded image to the Show object's showImage attribute, in order to prevent downloading the same image more than once
    @Override
    public void getJsonImage(Bitmap bitmap, int position) {
        shows.get(position).setShowImage(bitmap);
    }

    //Method fetches all show keys (show ID's) associated with the user's key, and adds them to an ArrayList. The ArrayList is then passed to the getUserShowData method, which fetches the JSON data for each show from the TVMAze API
    public void displayButtonText(String userKey, final String showID, final Button btnAddShow){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all shows and sets the text of the Button to - if the user has added the show to 'My Shows' already
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : lstSnapshots){
                    String showKey = snapshot.getKey();
                    if(showKey.equals(showID) && (boolean) snapshot.getValue()){
                        btnAddShow.setText("-");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i("FRB", "Error reading data");
            }
        });
    }

    //Method updates the shows that the user has added to 'My Shows' in the Firebase database
    public void pushUserShowSelection(String userKey, String showID, boolean showAdded){
        //Establishes a connection to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(userKey);

        //Generates the user's key and saves the value (the user's email address) to the Firebase database
        databaseReference.child(showID).setValue(showAdded);
    }
}