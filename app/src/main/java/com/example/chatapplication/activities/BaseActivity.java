package com.example.chatapplication.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapplication.R;
import com.example.chatapplication.utilities.Constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        documentReference=database.collection(Constants.KEY_COLLECTION_USERS)
                .document(Constants.KEY_USER_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update("availability",0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update("availability",1);
    }
}