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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.Objects;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    private String conversionId=null;
    private boolean isReceiverAvailable = false;

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
        if(conversionId != null){
            updateConversion(binding.idChatInputMsg.getText().toString());
        }
        else {
            HashMap<String,Object> conversion=new HashMap<>();
            conversion.put("senderId",Constants.KEY_USER_ID);
            conversion.put("senderName",Constants.KEY_NAME);
            conversion.put("senderImage",Constants.KEY_IMAGE);
            conversion.put("receiverId",receiverUser.id);
            conversion.put("receiverName",receiverUser.name);
            conversion.put("receiverImage",receiverUser.image);
            conversion.put("timestamp",new Date());
            addConversion(conversion);
        }
        binding.idChatInputMsg.setText(null);
    }

    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this,(value, error) -> {
                    if(error != null){
                        return;
                    }
                    if(value != null){
                        if(value.getLong("availability")!=null){
                            int availability = Objects.requireNonNull(value.getLong("availability"))
                                    .intValue();
                            isReceiverAvailable=availability==1;
                        }
                    }
                    if(isReceiverAvailable){
                        binding.idChatUserAvailability.setVisibility(View.VISIBLE);
                    }else{
                        binding.idChatUserAvailability.setVisibility(View.GONE);
                    }
                });
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
        if(conversionId==null){
            checkForConversion();
        }
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


    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId=documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update("lastMessage",message,"timestamp",new Date());

    }

    private void checkForConversion(){
        if(chatMessages.size()!=0){
            checkForConversionRemotely(Constants.KEY_USER_ID,receiverUser.id);
            checkForConversionRemotely(receiverUser.id,
                    Constants.KEY_USER_ID);
        }
    }

    private void checkForConversionRemotely(String senderID,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo("senderId",senderID)
                .whereEqualTo("receiverId",receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener=task->{
        if(task.isSuccessful()&&task.getResult()!=null&&task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
            conversionId=documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}