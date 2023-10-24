package com.example.sch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button loginBtn = findViewById(R.id.loginbutton);
        TextView title = findViewById(R.id.welcome);
        EditText username = findViewById(R.id.userinputText);
        EditText adminPininput= findViewById(R.id.adminpin);
        EditText passwordInput = findViewById(R.id.paaswordinputText);
        if (isLoggedIn()) {
            startMainActivity();
            return;
        }
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(view -> {
            String email = username.getText().toString() + "sarthak@developer.com";
            String password = passwordInput.getText().toString();
            String adminPin = adminPininput.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if(adminPin.equals("admin"))
                            {
                                saveUserStatus("admin");
                                saveLoginStatus(true);
                                startMainActivity();
                            }
                            else if(adminPin.equals(""))
                            {
                                saveLoginStatus(true);
                                startMainActivity();
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch adminSwitch = findViewById(R.id.switch1);
        EditText adminPinEditText = findViewById(R.id.adminpin);

        // Set OnClickListener for the Switch
        adminSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            title.setText(isChecked ? "Welcome Admin!" : "Welcome Students!");
            adminSwitch.setText(isChecked ? "Admin" : "Student");
            adminPinEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }
    private boolean isLoggedIn() {
        // Retrieve the login status from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void startMainActivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }
    private void saveUserStatus(String IsAdmin) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", IsAdmin);
        editor.apply();
    }

}