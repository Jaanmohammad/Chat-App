package com.jksurajpuriya.chatingapp.Activities;

import static com.jksurajpuriya.chatingapp.Activities.ChatActivity.senderRoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jksurajpuriya.chatingapp.R;
import com.jksurajpuriya.chatingapp.Adapters.UserAdapter;
import com.jksurajpuriya.chatingapp.Models.Users;
import com.jksurajpuriya.chatingapp.databinding.ActivityHomeBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;
    FirebaseAuth auth;
    UserAdapter adapter;
    FirebaseDatabase database;
    ArrayList<Users> usersArrayList;
    private long backPressedTime;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        if (auth.getCurrentUser()==null){
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        DatabaseReference reference=database.getReference().child("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Users users=dataSnapshot.getValue(Users.class);
                    usersArrayList.add(users);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        usersArrayList=new ArrayList<>();
        binding.mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new UserAdapter(HomeActivity.this,usersArrayList);
        binding.mainUserRecyclerView.setAdapter(adapter);

        binding.logOut.setOnClickListener(v -> {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            View view= getLayoutInflater().inflate(R.layout.dialog_layout,null);

            TextView noBtn, yesBtn;
            noBtn=view.findViewById(R.id.noBtn);
            yesBtn=view.findViewById(R.id.yesBtn);

            alert.setView(view);

            final AlertDialog alertDialog = alert.create();
            alertDialog.setCancelable(true);

            noBtn.setOnClickListener(v1 -> {
                alertDialog.dismiss();
            });

            yesBtn.setOnClickListener(v1 -> {
                FirebaseAuth.getInstance().signOut();
                alertDialog.dismiss();
                startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                finish();
            });
            alertDialog.show();
        });

        binding.settings.setOnClickListener(v -> {
           startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
        });
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime +2000>System.currentTimeMillis()){
            Intent intent=new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        backPressedTime=System.currentTimeMillis();
    }

    private void StatusAndTiming(String online){
        reference=FirebaseDatabase.getInstance().getReference("Users").child("StatusAndTiming").child(auth.getUid());
        Map<String, Object>hashMap =new HashMap<>();
        hashMap.put("onlineStatus",online);
        hashMap.put("time",String.valueOf(System.currentTimeMillis()));
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
}