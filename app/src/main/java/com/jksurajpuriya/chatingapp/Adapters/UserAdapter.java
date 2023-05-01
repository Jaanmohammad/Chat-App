package com.jksurajpuriya.chatingapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.jksurajpuriya.chatingapp.Activities.ChatActivity;
import com.jksurajpuriya.chatingapp.Models.Users;
import com.jksurajpuriya.chatingapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder>{

    Context homeActivity;
    ArrayList<Users> usersArrayList;


    public UserAdapter(Context homeActivity, ArrayList<Users> usersArrayList) {
        this.homeActivity = homeActivity;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(homeActivity).inflate(R.layout.item_user_row,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Users users= usersArrayList.get(position);

        // our profile hide
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(users.getUid())){
            holder.itemView.setVisibility(View.INVISIBLE);
        }

        holder.userName.setText(users.getName());
        holder.userStatus.setText(users.getStatus());
        Picasso.get().load(users.getImageUri()).into(holder.userProfile);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(homeActivity, ChatActivity.class);
            intent.putExtra("name", users.getName());
            intent.putExtra("ReciverImage",users.getImageUri());
            intent.putExtra("uid",users.getUid());
            homeActivity.startActivity(intent);
        });


    }



    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {
        CircleImageView userProfile;
        TextView userName, userStatus;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            userProfile=itemView.findViewById(R.id.userImage);
            userName=itemView.findViewById(R.id.userName);
            userStatus=itemView.findViewById(R.id.userStatus);
        }
    }
}
