package com.example.sch;

import static android.R.layout.simple_dropdown_item_1line;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Add extends AppCompatActivity {
    EditText studentUsername;
    EditText studentPassword;
    MultiAutoCompleteTextView studentSubjects;
    EditText studentName;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference attendanceReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Button backToHome= findViewById(R.id.backtohome);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("sch").child("students");
        Button addstudent = findViewById(R.id.addstudentbtn);
        attendanceReference = FirebaseDatabase.getInstance().getReference("sch").child("attendance");
        studentName =findViewById(R.id.nameinputTextaddstudent);
        studentSubjects = findViewById(R.id.subjectmulti);
        studentUsername = findViewById(R.id.userinputTextaddstudent);
        studentPassword = findViewById(R.id.paaswordinputTextaddstudent);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, simple_dropdown_item_1line, getDropdownOptions());
        studentSubjects.setAdapter(adapter);
        studentSubjects.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        addstudent.setOnClickListener(view -> {
            registerUser();
        });
        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                finish();
                loginUser();

            }
        });
    }

    private String[] getDropdownOptions() {

        return new String[]{"Math", "Physics", "Chemistry","other"};
    }
    public void registerUser() {
        String name = studentName.getText().toString();
        String subjects = studentSubjects.getText().toString();
        String username = studentUsername.getText().toString() + "sarthak@developer.com";
        String password = studentPassword.getText().toString();

        // Create a new user with email (username) and password
        firebaseAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User registration successful
                        String userId = task.getResult().getUser().getUid();
                        // Now, register the student's data in the Realtime Database
                        registerStudentData(userId, name, subjects);
                        //        studentSubjects.setText("");

                    } else {
                        // Handle the error if user registration fails
                        if (task.getException() instanceof FirebaseAuthException) {
                            FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                            // Show error message
                            Toast.makeText(this, "Registration error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void registerStudentData(String userId, String name, String subjects) {
        // Create a Student object with the user details
        Student student = new Student(userId, name, subjects);

        // Save the Student object to the Realtime Database
        databaseReference.child(userId).setValue(student)
                .addOnCompleteListener(databaseTask -> {
                    if (databaseTask.isSuccessful()) {
                        // Student data saved successfully

                        // Now save the student's name under each selected subject in the "attendance" node
                        String[] selectedSubjectsArray = subjects.split(", ");
                        for (String subject : selectedSubjectsArray) {
                            saveStudentNameForSubject(userId, subject, name);
                        }
                        Toast.makeText(this, "Student added successfully.", Toast.LENGTH_SHORT).show();
                        studentUsername.setText("");
                        studentPassword.setText("");
                        studentName.setText("");
                    } else {
                        // Show error message
                        Toast.makeText(this, "Failed to add student.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveStudentNameForSubject(String studentId, String subject, String name) {
        // Save the student's name to the "attendance" node under the selected subject
        attendanceReference.child(subject).child(studentId).child("name").setValue(name)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Student name saved successfully for the subject
                    } else {
                        // Failed to save student name for the subject
                    }
                });
    }
    // Inside LoginActivity.java

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword("1sarthak@developer.com", "SURYA@123")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveLoginStatus(true);
                        startActivity(new Intent(this, MainActivity.class));

                    } else {
                        // Handle the error if login fails
                        if (task.getException() instanceof FirebaseAuthException) {
                            FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                            // Show error message
                            Toast.makeText(this, "Login error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }
    private boolean isLoggedIn() {
        // Retrieve the login status from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
}