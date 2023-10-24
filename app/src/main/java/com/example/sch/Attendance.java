package com.example.sch;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class Attendance extends Fragment  {

    ArrayList<AttendanceModel> attendanceModels = new ArrayList<>();
    private DatabaseReference studentsRef;
    CardView attendancecard ;
            String subject;
            DatePicker datePicker;
    RecylerAdpterAttendance adapter;

    public static Attendance newInstance(String subject) {
        Attendance fragment = new Attendance();
        Bundle args = new Bundle();
        args.putString("subject", subject);
        fragment.setArguments(args);
        return fragment;
    }
    private String selectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
        RecyclerView recyclerView= rootView.findViewById(R.id.AttendanceList);
        ImageView backtbn = rootView.findViewById(R.id.backbtnattendace);
        TextView dateTextView = rootView.findViewById(R.id.currentdate);
        ImageView calendarImageView = rootView.findViewById(R.id.calender);
        TextView SubjectTittle= rootView.findViewById(R.id.attendancetittle);
        datePicker = rootView.findViewById(R.id.datePicker);
        subject = getArguments().getString("subject");
        SubjectTittle.setText(subject+" Attendance");
        dateTextView.setText(date());
        studentsRef = FirebaseDatabase.getInstance().getReference("sch").child("students");

        // Fetch students based on the selected subject from the database
        fetchStudentsForSubject(subject);



        // Set OnClickListener for the calendar icon (ImageView)




        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomnavigationview);
        backtbn.setOnClickListener(v -> {
            // Call the method to navigate back to the previous fragment
            goBackToPreviousFragment();
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));




         adapter = new RecylerAdpterAttendance(getContext(),attendanceModels,subject);
        recyclerView.setAdapter(adapter);

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }


        return rootView;
    }
    private void goBackToPreviousFragment() {
        FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() for nested fragments
        fragmentManager.popBackStack();

    }


    // Existing code...


    // Existing code...

    public String  date()
    {String currentDateStr=null;
        LocalDate currentDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
        }

// Create a string representation of the current date
        DateTimeFormatter formatter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDateStr = currentDate.format(formatter);
        }
        return currentDateStr;
    }

    private void fetchStudentsForSubject(String subject) {
        // Query the database to retrieve all students
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceModels.clear();

                // Loop through the students and add them to the attendanceModels list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Get the student data from the snapshot
                    String studentId = dataSnapshot.getKey();
                    String studentName = dataSnapshot.child("name").getValue(String.class);
                    String subjects = dataSnapshot.child("subjects").getValue(String.class);

                    // Check if the student has any subjects
                    if (subjects != null) {
                        // Split the subjects string into an array of subjects
                        String[] subjectArray = subjects.split(",");

                        // Loop through the subjects of the student
                        for (String studentSubject : subjectArray) {
                            // Trim the subject to remove any leading/trailing whitespaces
                            studentSubject = studentSubject.trim();

                            // Check if the student has the selected subject
                            if (studentSubject.equalsIgnoreCase(subject)) {
                                // Add the student to the attendanceModels list
                                attendanceModels.add(new AttendanceModel(studentName));
                                break; // Break out of the inner loop if the student has the selected subject
                            }
                        }
                    }
                }

                // Notify the adapter about the data change
                adapter.notifyDataSetChanged();

                // Check if the attendanceModels list is empty
                if (attendanceModels.isEmpty()) {
                    // Show a message to inform the user that no students were found for the selected subject
                    // For example, you can display a Toast message
                    Toast.makeText(getContext(), "No students found for " + subject, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if data retrieval is canceled
            }
        });
    }





}