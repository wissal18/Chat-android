package com.example.chatapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.utilities.Constants;
import com.example.chatapplication.utilities.PreferenceManager;

import java.util.Base64;

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
    }

    public void loadUserDetails(){
        binding.idMainName.setText(preferenceManager.getString(Constants.KEY_NAME));

       // byte[] bytes= Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));Check this one

        byte[] bytes = Base64.getDecoder().decode(Constants.KEY_IMAGE);
        System.out.println("here4"+Constants.KEY_IMAGE);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.idMainImageProfile.setImageBitmap(bitmap);
    }
}