package com.example.snakejava;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private TextView registerTextView, goPlayGame;
    private EditText loginEditText, passwdEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private String email, password;
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);

        loginEditText = findViewById(R.id.login_edittext);
        passwdEditText = findViewById(R.id.passwd_edittext);
        loginButton = findViewById(R.id.login_button);
        registerTextView = findViewById(R.id.register_click);
        goPlayGame = findViewById(R.id.goplay);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            updateUI(mAuth.getCurrentUser());
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = loginEditText.getText().toString();
                password = passwdEditText.getText().toString();
/*if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
{
    Toast.makeText(getApplicationContext(),"Enter email and password",
            Toast.LENGTH_SHORT).show();
    return;
}*/
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(getApplicationContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), Register.class);
                startActivity(registerIntent);

            }
        });

        goPlayGame.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent play_game = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(play_game);

            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    public void updateUI(FirebaseUser currentUser){
        Intent profileIntent = new Intent(getApplicationContext(), MainActivity.class);
        profileIntent.putExtra("email",currentUser.getEmail());
        Log.v("DATA",currentUser.getUid());
        startActivity(profileIntent);
    }
}