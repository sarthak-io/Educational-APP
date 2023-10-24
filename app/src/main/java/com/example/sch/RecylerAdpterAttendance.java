package com.example.sch;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecylerAdpterAttendance extends RecyclerView.Adapter<RecylerAdpterAttendance.ViewHolder> {
    Context context;
    ArrayList<AttendanceModel> arrattendace;
    String subject;
    String currentDate ;

    RecylerAdpterAttendance(Context context, ArrayList<AttendanceModel> arrattendance, String subject) {
        this.context = context;
        this.arrattendace = arrattendance;
        this.subject = subject;



    }

    // Define a variable to keep track of the click count
    private final HashMap<Integer, Integer> clickCountMap = new HashMap<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.attendance_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(arrattendace.get(position).name);

        // Check the click count for the current position
//        getAttendanceStatus(arrattendace.get(position).name,holder.userCard,holder.name);
        Integer clickCount = clickCountMap.get(position);
        if (clickCount == null) {
            // If no click count found, set the default color
            holder.userCard.setCardBackgroundColor(context.getResources().getColor(R.color.default2));
            clickCountMap.put(position, 0);
        } else {
            // Toggle the color based on the click count
            int backgroundColor;
            int textcolor;
            switch (clickCount % 3) {
                case 0:
                    backgroundColor = context.getResources().getColor(R.color.default2);
                    textcolor= context.getResources().getColor(R.color.black);
                    break;
                case 1:
                    backgroundColor = context.getResources().getColor(R.color.green);
                    textcolor= context.getResources().getColor(R.color.white);

                    break;
                case 2:
                    backgroundColor = context.getResources().getColor(R.color.red);
                    textcolor= context.getResources().getColor(R.color.white);

                    break;
                default:
                    backgroundColor = context.getResources().getColor(R.color.default2);
                    textcolor= context.getResources().getColor(R.color.black);
                    break;
            }
            holder.userCard.setCardBackgroundColor(backgroundColor);
            holder.name.setTextColor(textcolor);
        }

        // Set OnClickListener for the card
        holder.userCard.setOnClickListener(view -> {
            // Increment the click count for the current position
            int currentClickCount = clickCountMap.get(position);
            clickCountMap.put(position, currentClickCount + 1);

            // Update the color based on the new click count
            int backgroundColor;
            int textcolor;
            switch ((currentClickCount + 1) % 3) {
                case 1:
                    backgroundColor = context.getResources().getColor(R.color.green);
                    textcolor= context.getResources().getColor(R.color.white);
                    updateAttendanceStatus(arrattendace.get(position).name, "Present");
                    break;
                case 2:
                    backgroundColor = context.getResources().getColor(R.color.red);
                    textcolor= context.getResources().getColor(R.color.white);
                    updateAttendanceStatus(arrattendace.get(position).name, "Absent");
                    break;
                case 3:
                    backgroundColor = context.getResources().getColor(R.color.default2);
                    textcolor= context.getResources().getColor(R.color.black);
                    updateAttendanceStatus(arrattendace.get(position).name, "Not Marked");
                    break;
                default:
                    backgroundColor = context.getResources().getColor(R.color.default2);
                    textcolor= context.getResources().getColor(R.color.black);
                    break;
            }
            holder.userCard.setCardBackgroundColor(backgroundColor);
            holder.name.setTextColor(textcolor);
        });
    }

    private void updateAttendanceStatus(String studentName, String status) {
        if(subject==null)
        {
            return;
        }
//
        DatabaseReference subjectsRef = FirebaseDatabase.getInstance().getReference("sch").child("attendance").child(subject);
        // Query the "subject" node to get the list of user ids for a specific subject
        subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String studentNameFromDatabase = userSnapshot.child("name").getValue(String.class);
                     currentDate= date();
                    // Compare the fetched student name with the name you want to update the attendance for
                    if (studentName.equals(studentNameFromDatabase)) {
                        // If names match, update the attendance status for that student on the specific date
                        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("sch")
                                .child("attendance").child(subject).child(userId);
                        // Get the current date
                        attendanceRef.child(currentDate).setValue(status)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Attendance status updated successfully
                                        // You can add any additional logic or display a message here
                                    } else {
                                        // Failed to update attendance status
                                        // You can add error handling here if needed
                                    }
                                });
                        break; // Exit the loop since we have found the matching student
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors or exceptions that may occur during the database operation
            }
        });
    }


//public void getAttendanceStatus(String studentName, CardView card,TextView name)
//{
//    if(subject==null)
//    {
//        return;
//    }
//    int year = datePicker.getYear();
//    int month = datePicker.getMonth() + 1; // Month is zero-based, so add 1
//    int day = datePicker.getDayOfMonth();
//    currentDate= String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
//    DatabaseReference subjectsRef = FirebaseDatabase.getInstance().getReference("sch").child("attendance").child(subject);
//    // Query the "subject" node to get the list of user ids for a specific subject
//    subjectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                String userId = userSnapshot.getKey();
//                String studentNameFromDatabase = userSnapshot.child("name").getValue(String.class);
//
//                // Compare the fetched student name with the name you want to update the attendance for
//                if (studentName.equals(studentNameFromDatabase)) {
//                  DatabaseReference status = FirebaseDatabase.getInstance().getReference("sch").child("attendance").child(subject).child(userId);
//                    for (DataSnapshot dates : dataSnapshot.getChildren()) {
//
//                         if(dates.equals(currentDate));
//                        {
//                            String date = dates.getValue().toString();
//                            if(date.equals("Present")){
//                                int backgroundColor,textcolor;
//                                backgroundColor = context.getResources().getColor(R.color.green);
//                                textcolor= context.getResources().getColor(R.color.white);
//                                card.setCardBackgroundColor(backgroundColor);
//                                name.setTextColor(textcolor);
//                            } else{
//                            int backgroundColor,textcolor;
//                            backgroundColor = context.getResources().getColor(R.color.red);
//                                textcolor= context.getResources().getColor(R.color.white);
//                                name.setTextColor(textcolor);
//                            card.setCardBackgroundColor(backgroundColor);
//                        }
//                        }
//                    }
//
//
//                    break; // Exit the loop since we have found the matching student
//                }
//            }
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError databaseError) {
//            // Handle any errors or exceptions that may occur during the database operation
//        }
//    });
//
//}
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

    @Override
    public int getItemCount() {
        return arrattendace.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CardView userCard;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nametextattendance);
            userCard = itemView.findViewById(R.id.attendanceListcard);
        }
    }
}
