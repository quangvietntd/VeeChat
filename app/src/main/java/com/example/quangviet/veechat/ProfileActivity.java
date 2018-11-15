package com.example.quangviet.veechat;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;


    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



       mProfileImage = findViewById(R.id.profile_image);
       mProfileName = findViewById(R.id.profile_displayName);
       mProfileStatus = findViewById(R.id.profile_status);
       mProfileFriendsCount = findViewById(R.id.profile_totalFriends);
       mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
       mDeclineBtn = findViewById(R.id.profile_decline_btn);

       mCurrent_state = "not_friends";


        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);


       mProgressDialog = new ProgressDialog(this);
       mProgressDialog.setTitle("Loading User Data");
       mProgressDialog.setMessage("Please wait while we load the user data.");
       mProgressDialog.setCanceledOnTouchOutside(false);
       mProgressDialog.show();

       mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
       mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
       mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
       mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

       final String user_id = getIntent().getStringExtra("user_id");

       mRootRef = FirebaseDatabase.getInstance().getReference();

       mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
       mUsersDatabase.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String display_name = dataSnapshot.child("name").getValue().toString();
               String status = dataSnapshot.child("status").getValue().toString();
               String image = dataSnapshot.child("image").getValue().toString();

               mProfileName.setText(display_name);
               mProfileStatus.setText(status);
               Glide.with(ProfileActivity.this).load(image).into(mProfileImage);

               //-----FRIENDS LIST / REQUEST FEATURE-----
               mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {

                       if (dataSnapshot.hasChild(user_id)){
                           String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                           if (req_type.equals("received")){

                               mCurrent_state = "req_received";
                               mProfileSendReqBtn.setText("Accept Friend Request");

                               mDeclineBtn.setVisibility(View.VISIBLE);
                               mDeclineBtn.setEnabled(true);
                           }else if (req_type.equals("sent")){
                               mCurrent_state = "req_sent";
                               mProfileSendReqBtn.setText("Cancel Friend Request");

                               mDeclineBtn.setVisibility(View.INVISIBLE);
                               mDeclineBtn.setEnabled(false);
                           }

                           mProgressDialog.dismiss();
                       }else{
                           mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {
                                   if (dataSnapshot.hasChild(user_id)){
                                       mCurrent_state = "friends";
                                       mProfileSendReqBtn.setText("Unfriend this Person");

                                       mDeclineBtn.setVisibility(View.INVISIBLE);
                                       mDeclineBtn.setEnabled(false);
                                   }

                                   mProgressDialog.dismiss();
                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                                   mProgressDialog.dismiss();

                               }
                           });
                       }

                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });



           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

       mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               // ------------NOT FRIENDS STATE--------------

               mProfileSendReqBtn.setEnabled(false);

               if (mCurrent_state.equals("not_friends")){
//                   mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type")
//                           .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
//                       @Override
//                       public void onComplete(@NonNull Task<Void> task) {
//                           if (task.isSuccessful()){
//
//                               mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
//                                       .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
//                                   @Override
//                                   public void onSuccess(Void aVoid) {
//
//                                       HashMap<String,String> notificationData = new HashMap<>();
//                                       notificationData.put("from",mCurrent_user.getUid());
//                                       notificationData.put("type","request");
//
//                                       mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                           @Override
//                                           public void onSuccess(Void aVoid) {
//                                               mCurrent_state = "req_sent";
//                                               mProfileSendReqBtn.setText("Cancel Friend Request");
//
//                                               mDeclineBtn.setVisibility(View.INVISIBLE);
//                                               mDeclineBtn.setEnabled(false);
//                                           }
//                                       });
//
//
//
//                                     //  Toast.makeText(ProfileActivity.this, "Request sent successfully.", Toast.LENGTH_SHORT).show();
//
//                                   }
//                               });
//
//                           }else {
//                               Toast.makeText(ProfileActivity.this, "Failed Sending Request.", Toast.LENGTH_SHORT).show();
//                           }
//
//                           mProfileSendReqBtn.setEnabled(true);
//                       }
//                   });

                   DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                   String newNotificationId = newNotificationref.getKey();

                   HashMap<String,String> notificationData = new HashMap<>();
                   notificationData.put("from",mCurrent_user.getUid());
                   notificationData.put("type","request");

                   Map requestMap = new HashMap();
                   requestMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+ user_id+"/request_type", "sent");
                   requestMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid()+"/request_type","received");
                   requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                   mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if (databaseError !=null){
                               Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                           }

                           mProfileSendReqBtn.setEnabled(true);
                           mCurrent_state = "req_sent";
                           mProfileSendReqBtn.setText("Cancel Friend Request");

                       }
                   });
               }

               // ------------CANCEL REQUEST STATE--------------
               if (mCurrent_state.equals("req_sent")){
                   mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {

                           mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {

                                   mProfileSendReqBtn.setEnabled(true);
                                   mCurrent_state = "not_friends";
                                   mProfileSendReqBtn.setText("Send Friend Request");

                                   mDeclineBtn.setVisibility(View.INVISIBLE);
                                   mDeclineBtn.setEnabled(false);


                               }
                           });

                       }
                   });
               }

               //---------------REQ RECEIVED STATE------------------

               if (mCurrent_state.equals("req_received")){

//                   final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
//                   mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
//                       @Override
//                       public void onSuccess(Void aVoid) {
//
//                           mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
//                               @Override
//                               public void onSuccess(Void aVoid) {
//
//                                   mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                       @Override
//                                       public void onSuccess(Void aVoid) {
//
//                                           mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                               @Override
//                                               public void onSuccess(Void aVoid) {
//
//                                                   mProfileSendReqBtn.setEnabled(true);
//                                                   mCurrent_state = "friends";
//                                                   mProfileSendReqBtn.setText("UnFriend this Person");
//
//                                                   mDeclineBtn.setVisibility(View.INVISIBLE);
//                                                   mDeclineBtn.setEnabled(false);
//
//
//                                               }
//                                           });
//
//                                       }
//                                   });
//
//                               }
//                           });
//
//                       }
//                   });
                   String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                   Map friendsMap = new HashMap();
                   friendsMap.put("Friends/"+mCurrent_user.getUid()+"/"+user_id+"/date",currentDate);
                   friendsMap.put("Friends/"+user_id+"/"+mCurrent_user.getUid()+"/date",currentDate);

                   friendsMap.put("Friend_req/"+mCurrent_user.getUid()+"/"+user_id,null);
                   friendsMap.put("Friend_req/"+user_id+"/"+mCurrent_user.getUid(),null);

                   mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if (databaseError==null){
                               mProfileSendReqBtn.setEnabled(true);
                               mCurrent_state = "friends";
                               mProfileSendReqBtn.setText("Unfriend this person");

                               mDeclineBtn.setVisibility(View.INVISIBLE);
                               mDeclineBtn.setEnabled(false);
                           }else {

                               String error = databaseError.getMessage();
                               Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                           }
                       }
                   });

               }

                //--------------UNFRIENDS----------
               if (mCurrent_state.equals("friends")){
                   Map unfriendMap = new HashMap();
                   unfriendMap.put("Friends/"+mCurrent_user.getUid()+"/"+user_id,null);
                   unfriendMap.put("Friends/"+user_id+"/"+mCurrent_user.getUid(),null);

                   mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if (databaseError==null){
                               mCurrent_state = "not_friends";
                               mProfileSendReqBtn.setText("Send Friend Request");

                               mDeclineBtn.setVisibility(View.INVISIBLE);
                               mDeclineBtn.setEnabled(false);
                           }else {

                               String error = databaseError.getMessage();
                               Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                           }

                           mProfileSendReqBtn.setEnabled(true);

                       }
                   });

               }


           }
       });

    }
}
