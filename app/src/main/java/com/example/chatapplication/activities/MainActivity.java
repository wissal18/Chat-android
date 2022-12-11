package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.RecentConversationsAdapter;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.listeners.ConversionListener;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Base64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        System.out.println("here3"+Constants.KEY_IMAGE);
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();

    }

    private void init(){
        conversations=new ArrayList<>();
        conversationsAdapter=new RecentConversationsAdapter(conversations,this);
        binding.idConversationRecycleView.setAdapter(conversationsAdapter);
        database=FirebaseFirestore.getInstance();
    }

    public void setListeners(){
        binding.idLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        binding.idMainFabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),UsersActivity.class));
            }
        });
    }

    public void loadUserDetails(){
        binding.idMainName.setText(Constants.KEY_NAME);

       // byte[] bytes= Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));Check this one

        byte[] bytes = Base64.decode(Constants.KEY_IMAGE,Base64.DEFAULT);
        System.out.println("here4"+Constants.KEY_IMAGE);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.idMainImageProfile.setImageBitmap(bitmap);
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }


    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo("senderId",Constants.KEY_USER_ID)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo("receivedId",Constants.KEY_USER_ID)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener=(value, error)->{
        if(error != null){
            return;
        }if(value != null){
            for(DocumentChange documentChange:value.getDocumentChanges()){
                if(documentChange.getType()==DocumentChange.Type.ADDED){
                    String senderId=documentChange.getDocument().getString("senderId");
                    String receiverId=documentChange.getDocument().getString("receiverId");
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.senderID=senderId;
                    chatMessage.receiverID=receiverId;
                    if (Constants.KEY_USER_ID.equals(senderId)){
                        chatMessage.conversionImage=documentChange.getDocument().getString("receiverImage");
                        chatMessage.conversionName=documentChange.getDocument().getString("receiverName");
                        chatMessage.conversionId=documentChange.getDocument().getString("receiverId");
                    }else {
                        chatMessage.conversionImage=documentChange.getDocument().getString("senderImage");
                        chatMessage.conversionName=documentChange.getDocument().getString("senderName");
                        chatMessage.conversionId=documentChange.getDocument().getString("senderId");
                    }
                    chatMessage.message=documentChange.getDocument().getString("lastMessage");
                    chatMessage.dateObject=documentChange.getDocument().getDate("timestamp");
                    conversations.add(chatMessage);
                }else if(documentChange.getType()==DocumentChange.Type.MODIFIED){
                    for(int i=0;i<conversations.size();i++){
                        String senderId=documentChange.getDocument().getString("senderId");
                        String receiverId=documentChange.getDocument().getString("receiverId");
                        if(conversations.get(i).senderID.equals(senderId) && conversations.get(i).receiverID.equals(receiverId))
                        {
                            conversations.get(i).message=documentChange.getDocument().getString("lastMessage");
                            conversations.get(i).dateObject=documentChange.getDocument().getDate("timestamp");
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1,obj2)->obj2.dateObject.compareTo(obj1.dateObject));

            conversationsAdapter.notifyDataSetChanged();
            binding.idConversationRecycleView.smoothScrollToPosition(0);
            binding.idConversationRecycleView.setVisibility(View.VISIBLE);
            binding.idConversationsProgressBar.setVisibility(View.GONE);

        }

    };

    public void updateToken(String token){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(Constants.KEY_USER_ID);
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> showToast("Unable to update token"));


    }

    private  void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);

    }
    private void signOut(){
        showToast("Signing out....");
        Constants.KEY_IS_SIGNED_IN=false;
        FirebaseFirestore database =FirebaseFirestore.getInstance() ;
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(Constants.KEY_USER_ID);
        HashMap<String,Object> updates =new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused ->{
                    // preferenceManager.clear();
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                finish();
                })
                .addOnFailureListener(e->showToast("Unable to sign out"));
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra("user",user);
        startActivity(intent);
    }
}