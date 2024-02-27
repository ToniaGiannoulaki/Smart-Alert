package com.example.smart_alert;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class LoginEmployee extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_employee);

        /////////////////////////////////// VIEW MESSAGES //////////////////////////////////////////

        Button viewMessage = findViewById(R.id.button_viewMessages);

        viewMessage.setOnClickListener(v ->{
            // Create an Intent to start the new activity
            Intent intent = new Intent(LoginEmployee.this, IncidentEmployee.class);
            // Start the new activity
            startActivity(intent);
        });


        ////////////////////////////////// COMPOSE MESSAGE /////////////////////////////////////////

        Button composeMessage = findViewById(R.id.button_composeMessage);

        composeMessage.setOnClickListener(v ->{
            // Create an Intent to start the new activity
            Intent intent = new Intent(LoginEmployee.this, ComposeMessage.class);
            // Start the new activity
            startActivity(intent);
        });

        ////////////////////////////////////// LOG OUT /////////////////////////////////////////////

        Button logOut = findViewById(R.id.button_logout);

        logOut.setOnClickListener(v ->{
            // Create an AlertDialog to confirm exit
            new AlertDialog.Builder(LoginEmployee.this)
                    .setTitle(getString(R.string.exit_confirmation_title)) // Use resource for the title
                    .setMessage(getString(R.string.exit_confirmation_message)) // Use resource for the message
                    .setPositiveButton(getString(R.string.exit_confirmation_positive), (dialog, which) -> {
                        // User clicked "Yes", so exit the activity
                        Intent intent = new Intent(LoginEmployee.this, MainMenu.class);
                        startActivity(intent);
                        finish(); // Call this if you want to close the current activity as well
                    })
                    .setNegativeButton(getString(R.string.exit_confirmation_negative), null) // Use resource for the "No" button
                    .show(); // Show the AlertDialog
        });
    }
}