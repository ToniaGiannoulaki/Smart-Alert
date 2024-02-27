package com.example.smart_alert;

import static com.example.smart_alert.ComposeMessage.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Statistics extends AppCompatActivity {

    private TextView sentMess, sentFire, sentEarth, sentFlood, sentElse;
    private TextView receivedMess, receivedFire, receivedEarth, receivedFlood, receivedElse;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        sentMess = findViewById(R.id.textView_sentMessages);
        sentFire = findViewById(R.id.textView_sentFire);
        sentEarth = findViewById(R.id.textView_sentEarthquake);
        sentFlood = findViewById(R.id.textView_sentFlood);
        sentElse = findViewById(R.id.textView_sentSomethingElse);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize your TextViews for received statistics
        receivedMess = findViewById(R.id.textView_receivedMessages);
        receivedFire = findViewById(R.id.textView_receivedFire);
        receivedEarth = findViewById(R.id.textView_receivedEarthquake);
        receivedFlood = findViewById(R.id.textView_receivedFlood);
        receivedElse = findViewById(R.id.textView_receivedSomethingElse);

        if (currentUser != null) {
            // Get the user ID from the current FirebaseUser
            userId = currentUser.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId);

            // Retrieve user statistics from Firebase
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Update the TextViews if the data exists
                        updateTextView(sentMess, dataSnapshot, "dangerEvents");
                        updateTextView(sentFire, dataSnapshot, "fireEvent");
                        updateTextView(sentEarth, dataSnapshot, "earthquakeEvent");
                        updateTextView(sentFlood, dataSnapshot, "floodEvent");
                        updateTextView(sentElse, dataSnapshot, "elseEvent");
                        updateTextView(receivedMess, dataSnapshot, "EmployeeDangerEvents");
                        updateTextView(receivedFire, dataSnapshot, "EmployeeFireEvent");
                        updateTextView(receivedEarth, dataSnapshot, "EmployeeEarthEvent");
                        updateTextView(receivedFlood, dataSnapshot, "EmployeeFloodEvent");
                        updateTextView(receivedElse, dataSnapshot, "EmployeeElseEvent");
                    } else {
                        // Handle the case where the user data does not exist
                        Log.w(TAG, "User data does not exist.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                    // Handle error
                }
            });
        } else {
            // No user is logged in, handle this case
            Log.w(TAG, "User not logged in.");
        }

        /////////////////////////////////////// EXIT //////////////////////////////////////////////
        ImageButton exit = findViewById(R.id.imageButtonStatisticsExit);

        exit.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(Statistics.this, LoginUser.class);
            // Start the new activity
            startActivity(intent);
        });
    }

    private void updateTextView(TextView textView, DataSnapshot dataSnapshot, String field) {
        if (dataSnapshot.hasChild(field)) {
            Long count = dataSnapshot.child(field).getValue(Long.class);
            textView.setText(String.valueOf(count));
        } else {
            textView.setText("0");
        }
    }
}