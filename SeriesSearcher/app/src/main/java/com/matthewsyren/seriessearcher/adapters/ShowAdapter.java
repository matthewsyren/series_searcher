package com.matthewsyren.seriessearcher.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.SpecificShowActivity;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by matthew on 2017/01/28.
 * Class populates a RecyclerView with the data that is passed into the constructor
 */

@SuppressWarnings("WeakerAccess")
public class ShowAdapter
        extends RecyclerView.Adapter<ShowAdapter.ViewHolder>{
    //Declarations
    private static ArrayList<Show> sShows;
    private Context mContext;
    private boolean mIsHomeRecyclerView;

    /**
     * Constructor
     */
    public ShowAdapter(Context context, ArrayList<Show> shows, boolean isHomeRecyclerView) {
        mContext = context;
        sShows = shows;
        mIsHomeRecyclerView = isHomeRecyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(getLayoutToInflate(), parent, false);
        return new ViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        //Displays the app's launcher icon if the show has no poster or if data saving mode has been activated
        if(UserAccountUtilities.getDataSavingPreference(mContext) || sShows.get(position).getShowImageUrl() == null){
            viewHolder.ivShowPoster.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            //Displays the image for the show if the show has a poster and data saving mode hasn't been activated
            Picasso.with(mContext)
                    .load(sShows.get(position)
                            .getShowImageUrl())
                    .error(R.color.colorGray)
                    .placeholder(R.color.colorGray)
                    .into(viewHolder.ivShowPoster);
        }

        //Displays shared text
        displayTextInSharedTextViews(viewHolder, position);

        Resources resources = mContext.getResources();

        if(mIsHomeRecyclerView && viewHolder.tvShowNextEpisode != null){
            viewHolder.tvShowNextEpisode.setText(resources.getString(R.string.text_next_episode, sShows.get(position).getShowNextEpisode()));
        }
        else{
            //Displays the appropriate unit for the runtime
            if(viewHolder.tvShowRuntime != null){
                if(!sShows.get(position).getShowRuntime().equals(mContext.getString(R.string.n_a))){
                    viewHolder.tvShowRuntime.setText(
                            resources.getString(
                                    R.string.text_runtime_minutes,
                                    sShows.get(position).getShowRuntime()));
                }
                else{
                    viewHolder.tvShowRuntime.setText(
                            resources.getString(
                                    R.string.text_runtime,
                                    sShows.get(position).getShowRuntime()));
                }
            }
        }

        //Displays appropriate image for the ImageButton
        if(mIsHomeRecyclerView){
            viewHolder.ibToggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
        }
        else if(sShows.get(position).isShowAdded() != null && sShows.get(position).isShowAdded()){
            viewHolder.ibToggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
        }
        else if(sShows.get(position).isShowAdded() != null){
            viewHolder.ibToggleShow.setImageResource(R.drawable.ic_add_black_24dp);
        }

        //Sets onCLickListener for the buttons contained in each row of the RecyclerView
        viewHolder.ibToggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Adds the Show to My Series if the Show isn't already there, or prompts the user to confirm the removal of the Show from My Series if the Show is already there
                if(!sShows.get(position).isShowAdded()){
                    //Adds Show to My Series
                    viewHolder.ibToggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
                    sShows.get(position).setShowAdded(true);
                    pushUserShowSelection(UserAccountUtilities.getUserKey(mContext), "" + sShows.get(position).getShowId(), sShows.get(position).getShowTitle(), true);
                    notifyDataSetChanged();
                }
                else{
                    //Creates an AlertDialog to prompt the user to confirm their decision to remove the series from My Series
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    View view = View.inflate(mContext, R.layout.dialog_remove_series, null);
                    TextView textView = view.findViewById(R.id.tv_remove_series);
                    textView.setText(mContext.getString(R.string.series_removal_confirmation, sShows.get(position).getShowTitle()));
                    alertDialog.setView(view);

                    //Creates OnClickListener for the Dialog message
                    DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            switch(button){
                                //Removes the selected show from the My Series list
                                case AlertDialog.BUTTON_POSITIVE:
                                    //Updates the FirebaseDatabase and the UI
                                    pushUserShowSelection(UserAccountUtilities.getUserKey(mContext), "" + sShows.get(position).getShowId(), sShows.get(position).getShowTitle(), false);
                                    sShows.get(position).setShowAdded(false);

                                    //Removes the Show
                                    if(mIsHomeRecyclerView){
                                        sShows.remove(position);
                                    }

                                    viewHolder.ibToggleShow.setImageResource(R.drawable.ic_add_black_24dp);
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
    }

    @Override
    public int getItemCount() {
        return sShows.size();
    }

    /**
     * Returns the appropriate XML layout to inflate
     */
    private int getLayoutToInflate(){
        if(mIsHomeRecyclerView){
            return R.layout.home_list_row;
        }

        return R.layout.other_list_row;
    }

    /**
     * Displays the text on shared TextViews
     */
    private void displayTextInSharedTextViews(ViewHolder viewHolder, int position){
        //Displays the data in the appropriate Views
        Resources resources = mContext.getResources();
        viewHolder.tvShowTitle.setText(sShows.get(position).getShowTitle());
        viewHolder.tvShowRating.setText(resources.getString(R.string.text_rating, sShows.get(position).getShowRating()));
        viewHolder.tvShowStatus.setText(resources.getString(R.string.text_status, sShows.get(position).getShowStatus()));
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
    static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        //View bindings
        @BindView(R.id.image_show_poster) ImageView ivShowPoster;
        @BindView(R.id.text_show_title) TextView tvShowTitle;
        @BindView(R.id.text_show_rating) TextView tvShowRating;
        @BindView(R.id.text_show_status) TextView tvShowStatus;
        @Nullable
        @BindView(R.id.text_show_next_episode_date) TextView tvShowNextEpisode;
        @Nullable
        @BindView(R.id.text_show_runtime) TextView tvShowRuntime;
        @BindView(R.id.button_toggle_show) ImageButton ibToggleShow;
        
        //Variables
        private Context mContext;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            //Takes the user to the SpecificShowActivity (with an image animation)
            Intent intent = new Intent(mContext, SpecificShowActivity.class);
            intent.putExtra(SpecificShowActivity.SHOW_ID_KEY, "" + sShows.get(getAdapterPosition()).getShowId());
            ImageView imageView = v.findViewById(R.id.image_show_poster);
            
            Bundle bundle = ActivityOptions
                    .makeSceneTransitionAnimation((Activity) mContext, imageView, imageView.getTransitionName())
                    .toBundle();
            
            mContext.startActivity(intent, bundle);
        }
    }
}