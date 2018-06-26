package com.matthewsyren.seriessearcher.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity
        extends AppCompatActivity {
    //View bindings
    @BindView(R.id.button_google_sign_in) SignInButton mButtonGoogleSignIn;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.layout_login) RelativeLayout mLayoutLogin;
    @BindView(R.id.text_login_email) EditText mTextLoginEmail;
    @BindView(R.id.text_login_password) EditText mTextLoginPassword;

    //Declarations
    private FirebaseAuth firebaseAuth;
    private final int GOOGLE_SIGN_IN_KEY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();

        //Hides ProgressBar
        toggleProgressBarVisibility(View.INVISIBLE);

        //Takes the user to the HomeActivity if they have already signed in
        User user = new User(this);
        if(user.getUserEmailAddress() != null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        //Sets Listener for the Google Sign in Button
        mButtonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Displays the Progress Bar and begins the process to sign in via a Google account
                toggleProgressBarVisibility(View.VISIBLE);
                signInWithGoogleOnClick();
            }
        });
    }

    //Method signs the user in with their Google account
    public void signInWithGoogleOnClick(){
        //Attempts to sign the user in using their Google account
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, GOOGLE_SIGN_IN_KEY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Signs the user in once they have chosen their Google account
        if (requestCode == GOOGLE_SIGN_IN_KEY) {
            try {
                //Prepares the details required to sign in
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                //Signs the user in
                firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Finishes signing the user in
                        final User user = new User(account.getEmail(), "");
                        user.setUserKey(getApplicationContext());
                    }
                });
            }
            catch (ApiException e) {
                recreate();
            }
        }
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBarVisibility(int visibility){
        mProgressBar.setVisibility(visibility);

        if(visibility == View.VISIBLE){
            mLayoutLogin.setVisibility(View.INVISIBLE);
        }
        else{
            mLayoutLogin.setVisibility(View.VISIBLE);
        }
    }

    //Method prevents the user from going back to the previous Activity by clicking the back button
    @Override
    public void onBackPressed() {
    }

    //Method checks the information that the user has entered, and logs the user in if the information matches information in the Firebase authentication database
    public void loginOnClick(View view){
        String email = mTextLoginEmail.getText().toString();
        String password = mTextLoginPassword.getText().toString();

        if(email.length() == 0){
            Toast.makeText(getApplicationContext(), "Please enter your email address or sign in via Google", Toast.LENGTH_LONG).show();
        }
        else if(password.length() == 0){
            Toast.makeText(getApplicationContext(), "Please enter your password", Toast.LENGTH_LONG).show();
        }
        else{
            final User user = new User(email, password);
            final LoginActivity loginActivity = this;

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Tries to sign the user in using the Firebase authentication database
            firebaseAuth.signInWithEmailAndPassword(user.getUserEmailAddress(), user.getUserPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //Fetches the user's key from Firebase and then calls the writeToSharedPreferences method once the key is fetched
                        user.setUserKey(loginActivity);
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        toggleProgressBarVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    //Method takes the user to the CreateAccountActivity
    public void createAccountOnClick(View view){
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    //Method initiates the password recovery feature of this app
    public void forgotPasswordOnClick(View view){
        displayInputMessage("Please enter your email address. An email with a link to reset your password will be sent to you.");
    }

    //Method displays an AlertDialog to get an email address from the user
    public void displayInputMessage(String message){
        //Creates AlertDialog content
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTypeface(null, Typeface.BOLD);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //Adds content to AlertDialog
        LinearLayout relativeLayout = new LinearLayout(this);
        relativeLayout.setOrientation(LinearLayout.VERTICAL);
        relativeLayout.addView(textView);
        relativeLayout.addView(input);
        alertDialog.setView(relativeLayout);

        //Creates OnClickListener for the Dialog message
        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int button) {
                switch(button){
                    case AlertDialog.BUTTON_POSITIVE:
                        String emailAddress = input.getText().toString();
                        if(emailAddress.length() > 0){
                            toggleProgressBarVisibility(View.VISIBLE);

                            //Sends the email to the user if the email address is valid
                            firebaseAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Displays message telling the user the email has been sent successfully
                                        Toast.makeText(getApplicationContext(), "Email sent", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        String exceptionMessage = task.getException().toString();

                                        //Displays appropriate error messages based on the exception message details
                                        if(exceptionMessage.contains("FirebaseAuthInvalidUserException")){
                                            Toast.makeText(getApplicationContext(), "There is no account associated with that email address, please enter the email address you used to create an account for this app", Toast.LENGTH_LONG).show();
                                        }
                                        else if(exceptionMessage.contains("FirebaseAuthInvalidCredentialsException")){
                                            Toast.makeText(getApplicationContext(), "The email you entered is invalid, please ensure that the email address you enter exists", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    toggleProgressBarVisibility(View.INVISIBLE);
                                }
                            });
                        }
                        else{
                            //Asks the user to enter a valid email address
                            displayInputMessage("Please enter your email address");
                        }
                        break;
                    }
                }
            };

            //Assigns button and OnClickListener for the AlertDialog and displays the AlertDialog
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", dialogOnClickListener);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
    }
}