package syrenware.seriessearcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        //Takes the user to the HomeActivity if they have already signed in
        User user = new User(this);
        if(user.getUserEmailAddress() != null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    }

    //Method checks the information that the user has entered, and logs the user in if the information matches information in the Firebase authentication database
    public void loginOnClick(View view){
        try{
            EditText txtEmail = (EditText) findViewById(R.id.text_login_email);
            EditText txtPassword = (EditText) findViewById(R.id.text_login_password);

            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();
            final User user = new User(email, password);

            //Tries to sign the user in using the Firebase authentication database
            firebaseAuth.signInWithEmailAndPassword(user.getUserEmailAddress(), user.getUserPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d("TAG", "signInWithEmail:onComplete:" + task.isSuccessful());
                    if(task.isSuccessful()){
                        //Saves the user's email and key to the device's SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("userEmail", user.getUserEmailAddress());
                        user.setUserKey();
                        editor.putString("userKey", user.getUserKey());
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Log.w("TAG", "signInWithEmail", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method takes the user to the CreateAccountActivity
    public void createAccountOnClick(View view){
        try{
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}