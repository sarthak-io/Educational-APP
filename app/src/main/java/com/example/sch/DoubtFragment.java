package com.example.sch;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DoubtFragment extends Fragment {
    RecyclerView recyclerView;
    TextView welcomeTextView , warning;
    EditText messageEditText;
    ImageView sendButton ,backbtn;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    int currentCount;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_doubt, container, false);
        messageList = new ArrayList<>();
     backbtn= rootView.findViewById(R.id.backtomain);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        welcomeTextView =rootView.findViewById(R.id.welcome_text);
        messageEditText = rootView.findViewById(R.id.message_edit_text);
        sendButton = rootView.findViewById(R.id.send_btn);
  warning = rootView.findViewById(R.id.warning);
        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);




        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            if (!question.isEmpty()) {
                addToChat(question, Message.SENT_BY_ME);
                welcomeTextView.setVisibility(View.GONE);
                messageEditText.setText("");
                callAPI(question);
            }
        });








        backbtn.setOnClickListener(view -> {
            goBackToPreviousFragment();

            // Check if the parent activity is implementing the interface
            if (getActivity() instanceof OnBackButtonClickListener) {
                // Call the interface method to show the BottomNavigationView

                ((OnBackButtonClickListener) getActivity()).onBackButtonClicked();
            }
        });

        return rootView;
    }
        private void goBackToPreviousFragment() {
            FragmentManager fragmentManager = getParentFragmentManager(); // Use getParentFragmentManager() for nested fragments
            fragmentManager.popBackStack();

        }
    void addToChat(String message, String sentBy) {
        // Access the parent Activity using getActivity()
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageList.add(new Message(message, sentBy));
                    messageAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                }
            });
        }
    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat("According to SCH , "+response,Message.SENT_BY_BOT);
    }

    void callAPI(String question) {
        //okhttp
        // Retrieve the current date
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance().format(calendar.getTime());

        SharedPreferences countPrefs = getActivity().getSharedPreferences("MessageCountPrefs", Context.MODE_PRIVATE);
        int currentCount = countPrefs.getInt(currentDate, 0);
        String lastStoredDate = countPrefs.getString("lastStoredDate", "");

        if (!currentDate.equals(lastStoredDate)) {
            // If the current date is different from the last stored date, reset the count to 0
            currentCount = 0;

            // Update the last stored date in SharedPreferences
            SharedPreferences.Editor editor = countPrefs.edit();
            editor.putString("lastStoredDate", currentDate);
            editor.apply();
        }
        if (currentCount < 3) {
            messageList.add(new Message("Typing... ", Message.SENT_BY_BOT));

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("model", "text-davinci-003");
                jsonBody.put("prompt", question);
                jsonBody.put("max_tokens", 4000);
                jsonBody.put("temperature", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/completions")
                    .header("Authorization", "Bearer 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")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    addResponse("Failed to load response due to " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            JSONArray jsonArray = jsonObject.getJSONArray("choices");
                            String result = jsonArray.getJSONObject(0).getString("text");
                            addResponse(result.trim());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {
                        addResponse("Failed to load response due to " + response.body().toString());
                    }
                }
            });
            currentCount++;
            SharedPreferences.Editor editor = countPrefs.edit();
            editor.putInt(currentDate, currentCount);
            editor.apply();
        } else {
           recyclerView.setVisibility(View.GONE);
           welcomeTextView.setVisibility(View.GONE);
           warning.setText("Oops!! daily limit exceed");
        }




    }




}
