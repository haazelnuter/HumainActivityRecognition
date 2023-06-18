package com.example.humainactivityrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://projet-humain-activity-default-rtdb.firebaseio.com/");
    TextInputEditText email, password;
    Button login;
    ProgressBar progressBar;
    private FirebaseAuth auth;
    TextView textregister;


//    @Override
//    public void onStart() {
//        super.onStart();
//        FirebaseUser currentUser = auth.getCurrentUser();
//        if (currentUser != null) {
//            // Utilisateur déjà connecté, rediriger vers l'écran d'accueil
//            Intent intent = new Intent(getApplicationContext(), Home.class);
//            startActivity(intent);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.emailogin);
        password = findViewById(R.id.passwordlogin);
        progressBar = findViewById(R.id.progressbarlogin);
        textregister = findViewById(R.id.textregister);
        auth = FirebaseAuth.getInstance();
        login = findViewById(R.id.login);
        textregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), register.class);
                startActivity(intent);
                finish();
            }
        });
        //LOGIN BUTTON
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email1 = email.getText().toString().trim();//.replace(".", ",")
                String pswd = password.getText().toString().trim();
                if (TextUtils.isEmpty(email1) || TextUtils.isEmpty(pswd)) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Enter your email or password", Toast.LENGTH_SHORT).show();
                }
                // signin existing user
                auth.signInWithEmailAndPassword(email1, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login successful!!", Toast.LENGTH_LONG).show();
                            // hide the progress bar
                            progressBar.setVisibility(View.GONE);
                            // if sign-in is successful
                            // intent to home activity
                            Intent intent = new Intent(login.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            // sign-in failed
                            Toast.makeText(getApplicationContext(), "Login failed!!", Toast.LENGTH_LONG).show();
                            // hide the progress bar
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}