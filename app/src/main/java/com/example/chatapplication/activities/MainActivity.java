package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Base64;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        System.out.println("here3"+Constants.KEY_IMAGE);
        loadUserDetails();
        getToken();
        setListeners();
    }

    public void setListeners(){
        binding.idLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    public void loadUserDetails(){
        binding.idMainName.setText(preferenceManager.getString(Constants.KEY_NAME));

       // byte[] bytes= Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));Check this one

        byte[] bytes = Base64.decode(Constants.KEY_IMAGE,Base64.DEFAULT);
        System.out.println("here4"+Constants.KEY_IMAGE);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.idMainImageProfile.setImageBitmap(bitmap);
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
    public void updateToken(String token){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS).document(Constants.KEY_USER_ID);
        documentReference.update(Constants.KEY_FCM_TOKEN,token).addOnSuccessListener(umused -> showToast("Token updated successfully"))
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
}