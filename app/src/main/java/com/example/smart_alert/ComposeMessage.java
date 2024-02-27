package com.example.smart_alert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;


public class ComposeMessage extends AppCompatActivity {
    static final String TAG = "ComposeMessage";
    private EditText incident, lon, lat, editTextDate, editTextTime, distance, description;
    Button send;
    private DatabaseReference usersMessagesRef;
    private ValueEventListener messageListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);

        // Initialize EditTexts
        incident = findViewById(R.id.editTextText_reportIncident);
        lon = findViewById(R.id.editTextNumber_longitude);
        lat = findViewById(R.id.editTextNumber_latitude);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        distance = findViewById(R.id.editTextDistance);
        description = findViewById(R.id.editTextTextMultiLine_description);

        //////////////////////////// RETRIEVE THE ACCEPTED MESSAGE /////////////////////////////////


        // Initialize Firebase references
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        usersMessagesRef = databaseReference.child("UsersMessages");

        // Initialize and attach the ValueEventListener
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String longitude = snapshot.child("longitude").getValue(String.class);
                        String latitude = snapshot.child("latitude").getValue(String.class);
                        String dateValue = snapshot.child("date").getValue(String.class); // Use a different variable name
                        String timeValue = snapshot.child("time").getValue(String.class); // Use a different variable name

                        // Set the retrieved values to the EditTexts
                        lon.setText(longitude);
                        lat.setText(latitude);
                        editTextDate.setText(dateValue);
                        editTextTime.setText(timeValue);

                        // After setting the data, remove the listener
                        usersMessagesRef.removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        // Attach the listener to the reference
        usersMessagesRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(messageListener);

        send = findViewById(R.id.button_sendEmployee);

        send.setOnClickListener(v ->{
            // Get the text from the EditText fields
            String inc = incident.getText().toString().trim();
            String longitude = lon.getText().toString().trim();
            String latitude = lat.getText().toString().trim();
            String date = editTextDate.getText().toString().trim();
            String time = editTextTime.getText().toString().trim();
            String dist = distance.getText().toString().trim();
            String desc = description.getText().toString().trim();


            if(inc.isEmpty() || longitude.isEmpty() || latitude.isEmpty() || date.isEmpty() || time.isEmpty() || dist.isEmpty() || desc.isEmpty()){
                // Show the toast message
                Toast.makeText(ComposeMessage.this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
            }else{
                // Call the method to save the message
                saveEmployeesMessage(inc, longitude, latitude, date, time, dist, desc);
            }
        });


        /////////////////////////////////////// EXIT //////////////////////////////////////////////
        ImageButton exit = findViewById(R.id.imageButtonLoginEmployeeExit);

        exit.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(ComposeMessage.this, LoginEmployee.class);
            // Start the new activity
            startActivity(intent);
        });
    }

    private void saveEmployeesMessage(String inc, String longitude, String latitude, String date, String time, String dist, String desc) {
        // Reference to your 'EmployeeMessages' node in the database
        DatabaseReference employeesMessagesRef = FirebaseDatabase.getInstance().getReference("EmployeeMessages");

        // Generate a unique key for a new message
        String messageId = employeesMessagesRef.push().getKey();

        // Create a message object or use a HashMap if you don't have a Message class
        HashMap<String, Object> message = new HashMap<>();
        message.put("incident", inc);
        message.put("longitude", longitude);
        message.put("latitude", latitude);
        message.put("date", date);
        message.put("time", time);
        message.put("distance", dist);
        message.put("description", desc);

        // Check if messageId is not null to avoid NullPointerException
        if (messageId != null) {
            employeesMessagesRef.child(messageId).setValue(message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Notify the user of success
                            Toast.makeText(ComposeMessage.this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
                            // Remove the ValueEventListener
                            usersMessagesRef.removeEventListener(messageListener);
                        } else {
                            // Notify the user of failure
                            Toast.makeText(ComposeMessage.this, "Failed to send message: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}