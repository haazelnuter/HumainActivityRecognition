package com.example.humainactivityrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    TextInputEditText full_name, phone, filiere, email, password, confirmpassword;
    Button register;
    Spinner spinner;
    ProgressBar progressBar;
    TextView textlogin;
    String prefix = "+212";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        full_name = findViewById(R.id.full_name);
        phone = findViewById(R.id.phone);
        filiere = findViewById(R.id.filiere);
        spinner = findViewById(R.id.gender);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmpassword = findViewById(R.id.confirmpassword);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressbar);
        textlogin = findViewById(R.id.textlogin);

        textlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String fullNameValue = full_name.getText().toString().trim();
                String phoneNumberValue = phone.getText().toString().trim();
                String fullPhone = prefix + phoneNumberValue;
                String filiereValue = filiere.getText().toString().trim();
                String genderValue = spinner.getSelectedItem().toString();
                String emailValue = email.getText().toString().trim();
                String passwordValue = password.getText().toString().trim();
                String confirmPasswordValue = confirmpassword.getText().toString().trim();

                if (TextUtils.isEmpty(fullNameValue) || TextUtils.isEmpty(phoneNumberValue) ||
                        TextUtils.isEmpty(filiereValue) || TextUtils.isEmpty(genderValue) ||
                        TextUtils.isEmpty(emailValue) || TextUtils.isEmpty(passwordValue) ||
                        TextUtils.isEmpty(confirmPasswordValue)) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(register.this, "Please fill all the fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                    progressBar.setVisibility(View.GONE);
                    email.setError("Please provide a valid email!");
                    email.requestFocus();
                    return;
                }

                if (passwordValue.length() < 8) {
                    progressBar.setVisibility(View.GONE);
                    password.setError("Password must be at least 8 characters long!");
                    password.requestFocus();
                    return;
                }

                if (!passwordValue.equals(confirmPasswordValue)) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(register.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(emailValue, passwordValue).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(register.this, "Registration successful!", Toast.LENGTH_LONG).show();
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                String userId = currentUser.getUid();
                                User user = new User(fullNameValue, fullPhone, genderValue, filiereValue, emailValue, passwordValue);
                                usersRef.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressBar.setVisibility(View.GONE);
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(register.this, "Failed to register user data. Please try again later.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(register.this, "Registration failed! Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
