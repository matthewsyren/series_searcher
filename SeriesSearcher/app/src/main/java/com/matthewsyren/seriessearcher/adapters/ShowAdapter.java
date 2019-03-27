package com.matthewsyren.seriessearcher.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.HomeActivity;
import com.matthewsyren.seriessearcher.activities.SpecificShowActivity;
import com.matthewsyren.seriessearcher.customviews.RoundedImageView;
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
    private final Context mContext;
    private final boolean mIsHomeRecyclerView;

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
        //Displays images for the row
        displayImagesForRow(viewHolder, position);

        //Displays the text
        displayTextInSharedTextViews(viewHolder, position);
        displayTextInNonSharedTextViews(viewHolder, position);

        //Adds an OnClickListener for the toggle show ImageButton
        addListenerForToggleShowButton(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return sShows.size();
    }

    /**
     * Adds an OnClickListener for the toggle show ImageButton
     */
    private void addListenerForToggleShowButton(final ViewHolder viewHolder, final int position){
        //Sets onCLickListener for the buttons contained in each row of the RecyclerView
        viewHolder.ibToggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Show show = sShows.get(position);

                //Adds the Show to My Series if the Show isn't already there, or prompts the user to confirm the removal of the Show from My Series if the Show is already there
                if(!sShows.get(position).isShowAdded()){
                    //Sets showAdded to true and changes the Button image to a delete icon
                    viewHolder.ibToggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
                    sShows.get(position).setShowAdded(true);

                    //Updates the values in the Firebase database
                    show.pushUserShowSelection(
                            UserAccountUtilities.getUserKey(mContext),
                            true,
                            mContext);

                    //Updates the RecyclerView's data
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
                                    //Updates the Firebase database and the UI
                                    show.pushUserShowSelection(
                                            UserAccountUtilities.getUserKey(mContext),
                                            false,
                                            mContext);

                                    //Sets showAdded to false
                                    sShows.get(position).setShowAdded(false);

                                    //Removes the Show
                                    if(mIsHomeRecyclerView){
                                        sShows.remove(position);

                                        if(sShows.size() == 0){
                                            ((HomeActivity)mContext).fetchUsersShows();
                                        }
                                    }

                                    //Sets the Button's image to an add icon
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
                    alertDialog.show();
                }
            }
        });
    }

    /**
     * Displays the images for each row
     */
    private void displayImagesForRow(final ViewHolder viewHolder, int position){
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
     * Displays the text for TextViews that are shared between the layout files
     */
    private void displayTextInSharedTextViews(ViewHolder viewHolder, int position){
        //Displays the data in the appropriate Views
        Resources resources = mContext.getResources();
        viewHolder.tvShowTitle.setText(sShows.get(position).getShowTitle());

        viewHolder.tvShowRating.setText(
                Html.fromHtml(resources.getString(
                        R.string.text_rating,
                        sShows.get(position).getShowRating())));

        viewHolder.tvShowStatus.setText(
                Html.fromHtml(resources.getString(
                        R.string.text_status,
                        sShows.get(position).getShowStatus())));
    }

    /**
     * Displays text for the TextViews that aren't shared between the layout files
     */
    private void displayTextInNonSharedTextViews(ViewHolder viewHolder, int position){
        //Initialises a Resources object
        Resources resources = mContext.getResources();

        //Displays the text for Views that aren't shared between the two layout files
        if(mIsHomeRecyclerView && viewHolder.tvShowNextEpisode != null){
            viewHolder.tvShowNextEpisode.setText(
                    Html.fromHtml(resources.getString(
                            R.string.text_next_episode,
                            sShows.get(position).getShowNextEpisode())));
        }
        else{
            //Displays the appropriate unit for the runtime
            if(viewHolder.tvShowRuntime != null){
                if(!sShows.get(position).getShowRuntime().equals(mContext.getString(R.string.n_a))){
                    viewHolder.tvShowRuntime.setText(
                            Html.fromHtml(resources.getString(
                                    R.string.text_runtime_minutes,
                                    sShows.get(position).getShowRuntime())));
                }
                else{
                    viewHolder.tvShowRuntime.setText(
                            Html.fromHtml(resources.getString(
                                    R.string.text_runtime,
                                    sShows.get(position).getShowRuntime())));
                }
            }
        }
    }

    //ViewHolder class used to decrease the findViewById calls
    static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        //View bindings
        @BindView(R.id.image_show_poster) RoundedImageView ivShowPoster;
        @BindView(R.id.text_show_title) TextView tvShowTitle;
        @BindView(R.id.text_show_rating) TextView tvShowRating;
        @BindView(R.id.text_show_status) TextView tvShowStatus;
        @Nullable
        @BindView(R.id.text_show_next_episode_date) TextView tvShowNextEpisode;
        @Nullable
        @BindView(R.id.text_show_runtime) TextView tvShowRuntime;
        @BindView(R.id.button_toggle_show) ImageButton ibToggleShow;
        
        //Variables
        private final Context mContext;

        public ViewHolder(View itemView, final Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            ivShowPoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Displays an AlertDialog with the Show's poster
                    displayShowPosterAlertDialog(context);
                }
            });
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            //Takes the user to the SpecificShowActivity
            Intent intent = new Intent(mContext, SpecificShowActivity.class);
            intent.putExtra(SpecificShowActivity.SHOW_ID_KEY, "" + sShows.get(getAdapterPosition()).getShowId());

            //Animates image
            Bundle bundle = ActivityOptions
                    .makeSceneTransitionAnimation((Activity) mContext, ivShowPoster, ivShowPoster.getTransitionName())
                    .toBundle();
            
            mContext.startActivity(intent, bundle);
        }

        /**
         * Displays the full Show poster in an AlertDialog
         */
        private void displayShowPosterAlertDialog(Context context){
            //Creates an AlertDialog to display the Show's poster
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            View inflateView = View.inflate(context, R.layout.dialog_show_poster, null);
            ImageView imageView = inflateView.findViewById(R.id.image_view_show_poster);
            String showImageUrl = sShows.get(getAdapterPosition()).getShowImageUrl();

            //Displays a default image if the show doesn't have a poster or the user has enabled data saving mode, otherwise displays a default image
            if(UserAccountUtilities.getDataSavingPreference(context) || showImageUrl == null){
                //Displays a default image
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setImageResource(R.mipmap.ic_launcher);
            }
            else{
                //Loads the poster
                Picasso.with(context)
                        .load(showImageUrl)
                        .into(imageView);
            }

            //Sets the background to transparent and loads the View into the AlertDialog
            Window window = alertDialog.getWindow();
            if(window != null){
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            alertDialog.setView(inflateView);

            //Displays the AlertDialog
            alertDialog.show();
        }
    }
}