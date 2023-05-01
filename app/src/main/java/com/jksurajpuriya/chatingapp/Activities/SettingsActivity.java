package com.jksurajpuriya.chatingapp.Activities;

import static com.jksurajpuriya.chatingapp.Activities.ChatActivity.senderRoom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jksurajpuriya.chatingapp.Models.Users;
import com.jksurajpuriya.chatingapp.R;
import com.jksurajpuriya.chatingapp.databinding.ActivitySettingsBinding;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImageUri;
    String email;
    Dialog dialog;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        dialog=new Dialog(this);
        dialog.setContentView(R.layout.dialog_box);
        dialog.setCancelable(true);
        if (dialog.getWindow()!=null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        dialog.show();

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference=storage.getReference().child("upload").child(auth.getUid());

        binding.back.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this,HomeActivity.class));
            finish();
        });


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email=snapshot.child("email").getValue().toString();
                String name=snapshot.child("name").getValue().toString();
                String status=snapshot.child("status").getValue().toString();
                String image=snapshot.child("imageUri").getValue().toString();


                binding.name.setText(name);
                binding.status.setText(status);
                Picasso.get().load(image).into(binding.profileImage);
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.update.setOnClickListener(v -> {
            if (selectedImageUri!=null){

                dialog.show();

                String name =binding.name.getText().toString();
                String status=binding.status.getText().toString();

                storageReference.putFile(selectedImageUri).addOnCompleteListener(task ->
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String finalImageUri=uri.toString();
                    Users users= new Users(auth.getUid(),name,email,finalImageUri,status);

                    reference.setValue(users).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        dialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SettingsActivity.this,HomeActivity.class));
                        finish();
                    }else {
                        dialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                    });
                }));
            }else {
                dialog.show();
                StorageReference storage1=storage.getReference().child("upload/profile_image.png");
                storage1.getDownloadUrl().addOnSuccessListener(uri -> {
                    String name =binding.name.getText().toString();
                    String status=binding.status.getText().toString();

                    String finalImageUri=uri.toString();
                    Users users= new Users(auth.getUid(),name,email,finalImageUri,status);
                    reference.setValue(users).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            dialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SettingsActivity.this,HomeActivity.class));
                            finish();
                        }else {
                            dialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            if (data!=null){
                selectedImageUri=data.getData();
                binding.profileImage.setImageURI(selectedImageUri);
            }
        }
    }
    private void StatusAndTiming(String online){
        reference=FirebaseDatabase.getInstance().getReference("Users").child("StatusAndTiming").child(auth.getUid());
        Map<String, Object>hashMap =new HashMap<>();
        hashMap.put("onlineStatus",online);
        hashMap.put("time",String.valueOf(System.currentTimeMillis()));
        reference.updateChildren(hashMap);
    }
    private void Typing(String typing){
        reference=FirebaseDatabase.getInstance().getReference("Users").child("TextType").child(senderRoom);
        Map<String, Object>hashMap =new HashMap<>();
        hashMap.put("typing",typing);
        reference.updateChildren(hashMap);
    }
    @Override
    protected void onResume() {
        super.onResume();
        StatusAndTiming("online");
    }
    @Override
    protected void onPause() {
        super.onPause();
        StatusAndTiming("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}