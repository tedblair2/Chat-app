package com.example.whatsappclone.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.MessageActivity;
import com.example.whatsappclone.Model.Users;
import com.example.whatsappclone.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<Users> users;
    private boolean isChat;

    public UserAdapter(Context context, List<Users> users, boolean isChat) {
        this.context = context;
        this.users = users;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(v);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user=users.get(position);
        holder.txt.setText(user.getUsername());
        if (user.getImageurl().equals("default")){
            holder.image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(context).load(user.getImageurl()).into(holder.image);
        }

        if (isChat){
            if (user.getStatus().equals("online")){
                holder.status.setText("online");
                holder.status.setTextColor(R.color.teal_700);
            }else {
                holder.status.setText("offline");
            }
        }else {
            holder.status.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, MessageActivity.class);
                intent.putExtra("id",user.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image;
        public TextView txt;
        public TextView status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image=itemView.findViewById(R.id.profile_image);
            txt=itemView.findViewById(R.id.txt_username);
            status=itemView.findViewById(R.id.status);
        }
    }
}
