package com.example.sch;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class HomeFragment extends Fragment {
    private HorizontalScrollView scrollView;
    private LinearLayout linearLayout;
    TextView mathAttendance;
    TextView chemistryAttendance;
    TextView physicsAttendance;
    TextView name;
    FirebaseUser currentUser;
    private long startTimeMillis;
    private long totalTimeMillis;
    private TextView timeTextView,date;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        scrollView = rootView.findViewById(R.id.horizontalScrollView);
        linearLayout = rootView.findViewById(R.id.scrollViewLinearLayout);
        mathAttendance = rootView.findViewById(R.id.attendancephysicscardmaintext);
        chemistryAttendance = rootView.findViewById(R.id.attendancechemistrycardMaintext);
        physicsAttendance    = rootView.findViewById(R.id.attendancemathscardmaintext);
        Button ChemistryChat= rootView.findViewById(R.id.attendancechemistrycardchatbtn);
        Button MathChat= rootView.findViewById(R.id.attendancemathscardchatbtn);
        Button PhysicsChat= rootView.findViewById(R.id.attendancephysicscardchatbtn);

        MathChat.setOnClickListener(v -> Buttonclick("math","Chat"));
        ChemistryChat.setOnClickListener(v -> Buttonclick("chemistry","Chat"));
        PhysicsChat.setOnClickListener(v -> Buttonclick("physics","Chat"));
        name = rootView.findViewById(R.id.userNameTextHome);
  date = rootView.findViewById(R.id.shortdescriptionTextHome);
  date.setText(date());
        timeTextView = rootView.findViewById(R.id.mobileused);

        // Get the current time in milliseconds when the app starts
        startTimeMillis = System.currentTimeMillis();

        // Start the timer to update the timeTextView continuously
        startTimer();

        // Get the attendance reference for the current user and the "Math" subject
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userData = FirebaseDatabase.getInstance().getReference("sch")
                    .child("students").child(userId);
            userData.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Get the value of the "name" field from the dataSnapshot
                    String studentName = dataSnapshot.child("name").getValue(String.class);
                    name.setText("Hi, " + studentName+"!");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors or exceptions that may occur during the database operation
                }
            });
        }

            fetchAttendance(mathAttendance,"Math");
            fetchAttendance(chemistryAttendance,"Chemistry");
            fetchAttendance(physicsAttendance,"Physics");

        return rootView;
    }
    public void fetchAttendance(TextView subjectAttendance, String subject) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String math = subject;

            // Get the attendance reference for the current user and the "Math" subject
            DatabaseReference userAttendanceRef = FirebaseDatabase.getInstance().getReference("sch")
                    .child("attendance").child(math).child(userId);

            // Add a ValueEventListener to retrieve the attendance data for the user
            userAttendanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Initialize a count for the total attendance (present) of the user
                    int totalAttendance = 0;

                    // Loop through the dates and attendance status for each date
                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey(); // Get the date from the snapshot key

                        // Get the attendance status for the specific date
                        String attendanceStatus = dateSnapshot.getValue(String.class);

                        // Check if the attendance status is "Present" for the specific date
                        if (attendanceStatus != null && attendanceStatus.equals("Present")) {
                            // Increment the total attendance count
                            totalAttendance++;
                        }
                    }

                    // Set the total attendance count in the TextView
                    subjectAttendance.setText(String.valueOf("Total attendance of " + subject + " " + totalAttendance));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors or exceptions that may occur during the database operation
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        centerSecondCard();
    }
    public String  date()
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Months are 0-based, so add 1
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

// Create a string representation of the current date
        String currentDate = dayOfMonth + "-" + month + "-" + year;
        return currentDate;
    }

    private void centerSecondCard() {
        // Calculate the width of the HorizontalScrollView
        int scrollViewWidth = scrollView.getWidth();

        // Calculate the width of the second card
        View secondCard = linearLayout.getChildAt(1);
        int secondCardWidth = secondCard.getWidth();

        // Calculate the initial scroll position to center the second card
        int initialScrollX = (secondCard.getLeft() + secondCard.getRight() - scrollViewWidth) / 2;

        // Set the initial scroll position
        scrollView.scrollTo(initialScrollX, 0);
    }

    private void startTimer() {
        // Create a new runnable to update the timeTextView
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                // Schedule the runnable to run again after 1 second
                timerHandler.postDelayed(this, 1000);
            }
        };

        // Start the first run of the runnable
        timerHandler.post(timerRunnable);
    }

    private void updateTimer() {
        // Get the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Calculate the total time spent by subtracting the start time from the current time
        totalTimeMillis = currentTimeMillis - startTimeMillis;

        // Convert total time to hours, minutes, and seconds (optional)
        long totalSeconds = totalTimeMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // Format the time and display it in the TextView
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeTextView.setText(timeString);
    }
    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    private void stopTimer() {
        // Remove the timer runnable callbacks to stop updating the timeTextView
        timerHandler.removeCallbacks(timerRunnable);
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager(); // Use getChildFragmentManager() for nested fragments
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homefrag, fragment); // Replace with the ID of the fragment container in subject_layout.xml
        fragmentTransaction.addToBackStack(null); // Optional: Add the fragment to the back stack
        fragmentTransaction.commit();
    }
    private void Buttonclick(String subject, String fragmentName) {
        // Create a new instance of the child fragment and pass the data using 'newInstance'
        Fragment childFragment;
        if (fragmentName.equals("Chat")) {
            childFragment = ChatFragment.newInstance(subject);
            replaceFragment(childFragment);
        } else {
            childFragment = Attendance.newInstance(subject);
            replaceFragment(childFragment);
        }


    }

}

