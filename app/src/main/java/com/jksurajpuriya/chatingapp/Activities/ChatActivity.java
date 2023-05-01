package com.jksurajpuriya.chatingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jksurajpuriya.chatingapp.Adapters.MessagesAdapter;
import com.jksurajpuriya.chatingapp.Models.Messages;
import com.jksurajpuriya.chatingapp.databinding.ActivityChatBinding;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    String ReceiverImage, ReceiverUID, ReceiverName, SenderUID;
    FirebaseDatabase database;
    FirebaseAuth auth;
    public static String sImage;
    public static String rImage;
    public static String senderRoom, reciverRoom;
    ArrayList<Messages> messagesArrayList;
    MessagesAdapter adapter;
    DatabaseReference reference;
    String typingString, timeString, onlineString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();


        ReceiverImage=getIntent().getStringExtra("ReciverImage");
        ReceiverName=getIntent().getStringExtra("name");
        ReceiverUID=getIntent().getStringExtra("uid");
        messagesArrayList=new ArrayList<>();



        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);

        binding.messageAdapter.setLayoutManager(linearLayoutManager);
        adapter=new MessagesAdapter(this, messagesArrayList);
        binding.messageAdapter.setAdapter(adapter);

        Picasso.get().load(ReceiverImage).into(binding.profileImage);
        binding.reciverName.setText("" + ReceiverName);

        SenderUID=auth.getUid();

        senderRoom=SenderUID+ReceiverUID;
        reciverRoom=ReceiverUID+SenderUID;
        binding.messageAdapter.smoothScrollToPosition(adapter.getItemCount());

        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesArrayList.add(messages);
                    binding.messageAdapter.scrollToPosition(messagesArrayList.size() - 1);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.edtMessage.setOnTouchListener((v, event) -> {
            binding.edtMessage.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.edtMessage, InputMethodManager.SHOW_IMPLICIT);

            return true;

        });


        reference = database.getReference().child("user").child(auth.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                sImage=snapshot.child("imageUri").getValue().toString();
                rImage=ReceiverImage;


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        DatabaseReference typingRef = database.getReference("Users").child("TextType").child(reciverRoom);
        typingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()) {
                    typingString = snapshot.child("typing").getValue().toString();
                    if (typingString.equals("Typing...")){
                        binding.dateAndTime.setText(typingString);
                        binding.online.setText("");
                    }
                    else{
                        binding.dateAndTime.setText(onlineString);
                        binding.online.setText("");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        DatabaseReference onlineStatusRef = database.getReference("Users").child("StatusAndTiming").child(ReceiverUID);
        onlineStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()) {
                    timeString = snapshot.child("time").getValue().toString();
                    onlineString = snapshot.child("onlineStatus").getValue().toString();


                     if (onlineString.equals("online")){
                        binding.dateAndTime.setText(onlineString);
                        binding.online.setText("");
                    }else {
                         long jk=Long.parseLong(String.valueOf(timeString));
                        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("   dd/MMM/yyyy", Locale.getDefault());
                        String time = simpleTimeFormat.format(jk);
                        String date = simpleDateFormat.format(jk);
                        binding.online.setText(time);
                        binding.dateAndTime.setText(date);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0){
                    Typing("");
                }else {
                    Typing("Typing...");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.sendBtn.setOnClickListener(v -> {
          String message= binding.edtMessage.getText().toString();

          if (message.isEmpty()){
              Toast.makeText(this, "Please Enter Valid Messages", Toast.LENGTH_SHORT).show();
              return;
          }
          binding.edtMessage.setText("");
            Date date = new Date();
            Messages messages = new Messages(message,SenderUID,date.getTime());

// add chat in Firebase Realtime database
            database=FirebaseDatabase.getInstance();
            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .push()
                    .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            database.getReference().child("chats")
                                    .child(reciverRoom)
                                    .child("messages")
                                    .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                        }
                    });
            //.....................................


        });

        binding.back.setOnClickListener(v -> {
           startActivity(new Intent(ChatActivity.this,HomeActivity.class));
        });

    }


    private void Typing(String typing){
        reference=FirebaseDatabase.getInstance().getReference("Users").child("TextType").child(senderRoom);
        Map<String, Object>hashMap =new HashMap<>();
        hashMap.put("typing",typing);
        reference.updateChildren(hashMap);
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
        Typing("typing");
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatusAndTiming("");
        Typing("");
    }


}