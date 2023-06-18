package com.example.humainactivityrecognition;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Profile extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private Button editProfile;
    private ImageView imageView;
    private TextView genre, nom, sector, mail, nmra;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private SharedPreferences sharedPreferences;
    private static final String IMAGE_PATH_KEY = "imagePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // use ActionBar utility methods
        ActionBar actionBar = getSupportActionBar();
        // providing title for the ActionBar
        actionBar.setTitle("Profile");
        imageView = findViewById(R.id.imgprofil);
        genre = findViewById(R.id.genre);
        nom = findViewById(R.id.nom);
        sector = findViewById(R.id.sector);
        mail = findViewById(R.id.mail);
        nmra = findViewById(R.id.nmra);
        editProfile = findViewById(R.id.editprofil);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String imagePath = sharedPreferences.getString(IMAGE_PATH_KEY, null);
        if (imagePath != null) {
            Bitmap bitmap = decodeBase64(imagePath);
            imageView.setImageBitmap(bitmap);
        }else{
            imageView.setImageBitmap(null);
            imageView.setImageResource(R.drawable.edited);
        }

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);
                    String filiere = dataSnapshot.child("filiere").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    nom.setText(fullName);
                    nmra.setText(phoneNumber);
                    sector.setText(filiere);
                    genre.setText(gender);
                    mail.setText(email);

                    String imagePath = dataSnapshot.child("imagePath").getValue(String.class);
                    if (imagePath != null) {
                        Bitmap bitmap = decodeBase64(imagePath);
                        imageView.setImageBitmap(bitmap);
                    }else{
                        imageView.setImageBitmap(null);
                        imageView.setImageResource(R.drawable.edited);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", "onCancelled: " + databaseError.getMessage());
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageOptionsDialog();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileIntent = new Intent(Profile.this, EditeProfil.class);
                startActivity(editProfileIntent);
            }
        });
    }

    private void showImageOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Choose an option")
                .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Open camera
                                checkCameraPermission();
                                break;
                            case 1:
                                // Choose from gallery
                                chooseFromGallery();
                                break;
                            case 2:
                                // Delete image
                                deleteImage();
                                break;
                        }
                    }
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(Profile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Profile.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }


    private void chooseFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    private void deleteImage() {
        // Clear the SharedPreferences value
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(IMAGE_PATH_KEY);
        editor.apply();

        // Implement the logic to delete the image here
        // For example, you can set a default image or clear the ImageView
        imageView.setImageResource(R.drawable.ic_launcher_background);

        // Update the image path in the user's Firebase database node
        userRef.child("imagePath").setValue(null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                // Camera photo captured
                // Handle the captured photo
                Bitmap bitmap = null;
                if (data != null && data.getExtras() != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                }

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);

                    String imagePath = encodeToBase64(bitmap);

                    // Store the image path in the user's Firebase database node
                    userRef.child("imagePath").setValue(imagePath);

                    // Store the image path in SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(IMAGE_PATH_KEY, imagePath);
                    editor.apply();
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                // Photo selected from gallery
                // Handle the selected photo
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    imageView.setImageBitmap(bitmap);

                    String imagePath = encodeToBase64(bitmap);

                    // Store the image path in the user's Firebase database node
                    userRef.child("imagePath").setValue(imagePath);

                    // Store the image path in SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(IMAGE_PATH_KEY, imagePath);
                    editor.apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Camera permission denied
                // You can show a toast message or display an error dialog to the user
            }
        }
    }
}
