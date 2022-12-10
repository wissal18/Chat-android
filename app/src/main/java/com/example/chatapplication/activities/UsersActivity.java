package com.example.chatapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatapplication.R;
import com.example.chatapplication.adapters.UsersAdapter;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.databinding.ActivityUsersBinding;
import com.example.chatapplication.listeners.UserListener;
import com.example.chatapplication.models.User;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {
private  ActivityUsersBinding binding;
private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getUsers();
    }
    private void setListeners(){
        binding.idImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task->{
                    loading(false);
                    String currentUserId=Constants.KEY_USER_ID;
                    if(task.isSuccessful() && task.getResult()!=null){
                        List<User> users=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.name=queryDocumentSnapshot.getString("name");
                            user.email=queryDocumentSnapshot.getString("email");

                            user.image=queryDocumentSnapshot.getString("image");
                            user.token=queryDocumentSnapshot.getString("fcmToken");
                            user.id=queryDocumentSnapshot.getId();
                            users.add(user);

                        }
                        if(users.size()>0){
                            UsersAdapter usersAdapter=new UsersAdapter(users,this);
                            binding.idUsersRecyclerView.setAdapter(usersAdapter);
                            binding.idUsersRecyclerView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    }else{
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage(){
        binding.idUsersErrorMessage.setText(String.format("%s","No user available"));
        binding.idUsersErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.idUsersProgressBar.setVisibility(View.VISIBLE);
        }else {
            binding.idUsersProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}