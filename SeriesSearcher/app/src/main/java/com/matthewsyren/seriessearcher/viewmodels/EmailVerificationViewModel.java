package com.matthewsyren.seriessearcher.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

/**
 * Class used to handle tasks related to verifying the user's email address
 */

public class EmailVerificationViewModel
        extends AndroidViewModel {
    //Variables
    private MutableLiveData<Boolean> mIsOngoingOperation = new MutableLiveData<>();
    private MutableLiveData<Integer> mReauthenticationSuccessful = new MutableLiveData<>();
    private MutableLiveData<Integer> mVerificationEmailSent = new MutableLiveData<>();

    //Codes
    public static final int REAUTHENTICATION_SUCCESSFUL = 1001;
    public static final int REAUTHENTICATION_WRONG_PASSWORD = 1002;
    public static final int REAUTHENTICATION_ERROR = 1003;
    public static final int VERIFICATION_EMAIL_SENT = 2001;
    public static final int VERIFICATION_EMAIL_NOT_SENT = 2002;

    /**
     * Constructor
     */
    public EmailVerificationViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Sends a verification email to the user
     * @param firebaseUser The FirebaseUser to send a verification email to
     */
    public void sendVerificationEmail(FirebaseUser firebaseUser){
        //Marks the operation as ongoing
        mIsOngoingOperation.setValue(true);

        //Sends a verification email to the user
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Marks the verification email as sent, and marks the operation as finished
                        mVerificationEmailSent.setValue(VERIFICATION_EMAIL_SENT);
                        mIsOngoingOperation.setValue(false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Marks the verification email as not sent, and marks the operation as finished
                        mVerificationEmailSent.setValue(VERIFICATION_EMAIL_NOT_SENT);
                        mIsOngoingOperation.setValue(false);
                    }
                });
    }

    /**
     * Reauthenticates the user to refresh the verification status of their email address
     * @param firebaseUser The FirebaseUser to reauthenticate
     * @param password The password that the user types in
     */
    public void reauthenticateUser(FirebaseUser firebaseUser, String password){
        //Marks the operation as ongoing
        mIsOngoingOperation.setValue(true);

        if(firebaseUser != null && firebaseUser.getEmail() != null){
            //Attempts to reauthenticate the user
            firebaseUser.reauthenticate(EmailAuthProvider.getCredential(firebaseUser.getEmail(), password))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Marks the reauthentication as successful
                            if(task.isSuccessful()){
                                mReauthenticationSuccessful.setValue(REAUTHENTICATION_SUCCESSFUL);
                            }

                            //Marks the operation as finished
                            mIsOngoingOperation.setValue(false);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Marks the reauthentication as failed, with the appropriate error code
                            if(e.getMessage().toLowerCase().contains("password")){
                                mReauthenticationSuccessful.setValue(REAUTHENTICATION_WRONG_PASSWORD);
                            }
                            else{
                                mReauthenticationSuccessful.setValue(REAUTHENTICATION_ERROR);
                            }

                            //Marks the operation as finished
                            mIsOngoingOperation.setValue(false);
                        }
                    });
        }
    }

    /**
     * Returns an observable instance of mIsOngoingOperation
     * @return A variable used to check if there is an ongoing operation
     */
    public MutableLiveData<Boolean> getObservableOngoingOperation(){
        return mIsOngoingOperation;
    }

    /**
     * Returns an observable instance of mVerificationEmailSent
     * @return A variable used to check if the verification email has been sent
     */
    public MutableLiveData<Integer> getObservableVerificationEmailSent(){
        return mVerificationEmailSent;
    }

    /**
     * Returns an observable instance of mReauthenticationSuccessful
     * @return A variable used to check if the user has successfully reauthenticated
     */
    public MutableLiveData<Integer> getObservableReauthenticationSuccessful(){
        return mReauthenticationSuccessful;
    }
}