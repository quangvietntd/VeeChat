package com.example.quangviet.veechat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;


    public MessageAdapter(List<Messages> mMessageList){

        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            displayName = view.findViewById(R.id.name_text_layout);
            messageImage = view.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();

        if (mMessageList!=null) {

            Messages c = mMessageList.get(position);
            String from_user = c.getFrom();
            String message_type = c.getType();

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name  = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    holder.displayName.setText(name);

                    Picasso.with(holder.profileImage.getContext()).load(image)
                            .placeholder(R.drawable.default_avatar).into(holder.profileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (message_type.equals("text")){
                holder.messageText.setText(c.getMessage());
                holder.messageImage.setVisibility(View.INVISIBLE);
            }else {

                holder.messageText.setVisibility(View.INVISIBLE);

                Glide.with(holder.profileImage.getContext()).load(c.getMessage()).into(holder.messageImage);

//                Picasso.with(holder.profileImage.getContext()).load(c.getMessage())
//                        .placeholder(R.drawable.default_avatar).into(holder.messageImage);
            }


//            if (from_user.equals(current_user_id)) {
//
//                holder.messageText.setBackgroundColor(Color.WHITE);
//                holder.messageText.setTextColor(Color.BLACK);
//
//            } else {
//
//                holder.messageText.setBackgroundResource(R.drawable.message_text_background);
//                holder.messageText.setTextColor(Color.WHITE);
//
//            }
//
//            holder.messageText.setText(c.getMessage());

        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
