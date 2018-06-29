package com.matthewsyren.seriessearcher.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.matthewsyren.seriessearcher.R;
import com.matthewsyren.seriessearcher.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateAccountActivity
        extends AppCompatActivity {
    //View bindings
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.layout_create_account) RelativeLayout mLayoutCreateAccount;
    @BindView(R.id.text_create_account_email) EditText mTextCreateAccountEmail;
    @BindView(R.id.text_create_account_password) EditText mTextCreateAccountPassword;
    @BindView(R.id.text_create_account_confirm_password) EditText mTextCreateAccountConfirmPassword;

    //Declarations
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        ButterKnife.bind(this);

        //Hides ProgressBar
        toggleProgressBar(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    //Method toggles the visibility of the ProgressBar
    public void toggleProgressBar(int visibility){
        mProgressBar.setVisibility(visibility);

        if(visibility == View.VISIBLE){
            mLayoutCreateAccount.setVisibility(View.INVISIBLE);
        }
        else{
            mLayoutCreateAccount.setVisibility(View.VISIBLE);
        }
    }

    //Method creates an account for the user
    public void createAccountOnClick(View view){
        String email = mTextCreateAccountEmail.getText().toString();
        String password = mTextCreateAccountPassword.getText().toString();
        String confirmPassword = mTextCreateAccountConfirmPassword.getText().toString();

        if(email.length() == 0){
            Toast.makeText(getApplicationContext(), R.string.enter_email_address, Toast.LENGTH_LONG).show();
        }
        else if(password.length() == 0){
            Toast.makeText(getApplicationContext(), R.string.enter_password, Toast.LENGTH_LONG).show();
        }
        else{
            //Displays ProgressBar
            toggleProgressBar(View.VISIBLE);

            //Creates an account if the user's passwords match and they have entered valid data
            if(password.equals(confirmPassword)){
                final User user = new User(email, password);
                firebaseAuth.createUserWithEmailAndPassword(user.getUserEmailAddress(), user.getUserPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Registers the user in the Firebase authentication for this app
                            user.pushUser(getApplicationContext());
                        }
                        else if(task.getException().toString().contains("FirebaseAuthUserCollisionException")){
                            Toast.makeText(CreateAccountActivity.this, R.string.email_address_taken, Toast.LENGTH_LONG).show();
                            toggleProgressBar(View.INVISIBLE);
                        }
                        else if(task.getException().toString().contains("FirebaseAuthWeakPasswordException")){
                            Toast.makeText(CreateAccountActivity.this, R.string.stronger_password, Toast.LENGTH_LONG).show();
                            toggleProgressBar(View.INVISIBLE);
                        }
                        else{
                            Toast.makeText(CreateAccountActivity.this, R.string.error_create_account, Toast.LENGTH_LONG).show();
                            toggleProgressBar(View.INVISIBLE);
                        }
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(), R.string.passwords_do_not_match, Toast.LENGTH_LONG).show();
                toggleProgressBar(View.INVISIBLE);
            }
        }
    }
}