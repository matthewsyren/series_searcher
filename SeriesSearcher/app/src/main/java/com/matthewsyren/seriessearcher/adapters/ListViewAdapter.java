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
public class ListViewAdapter
        extends ArrayAdapter{
    //Declarations
    private ArrayList<Show> mShows;
    private Context mContext;
    private boolean mIsHomeListView;
    private ViewHolder viewHolder;

    /**
     * Constructor
     */
    public ListViewAdapter(Context context, ArrayList<Show> shows, boolean isHomeListView) {
        super(context, R.layout.home_list_row,shows);
        mContext = context;
        mShows = shows;
        mIsHomeListView = isHomeListView;
    }

    /**
     * Populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
     */
    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            //Inflates the home_list_row View for the ListView
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(getLayoutToInflate(), parent, false);
            viewHolder = new ViewHolder();
            initialiseSharedViews(viewHolder, convertView);

            if(mIsHomeListView){
                initialiseHomeListRowViews(viewHolder, convertView);
            }
            else{
                initialiseSearchListRowViews(viewHolder, convertView);
            }
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Displays the app's launcher icon if the show has no poster or if data saving mode has been activated
        if(UserAccountUtilities.getDataSavingPreference(mContext) || mShows.get(position).getShowImageUrl() == null){
            viewHolder.poster.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            //Displays the image for the show if the show has a poster and data saving mode hasn't been activated
            Picasso.with(mContext)
                    .load(mShows.get(position)
                            .getShowImageUrl())
                    .error(R.color.colorGray)
                    .placeholder(R.color.colorGray)
                    .into(viewHolder.poster);
        }

        //Displays shared text
        displayTextInSharedTextViews(viewHolder, position);

        Resources resources = mContext.getResources();

        if(mIsHomeListView){
            viewHolder.nextEpisode.setText(resources.getString(R.string.text_next_episode, mShows.get(position).getShowNextEpisode()));
        }
        else{
            //Displays the appropriate unit for the runtime
            if(!mShows.get(position).getShowRuntime().equals(mContext.getString(R.string.n_a))){
                viewHolder.runtime.setText(
                        resources.getString(
                                R.string.text_runtime_minutes,
                                mShows.get(position).getShowRuntime()));
            }
            else{
                viewHolder.runtime.setText(
                        resources.getString(
                                R.string.text_runtime,
                                mShows.get(position).getShowRuntime()));
            }
        }

        //Displays appropriate image for the ImageButton
        if(mIsHomeListView){
            viewHolder.toggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
        }
        else if(mShows.get(position).isShowAdded() != null && mShows.get(position).isShowAdded()){
            viewHolder.toggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
        }
        else if(mShows.get(position).isShowAdded() != null){
            viewHolder.toggleShow.setImageResource(R.drawable.ic_add_black_24dp);
        }

        //Sets onCLickListener for the buttons contained in each row of the ListView
        viewHolder.toggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Adds the Show to My Series if the Show isn't already there, or prompts the user to confirm the removal of the Show from My Series if the Show is already there
                if(!mShows.get(position).isShowAdded()){
                    //Adds Show to My Series
                    viewHolder.toggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
                    mShows.get(position).setShowAdded(true);
                    pushUserShowSelection(UserAccountUtilities.getUserKey(mContext), "" + mShows.get(position).getShowId(), mShows.get(position).getShowTitle(), true);
                    notifyDataSetChanged();
                }
                else{
                    //Creates an AlertDialog to prompt the user to confirm their decision to remove the series from My Series
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    View view = View.inflate(mContext, R.layout.dialog_remove_series, null);
                    TextView textView = view.findViewById(R.id.tv_remove_series);
                    textView.setText(mContext.getString(R.string.series_removal_confirmation, mShows.get(position).getShowTitle()));
                    alertDialog.setView(view);

                    //Creates OnClickListener for the Dialog message
                    DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            switch(button){
                                //Removes the selected show from the My Series list
                                case AlertDialog.BUTTON_POSITIVE:
                                    //Updates the FirebaseDatabase and the UI
                                    pushUserShowSelection(UserAccountUtilities.getUserKey(mContext), "" + mShows.get(position).getShowId(), mShows.get(position).getShowTitle(), false);
                                    mShows.get(position).setShowAdded(false);

                                    //Removes the Show
                                    if(mIsHomeListView){
                                        mShows.remove(position);
                                    }

                                    viewHolder.toggleShow.setImageResource(R.drawable.ic_add_black_24dp);
                                    notifyDataSetChanged();
                                    break;
                                case AlertDialog.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    //Assigns button an OnClickListener for the AlertDialog and displays the AlertDialog
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.yes), dialogOnClickListener);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.no), dialogOnClickListener);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        });
        return convertView;
    }

    /**
     * Returns the appropriate XML layout to inflate
     */
    private int getLayoutToInflate(){
        if(mIsHomeListView){
            return R.layout.home_list_row;
        }

        return R.layout.other_list_row;
    }

    /**
     * Initialises the Views that are shared between home_list_row and other_list_row
     */
    private void initialiseSharedViews(ViewHolder viewHolder, View convertView){
        //Component assignments
        viewHolder.poster = convertView.findViewById(R.id.image_show_poster);
        viewHolder.title = convertView.findViewById(R.id.text_show_title);
        viewHolder.rating = convertView.findViewById(R.id.text_show_rating);
        viewHolder.status = convertView.findViewById((R.id.text_show_status));
        viewHolder.toggleShow = convertView.findViewById(R.id.button_toggle_show);
    }

    /**
     * Initialises the home_list_row extra Views
     */
    private void initialiseHomeListRowViews(ViewHolder viewHolder, View convertView){
        //Component assignments
        viewHolder.nextEpisode = convertView.findViewById((R.id.text_show_next_episode_date));
    }

    /**
     * Initialises the other_list_row extra Views
     */
    private void initialiseSearchListRowViews(ViewHolder viewHolder, View convertView){
        //Component assignments
        viewHolder.runtime = convertView.findViewById((R.id.text_show_runtime));
    }

    /**
     * Displays the text on shared TextViews
     */
    private void displayTextInSharedTextViews(ViewHolder viewHolder, int position){
        //Displays the data in the appropriate Views
        Resources resources = mContext.getResources();
        viewHolder.title.setText(mShows.get(position).getShowTitle());
        viewHolder.rating.setText(resources.getString(R.string.text_rating, mShows.get(position).getShowRating()));
        viewHolder.status.setText(resources.getString(R.string.text_status, mShows.get(position).getShowStatus()));
    }

    /**
     * Updates the shows that the user has added to 'My Series' in the Firebase database
     */
    private void pushUserShowSelection(String userKey, String showID, String showTitle, boolean showAdded){
        //Establishes a connection to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(userKey);
        String message = showAdded ? mContext.getString(R.string.added_to_my_series, showTitle) : mContext.getString(R.string.removed_from_my_series, showTitle);

        //Generates the user's key and saves the value (the user's email address) to the Firebase database
        databaseReference.child(showID).setValue(showAdded);
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    //ViewHolder class used to decrease the findViewById calls
    static class ViewHolder{
        //Component assignments
        ImageView poster;
        TextView title;
        TextView rating;
        TextView status;
        TextView nextEpisode;
        TextView runtime;
        ImageButton toggleShow;
    }
}