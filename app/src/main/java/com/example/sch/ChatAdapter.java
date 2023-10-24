package com.example.sch;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessageList;
    private onItemClickListner onItemClickListener;

    Context context;
    private onItemHoldListener onItemHoldListener;
    public ChatAdapter( Context context,List<ChatMessage> chatMessageList,onItemClickListner onItemClickListener,onItemHoldListener onItemHoldListener) {
        this.chatMessageList = chatMessageList;
        this.onItemClickListener=onItemClickListener;
        this.onItemHoldListener=onItemHoldListener;
        this.context=context;
    }
public  ChatAdapter()
{

}


    @Override
    public int getItemViewType(int position) {
        // Return the view type (0 for text, 1 for image) based on the message type
        return chatMessageList.get(position).getMessageType() == ChatMessage.MessageType.TEXT ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create and return the appropriate view holder based on the view type
        if (viewType == 0) {
            View textMessageView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_text_message, parent, false);
            return new TextMessageViewHolder(textMessageView);
        } else {
            View imageMessageView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image_message, parent, false);
            return new ImageMessageViewHolder(imageMessageView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessageList.get(position);

        if (holder instanceof TextMessageViewHolder) {
            // Bind text message to the TextMessageViewHolder
            TextMessageViewHolder textMessageViewHolder = (TextMessageViewHolder) holder;
            textMessageViewHolder.textMessageTextView.setText(chatMessage.getContent());

        } else if (holder instanceof ImageMessageViewHolder) {
            // Bind image message to the ImageMessageViewHolder
            ImageMessageViewHolder imageMessageViewHolder = (ImageMessageViewHolder) holder;
            Glide.with(imageMessageViewHolder.itemView)
                    .load(chatMessage.getContent()) // Assuming you have an image URL or resource ID here
                    .into(imageMessageViewHolder.imageMessageImageView);

        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    // ViewHolder for text messages
    private  class TextMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessageTextView;
        CardView msgcard;

        TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String id = user.getUid();

            textMessageTextView = itemView.findViewById(R.id.textMessageTextView);
msgcard=itemView.findViewById(R.id.msgcard);
            // Detect long-press action here
            itemView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && id.equals("9pFmIAi6plS3qcEgvgF7KxaQOUi1")) {



                            onItemHoldListener.onItemHold(chatMessageList.get(position));
                        }
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return false;
                }
            });
        }
    }


    // ViewHolder for image messages
    private class ImageMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMessageImageView;


        ImageMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String id = user.getUid();
            imageMessageImageView = itemView.findViewById(R.id.imageMessageImageView);
//            deltext= itemView.findViewById(R.id.deletingtext);
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    int position = getAdapterPosition();
//                    if (position != RecyclerView.NO_POSITION && id.equals("9pFmIAi6plS3qcEgvgF7KxaQOUi1")) {
//                        deltext.setVisibility(View.VISIBLE);
//                        imageMessageImageView.setVisibility(View.GONE);
//
//                        onItemHoldListener.onItemHold(chatMessageList.get(position));
//                        return true; // Consume the long press event
//                    }
//                    return false;
//                }
//            });


            imageMessageImageView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(chatMessageList.get(position));
                }
            });
        }
    }



}
