package com.jksurajpuriya.chatingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.jksurajpuriya.chatingapp.R;
import com.jksurajpuriya.chatingapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    FirebaseAuth auth;
    // email validation in android regex in google search
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth=FirebaseAuth.getInstance();
        if (auth.getCurrentUser()!=null){
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        dialog=new Dialog(this);
        dialog.setContentView(R.layout.dialog_box);
        dialog.setCancelable(true);
        if (dialog.getWindow()!=null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        binding.textSignUp.setOnClickListener(v -> {
           startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
           finish();
        });
        
        binding.signIn.setOnClickListener(v -> {
            String email = binding.loginEmail.getText().toString();
            String password = binding.loginPassword.getText().toString();
            
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText(this, "Please Enter valid Email and Password", Toast.LENGTH_SHORT).show();
            }else if (!email.matches(emailPattern)){
                binding.loginEmail.setError("Invalid email");
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            }else if (password.length()<7){
                binding.loginPassword.setError("Invalid Password");
                Toast.makeText(this, "Please Enter valid password", Toast.LENGTH_SHORT).show();
            }else {
                dialog.show();
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            dialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }else{
                            dialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Error in login", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}