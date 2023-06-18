package com.example.humainactivityrecognition;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;

public class EditeProfil extends AppCompatActivity {

    EditText emailogin, fullname, phone, etude;
    Button save;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edite_profil);

        emailogin = findViewById(R.id.emailogin);
        fullname = findViewById(R.id.fullname);
        phone = findViewById(R.id.phone);
        etude = findViewById(R.id.etude);
        save = findViewById(R.id.save);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            emailogin.setText(currentUser.getEmail());
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

            DatabaseReference userRef = mDatabase.getReference("users").child(currentUser.getUid());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                        String filiere = dataSnapshot.child("filiere").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        fullname.setText(fullName);
                        phone.setText(phoneNumber);
                        etude.setText(filiere);
                        emailogin.setText(email);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                }
            });
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = emailogin.getText().toString();
                String fullNameValue = fullname.getText().toString();
                String phoneNumberValue = phone.getText().toString();
                String filiereValue = etude.getText().toString();

                if (newEmail.isEmpty()) {
                    Toast.makeText(EditeProfil.this, "Please enter your new email.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prompt the user to confirm the password
                AlertDialog.Builder builder = new AlertDialog.Builder(EditeProfil.this);
                builder.setTitle("Confirm Password");
                builder.setMessage("Please enter your current password to proceed with the email update.");

                final EditText input = new EditText(EditeProfil.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enteredPassword = input.getText().toString();
                        // Re-authenticate the user with the entered password
                        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
                        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Password is correct, update the email address
                                    currentUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User email address updated.");
                                                updateUserDataInDatabase(fullNameValue, phoneNumberValue, filiereValue, newEmail);
                                            } else {
                                                Log.d(TAG, "Failed to update user email address: " + task.getException().getMessage());
                                                Toast.makeText(EditeProfil.this, "Failed to update email. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    // Incorrect password
                                    Toast.makeText(EditeProfil.this, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

    }
    private void updateUserDataInDatabase(String fullNameValue, String phoneNumberValue, String filiereValue, String newEmail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            // Update the email field in the Realtime Database
            userRef.child("email").setValue(newEmail);

            // Update other fields (name, phone, sector) in the Realtime Database
            userRef.child("fullName").setValue(fullNameValue);
            userRef.child("phoneNumber").setValue(phoneNumberValue);
            userRef.child("filiere").setValue(filiereValue);

            // Set the updated email in the Authentication
            user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Display a message to the user indicating the update was successful
                        Toast.makeText(EditeProfil.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();

                        // Return to the previous activity
                        finish();
                    } else {
                        // Display an error message if updating the email in Authentication fails
                        Toast.makeText(EditeProfil.this, "Failed to update email. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }




}
