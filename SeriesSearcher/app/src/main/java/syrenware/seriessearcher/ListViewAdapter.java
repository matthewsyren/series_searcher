package syrenware.seriessearcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
public class ListViewAdapter extends ArrayAdapter {
    //Declarations
    private ArrayList<Show> shows;
    private Context context;
    private boolean saveImages;

    //Constructor
    public ListViewAdapter(Context context, ArrayList<Show> shows, boolean saveImages) {
        super(context, R.layout.list_row,shows);
        this.context = context;
        this.shows = shows;
        this.saveImages = saveImages;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_row, parent, false);

        //Component assignments
        final ImageView image = convertView.findViewById(R.id.show_poster);
        final TextView title = convertView.findViewById(R.id.show_title);
        final TextView rating = convertView.findViewById(R.id.show_rating);
        final TextView latestEpisode = convertView.findViewById((R.id.show_latest_episode));
        final TextView nextEpisode = convertView.findViewById((R.id.show_next_episode_date));
        final ImageButton btnToggleShow = convertView.findViewById(R.id.button_toggle_show);

        //Fetches images from TVMaze API if the user has not activated Data Saving Mode
        if(!User.getDataSavingPreference(context)){
            //Populates ImageView from URL if image hasn't been stored in the Show object yet. If the image has been stored, the ImageView is populated with the stored image from the Show object
            if(shows.get(position).getShowImageUrl() != null){
                ImageLoad imageLoad = new ImageLoad(shows.get(position).getShowImageUrl(), image, context, shows.get(position).getShowId(), saveImages);
                imageLoad.execute();
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
                if(btnToggleShow.getTag().equals("Add")){
                    btnToggleShow.setImageResource(R.drawable.ic_remove_black_24dp);
                    btnToggleShow.setTag("Remove");
                    pushUserShowSelection(user.getUserKey(), "" + shows.get(position).getShowId(), shows.get(position).getShowTitle(), true);
                }
                else{
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Are you sure you want to remove " + shows.get(position).getShowTitle() + " from My Series?");

                    //Creates OnClickListener for the Dialog message
                    DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            switch(button){
                                //Removes the selected show from the My Series list
                                case AlertDialog.BUTTON_POSITIVE:
                                    btnToggleShow.setImageResource(R.drawable.ic_add_black_24dp);
                                    btnToggleShow.setTag("Add");
                                    if(saveImages) {
                                        shows.remove(position);
                                    }
                                    pushUserShowSelection(user.getUserKey(), "" + shows.get(position).getShowId(), shows.get(position).getShowTitle(), false);
                                    break;
                                case AlertDialog.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    //Assigns button an OnClickListener for the AlertDialog and displays the AlertDialog
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", dialogOnClickListener);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", dialogOnClickListener);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        });
        return convertView;
    }

    //Method fetches all show keys (show ID's) associated with the user's key, and adds them to an ArrayList. The ArrayList is then passed to the getUserShowData method, which fetches the JSON data for each show from the TVMAze API
    public void displayButtonText(String userKey, final String showID, final ImageButton btnAddShow){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(userKey);

        //Adds Listeners for when the data is changed
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loops through all shows and sets the text of the Button to - if the user has added the show to 'My Series' already
                Iterable<DataSnapshot> lstSnapshots = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : lstSnapshots){
                    String showKey = snapshot.getKey();
                    if(showKey.equals(showID) && (boolean) snapshot.getValue()){
                        btnAddShow.setImageResource(R.drawable.ic_remove_black_24dp);
                        btnAddShow.setTag("Remove");
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

    //Method updates the shows that the user has added to 'My Series' in the Firebase database
    public void pushUserShowSelection(String userKey, String showID, String showTitle, boolean showAdded){
        //Establishes a connection to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(userKey);
        String message = showAdded ? showTitle + " added to My Series" : showTitle + " removed from My Series";

        //Generates the user's key and saves the value (the user's email address) to the Firebase database
        databaseReference.child(showID).setValue(showAdded);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}