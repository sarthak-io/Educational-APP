package com.example.sch;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FullscreenImage extends Fragment implements View.OnTouchListener {
ImageView delBtn;
 ChatAdapter adapter;
    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
private  String subjects;
    public FullscreenImage() {
        // Required empty public constructor
    }
    private String ImageUris;
    public static FullscreenImage newInstance( String id,long uni,String sub) {
        FullscreenImage fragment = new FullscreenImage();
        Bundle args = new Bundle();
        args.putString("Id",id);
      args.putLong("timestamp",uni);
      args.putString("subjects",sub);

        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
       ImageView backbtn= rootView.findViewById(R.id.backbtnfullscreen);
        ImageView Fullscreenimg = rootView.findViewById(R.id.imageViewFullScreen);
   String id = getArguments().getString("Id");

   long uni = getArguments().getLong("timestamp");
        Fullscreenimg.setOnTouchListener(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String vis = user.getUid();
        adapter = new ChatAdapter();
        delBtn = rootView.findViewById(R.id.deleteimagebtn);
 if(vis.equals("9pFmIAi6plS3qcEgvgF7KxaQOUi1"))
 {
     delBtn.setVisibility(View.VISIBLE);
 }

     backbtn.setOnClickListener(view -> {
         goBackToPreviousFragment();
         CardView bottomNavigationView = getActivity().findViewById(R.id.searchbar);
         if (bottomNavigationView != null) {
             bottomNavigationView.setVisibility(View.VISIBLE);

         }
         CardView topNavigationView = getActivity().findViewById(R.id.topnavifullscreen);
         if(topNavigationView!= null)
         {
             topNavigationView.setVisibility(View.VISIBLE);
         }
     });


delBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        deleteMsg(uni);
        Fullscreenimg.setVisibility(View.GONE);
    }
});
        Uri imageUri = Uri.parse(id);
        Glide.with(this).load(imageUri).into(Fullscreenimg);
        return rootView;
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager(); // Use getChildFragmentManager() for nested fragments
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fullscreenimage, fragment); // Replace with the ID of the fragment container in subject_layout.xml
        fragmentTransaction.addToBackStack(null); // Optional: Add the fragment to the back stack
        fragmentTransaction.commit();
    }
    private void goBackToPreviousFragment() {
        FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() for nested fragments
        fragmentManager.popBackStack();

    }

    public void deleteMsg(long timeStamp) {

         subjects = getArguments().getString("subjects");
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("sch").child("chats").child(subjects);

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    long msgTimestamp = dataSnapshot.child("timestamp").getValue(Long.class);
                    if (msgTimestamp == timeStamp) {
                        // Use the reference to remove the child node from the database
                        dataSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // Child node removed successfully
                                    delBtn.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error if the child node couldn't be removed
                                });
                    }
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if the data retrieval is canceled
            }
        });
    }
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                matrix.set(view.getImageMatrix());
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }
}