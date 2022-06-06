package com.example.whatsappclone.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.Model.Users;
import com.example.whatsappclone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private ImageView profile;
    private TextView username;

    FirebaseUser fUser;
    DatabaseReference ref;
    StorageReference storage;

    private Uri imageUri;
    private StorageTask upload;

    private static final int IMAGE = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);

        profile=view.findViewById(R.id.img_prof);
        username=view.findViewById(R.id.prof_username);
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        storage= FirebaseStorage.getInstance().getReference("Uploads");

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImages();
            }
        });

        ref= FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users=snapshot.getValue(Users.class);
                username.setText(users.getUsername());
                if (users.getImageurl().equals("default")){
                    profile.setImageResource(R.mipmap.ic_launcher);
                }else{
                    if (getContext()!=null){
                        Glide.with(getContext()).load(users.getImageurl()).into(profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void selectImages() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE);
    }
    private String getFileExtension(Uri uri){
        ContentResolver resolver=getContext().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }
    private void UploadImage(){
        ProgressDialog prog=new ProgressDialog(getContext());
        prog.setMessage("Uploading...");
        prog.show();

        if (imageUri != null){
            final StorageReference reference=storage.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            upload=reference.putFile(imageUri);
            upload.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        String mUri=downloadUri.toString();

                        HashMap<String,Object> map=new HashMap<>();
                        map.put("imageurl",mUri);

                        ref.updateChildren(map);
                        Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                        prog.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    prog.dismiss();
                }
            });
        }else {
            Toast.makeText(getContext(), "No Image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE && resultCode == RESULT_OK && data != null && data.getData() !=null){
            imageUri=data.getData();

            if (upload !=null && upload.isInProgress()){
                Toast.makeText(getContext(), "Upload in progress...", Toast.LENGTH_SHORT).show();
            }
            else {
                UploadImage();
            }
        }
    }
}