package com.example.sch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements onItemClickListner,onItemHoldListener{

    private static final int PICK_IMAGE_REQUEST = 1;
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private String ImageUris;
    private RecyclerView recyclerView;
    private EditText sendTextInput;
    private ImageView imageSendBtn,sendbtn,delBtn;
    private Uri selectedImageUri;
    private ChatAdapter chatAdapter;
    ProgressBar imageProgressBar;
    ImageView cancelbtn;
    String user;
CardView msgcard;
    FirebaseUser currentUser;
    public static ChatFragment newInstance(String subject) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("subject", subject);
        fragment.setArguments(args);
        return fragment;
    }

    // ActivityResultLauncher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                selectedImageUri = data.getData();
                                ImageUris=String.valueOf(selectedImageUri);
                                // Display the selected image in the ImageView (imagesendbtn)

                                Glide.with(this).load(selectedImageUri).into(imageSendBtn);
                            }
                        }
                    });

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ImageView backButton = rootView.findViewById(R.id.backbtnchat);
         imageProgressBar = rootView.findViewById(R.id.imageProgressBar);
        TextView SubjectTittle= rootView.findViewById(R.id.SubjectTittleChat);
        String subject = getArguments().getString("subject");
        SubjectTittle.setText(subject);
        delBtn=rootView.findViewById(R.id.deletechatbtn);
CardView readmode = rootView.findViewById(R.id.readmode);
 cancelbtn = rootView.findViewById(R.id.cancelbtn);
  cancelbtn.setOnClickListener(view -> {
            delBtn.setVisibility(View.GONE);
            cancelbtn.setVisibility(View.GONE);

        });
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
        if(user.equals("admin"))
        {
            readmode.setVisibility(View.GONE);
        } else if (user.equals("student")) {
            readmode.setVisibility(View.VISIBLE);
        }
        DatabaseReference chatsRefview = FirebaseDatabase.getInstance().getReference("sch").child("chats").child(subject);

        // Add a ValueEventListener to listen for changes in the chat data
        chatsRefview.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessageList.clear();

                // Loop through the chat messages and add them to the list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    chatMessageList.add(chatMessage);
                }

                // Notify the adapter that the data has changed
                chatAdapter.notifyDataSetChanged();
                // Scroll to the last item in the RecyclerView (optional)
                recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if data retrieval is canceled
            }
        });



        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomnavigationview);


        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);


        }   backButton.setOnClickListener(v -> {
            // Call the method to navigate back to the previous fragment
            goBackToPreviousFragment();
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);

            }
        });


        

        recyclerView = rootView.findViewById(R.id.chatlist);
        sendTextInput = rootView.findViewById(R.id.sendtextinput);
        imageSendBtn = rootView.findViewById(R.id.imagesendbtn);
        sendbtn= rootView.findViewById(R.id.sendchatbtn);

        // Initialize RecyclerView and ChatAdapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatAdapter = new ChatAdapter(getContext(),chatMessageList,this::onItemClick ,this::onItemHold);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        rootView.findViewById(R.id.sendchatbtn).setOnClickListener(v -> {
            String messageText = sendTextInput.getText().toString().trim();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String senderUID = currentUser.getUid();
