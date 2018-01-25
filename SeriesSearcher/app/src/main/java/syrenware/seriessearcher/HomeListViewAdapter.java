package syrenware.seriessearcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by matthew on 2017/01/28.
 * Class populates a ListView with the data that is passed into the constructor
 */

@SuppressWarnings("WeakerAccess")
public class HomeListViewAdapter extends ArrayAdapter{
    //Declarations
    private ArrayList<Show> shows;
    private Context context;

    //Constructor
    public HomeListViewAdapter(Context context, ArrayList<Show> shows) {
        super(context, R.layout.home_list_row,shows);
        this.context = context;
        this.shows = shows;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        //Inflates the home_list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.home_list_row, parent, false);

        //Component assignments
        final ImageView image = convertView.findViewById(R.id.show_poster);
        final TextView title = convertView.findViewById(R.id.show_title);
        final TextView rating = convertView.findViewById(R.id.show_rating);
        final TextView status = convertView.findViewById((R.id.show_status));
        final TextView nextEpisode = convertView.findViewById((R.id.show_next_episode_date));
        final ImageButton btnToggleShow = convertView.findViewById(R.id.button_toggle_show);

        //Fetches images from TVMaze API if the user has not activated Data Saving Mode
        if(!User.getDataSavingPreference(context)){
            //Populates ImageView from URL if image hasn't been stored in the Show object yet. If the image has been stored, the ImageView is populated with the stored image from the Show object
            if(shows.get(position).getShowImageUrl() != null){
                ImageLoad imageLoad = new ImageLoad(shows.get(position).getShowImageUrl(), image, context, shows.get(position).getShowId(), true);
                imageLoad.execute();
            }
        }

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        title.setText(shows.get(position).getShowTitle());
        rating.setText(resources.getString(R.string.text_rating, shows.get(position).getShowRating()));
        status.setText(resources.getString(R.string.text_status, shows.get(position).getShowStatus()));
        nextEpisode.setText(resources.getString(R.string.text_next_episode, shows.get(position).getShowNextEpisode()));
        final User user = new User(context);

        //Displays the remove button
        btnToggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
        btnToggleShow.setTag("Remove");

        //Sets onCLickListener for the buttons contained in each row of the ListView
        btnToggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Are you sure you want to remove " + shows.get(position).getShowTitle() + " from My Series?");

                //Creates OnClickListener for the Dialog message
                DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        switch(button){
                            //Removes the selected show from the My Series list
                            case AlertDialog.BUTTON_POSITIVE:
                                pushUserShowSelection(user.getUserKey(), "" + shows.get(position).getShowId(), shows.get(position).getShowTitle(), false);
                                shows.remove(position);
                                notifyDataSetChanged();
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
        });
        return convertView;
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