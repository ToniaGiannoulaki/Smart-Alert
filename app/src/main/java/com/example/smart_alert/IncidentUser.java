package com.example.smart_alert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationRequest;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.util.HashMap;
import java.util.Locale;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class IncidentUser extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView incident, latitude, longitude, date, time;
    ImageView imageView;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat, simpleDateFormat1;
    String Date, Time;
    EditText description;
    Button send, upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_user);

        //Make image view transparent
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);

        // Transfer DATA from LoginUser class
        incident = findViewById(R.id.textViewIncident);
        Intent incidentUser = getIntent();
        String incidentType = incidentUser.getStringExtra("incidentType");
        incident.setText(incidentType);

        //////////////////////////////// COORDINATES /////////////////////////////////////
        latitude = findViewById(R.id.textViewLatitude);
        longitude = findViewById(R.id.textViewLongitude);

        // Get user's current coordinates (longitude and latitude)
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(IncidentUser.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            currentLocation();
        }


        /////////////////////////////// DATE AND TIME //////////////////////////////
        //Receive user's date and time
        calendar = Calendar.getInstance();
        date = findViewById(R.id.textViewDate);

        // Format the date as "Date: dd-MM-yyyy"
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date = simpleDateFormat.format(calendar.getTime());
        date.setText(Date);

        time = findViewById(R.id.textViewTime);
        // Format the time as "Time: HH:mm"
        simpleDateFormat1 = new SimpleDateFormat("HH:mm");
        Time = simpleDateFormat1.format(calendar.getTime());
        time.setText(Time);


        ////////////////////////////// DESCRIPTION, IMAGE AND SAVE MESSAGE ////////////////////////////////
        send = findViewById(R.id.buttonUserSend);
        description = findViewById(R.id.editTextTextMultiLine);

        // Check if edit text is empty, it's a required field
        send.setOnClickListener(view -> {
            if (description.getText().toString().trim().isEmpty()) {
                Toast.makeText(IncidentUser.this, getString(R.string.toast_message_required), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(IncidentUser.this, getString(R.string.toast_message_success), Toast.LENGTH_LONG).show();
                saveMessageToDatabase();
            }
        });


        /////////////////////////////////// UPLOAD IMAGE //////////////////////////////////////////
        upload = findViewById(R.id.button_image);
        upload.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);

        });


        /////////////////////////////////////// EXIT //////////////////////////////////////////////
        ImageButton exit = findViewById(R.id.imageButtonLoginEmployeeExit);

        exit.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(IncidentUser.this, LoginUser.class);
            // Start the new activity
            startActivity(intent);
        });
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// USER'S LOCATION //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void fetchLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Use the last known location
                        latitude.setText(String.format(Locale.US, "%.2f", location.getLatitude()));
                        longitude.setText(String.format(Locale.US, "%.2f", location.getLongitude()));
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Immediately try to fetch the last known location
                fetchLastKnownLocation();
                // Then continue with regular location updates
                currentLocation();
            } else {
                // Permission was denied, redirect to LoginUser activity
                // Permission was denied, redirect to LoginUser activity
                Toast.makeText(this, getString(R.string.permission_denied_redirect), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(IncidentUser.this, LoginUser.class);
                startActivity(intent);
                finish(); // Optional: if you want to remove this activity from the back stack
            }
        }
    }
    private void currentLocation() {
        LocationRequest locationRequest = LocationRequest.create();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Your code to handle the location result
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    double lat = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    double lon = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                    // Correctly format and display the latitude and longitude
                    latitude.setText(String.format(Locale.US, "%.2f", lat));
                    longitude.setText(String.format(Locale.US, "%.2f", lon));
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(IncidentUser.this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// LOAD IMAGE FROM GALLERY //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // Show the image on an ImageView
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);

            // Upload the image to Firebase Storage
            uploadImageToFirebaseStorage(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");
        final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

        fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Image upload successful
            Toast.makeText(getApplicationContext(), getString(R.string.upload_successful), Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// SAVE MESSAGE REALTIME DB /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void incrementUserEventCount(String incidentType) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

            // Determine which event type to increment based on incidentType
            String eventType;
            if (incidentType.equals("Fire") || incidentType.equals("Φωτιά")) {
                eventType = "fireEvent";
            } else if (incidentType.equals("Earthquake") || incidentType.equals("Σεισμός")) {
                eventType = "earthquakeEvent";
            } else if (incidentType.equals("Flood") || incidentType.equals("Πλημμύρα")) {
                eventType = "floodEvent";
            } else if (incidentType.equals("Else") || incidentType.equals("Αλλο")) {
                eventType = "elseEvent";
            }else eventType= null;


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
                        if (!committed) {
                            Log.d("IncidentUser", "incrementUserEventCount: failed to increment " + eventType);
                        }
                    }
                });

                userRef.child("dangerEvents").runTransaction(new Transaction.Handler() {
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
                        if (!committed) {
                            Log.d("IncidentUser", "incrementUserEventCount: failed to increment " + "dangerEvents");
                        }
                    }
                });
            }
        }
    }



    private void saveMessageToDatabase() {
        String incidentType = incident.getText().toString().replace("Incident", "");
        String lat = latitude.getText().toString().replace("Latitude: ", "");
        String lon = longitude.getText().toString().replace("Longitude: ", "");
        String dateText = date.getText().toString().replace("Date: ", "");
        String timeText = time.getText().toString().replace("Time: ", "");
        String descriptionText = description.getText().toString();

        HashMap<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("incident", incidentType);
        messageDetails.put("latitude", lat);
        messageDetails.put("longitude", lon);
        messageDetails.put("date", dateText);
        messageDetails.put("time", timeText);
        messageDetails.put("description", descriptionText);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("UsersMessages");
        String messageId = databaseRef.push().getKey();
        if (messageId != null) {
            databaseRef.child(messageId).setValue(messageDetails)
                    .addOnSuccessListener(aVoid -> {
                        // After message is saved successfully, increment the corresponding event count
                        incrementUserEventCount(incidentType);

                        Toast.makeText(IncidentUser.this, getString(R.string.message_saved_successfully), Toast.LENGTH_LONG).show();
                        Log.d("IncidentType", "Incident Type is: " + incidentType);

                    })
                    .addOnFailureListener(e -> Toast.makeText(IncidentUser.this, getString(R.string.failed_to_save_message), Toast.LENGTH_LONG).show());
        }
    }
}