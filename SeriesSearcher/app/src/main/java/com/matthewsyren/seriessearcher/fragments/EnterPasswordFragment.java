package com.matthewsyren.seriessearcher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.matthewsyren.seriessearcher.R;

/**
 * Fragment used to allow the user to enter their password for the app when reauthenticating
 */

public class EnterPasswordFragment
        extends DialogFragment {
    //Constants
    public static final String ENTER_PASSWORD_FRAGMENT_TAG = "enter_password_fragment_tag";

    //Variables
    private IEnterPasswordFragmentOnClickListener mEnterPasswordFragmentOnClickListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Builds the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = View.inflate(getActivity(), R.layout.fragment_enter_password, null);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, null);

        //Creates the AlertDialog
        final AlertDialog alertDialog = builder.create();

        //Sets the OnClickListener for the AlertDialog
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //Initialises the Button variables
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

                //Sets up the positiveButton OnClickListener
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Fetches the user's password
                        EditText etPassword = view.findViewById(R.id.et_password);
                        String password = etPassword.getText().toString();

                        //Sends the user's password back to the Activity if it isn't empty, otherwise displays an error message
                        if(password.length() > 0){
                            returnResult(password);
                            dismiss();
                        }
                        else{
                            Toast.makeText(getActivity(), R.string.enter_password, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                //Sets up the negativeButton OnClickListener
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Closes the AlertDialog
                        returnResult(null);
                        dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

    /**
     * Sends the user's password back to the appropriate Activity (if the Activity has been set)
     * @param password The password that the user entered
     */
    private void returnResult(String password){
        if(mEnterPasswordFragmentOnClickListener != null){
            mEnterPasswordFragmentOnClickListener.onEnterPasswordFragmentClick(password);
        }
    }

    /**
     * Setter method (used to set the Activity/class to which data must be sent once the user has entered their password)
     * @param iEnterPasswordFragmentOnClickListener The instance of the IEnterPasswordFragmentOnClickListener class to notify when there is a click
     */
    public void setEnterPasswordFragmentOnClickListener(IEnterPasswordFragmentOnClickListener iEnterPasswordFragmentOnClickListener){
        mEnterPasswordFragmentOnClickListener = iEnterPasswordFragmentOnClickListener;
    }

    /**
     * Interface is used to send data back to the appropriate Activity once the user clicks on a button
     */
    public interface IEnterPasswordFragmentOnClickListener {
        /**
         * Sends data back to the appropriate Activity once the user has entered their password
         * @param password The password that was entered by the user
         */
        void onEnterPasswordFragmentClick(String password);
    }
}