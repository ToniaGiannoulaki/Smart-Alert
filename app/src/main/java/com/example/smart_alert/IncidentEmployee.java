package com.example.smart_alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.List;

public class IncidentEmployee extends AppCompatActivity {
    TextView message;
    Button accept, delete, deleteAll;
    private String newestMessageKey;
    private StorageReference latestFileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_employee);
        message = findViewById(R.id.textView_messages);
        // Retrieve the information from realtime DB
        fetchMessagesFromDatabase();

        ////////////////////////////////// ACCEPT MESSAGE //////////////////////////////////////////

        accept = findViewById(R.id.button_accept);

        accept.setOnClickListener(v ->{
            // Show the toast message
            Toast.makeText(IncidentEmployee.this, getString(R.string.redirecting_message), Toast.LENGTH_SHORT).show();

            // Create an Intent to start the ComposeMessage activity
            Intent intent = new Intent(IncidentEmployee.this, ComposeMessage.class);
            startActivity(intent);
        });

        ////////////////////////////// DELETE THE LAST MESSAGE /////////////////////////////////////
        delete = findViewById(R.id.button_delete);

        delete.setOnClickListener(v -> showDeleteConfirmationDialog());

        ///////////////////////////////// DELETE ALL MESSAGES //////////////////////////////////////

        deleteAll = findViewById(R.id.button_deleteAll);

        deleteAll.setOnClickListener(v -> showDeleteAllConfirmationDialog());

        //////////////////////////////// RETRIEVE IMAGE ////////////////////////////////////////////
        // Find the ImageView by its ID
        ImageView image = findViewById(R.id.imageView_incidentImage);

        // Get a reference to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");

        // List all items (files) in the "uploads" directory
        storageRef.listAll().addOnSuccessListener(listResult -> {
            if (listResult.getItems().size() > 0) {
                // Assuming the files are named with timestamps, the last one should be the latest
                List<StorageReference> fileRefs = listResult.getItems();
                Collections.sort(fileRefs, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                // Store the reference to the latest file
                latestFileRef = fileRefs.get(fileRefs.size() - 1);
                // Get the latest file
                StorageReference latestFileRef = fileRefs.get(fileRefs.size() - 1);

                // Now get the download URL for the latest file
                latestFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Got the download URL for the latest file, use Glide to load it into the ImageView
                    Glide.with(IncidentEmployee.this)
                            .load(uri)
                            .into(image);
                }).addOnFailureListener(exception -> {
                    // Handle any errors in getting the download URL
                    Toast.makeText(IncidentEmployee.this, "Error loading image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(IncidentEmployee.this, "No images found in 'uploads'", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle any errors in listing the directory
            Toast.makeText(IncidentEmployee.this, "Error listing images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });




        /////////////////////////////////////// EXIT //////////////////////////////////////////////
        ImageButton exit = findViewById(R.id.imageButtonIncidentEmployeeExit);

        exit.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(IncidentEmployee.this, LoginEmployee.class);
            // Start the new activity
            startActivity(intent);
        });
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// RETRIEVE MESSAGES FROM REALTIME DB ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void fetchMessagesFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("UsersMessages");

        // Query for the last message based on a timestamp
        Query lastMessageQuery = databaseRef.orderByChild("timestamp").limitToLast(1);

        lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Since limitToLast is used, dataSnapshot still contains a list of children, but only with the newest message
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Extract data
                        String incident = snapshot.child("incident").getValue(String.class);
                        String latitude = snapshot.child("latitude").getValue(String.class);
                        String longitude = snapshot.child("longitude").getValue(String.class);
                        String date = snapshot.child("date").getValue(String.class);
                        String time = snapshot.child("time").getValue(String.class);
                        String description = snapshot.child("description").getValue(String.class);
                        // Save the key of the newest message
                        newestMessageKey = snapshot.getKey();
                        // Build the message String
                        StringBuilder messagesBuilder = new StringBuilder();
                        messagesBuilder.append("Incident: ").append(incident).append("\n")
                                .append("Latitude: ").append(latitude).append("\n")
                                .append("Longitude: ").append(longitude).append("\n")
                                .append("Date: ").append(date).append("\n")
                                .append("Time: ").append(time).append("\n")
                                .append("Description: ").append(description).append("\n\n");

                        // Display the newest message in TextView
                        message.setText(messagesBuilder.toString());
                        break; // Since it's the newest and only message we are querying, break after processing it
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(IncidentEmployee.this, "Failed to load messages", Toast.LENGTH_LONG).show();
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////// DELETE LAST MESSAGE FROM REALTIME DB AND STORAGE //////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_message_title))
                .setMessage(getString(R.string.delete_message_confirmation))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteMessageFromDatabase();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void deleteLastImageFromStorage() {
        if (latestFileRef != null) {
            latestFileRef.delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(IncidentEmployee.this, getString(R.string.last_image_deleted_successfully), Toast.LENGTH_SHORT).show();
                latestFileRef = null;
            }).addOnFailureListener(exception -> Toast.makeText(IncidentEmployee.this, getString(R.string.failed_to_delete_last_image), Toast.LENGTH_SHORT).show());
        }
    }


    private void deleteMessageFromDatabase() {
        if (newestMessageKey != null) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("UsersMessages");
            databaseRef.child(newestMessageKey).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(IncidentEmployee.this, getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
                    message.setText("");
                    newestMessageKey = null;
                    deleteLastImageFromStorage();
                } else {
                    Toast.makeText(IncidentEmployee.this, getString(R.string.failed_to_delete_message), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(IncidentEmployee.this, getString(R.string.no_message_to_delete), Toast.LENGTH_SHORT).show();
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////// DELETE ALL MESSAGES FROM REALTIME DB AND STORAGE //////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_all_messages_title))
                .setMessage(getString(R.string.delete_all_messages_confirmation))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteAllMessagesFromDatabase();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void deleteAllMessagesFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("UsersMessages");
        databaseRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(IncidentEmployee.this, getString(R.string.all_messages_deleted), Toast.LENGTH_SHORT).show();
                deleteAllImagesFromStorage();
                message.setText("");
            } else {
                Toast.makeText(IncidentEmployee.this, getString(R.string.failed_to_delete_messages), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAllImagesFromStorage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");
        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference itemRef : listResult.getItems()) {
                itemRef.delete().addOnSuccessListener(aVoid -> {
                    // Optionally, handle individual file deletion success
                }).addOnFailureListener(exception -> Toast.makeText(IncidentEmployee.this, getString(R.string.error_deleting_image, exception.getMessage()), Toast.LENGTH_SHORT).show());
            }
            Toast.makeText(IncidentEmployee.this, getString(R.string.all_images_deleted_successfully), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(IncidentEmployee.this, getString(R.string.failed_to_list_images_for_deletion, e.getMessage()), Toast.LENGTH_SHORT).show());
    }
}


////////////////////////////// STORAGE RULES ///////////////////////////////////////
/*
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      // Allow read access to all users, authenticated or not
      allow read;
      // Allow write access only if the user is authenticated
      allow write: if request.auth != null;
    }
  }
}
 */