package com.example.chatapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivitySignInBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {
private ActivitySignInBinding binding;
private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(Constants.KEY_IS_SIGNED_IN){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
startActivity(intent);
finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

   setListeners();
    }
private void setListeners() {
    binding.idLoginSignup.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            System.out.println("1111111");
            startActivity(new Intent(SignInActivity.this,
                    SignUpActivity.class));
        }
    });

    binding.idLoginBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
if(isValidSignInDetails()){
    signIn();
}
        }
    });
}

private void showToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails(){
       if(binding.idLoginEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.idLoginEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }
        else if(binding.idLoginPwd.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }

       else{
            return true;
        }
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.idLoginBtn.setVisibility(View.INVISIBLE);
            binding.idLoginProgressbar.setVisibility(View.VISIBLE);
        }else {
            binding.idLoginProgressbar.setVisibility(View.INVISIBLE);
            binding.idLoginBtn.setVisibility(View.VISIBLE);
        }
    }

    private void signIn(){
loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.idLoginEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.idLoginPwd.getText().toString())
                .get()
                .addOnCompleteListener(task ->{
                    if(task.isSuccessful()&& task.getResult()!=null && task.getResult().getDocuments().size()>0){
                        System.out.println("okkk");
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        System.out.println("555"+documentSnapshot);
//                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
//                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        Constants.KEY_NAME=documentSnapshot.get("name").toString();
                        Constants.KEY_IMAGE=documentSnapshot.get("image").toString();
                        Constants.KEY_USER_ID=documentSnapshot.getId();
                        Constants.KEY_IS_SIGNED_IN=true;
                        //preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.get("name").toString());
                        //preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        System.out.println("here1"+documentSnapshot.get("name").toString());
                        System.out.println("hereeeeee ******** "+Constants.KEY_USER_ID+"   "+Constants.KEY_IMAGE+"   "+Constants.KEY_NAME+"   ");
                    }else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
  }
}




//    private void addDataToFirestore(){
//        FirebaseFirestore database=FirebaseFirestore.getInstance();
//        HashMap<String,Object> data =new HashMap<>();
//        data.put("firstName","Wissal");
//        data.put("lastName","Lamouchi");
//        database.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(),"Data inserted",Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(exception ->{
//                    Toast.makeText(getApplicationContext(),exception.getMessage(),Toast.LENGTH_SHORT).show();
//                });
//
//    }