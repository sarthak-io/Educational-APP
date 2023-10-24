package com.example.sch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SubjectFragment extends Fragment {

    // Constants for gesture detection
    public SubjectFragment() {
        // Required empty public constructor
    }
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private float initialXPhysicsCard = 0;
    private float initialXChemistryCard = 0;

    private float initialXMathsCard =0;

    private GestureDetector gestureDetector;
    private CardView chemistryCard;
    private CardView crosChemistryCard;
    private CardView physicsCard;
    private CardView crosPhysicsCard;
    private CardView mathsCard;
    private CardView crosMathsCard;
    private CardView touchedCard;
    String user;

    FirebaseUser currentUser;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subject, container, false);
        Bundle data= new Bundle();
        data.putString("subject1","From Subject Frgament");
        getParentFragmentManager().setFragmentResult("SubjectFragment",data);
        chemistryCard =rootView.findViewById(R.id.chemistrycard);
        crosChemistryCard = rootView.findViewById(R.id.croschemistrycard);
        physicsCard =rootView.findViewById(R.id.physicscard);
        crosPhysicsCard = rootView.findViewById(R.id.crosphysicscard);
        mathsCard =rootView.findViewById(R.id.mathscard);
        crosMathsCard = rootView.findViewById(R.id.crosmathscard);
        // Find the CardView by its ID inside the inflated layout
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();


            if(userId.equals("9pFmIAi6plS3qcEgvgF7KxaQOUi1"))
            {
                user="admin";
            }
            else{
                user ="student";
            }}
        Button btnChatmath = rootView.findViewById(R.id.mathschatbtn);
        Button btnAttendancemath = rootView.findViewById(R.id.mathsattendancebtn);
        Button btnChatchemsitry = rootView.findViewById(R.id.chemistrychatbtn);
        Button btnAttendancechemistry = rootView.findViewById(R.id.chemsitryattendancebtn);
        Button btnChatphysics = rootView.findViewById(R.id.phsyicschat);
        Button btnAttendancephysics = rootView.findViewById(R.id.phsyicsattendancebtn);
         Button stuentPhysicchat = rootView.findViewById(R.id.phsyicschat1);
        Button stuentchemiscchat = rootView.findViewById(R.id.chemistrychatbtn1);
        Button stuentmathschat = rootView.findViewById(R.id.mathschatbtn1);
        btnChatmath.setOnClickListener(v -> Buttonclick("math","Chat"));
        stuentmathschat.setOnClickListener(v -> Buttonclick("math","Chat"));
        btnAttendancemath.setOnClickListener(v -> Buttonclick("Math","Attendance"));
        btnChatchemsitry.setOnClickListener(v -> Buttonclick("chemistry","Chat"));
        stuentchemiscchat.setOnClickListener(v -> Buttonclick("chemistry","Chat"));
        btnAttendancechemistry.setOnClickListener(v -> Buttonclick("Chemistry","Attendance"));
        btnChatphysics.setOnClickListener(v -> Buttonclick("physics","Chat"));
        stuentPhysicchat.setOnClickListener(v -> Buttonclick("physics","Chat"));
        btnAttendancephysics.setOnClickListener(v -> Buttonclick("Physics","Attendance"));
        // Check if the 'user' variable is not null before using it
if(user.equals("admin"))
{
    btnAttendancechemistry.setVisibility(View.VISIBLE);
    btnAttendancemath.setVisibility(View.VISIBLE);
    btnAttendancephysics.setVisibility(View.VISIBLE);
} else if (user.equals("student")) {
    btnAttendancechemistry.setVisibility(View.GONE  );
    btnAttendancemath.setVisibility(View.GONE);
    btnAttendancephysics.setVisibility(View.GONE);
    crosPhysicsCard.setVisibility(View.GONE);
    crosMathsCard.setVisibility(View.GONE);
    crosChemistryCard.setVisibility(View.GONE);
}


        return rootView;
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager(); // Use getChildFragmentManager() for nested fragments
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.subjectfrag, fragment); // Replace with the ID of the fragment container in subject_layout.xml
        fragmentTransaction.addToBackStack(null); // Optional: Add the fragment to the back stack
        fragmentTransaction.commit();
    }

public  void AnimationCard(CardView crosCard,CardView subjectCard)
{
    int crosCardWidth = crosCard.getWidth();
    int subjectCardWidth = subjectCard.getWidth();

    // Calculate the maximum distance to animate the upper card
    float maxTranslationX = -subjectCardWidth;

    // Animate the translation of the upper card and its position
    ViewPropertyAnimator animator = subjectCard.animate()
            .translationX(maxTranslationX)
            .setDuration(500); // Adjust the duration as needed

    animator.setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            // Set the visibility of the upper card to INVISIBLE when the animation ends
            subjectCard.setVisibility(View.INVISIBLE);

            // Reset the position of the upper card so that it starts from the initial position next time
            subjectCard.setX(0f);
        }
    });

    animator.start();
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