try {
    if (!messageText.isEmpty()) {
// Get the current authenticated user's UID


        if (currentUser != null) {


            if (selectedImageUri != null) {
                // Image is selected, upload it to Firebase Storage
                uploadImageToFirebaseStorage(senderUID, messageText);
            } else {
                // No image selected, save text message to the Realtime Database
                saveTextMessageToFirebaseDatabase(senderUID, messageText);
            }
        }




    } else if (selectedImageUri != null) {
        uploadImageToFirebaseStorage(senderUID, messageText);

        // Hide the imageSendBtn and display the selected image
        imageSendBtn.setVisibility(View.GONE);
        Glide.with(this).load(selectedImageUri).into(imageSendBtn);

        // Clear the selectedImageUri to prepare for the next image selection
        selectedImageUri = null;

        // After a delay of 2000ms (2 seconds), revert the imageSendBtn to its original state
        rootView.postDelayed(() -> {
            // Show the imageSendBtn again with the original drawable
            imageSendBtn.setVisibility(View.VISIBLE);
            imageSendBtn.setImageResource(R.drawable.imagebtn);
        }, 2000);
    }
} catch (Exception e) {
                e.printStackTrace();
            }
            sendTextInput.setText("");
        });

        imageSendBtn.setOnClickListener(v -> {
            // Open the gallery using an Intent to select an image
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        return rootView;
    }
    private void goBackToPreviousFragment() {
        FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() for nested fragments
        fragmentManager.popBackStack();

    }



    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager(); // Use getChildFragmentManager() for nested fragments
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.chatfrgament, fragment); // Replace with the ID of the fragment container in subject_layout.xml
        fragmentTransaction.addToBackStack(null); // Optional: Add the fragment to the back stack
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(ChatMessage image) {

        CardView bottomNavigationView = getActivity().findViewById(R.id.searchbar);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);

        }
        CardView topNavigationView = getActivity().findViewById(R.id.topnavifullscreen);
        if (topNavigationView != null) {
            topNavigationView.setVisibility(View.GONE);

        }
        String subject = getArguments().getString("subject");
        Fragment fullscreenImageFragment = FullscreenImage.newInstance(image.getContent(),image.getTimestamp(),subject);
        replaceFragment(fullscreenImageFragment);
    }

    @Override
    public void onItemHold(ChatMessage item) {

     delBtn.setVisibility(View.VISIBLE);
        cancelbtn.setVisibility(View.VISIBLE);
     delBtn.setOnClickListener(view -> {
    deleteMsg(item.getTimestamp());
     });
    }

    private void uploadImageToFirebaseStorage(String senderUID, String messageText) {
        // Get a reference to the Firebase Storage and create a unique image filename

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageFileName = System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child("images/" + imageFileName);

        // Upload the image to Firebase Storage
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL

                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save the image URI and other chat details to the Realtime Database
                        saveImageMessageToFirebaseDatabase(senderUID, messageText, uri.toString());
                    }).addOnFailureListener(e -> {
                        // Handle the error if getting the image URI fails

                    });
                })
                .addOnFailureListener(e -> {
                    // Handle the error if the image upload fails
                });
    }

    private void saveImageMessageToFirebaseDatabase(String senderUID, String messageText, String imageUri) {
        String subject = getArguments().getString("subject");
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("sch").child("chats").child(subject);
        String chatId = chatsRef.push().getKey();

        ChatMessage imageMessage = new ChatMessage(ChatMessage.MessageType.IMAGE, imageUri, System.currentTimeMillis(), senderUID);
        chatsRef.child(chatId).setValue(imageMessage)
                .addOnSuccessListener(aVoid -> {
                    // Chat message saved successfully
                })
                .addOnFailureListener(e -> {
                    // Handle the error if the chat message couldn't be saved
                });
    }

    private void saveTextMessageToFirebaseDatabase(String senderUID, String messageText) {
        String subject = getArguments().getString("subject");
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("sch").child("chats").child(subject);
        String chatId = chatsRef.push().getKey();

        ChatMessage textMessage = new ChatMessage(ChatMessage.MessageType.TEXT, messageText, System.currentTimeMillis(), senderUID);
        chatsRef.child(chatId).setValue(textMessage)
                .addOnSuccessListener(aVoid -> {
                    // Chat message saved successfully
                })
                .addOnFailureListener(e -> {
                    // Handle the error if the chat message couldn't be saved
                });
    }
    public void deleteMsg(long timeStamp) {

        String subject = getArguments().getString("subject");
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("sch").child("chats").child(subject);

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
                                    cancelbtn.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error if the child node couldn't be removed
                                });
                    }
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if the data retrieval is canceled
            }
        });
    }

}
