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

        messages.setOnClickListener(v ->{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("EmployeeMessages");

            //todo add comments
            LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            messagesRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Location messageLocation = new Location("");
                                            messageLocation.setLatitude(Double.parseDouble(snapshot.child("latitude").getValue(String.class)));
                                            messageLocation.setLongitude(Double.parseDouble(snapshot.child("longitude").getValue(String.class)));
                                            double radius = Double.parseDouble(snapshot.child("distance").getValue(String.class));

                                            float distance = location.distanceTo(messageLocation);

                                            if (distance <= radius) {
                                                // Construct a message from all the node's data
                                                String messageInfo = "Date: " + snapshot.child("date").getValue(String.class) + "\n" +
                                                        "Description: " + snapshot.child("description").getValue(String.class) + "\n" +
                                                        "Distance: " + snapshot.child("distance").getValue(String.class) + " meters\n" +
                                                        "Incident: " + snapshot.child("incident").getValue(String.class) + "\n" +
                                                        "Latitude: " + snapshot.child("latitude").getValue(String.class) + "\n" +
                                                        "Longitude: " + snapshot.child("longitude").getValue(String.class) + "\n" +
                                                        "Time: " + snapshot.child("time").getValue(String.class);

                                                // Show the complete message info in an alert dialog
                                                showAlert("EMERGENCY!", messageInfo);
                                            }
                                        }
                                    } else {
                                        showAlert(getString(R.string.alert_title), getString(R.string.alert_message));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
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
                Intent intent = new Intent(this, Statistics.class);
                startActivity(intent);
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

    private void resetFireStats(DatabaseReference userRef){
        userRef.child("EmployeeFireEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // This method will be called once with the results of the transaction.
                if (committed) {
                    resetFloodStats(userRef);
                }
            }
        });
    }
    private void resetFloodStats(DatabaseReference userRef){
        userRef.child("EmployeeFloodEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // This method will be called once with the results of the transaction.
                if (committed) {
                    resetEarthStats(userRef);
                }
            }
        });
    }
    private void resetEarthStats(DatabaseReference userRef){
        userRef.child("EmployeeEarthEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // This method will be called once with the results of the transaction.
                if (committed) {
                    resetElseStats(userRef);
                }
            }
        });
    }
    private void resetElseStats(DatabaseReference userRef){
        userRef.child("EmployeeElseEvent").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // This method will be called once with the results of the transaction.
                if (committed) {
                    resetDangerStats(userRef);
                }
            }
        });
    }
    private void resetDangerStats(DatabaseReference userRef){
        userRef.child("EmployeeDangerEvents").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                // This method will be called once with the results of the transaction.
                if (committed) {
                    calculateStats(userRef);
                }
            }
        });
    }

    private void calculateStats(DatabaseReference userRef){
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("EmployeeMessages");
        messagesRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

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
                } else {
                    showAlert("Messages", "No messages available.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showAlert("Database Error", databaseError.getMessage());
            }
        });
    }

    private void calculateDangerMessages(DatabaseReference userRef){
        userRef.child("EmployeeDangerEvents").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer currentCount = mutableData.getValue(Integer.class);
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
                    showStats();
                }
            }
        });
    }
    private void showStats(){
        // Create an Intent to start the new activity
        Intent intent = new Intent(LoginUser.this, Statistics.class);
        // Start the new activity
        startActivity(intent);
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(LoginUser.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.stat_sys_warning)
                .show();
    }
}
