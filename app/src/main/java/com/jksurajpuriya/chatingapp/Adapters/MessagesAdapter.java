package com.jksurajpuriya.chatingapp.Adapters;

import static com.jksurajpuriya.chatingapp.Activities.ChatActivity.rImage;
import static com.jksurajpuriya.chatingapp.Activities.ChatActivity.sImage;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.jksurajpuriya.chatingapp.Models.Messages;
import com.jksurajpuriya.chatingapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<Messages>messagesArrayList;
    int ITEM_SEND=1;
    int ITEM_RECIVE=2;
    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==ITEM_SEND){
            View view= LayoutInflater.from(context).inflate(R.layout.sender_layout,parent,false);
            return new SenderViewHolder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.reciver_layout,parent,false);
            return new ReciverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages messages = messagesArrayList.get(position);
            if (holder.getClass()==SenderViewHolder.class){
             SenderViewHolder viewHolder=(SenderViewHolder) holder;
             viewHolder.txtMessages.setText(messages.getMessage());


                Picasso.get().load(sImage).into(viewHolder.circleImageView);
            }else {
                ReciverViewHolder viewHolder=(ReciverViewHolder) holder;
                viewHolder.txtMessages.setText(messages.getMessage());

                Picasso.get().load(rImage).into(viewHolder.circleImageView);
            }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = messagesArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId())){
            return ITEM_SEND;
        }else {
            return ITEM_RECIVE;
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView txtMessages;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView=itemView.findViewById(R.id.profile_image);
            txtMessages=itemView.findViewById(R.id.textMessages);

        }
    }
    class ReciverViewHolder extends RecyclerView.ViewHolder{
        CircleImageView circleImageView;
        TextView txtMessages;
        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView=itemView.findViewById(R.id.profile_image);
            txtMessages=itemView.findViewById(R.id.textMessages);

        }
    }
}
