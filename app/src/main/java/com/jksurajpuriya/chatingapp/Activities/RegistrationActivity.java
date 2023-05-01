package com.jksurajpuriya.chatingapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jksurajpuriya.chatingapp.Models.Users;
import com.jksurajpuriya.chatingapp.R;
import com.jksurajpuriya.chatingapp.databinding.ActivityRegistrationBinding;

public class RegistrationActivity extends AppCompatActivity {
    String imageView;
    ActivityRegistrationBinding binding;
    Uri imageUri;
    // email validation in android regex in google search
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    String imageURI;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        


        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();

        dialog=new Dialog(this);
        dialog.setContentView(R.layout.dialog_box);
        dialog.setCancelable(true);
        if (dialog.getWindow()!=null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }



        binding.signUp.setOnClickListener(v -> {
            String name=binding.registrationName.getText().toString();
            String email=binding.registrationEmail.getText().toString();
            String password=binding.registrationPassword.getText().toString();
            String conformPassword=binding.registrationConformPassword.getText().toString();
            String status="Hey There I'm Using This Application";



            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password)  || TextUtils.isEmpty(conformPassword)){
                Toast.makeText(this, "Please Enter Valid Data", Toast.LENGTH_SHORT).show();
            }else if (!email.matches(emailPattern)){
                binding.registrationEmail.setError("Please Enter Valid Email");
                Toast.makeText(this, "Please Enter valid Email", Toast.LENGTH_SHORT).show();
            }else if (!password.equals(conformPassword)){
                Toast.makeText(this, "Password does not Match...", Toast.LENGTH_SHORT).show();
            }
            else if (password.length()<7){
                Toast.makeText(this, "Enter Minimum 8 Character Password", Toast.LENGTH_SHORT).show();
            }else {
                dialog.show();
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){




                        // save data in database ...................//

                            DatabaseReference reference= database.getReference().child("user").child(auth.getUid());
                            StorageReference storageReference= storage.getReference().child("upload").child(auth.getUid());

                            if (imageUri!=null){
                                storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()){
                                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    imageURI=uri.toString();
                                                    Users users=new Users(auth.getUid(),name,email,imageURI,status);
                                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()){
                                                                dialog.dismiss();
                                                                Toast.makeText(RegistrationActivity.this, "User Create SuccessFully", Toast.LENGTH_SHORT).show();
                                                                startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                                                                finish();
                                                            }else {
                                                                Toast.makeText(RegistrationActivity.this, "Error in Registration....", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });
                            }else {
                                String status="Hey There I'm Using This Application";
                                imageURI="https://firebasestorage.googleapis.com/v0/b/chat-e2cfe.appspot.com/o/profile_image.png?alt=media&token=cbaa8f2c-62ea-4b8e-9a03-929a56de2216";
                                Users users=new Users(auth.getUid(),name,email,imageURI,status);
                                reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            dialog.dismiss();
                                            Toast.makeText(RegistrationActivity.this, "User Create SuccessFully", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegistrationActivity.this,HomeActivity.class));
                                            finish();
                                        }else {
                                            Toast.makeText(RegistrationActivity.this, "Error in Registration....", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            // save data in database ...................//




                        }else {
                            dialog.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }





        });

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
        });


        binding.textSignIn.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            if (data!=null){
                imageUri=data.getData();
                binding.profileImage.setImageURI(imageUri);
            }
        }
    }



}