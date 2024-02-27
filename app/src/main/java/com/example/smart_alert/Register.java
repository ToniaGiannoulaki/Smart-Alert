package com.example.smart_alert;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private EditText editText_email, editText_password, editText_password_repeat;
    Button button_confirm;
    private Boolean passwordVisible, passwordVisibleRepeat;
    private TextWatcher logintextWatcher;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize the Booleans
        passwordVisible = false;
        passwordVisibleRepeat = false;

        editText_email = findViewById(R.id.editTextTextEmailAddressRegister);
        editText_password = findViewById(R.id.editTextTextPasswordRegister);
        editText_password_repeat = findViewById(R.id.editTextTextPasswordRegisterRepeat);
        button_confirm = findViewById(R.id.button_register);

        // Initialize the TextWatcher
        logintextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String emailInput = editText_email.getText().toString().trim();
                String passwordInput = editText_password.getText().toString().trim();
                String passwordInputRepeat = editText_password_repeat.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    editText_email.setError(getString(R.string.invalid_email));
                } else if (emailInput.endsWith("@gov.gr")) {
                    editText_email.setError(getString(R.string.restricted_email_domain));
                } else if (passwordInput.length() < 6) {
                    editText_password.setError(getString(R.string.password_length_error));
                } else if (!passwordInput.equals(passwordInputRepeat)) {
                    editText_password_repeat.setError(getString(R.string.passwords_do_not_match));
                } else {
                    /////////////////////////// SIGN UP AUTHENTICATION /////////////////////////////

                    button_confirm.setOnClickListener(v -> {
                        String email = editText_email.getText().toString().trim();
                        String password = editText_password.getText().toString().trim();

                        // Now proceed with the registration
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Registration successful in Firebase Authentication
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (firebaseUser != null) {
                                            String userId = firebaseUser.getUid(); // Get the user ID from the authentication
                                            UserStatistics newUser = new UserStatistics(email); // Create a new User object with initialized fields

                                            // Save the user data in Realtime Database
                                            FirebaseDatabase.getInstance().getReference("users") // Ensure this is the correct node as per your database
                                                    .child(userId)
                                                    .setValue(newUser)
                                                    .addOnCompleteListener(dbTask -> {
                                                        if (dbTask.isSuccessful()) {
                                                            // User data saved successfully
                                                            Toast.makeText(getApplicationContext(), getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                                                            // Redirect the user to the next activity or login page
                                                        } else {
                                                            // Handle failure to save user data in the Realtime Database
                                                            Toast.makeText(getApplicationContext(), getString(R.string.db_failure), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    } else {
                                        // Registration failed in Firebase Authentication
                                        Toast.makeText(getApplicationContext(), getString(R.string.registration_failure), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        };

        editText_email.addTextChangedListener(logintextWatcher);
        editText_password.addTextChangedListener(logintextWatcher);
        editText_password_repeat.addTextChangedListener(logintextWatcher);

        ////////////////////////////////// EXIT //////////////////////////////////

        // Find the ImageButton by its ID
        ImageButton back = findViewById(R.id.imageButton_back);

        // Set OnClickListener for the ImageButton
        back.setOnClickListener(v -> {
            // Create an Intent to start the new activity
            Intent intent = new Intent(Register.this, MainMenu.class);
            // Start the new activity
            startActivity(intent);
        });



        ////////////////////// PASSWORD VISIBILITY //////////////////////////////
        editText_password.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Define the drawable index for start (left) as 0
                final int DRAWABLE_START = 0;

                // Get the drawable and check if it is null
                Drawable startDrawable = editText_password.getCompoundDrawables()[DRAWABLE_START];
                if (startDrawable != null) {
                    // Check if the touch event is within the bounds of the drawable
                    // taking into account the start position of the EditText and drawable padding
                    if (motionEvent.getRawX() >= editText_password.getLeft()
                            && motionEvent.getRawX() <= (editText_password.getLeft()
                            + startDrawable.getBounds().width() + editText_password.getCompoundDrawablePadding())) {

                        // Toggle password visibility
                        int selection = editText_password.getSelectionEnd();
                        if (passwordVisible) {
                            // Hide password
                            editText_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_visibility_off_24, 0, 0, 0);
                            editText_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        } else {
                            // Show password
                            editText_password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_visibility_24, 0, 0, 0);
                            editText_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        editText_password.setSelection(selection);
                        return true;
                    }
                }
            }
            return false;
        });


        ////////////////////// PASSWORD VISIBILITY REPEAT//////////////////////////////
        editText_password_repeat.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Define the drawable index for start (left) as 0
                final int DRAWABLE_START = 0;

                // Get the drawable and check if it is null
                Drawable startDrawable = editText_password_repeat.getCompoundDrawables()[DRAWABLE_START];
                if (startDrawable != null) {
                    // Check if the touch event is within the bounds of the drawable
                    // taking into account the start position of the EditText and drawable padding
                    if (motionEvent.getRawX() >= editText_password_repeat.getLeft()
                            && motionEvent.getRawX() <= (editText_password_repeat.getLeft()
                            + startDrawable.getBounds().width() + editText_password_repeat.getCompoundDrawablePadding())) {

                        // Toggle password visibility
                        int selection = editText_password_repeat.getSelectionEnd();
                        if (passwordVisibleRepeat) {
                            // Hide password
                            editText_password_repeat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_visibility_off_24, 0, 0, 0);
                            editText_password_repeat.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisibleRepeat = false;
                        } else {
                            // Show password
                            editText_password_repeat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_visibility_24, 0, 0, 0);
                            editText_password_repeat.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisibleRepeat = true;
                        }
                        editText_password_repeat.setSelection(selection);
                        return true;
                    }
                }
            }
            return false;
        });
    }

}
