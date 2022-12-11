package com.example.chatapplication.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.databinding.ItemContainerRecentConversionBinding;
import com.example.chatapplication.listeners.ConversionListener;
import com.example.chatapplication.models.ChatMessage;
import com.example.chatapplication.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    public RecentConversationsAdapter(List<ChatMessage> chatMessages,ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener=conversionListener;
    }

    @NonNull
    @NotNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ConversionViewHolder holder, int position) {
holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding=itemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.idImageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.idItemUserName.setText(chatMessage.conversionName);
            binding.idRecentMsg.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User user=new User();
                    user.id=chatMessage.conversionId;
                    user.name=chatMessage.conversionName;
                    user.image=chatMessage.conversionImage;
                    conversionListener.onConversionClicked(user);

                }
            });
        }
    }
    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
