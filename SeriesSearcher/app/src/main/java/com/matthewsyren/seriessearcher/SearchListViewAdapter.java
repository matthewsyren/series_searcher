package com.matthewsyren.seriessearcher;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by matthew on 2017/01/28.
 * Class populates a ListView with the data that is passed into the constructor
 */

@SuppressWarnings("WeakerAccess")
public class SearchListViewAdapter extends ArrayAdapter{
    //Declarations
    private ArrayList<Show> shows;
    private Context context;
    private ViewHolder viewHolder;

    //Constructor
    public SearchListViewAdapter(Context context, ArrayList<Show> shows) {
        super(context, R.layout.home_list_row,shows);
        this.context = context;
        this.shows = shows;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            //Inflates the search_list_row view for the ListView
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.search_list_row, parent, false);
            viewHolder = new ViewHolder();

            //Component assignments
            viewHolder.poster = convertView.findViewById(R.id.image_show_poster);
            viewHolder.title = convertView.findViewById(R.id.text_show_title);
            viewHolder.rating = convertView.findViewById(R.id.text_show_rating);
            viewHolder.status = convertView.findViewById((R.id.text_show_status));
            viewHolder.runtime = convertView.findViewById((R.id.text_show_runtime));
            viewHolder.toggleShow = convertView.findViewById(R.id.button_toggle_show);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Fetches images from TVMaze API if the user has not activated Data Saving Mode
        if(!User.getDataSavingPreference(context)){
            //Populates ImageView from URL if image hasn't been stored in the Show object yet. If the image has been stored, the ImageView is populated with the stored image from the Show object
            if(shows.get(position).getShowImageUrl() != null){
                Picasso.with(context).load(shows.get(position).getShowImageUrl()).into(viewHolder.poster);
            }
            else{
                viewHolder.poster.setImageResource(R.mipmap.ic_launcher);
            }
        }

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        viewHolder.title.setText(shows.get(position).getShowTitle());
        viewHolder.rating.setText(resources.getString(R.string.text_rating, shows.get(position).getShowRating()));
        viewHolder.status.setText(resources.getString(R.string.text_status, shows.get(position).getShowStatus()));
        viewHolder.runtime.setText(resources.getString(R.string.text_runtime, shows.get(position).getShowRuntime()));
        final User user = new User(context);

        //Displays appropriate image for the ImageButton
        if(shows.get(position).isShowAdded() != null && shows.get(position).isShowAdded()){
            viewHolder.toggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
        }
        else if(shows.get(position).isShowAdded() != null){
            viewHolder.toggleShow.setImageResource(R.drawable.ic_add_black_24dp);
        }

        //Sets onCLickListener for the buttons contained in each row of the ListView
        viewHolder.toggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Adds the Show to My Series if the Show isn't already there, or prompts the user to confirm the removal of the Show from My Series if the Show is already there
                if(!shows.get(position).isShowAdded()){
                    //Adds Show to My Series
                    viewHolder.toggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
                    shows.get(position).setShowAdded(true);
                    pushUserShowSelection(user.getUserKey(), "" + shows.get(position).getShowId(), shows.get(position).getShowTitle(), true);
                    notifyDataSetChanged();
                }
                else{
                    //Prompts the user to confirm the removal of the Show from My Series
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Are you sure you want to remove " + shows.get(position).getShowTitle() + " from My Series?");

                    //Creates OnClickListener for the Dialog message
                    DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            switch(button){
                                //Removes the selected show from the My Series list
                                case AlertDialog.BUTTON_POSITIVE:
                                    //Updates the FirebaseDatabase and the UI
                                    pushUserShowSelection(user.getUserKey(), "" + shows.get(position).getShowId(), shows.get(position).getShowTitle(), false);
                                    shows.get(position).setShowAdded(false);
                                    viewHolder.toggleShow.setImageResource(R.drawable.ic_add_black_24dp);
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

    //ViewHolder class used to decrease the findViewById calls
    static class ViewHolder{
        //Component assignments
        ImageView poster;
        TextView title;
        TextView rating;
        TextView status;
        TextView runtime;
        ImageButton toggleShow;
    }
}