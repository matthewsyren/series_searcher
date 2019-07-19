package com.matthewsyren.seriessearcher.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.activities.HomeActivity;
import com.matthewsyren.seriessearcher.activities.ShowPosterActivity;
import com.matthewsyren.seriessearcher.activities.SpecificShowActivity;
import com.matthewsyren.seriessearcher.customviews.RoundedImageView;
import com.matthewsyren.seriessearcher.fragments.RemoveShowFromMySeriesFragment;
import com.matthewsyren.seriessearcher.fragments.RemoveShowFromMySeriesFragment.IRemoveShowFromMySeriesFragmentOnClickListener;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.models.ShowImage;
import com.matthewsyren.seriessearcher.utilities.NetworkUtilities;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Class populates a RecyclerView with the data that is passed into the constructor
 */

@SuppressWarnings("WeakerAccess")
public class ShowAdapter
        extends RecyclerView.Adapter<ShowAdapter.ViewHolder>
        implements IRemoveShowFromMySeriesFragmentOnClickListener{
    //Variables
    private static ArrayList<Show> sShows;
    private final Context mContext;
    private final boolean mIsHomeRecyclerView;
    private IRemoveShowFromMySeriesFragmentOnClickListener mRemoveShowFromMySeriesFragmentOnClickListener;

    /**
     * Constructor
     */
    public ShowAdapter(Context context, ArrayList<Show> shows, boolean isHomeRecyclerView) {
        mContext = context;
        sShows = shows;
        mIsHomeRecyclerView = isHomeRecyclerView;
        mRemoveShowFromMySeriesFragmentOnClickListener = this;
    }

    /**
     * Setter method
     */
    public void setShows(ArrayList<Show> shows){
        sShows = shows;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(getLayoutToInflate(), parent, false);

        //Fetches the RemoveShowFromMySeriesFragment
        RemoveShowFromMySeriesFragment removeShowFromMySeriesFragment = (RemoveShowFromMySeriesFragment) ((AppCompatActivity)mContext).getFragmentManager()
                .findFragmentByTag(RemoveShowFromMySeriesFragment.REMOVE_SHOW_FROM_MY_SERIES_FRAGMENT_TAG);

        //Ensures updates from RemoveShowFromMySeriesFragment are sent to this class
        if(removeShowFromMySeriesFragment != null){
            removeShowFromMySeriesFragment.setRemoveShowFromMySeriesFragmentOnClickListener(this);
        }

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
     * @param viewHolder The ViewHolder for the RecyclerView
     * @param position The position of the item in the RecyclerView
     */
    private void addListenerForToggleShowButton(final ViewHolder viewHolder, final int position){
        //Sets onCLickListener for the buttons contained in each row of the RecyclerView
        viewHolder.ibToggleShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkUtilities.isOnline(mContext)){
                    final Show show = sShows.get(position);

                    //Adds the Show to My Series if the Show isn't already there, or prompts the user to confirm the removal of the Show from My Series if the Show is already there
                    if(!sShows.get(position).isShowAdded()){
                        //Sets showAdded to true and changes the Button image to a delete icon
                        viewHolder.ibToggleShow.setImageResource(R.drawable.ic_delete_black_24dp);
                        sShows.get(position).setShowAdded(true);

                        //Updates the values in the Firebase database
                        show.updateShowInDatabase(true, mContext);

                        //Updates the RecyclerView's data
                        notifyItemChanged(position);
                    }
                    else{
                        //Initialises a DialogFragment that makes the user confirm their decision
                        FragmentManager fragmentManager = ((AppCompatActivity)mContext).getFragmentManager();
                        RemoveShowFromMySeriesFragment removeShowFromMySeriesFragment = new RemoveShowFromMySeriesFragment();

                        //Sends data to the DialogFragment
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(RemoveShowFromMySeriesFragment.SHOW_KEY, show);
                        removeShowFromMySeriesFragment.setRemoveShowFromMySeriesFragmentOnClickListener(mRemoveShowFromMySeriesFragmentOnClickListener);
                        removeShowFromMySeriesFragment.setArguments(arguments);

                        //Displays the DialogFragment
                        removeShowFromMySeriesFragment.show(fragmentManager, RemoveShowFromMySeriesFragment.REMOVE_SHOW_FROM_MY_SERIES_FRAGMENT_TAG);
                    }
                }
                else{
                    //Displays a message telling the user to connect to the Internet
                    Toast.makeText(mContext, mContext.getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Displays the images for each row
     * @param viewHolder The ViewHolder for the RecyclerView
     * @param position The position of the item in the RecyclerView
     */
    private void displayImagesForRow(final ViewHolder viewHolder, int position){
        //Displays the app's launcher icon if the show has no poster or if data saving mode has been activated
        if(UserAccountUtilities.getDataSavingPreference(mContext) || sShows.get(position).getShowImageUrl() == null){
            viewHolder.ivShowPoster.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            //Initialises ShowImage
            ShowImage showImage = new ShowImage(((Activity)mContext).getWindowManager(), mContext);

            //Displays the image for the show if the show has a poster and data saving mode hasn't been activated
            Picasso.with(mContext)
                    .load(sShows.get(position)
                            .getShowImageUrl())
                    .resize(showImage.getWidth(), showImage.getHeight())
                    .onlyScaleDown()
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
            return R.layout.list_row_home;
        }

        return R.layout.list_row_other;
    }

    /**
     * Displays the text for TextViews that are shared between the layout files
     * @param viewHolder The ViewHolder for the RecyclerView
     * @param position The position of the item in the RecyclerView
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
     * @param viewHolder The ViewHolder for the RecyclerView
     * @param position The position of the item in the RecyclerView
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

    @Override
    public void onRemoveShowFromMySeriesFragmentClick(boolean removed, Show show) {
        //Performs various tasks if the user removed the Show from My Series
        if(removed){
            //Fetches the position of the Show
            int position = sShows.indexOf(show);

            //Removes the Show if the HomeActivity is open, otherwise changes the Button icon for the Show from a delete icon to an add icon
            if(mIsHomeRecyclerView){
                if(sShows.size() > 0){
                    //Removes the Show from the sShows List
                    sShows.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                }

                if(sShows.size() == 0){
                    //Displays a message telling the user to add Shows to My Series if My Series is empty
                    ((HomeActivity)mContext).fetchUsersShows();
                }
            }
            else{
                //Updates the Button icon for the Show
                notifyItemChanged(position);
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

        /**
         * Constructor
         */
        public ViewHolder(View itemView, final Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            ivShowPoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Displays a Dialog with the Show's poster
                    displayShowPosterDialog();
                }
            });
            mContext = context;
        }

        @Override
        public void onClick(View v) {
            //Takes the user to the SpecificShowActivity
            Show show = sShows.get(getAdapterPosition());
            Intent intent = new Intent(mContext, SpecificShowActivity.class);
            intent.putExtra(SpecificShowActivity.SHOW_ID_KEY, "" + show.getShowId());
            intent.putExtra(SpecificShowActivity.SHOW_IS_ADDED_KEY, show.isShowAdded());
            intent.putExtra(SpecificShowActivity.SHOW_TITLE_KEY, show.getShowTitle());

            //Starts the SpecificShowActivity and requests that a result be sent to the current Activity
            ((Activity) mContext).startActivityForResult(
                    intent,
                    SpecificShowActivity.SPECIFIC_SHOW_ACTIVITY_REQUEST_CODE);
        }

        /**
         * Displays the full Show poster in a Dialog
         */
        private void displayShowPosterDialog(){
            //Fetches the Show
            Show show = sShows.get(getAdapterPosition());

            //Creates a Bundle to animate the shared image
            Bundle bundle = ActivityOptions
                    .makeSceneTransitionAnimation((Activity) mContext, ivShowPoster, ivShowPoster.getTransitionName())
                    .toBundle();

            //Opens ShowPosterActivity
            Intent intent = new Intent(mContext, ShowPosterActivity.class);
            intent.putExtra(ShowPosterActivity.SHOW_KEY, show);
            mContext.startActivity(intent, bundle);
        }
    }
}