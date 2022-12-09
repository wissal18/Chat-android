package com.example.chatapplication.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatapplication.databinding.ActivitySignUpBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
private ActivitySignUpBinding binding;
private PreferenceManager preferenceManager;
private String encodeImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setListeners();

    }

    private void setListeners(){
        binding.idSignupLogin.setOnClickListener(v -> onBackPressed());
        binding.idSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidSignUpDetails()){
                    signup();
                }
            }
        });

        binding.idSignupLayoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
    }
 
private void showToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
}

private void signup(){
loading(true);
// Authentication using FirebaseAuth
//    mAuth = FirebaseAuth.getInstance();
//    mAuth.createUserWithEmailAndPassword(binding.idSignupEmail.getText().toString(), binding.idSignupPwd.getText().toString());





    FirebaseFirestore database=FirebaseFirestore.getInstance();
    HashMap<String,Object> user=new HashMap<>();
    user.put(Constants.KEY_NAME,binding.idSignupUsername.getText().toString());
    user.put(Constants.KEY_EMAIL,binding.idSignupEmail.getText().toString());
    user.put(Constants.KEY_IMAGE,encodeImage);
    user.put(Constants.KEY_PASSWORD,binding.idSignupPwd.getText().toString());
    database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener(documentReference -> {
//preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
preferenceManager.putString(Constants.KEY_NAME,binding.idSignupUsername.getText().toString());
preferenceManager.putString(Constants.KEY_IMAGE,encodeImage);
Intent intent=new Intent(getApplicationContext(),MainActivity.class);
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(intent);
            })
            .addOnFailureListener(exception ->{
loading(false);
showToast(exception.getMessage());
            });

}
private String encodeImage(Bitmap bitmap){
     int previewWidth=150;
     int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
     Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
    previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
    byte[] bytes=byteArrayOutputStream.toByteArray();
    return Base64.encodeToString(bytes,Base64.DEFAULT);

}
private final ActivityResultLauncher<Intent> pickImage=registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),result -> {
          if(result.getResultCode()==RESULT_OK){
              if(result.getData() != null){
                  Uri imageUri=result.getData().getData();
                  try {
                      InputStream inputStream=getContentResolver().openInputStream(imageUri);
                      Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                      binding.idSignupImageProfile.setImageBitmap(bitmap);
                      binding.idSignupImageTxt.setVisibility((View.GONE));
                      encodeImage=encodeImage(bitmap);
                  }catch (FileNotFoundException e){
                      e.printStackTrace();
                  }
              }
          }
        }
);


private Boolean isValidSignUpDetails(){
        if(encodeImage ==null) {
            showToast("Select profile image");
            return false;

        }else if(binding.idSignupUsername.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        }else if(binding.idSignupEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.idSignupEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }
        else if(binding.idSignupPwd.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        else if(binding.idSignupConfirmpwd.getText().toString().trim().isEmpty()){
            showToast("confirm password");
            return false;
        }
        else if(!binding.idSignupConfirmpwd.getText().toString().equals(binding.idSignupPwd.getText().toString())){
            showToast("password and confirm password must be the same");
            return false;
        }else{
            return true;
        }
}

private void loading(Boolean isLoading){
        if(isLoading){
            binding.idSignupBtn.setVisibility(View.INVISIBLE);
            binding.idSignupProgressbar.setVisibility(View.VISIBLE);
        }else {
            binding.idSignupProgressbar.setVisibility(View.INVISIBLE);
            binding.idSignupBtn.setVisibility(View.VISIBLE);
        }
}




}

