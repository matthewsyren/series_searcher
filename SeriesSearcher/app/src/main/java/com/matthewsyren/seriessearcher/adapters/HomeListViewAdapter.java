package com.matthewsyren.seriessearcher.adapters;

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
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by matthew on 2017/01/28.
 * Class populates a ListView with the data that is passed into the constructor
 */

@SuppressWarnings("WeakerAccess")
public class HomeListViewAdapter
        extends ArrayAdapter{
    //Declarations
    private ArrayList<Show> shows;
    private Context context;
    private ViewHolder viewHolder;

    //Constructor
    public HomeListViewAdapter(Context context, ArrayList<Show> shows) {
        super(context, R.layout.home_list_row,shows);
        this.context = context;
        this.shows = shows;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            //Inflates the home_list_row View for the ListView
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.home_list_row, parent, false);
            viewHolder = new ViewHolder();

            //Component assignments
            viewHolder.poster = convertView.findViewById(R.id.image_show_poster);
            viewHolder.title = convertView.findViewById(R.id.text_show_title);
            viewHolder.rating = convertView.findViewById(R.id.text_show_rating);
            viewHolder.status = convertView.findViewById((R.id.text_show_status));
            viewHolder.nextEpisode = convertView.findViewById((R.id.text_show_next_episode_date));
            viewHolder.toggleShow = convertView.findViewById(R.id.button_toggle_show);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Displays the app's launcher icon if the show has no poster or if data saving mode has been activated
        if(UserAccountUtilities.getDataSavingPreference(context) || shows.get(position).getShowImageUrl() == null){
            viewHolder.poster.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            //Displays the image for the show if the show has a poster and data saving mode hasn't been activated
            Picasso.with(context)
                    .load(shows.get(position)
                            .getShowImageUrl())
                    .error(R.color.colorGray)
                    .placeholder(R.color.colorGray)
                    .into(viewHolder.poster);
        }

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        viewHolder.title.setText(shows.get(position).getShowTitle());
        viewHolder.rating.setText(resources.getString(R.string.text_rating, shows.get(position).getShowRating()));
        viewHolder.status.setText(resources.getString(R.string.text_status, shows.get(position).getShowStatus()));
        viewHolder.nextEpisode.setText(resources.getString(R.string.text_next_episode, shows.get(position).getShowNextEpisode()));

        //Displays the remove button
        viewHolder.toggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
        viewHolder.toggleShow.setTag("Remove");

        //Sets onCLickListener for the buttons contained in each row of the ListView
        viewHolder.toggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(context.getString(R.string.series_removal_confirmation, shows.get(position).getShowTitle()));

                //Creates OnClickListener for the Dialog message
                DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        switch(button){
                            //Removes the selected show from the My Series list
                            case AlertDialog.BUTTON_POSITIVE:
                                //Updates FirebaseDatabase and UI
                                pushUserShowSelection(UserAccountUtilities.getUserKey(context), "" + shows.get(position).getShowId(), shows.get(position).getShowTitle(), false);
                                shows.remove(position);
                                notifyDataSetChanged();
                                break;
                            case AlertDialog.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                //Assigns button an OnClickListener for the AlertDialog and displays the AlertDialog
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.yes), dialogOnClickListener);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no), dialogOnClickListener);
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
        String message = showAdded ? context.getString(R.string.added_to_my_series, showTitle) : context.getString(R.string.removed_from_my_series, showTitle);

        //Generates the user's key and saves the value (the user's email address) to the Firebase database
        databaseReference.child(showID).setValue(showAdded);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    //ViewHolder class used to decrease the findViewById calls
    static class ViewHolder{
        //Component assignments
        ImageView poster;
        TextView title;
        TextView rating;
        TextView status;
        TextView nextEpisode;
        ImageButton toggleShow;
    }
}