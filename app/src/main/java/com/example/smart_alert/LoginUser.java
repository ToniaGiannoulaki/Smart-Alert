package com.example.smart_alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class LoginUser extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        /////////////////////// VIEW MESSAGE /////////////////////////

        Button messages = findViewById(R.id.button_messagesLast);

        // Set an onClick listener for the 'messages' button
        messages.setOnClickListener(v -> {
            // Request location permissions at runtime for accessing the fine location
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);

            // Check if location permissions have been granted; if not, exit the listener
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            // Reference to the 'EmployeeMessages' node in Firebase Database
            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("EmployeeMessages");

            // Fetch the last known location of the device
            LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Check if a location was successfully received
                        if (location != null) {
                            // Query the Firebase Database for messages, ordered by their keys
                            messagesRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // Check if there are any messages in the database
                                    if (dataSnapshot.exists()) {
                                        // Iterate through each message
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            // Create a new Location object for the message's location
                                            Location messageLocation = new Location("");
                                            messageLocation.setLatitude(Double.parseDouble(snapshot.child("latitude").getValue(String.class)));
                                            messageLocation.setLongitude(Double.parseDouble(snapshot.child("longitude").getValue(String.class)));
                                            // Retrieve the radius within which the message is relevant
                                            double radius = Double.parseDouble(snapshot.child("distance").getValue(String.class));

                                            // Calculate the distance from the current location to the message's location
                                            float distance = location.distanceTo(messageLocation);

                                            // Check if the current location is within the message's relevant radius
                                            if (distance <= radius) {
                                                // Construct a detailed message string from the message's data
                                                String messageInfo = "Date: " + snapshot.child("date").getValue(String.class) + "\n" +
                                                        "Description: " + snapshot.child("description").getValue(String.class) + "\n" +
                                                        "Distance: " + snapshot.child("distance").getValue(String.class) + " meters\n" +
                                                        "Incident: " + snapshot.child("incident").getValue(String.class) + "\n" +
                                                        "Latitude: " + snapshot.child("latitude").getValue(String.class) + "\n" +
                                                        "Longitude: " + snapshot.child("longitude").getValue(String.class) + "\n" +
                                                        "Time: " + snapshot.child("time").getValue(String.class);

                                                // Display the message information in an alert dialog
                                                showAlert("EMERGENCY!", messageInfo);
                                            }
                                        }
                                    } else {
                                        // If there are no messages, show a default alert dialog
                                        showAlert(getString(R.string.alert_title), getString(R.string.alert_message));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle database access errors
                                    showAlert("Database Error", databaseError.getMessage());
                                }
                            });
                        }
                    });
        });

        /////////////////////// FIRE INCIDENT /////////////////////////
        TextView fire = findViewById(R.id.textView_fire);
        ImageButton fireButton = findViewById(R.id.imageButton_fire);

        fireButton.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(LoginUser.this, IncidentUser.class);
            // Passing a string extra
            intent.putExtra("incidentType", fire.getText().toString());
            // Start the new activity
            startActivity(intent);
        });

        //////////////////// EARTHQUAKE INCIDENT /////////////////////
        TextView earthquake = findViewById(R.id.textView_earthquake);
        ImageButton earthquakeButton = findViewById(R.id.imageButton_earthquake);

        earthquakeButton.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(LoginUser.this, IncidentUser.class);
            // Passing a string extra
            intent.putExtra("incidentType", earthquake.getText().toString());
            // Start the new activity
            startActivity(intent);
        });

        ////////////////////// FLOOD INCIDENT ////////////////////////
        TextView flood = findViewById(R.id.textView_flood);
        ImageButton floodButton = findViewById(R.id.imageButton_flood);

        floodButton.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(LoginUser.this, IncidentUser.class);
            // Passing a string extra
            intent.putExtra("incidentType", flood.getText().toString());
            // Start the new activity
            startActivity(intent);
        });


        ////////////////// SOMETHING ELSE INCIDENT ////////////////////
        TextView other = findViewById(R.id.textView_question);
        ImageButton otherButton = findViewById(R.id.imageButton_other);

        otherButton.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(LoginUser.this, IncidentUser.class);
            // Passing a string extra
            intent.putExtra("incidentType", other.getText().toString());
            // Start the new activity
            startActivity(intent);
        });

        ////////////////////////// STATISTICS ///////////////////////////

        Button stat = findViewById(R.id.button_statistics);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            stat.setOnClickListener(v -> {
                Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
                resetFireStats(userRef);
            });
        }


        //////////////////////////// EXIT //////////////////////////////
        ImageButton back = findViewById(R.id.imageButton);

        back.setOnClickListener(v -> {
            // Create an AlertDialog to confirm exit
            new AlertDialog.Builder(LoginUser.this)
                    .setTitle(getString(R.string.exit_confirmation_title)) // Set the title of the dialog box
                    .setMessage(getString(R.string.exit_confirmation_message)) // Set the message to show in the dialog box
                    .setPositiveButton(getString(R.string.exit_confirmation_positive), (dialog, which) -> {
                        // User clicked "Yes", so exit the activity
                        Intent intent = new Intent(LoginUser.this, MainMenu.class);
                        startActivity(intent);
                        finish(); // Call this if you want to close the current activity as well
                    })
                    .setNegativeButton(getString(R.string.exit_confirmation_negative), null) // User clicked "No", so dismiss the dialog and do nothing
                    .show(); // Show the AlertDialog
        });
    }

    // Method to reset fire event statistics to 0.
    private void resetFireStats(DatabaseReference userRef){
        // Access the specific child in the database and run a transaction to reset its value.
        userRef.child("EmployeeFireEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Set the value of the event count to 0.
                mutableData.setValue(0);
                // Indicate that the transaction was successful.
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // This callback is triggered after the transaction is completed.
                if (committed) {
                    // If the transaction is successful, proceed to reset flood event statistics.
                    resetFloodStats(userRef);
                }
            }
        });
    }

    // Method to reset flood event statistics to 0.
    private void resetFloodStats(DatabaseReference userRef){
        // Similar structure to resetFireStats. Targets a different event type in the database.
        userRef.child("EmployeeFloodEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Reset the value to 0.
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                if (committed) {
                    // Proceed to reset earthquake event statistics next.
                    resetEarthStats(userRef);
                }
            }
        });
    }

    // Method to reset earthquake event statistics to 0.
    private void resetEarthStats(DatabaseReference userRef){
        // Similar structure to the previous methods. Resets a different event type.
        userRef.child("EmployeeEarthEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Reset the value to 0.
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                if (committed) {
                    // Next, reset the statistics for the 'else' category of events.
                    resetElseStats(userRef);
                }
            }
        });
    }

    // Method to reset 'else' category event statistics to 0.
    private void resetElseStats(DatabaseReference userRef){
        // Follows the same pattern as the previous reset methods. Aims at a generic 'else' event category.
        userRef.child("EmployeeElseEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Reset the value to 0.
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                if (committed) {
                    // Finally, reset the statistics for all dangerous events.
                    resetDangerStats(userRef);
                }
            }
        });
    }

    // Resets the statistics for all "danger" events to 0.
    private void resetDangerStats(DatabaseReference userRef){
        userRef.child("EmployeeDangerEvents").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Set the danger event count to 0.
                mutableData.setValue(0);
                return Transaction.success(mutableData); // Mark the transaction as successful.
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // Called once the transaction completes successfully.
                if (committed) {
                    // Proceed to calculate updated stats after resetting.
                    calculateStats(userRef);
                }
            }
        });
    }

    // Calculates and updates the count of each type of event based on existing messages.
    private void calculateStats(DatabaseReference userRef){
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("EmployeeMessages");
        // Retrieve all messages and process them one by one.
        messagesRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Determine the type of incident from each message.
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String incidentType = snapshot.child("incident").getValue(String.class);
                        String eventType;
                        if (incidentType.equals("fire")) {
                            eventType = "EmployeeFireEvent";
                        } else if (incidentType.equals("earthquake")) {
                            eventType = "EmployeeEarthEvent";
                        } else if (incidentType.equals("flood")) {
                            eventType = "EmployeeFloodEvent";
                        } else if (incidentType.equals("else")) {
                            eventType = "EmployeeElseEvent";
                        } else eventType = null;

                        if (eventType != null) {
                            // Run the transaction on the determined event type
                            userRef.child(eventType).runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    Integer currentCount = mutableData.getValue(Integer.class);
                                    // Increment the counter for the determined event type.
                                    if (currentCount == null) {
                                        mutableData.setValue(1);
                                    } else {
                                        mutableData.setValue(currentCount + 1);
                                    }
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                                    // This method will be called once with the results of the transaction.
                                    if (committed) {
                                        calculateDangerMessages(userRef);
                                    }
                                }
                            });
                        }
                    }
                    // Show an alert if no messages are available.
                } else {
                    showAlert("Messages", "No messages available.");
                }
            }
            // Show an alert if there was an error accessing the database.
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showAlert("Database Error", databaseError.getMessage());
            }
        });
    }

    // Increments the count for "danger" messages
    private void calculateDangerMessages(DatabaseReference userRef){
        userRef.child("EmployeeDangerEvents").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // Increment the count by 1, or initialize it to 1 if it's null.
                Integer currentCount = mutableData.getValue(Integer.class);
                // Called once the transaction completes successfully.
                if (currentCount == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentCount + 1);
                }
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // This method will be called once with the results of the transaction.
                if (committed) {
                    // Navigate to the statistics display activity.
                    showStats();
                }
            }
        });
    }
    // Navigates to the Statistics activity to display the updated counts
    private void showStats(){
        // Create an Intent to start the new activity
        Intent intent = new Intent(this, Statistics.class);
        // Start the new activity
        startActivity(intent);
    }

    // Displays an alert dialog with a specified title and message.
    private void showAlert(String title, String message) {
        new AlertDialog.Builder(LoginUser.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.stat_sys_warning)
                .show();
    }
}
