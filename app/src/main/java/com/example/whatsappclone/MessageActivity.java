package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Adapter.MessageAdapter;
import com.example.whatsappclone.Model.Chat;
import com.example.whatsappclone.Model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    ImageView profile,send;
    TextView username;
    EditText message;
    TextView status;
    RecyclerView recyclerView;
    List<Chat> chats;
    MessageAdapter messageAdapter;
    String userId;

    ValueEventListener listener;

    FirebaseUser fUser;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        fUser= FirebaseAuth.getInstance().getCurrentUser();
        ref= FirebaseDatabase.getInstance().getReference();
        profile=findViewById(R.id.toolbar_profile);
        username=findViewById(R.id.toolbar_username);
        send=findViewById(R.id.button_send);
        message=findViewById(R.id.message_send);
        status=findViewById(R.id.status_msg);
        recyclerView=findViewById(R.id.recycler_message);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        chats=new ArrayList<>();

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Intent intent=getIntent();
        userId=intent.getStringExtra("id");

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=message.getText().toString();
                if (TextUtils.isEmpty(msg)){
                    Toast.makeText(MessageActivity.this, "You cannot send an empty message", Toast.LENGTH_SHORT).show();
                }else{
                    showMessage(fUser.getUid(),userId,msg);
                    message.setText("");
                }
            }
        });

        ref.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user=snapshot.getValue(Users.class);
                assert user != null;
                username.setText(user.getUsername());
                status.setText(user.getStatus());
                if (user.getImageurl().equals("default")){
                    profile.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageurl()).into(profile);
                }
                readMessages(fUser.getUid(),userId,user.getImageurl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenMessage(userId);
    }

    private void showMessage(String sender, String receiver, String msg) {
        HashMap<String,Object> map=new HashMap<>();
        map.put("sender",sender);
        map.put("receiver",receiver);
        map.put("message",msg);
        map.put("isseen",false);

        ref.child("Chats").push().setValue(map);

        DatabaseReference reference=ref.child("ChatList").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    HashMap<String,Object> map1=new HashMap<>();
                    map1.put("receiver",userId);
                    map1.put("sender",fUser.getUid());
                    reference.setValue(map1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void seenMessage(String userId){//check if message is delivered and read
        DatabaseReference reference=ref.child("Chats");
        listener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(fUser.getUid())&&chat.getSender().equals(userId)){
                        HashMap<String,Object> map=new HashMap<>();
                        map.put("isseen",true);

                        snapshot1.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void readMessages(String myId,String userId,String imageurl){//messages as seen in message activity
        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    if (chat.getSender().equals(myId) && chat.getReceiver().equals(userId) || chat.getReceiver().equals(myId)
                    && chat.getSender().equals(userId)){
                        chats.add(chat);
                    }
                    messageAdapter=new MessageAdapter(MessageActivity.this,chats,imageurl);
                    recyclerView.setAdapter(messageAdapter);
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void chechStatus(String status){//check if user is online or offline
        HashMap<String,Object> map=new HashMap<>();
        map.put("status",status);

        ref.child("Users").child(fUser.getUid()).updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chechStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref.removeEventListener(listener);
        chechStatus("offline");
    }
}