package com.matthewsyren.seriessearcher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.Show;
import com.matthewsyren.seriessearcher.utilities.UserAccountUtilities;

public class RemoveShowFromMySeriesFragment
        extends DialogFragment {
    //Constants
    public static final String SHOW_KEY = "show_key";
    public static final String REMOVE_SHOW_FROM_MY_SERIES_FRAGMENT_TAG = "remove_show_from_my_series_fragment_tag";

    //Variables
    private IRemoveShowFromMySeriesFragmentOnClickListener mRemoveShowFromMySeriesFragmentOnClickListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Fetches arguments from the Bundle
        Bundle bundle = getArguments();
        final Show show = bundle.getParcelable(SHOW_KEY);

        if(show != null){
            //Builds the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = View.inflate(getActivity(), R.layout.fragment_remove_show_from_my_series, null);
            TextView textView = view.findViewById(R.id.tv_remove_series);
            textView.setText(getActivity().getString(R.string.series_removal_confirmation, show.getShowTitle()));
            builder.setView(view);

            //Creates OnClickListener for the Dialog message
            DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    switch(button){
                        //Removes the selected show from the My Series list
                        case AlertDialog.BUTTON_POSITIVE:
                            //Updates the Firebase database and the UI
                            show.pushUserShowSelection(
                                    UserAccountUtilities.getUserKey(getActivity()),
                                    false,
                                    getActivity());

                            //Sets showAdded to false
                            show.setShowAdded(false);

                            //Sends the decision back to the appropriate Activity/class
                            returnResult(true, show);
                            break;
                        //Cancels the action
                        case AlertDialog.BUTTON_NEGATIVE:
                            //Sends the decision back to the appropriate Activity/class
                            returnResult(false, show);
                            break;
                    }
                }
            };

            //Assigns Buttons and OnClickListeners for the AlertDialog
            builder.setPositiveButton(getActivity().getString(R.string.yes), dialogOnClickListener);
            builder.setNegativeButton(getActivity().getString(R.string.no), dialogOnClickListener);

            //Creates the AlertDialog
            return builder.create();
        }
        else{
            return null;
        }
    }

    /**
     * Sends the decision back to the appropriate Activity (if the Activity has been set)
     */
    private void returnResult(boolean removed, Show show){
        if(mRemoveShowFromMySeriesFragmentOnClickListener != null){
            mRemoveShowFromMySeriesFragmentOnClickListener.onRemoveShowFromMySeriesFragmentClick(removed, show);
        }
    }

    /**
     * Setter method (used to set the Activity/class to which data must be sent once a decision has been made by the user)
     */
    public void setRemoveShowFromMySeriesFragmentOnClickListener(IRemoveShowFromMySeriesFragmentOnClickListener iRemoveShowFromMySeriesFragmentOnClickListener){
        mRemoveShowFromMySeriesFragmentOnClickListener = iRemoveShowFromMySeriesFragmentOnClickListener;
    }
}