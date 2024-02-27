package com.example.smart_alert;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainMenu extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private boolean passwordVisible;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        Button button_login = findViewById(R.id.button_login);


        button_login.setOnClickListener(view -> loginUser());

        Button button_sign_up = findViewById(R.id.button_sign_up);

        button_sign_up.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(MainMenu.this, Register.class);
            // Start the new activity
            startActivity(intent);
        });


        ////////////////////// PASSWORD VISIBILITY //////////////////////////////

        passwordEditText.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Define the drawable index for start (left) as 0
                final int DRAWABLE_START = 0;

                // Get the drawable and check if it is null
                Drawable startDrawable = passwordEditText.getCompoundDrawables()[DRAWABLE_START];
                if (startDrawable != null) {
                    // Check if the touch event is within the bounds of the drawable
                    // taking into account the start position of the EditText and drawable padding
                    if (motionEvent.getRawX() >= passwordEditText.getLeft()
                            && motionEvent.getRawX() <= (passwordEditText.getLeft()
                            + startDrawable.getBounds().width() + passwordEditText.getCompoundDrawablePadding())) {

                        // Toggle password visibility
                        int selection = passwordEditText.getSelectionEnd();
                        if (passwordVisible) {
                            // Hide password
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_visibility_off_24, 0, 0, 0);
                            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            // Show password
                            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_visibility_24, 0, 0, 0);
                            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        passwordEditText.setSelection(selection);
                        return true;
                    }
                }
            }
            return false;
        });
    }


    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_empty));
            emailEditText.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_empty));
            passwordEditText.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MainMenu.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                    // Check if the email ends with "gov.gr"
                    if (email.endsWith("gov.gr")) {
                        // If it does, start the LoginEmployee activity
                        startActivity(new Intent(MainMenu.this, LoginEmployee.class));
                        Toast.makeText(MainMenu.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                    } else {
                        // If not, continue with the original flow
                        startActivity(new Intent(MainMenu.this, LoginUser.class));
                    }

                } else {
                    Toast.makeText(MainMenu.this, getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
                }
            });
        }


    ////////////////////////////// SIGN UP //////////////////////////////////
        Button button_sing_up = findViewById(R.id.button_sign_up);

        button_sing_up.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(MainMenu.this, Register.class);
            // Start the new activity
            startActivity(intent);
        });

    }

}
