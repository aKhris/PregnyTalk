package com.akhris.pregnytalk.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.MessageClickListener;
import com.akhris.pregnytalk.adapters.MessagesListAdapter;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.akhris.pregnytalk.contract.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

/**
 * ChatFragment class representing a chat:
 * -list of messages;
 * -block of views to type and send a new message or a picture;
 */
public class ChatFragment extends NavigationFragment
        implements MessageClickListener,
        MenuItem.OnMenuItemClickListener {

    @Nullable @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.rv_chat) RecyclerView mChatList;
    @BindView(R.id.et_send_message_text) EditText mEditText;
    @BindView(R.id.ib_send_message) ImageButton sendMessageButton;

    private static final int RC_PHOTO_PICKER = 10;

    private MessagesListAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    // Argument passed to new instance of a fragment
    private final static String ARG_CHAT_ROOM_ID ="chat_room_id";
    private String mChatRoomId;

    // Firebase
    private DatabaseReference mRoomMessagesReference;
    private DatabaseReference mChatRoomLastMessage;
    private ChildEventListener mMessagesEventListener;
    private DatabaseReference mChatRoomMetaDataReference;
    private ValueEventListener mChatRoomMetaDataEventListener;
    private StorageReference mChatroomFilesStorage;



    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Making new ChatFragment instance for given chatRoomId
     * @param chatRoomId - ID of the chatroom in the Firebase Database
     */
    public static ChatFragment newInstance(String chatRoomId) {
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ROOM_ID, chatRoomId);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, rootView);
        setupMessageList();
        setupEditText();
        if(getArguments()!=null){
            mChatRoomId = getArguments().getString(ARG_CHAT_ROOM_ID);
        }
        setupReferences();
        setHasOptionsMenu(true);
        return rootView;
    }

    /**
     * Initialization of mChatList RecyclerView
     */
    private void setupMessageList() {
        mAdapter = new MessagesListAdapter(this);
        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mChatList.setLayoutManager(mLinearLayoutManager);
        mChatList.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_info, menu);
        menu.findItem(R.id.action_info).setOnMenuItemClickListener(this);
    }

    /**
     * Handling adding selected picture to FirebaseStorage
     * and then sending it's URL in the Message object.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){return;}
        switch (requestCode){
            case RC_PHOTO_PICKER:
                Uri selectedImageUri = data.getData();
                if(selectedImageUri==null){break;}
                final StorageReference photoRef = mChatroomFilesStorage.child(selectedImageUri.getLastPathSegment());
                photoRef
                        .putFile(selectedImageUri)
                        .addOnSuccessListener(
                                taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Message message = new Message();
                    message.setUserId(MainActivity.sMyUid);
                    message.setMessage(mEditText.getText().toString());
                    message.setTimeStamp(System.currentTimeMillis());
                    message.setUserName(MainActivity.sMe.getName());
                    message.setPictureUrl(uri.toString());
                    mRoomMessagesReference.push().setValue(message);
                    mChatRoomLastMessage.setValue(message);
                    mEditText.setText("");
                }));
                break;
        }
    }

    /**
     * Sending message when user clicks on send button on the keyboard
     */
    private void setupEditText() {
        mEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessageButton.performClick();
                return true;
            }
            return false;
        });
    }

    /**
     * Setting up Firebase Database and Storage references
     */
    private void setupReferences() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        mRoomMessagesReference = database.getReference()
                .child(FirebaseContract.CHILD_ROOM_MESSAGES)
                .child(mChatRoomId);

        mChatRoomLastMessage = FirebaseContract
                .getRoomMetaDataReference(mChatRoomId)
                .child(FirebaseContract.CHILD_ROOM_LAST_MESSAGE);

        mChatRoomMetaDataReference = FirebaseContract
                .getRoomMetaDataReference(mChatRoomId);

        mChatroomFilesStorage = FirebaseStorage
                .getInstance()
                .getReference()
                .child(FirebaseContract.STORAGE_CHILD_CHATROOM_FILES)
                .child(mChatRoomId);
    }

    /**
     * Making (if null) and attaching listeners to Firebase references
     */
    private void attachListeners(){
        if(mMessagesEventListener==null){
            mMessagesEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    mAdapter.addMessage(message);
                    mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount()-1);
                }
                @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
                @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
                @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
            };
        }

        if(mChatRoomMetaDataEventListener==null){
            mChatRoomMetaDataEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if(chatRoom==null){
                        return;
                    }
                    if(mToolbar==null){return;}
                    mToolbar.setTitle(chatRoom.getName());
                    if(chatRoom.getUsersMap()==null){return;}
                    mToolbar.setSubtitle(String.format(getString(R.string.chat_fragment_toolbar_subtitle_format_string), chatRoom.getUsersMap().size()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }

        mRoomMessagesReference.addChildEventListener(mMessagesEventListener);

        mChatRoomMetaDataReference.addValueEventListener(mChatRoomMetaDataEventListener);
    }


    /**
     * Making null and detaching listeners from Firebase references
     */
    private void detachListeners() {
        if(mMessagesEventListener!=null){
            mRoomMessagesReference.removeEventListener(mMessagesEventListener);
            mMessagesEventListener=null;
        }

        if(mChatRoomMetaDataEventListener!=null){
            mChatRoomMetaDataReference.removeEventListener(mChatRoomMetaDataEventListener);
            mChatRoomMetaDataEventListener=null;
        }
    }


    /**
     * Handling clicking on a send message button
     * Send message if it's length>0 and clearing mEditText.
     */
    @OnClick(R.id.ib_send_message)
    public void onSendMessageClick(){
        if(MainActivity.sMe==null){return;}
        if(mEditText.getText().length()==0){return;}
        Message message = new Message();
        message.setUserId(MainActivity.sMyUid);
        message.setMessage(mEditText.getText().toString());
        message.setTimeStamp(System.currentTimeMillis());
        message.setUserName(MainActivity.sMe.getName());
        mRoomMessagesReference.push().setValue(message);
        mChatRoomLastMessage.setValue(message);
        mEditText.setText("");
    }

    /**
     * Handling clicking on a send image button.
     * If user has also filled in mEditText, it will be also sent in a message with picture.
     */
    @OnClick(R.id.ib_send_message_image)
    public void onSendImageClick(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.picture_chooser_title)), RC_PHOTO_PICKER);
    }

    @Override
    Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public void onResume() {
        super.onResume();
        attachListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachListeners();
        mAdapter.clear();
    }

    @Override
    public boolean withBackButton() {
        return true;
    }

    /**
     * Showing user info on clicking the user name on a message
     */
    @Override
    public void onUserNameClick(int position) {
        Message message = mAdapter.getMessage(position);
        String userId = message.getUserId();
        Intent intent = UserInfoActivity.getUserIntent(getContext(), userId);
        startActivity(intent);
    }


    /**
     * Showing chat info after clicking on corresponding menu item
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId()==R.id.action_info) {
            getNavigationManager().navigateToChatInfo(mChatRoomId);
            return true;
        }
        return false;
    }
}
