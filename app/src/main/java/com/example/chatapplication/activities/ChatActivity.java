package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.ChatAdapter;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();
    }

    private void init(){
        chatMessages=new ArrayList<>();
        chatAdapter=new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),Constants.KEY_USER_ID
        );
        binding.idChatRecyclerView.setAdapter(chatAdapter);
        database=FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String,Object> message=new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,Constants.KEY_USER_ID);
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.idChatInputMsg.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        binding.idChatInputMsg.setText(null);
    }
    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo("senderId",Constants.KEY_USER_ID)
                .whereEqualTo("receiverId",receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo("senderId",receiverUser.id)
                .whereEqualTo("receiverId",Constants.KEY_USER_ID)
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener=(value, error)->{
        if(error!=null){
            return ;
        }
        if(value != null){
            int count=chatMessages.size();
            for(DocumentChange documentChange:value.getDocumentChanges()){
                if(documentChange.getType()==DocumentChange.Type.ADDED){
                ChatMessage chatMessage=new ChatMessage();
                chatMessage.senderID=documentChange.getDocument().getString("senderId");
                chatMessage.receiverID=documentChange.getDocument().getString("receiverId");
                chatMessage.message=documentChange.getDocument().getString("message");
                chatMessage.dateTime=getReadableDateTime(documentChange.getDocument().getDate("timestamp"));
                chatMessage.dateObject= documentChange.getDocument().getDate("timestamp");
                chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2)->obj1.dateObject.compareTo(obj2.dateObject));
            if(count==0){
                chatAdapter.notifyDataSetChanged();
            }else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                binding.idChatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.idChatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.idChatProgressBar.setVisibility(View.GONE);
    };



    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private void loadReceiverDetails(){
        receiverUser=(User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.idChatName.setText(receiverUser.name);
    }


    private void setListeners(){
        binding.idChatImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.idLayoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}