package com.example.sch;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sch.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements OnBackButtonClickListener {
    ActivityMainBinding Binding;
    String user;
    TextView date;
    FirebaseUser currentUser;
int count =0;
    @Override
    public void onBackButtonClicked() {
        // Show the BottomNavigationView
        Binding.bottomnavigationview.setVisibility(View.VISIBLE);
        Binding.bottomnavigationview.getMenu().findItem(R.id.home).setChecked(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(Binding.getRoot());
        replaceFragment(new HomeFragment(),true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Binding.bottomnavigationview.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment(),true);


            } else if (itemId == R.id.profile) {

                showBottomDialog();



            } else if (itemId == R.id.doubt) {
                replaceFragment(new DoubtFragment(),true);
                Binding.bottomnavigationview.setVisibility(View.GONE);


            } else if (itemId == R.id.subjectbar) {
                replaceFragment(new SubjectFragment(),true);

            }

            return true;
        });

    }


    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager(); // Use the appropriate FragmentManager method based on your setup
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Frame, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }


    @SuppressLint("ResourceType")
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        TextView tittle = dialog.findViewById(R.id.createText);
        TextView name= dialog.findViewById(R.id.studentnameprofile);
        TextView subject = dialog.findViewById(R.id.subjectstudyprofile);
        Button addstudent = dialog.findViewById(R.id.addstudent);
        Button addcahnges = dialog.findViewById(R.id.addtricks);
        fetchProfile(name,subject);
        addstudent.setOnClickListener(view -> {
//            replaceFragment(new AddStudent(), true);
            Intent intent = new Intent(this, Add.class);
            startActivity(intent);
            dialog.dismiss();

        });
        if(user.equals("admin"))
        {
            tittle.setText("Admin");
            addstudent.setVisibility(View.VISIBLE);
            addcahnges.setVisibility(View.VISIBLE);
        }
        else if(user.equals("student"))
        {
            tittle.setText("Student");
            addstudent.setVisibility(View.INVISIBLE);
            addcahnges.setVisibility(View.INVISIBLE);

            LinearLayout ll = dialog.findViewById(R.id.bottomlayout);
            ll.getLayoutParams().height = 700;
            ll.requestLayout();
        }
        Button logout = dialog.findViewById(R.id.logout);
        logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            saveLoginStatus(false); // Clear the login status
            // Redirect the user to the login screen
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
            dialog.dismiss();
        });
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    @Override
    public void onBackPressed() {
        // Show a toast message when the hardware back button is pressed
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigationview);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            bottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);

        }

        // Call the super method to handle the back press event (which will likely navigate back)
        super.onBackPressed();
    }
    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    public void fetchProfile(TextView name, TextView subject) {
        if (currentUser != null) {
            String userId = currentUser.getUid();


            if(userId.equals("9pFmIAi6plS3qcEgvgF7KxaQOUi1"))
            {
                user="admin";
            }
            else{
                user ="student";
            }
            // Get the user reference for the current user
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("sch")
                    .child("students").child(userId);

            // Add a ValueEventListener to retrieve the user data
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Retrieve the name and subjects from the dataSnapshot
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    String userSubjects = dataSnapshot.child("subjects").getValue(String.class);

                    // Set the name and subjects in the respective TextViews
                    name.setText(userName);
                    subject.setText(userSubjects);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors or exceptions that may occur during the database operation
                }
            });
        }
    }

}