package com.akhris.pregnytalk.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.ChatRoomDetailsListAdapter;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Class representing chat information
 */
public class ChatInfoFragment extends NavigationFragment
        implements NavigationManagerCallback,
        ChatRoomDetailsListAdapter.Callback {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.rv_chat_info_list) RecyclerView mChatInfoList;

    // Argument passed to new instance of a fragment
    private static final String ARG_CHAT_ROOM_ID = "chat_room_id";
    private String mChatRoomId;

    // Firebase
    private DatabaseReference mChatRoomMetaDataReference;
    private ValueEventListener mChatRoomMetaDataEventListener;

    public ChatInfoFragment() {
        // Required empty public constructor
    }

    public static ChatInfoFragment newInstance(String chatRoomId) {
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ROOM_ID, chatRoomId);
        ChatInfoFragment fragment = new ChatInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * Add back button to the toolbar in parent Activity
     */
    @Override
    public boolean withBackButton() {
        return true;
    }

    /**
     * Initialize ChatRoom id from the value in arguments
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null && getArguments().containsKey(ARG_CHAT_ROOM_ID)){
            mChatRoomId = getArguments().getString(ARG_CHAT_ROOM_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_info, container, false);
        ButterKnife.bind(this, rootView);
        mToolbar.setTitle(R.string.chat_info_fragment_title);
        setupInfoList();
        addContactsFragment();
        setupReference();
        return rootView;
    }

    /**
     * Adding layout manager to mChatInfoList
     */
    private void setupInfoList() {
        mChatInfoList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    /**
     * Using NavigationManager to load ContactsList in a container
     */
    private void addContactsFragment() {
        String childPath = FirebaseContract.CHILD_ROOM_META_DATA+"/"+mChatRoomId+"/"+FirebaseContract.CHILD_ROOM_USERS_MAP;
        getNavigationManager()
                .navigateToContactsList(childPath, R.id.fl_chat_info_contacts_container, getChildFragmentManager());
    }

    /**
     * Setting Firebase Database Reference
     */
    private void setupReference() {
        mChatRoomMetaDataReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(FirebaseContract.CHILD_ROOM_META_DATA)
                .child(mChatRoomId);
    }


    @Override
    public void onResume() {
        super.onResume();
        attachListeners();
    }

    /**
     * Making listeners (if null) and attaching it to Firebase Database Reference
     */
    private void attachListeners() {
        if(mChatRoomMetaDataEventListener==null) {
            mChatRoomMetaDataEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if(chatRoom==null){
                        return;
                    }
                    initViewsWithChatRoom(chatRoom);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mChatRoomMetaDataReference.addValueEventListener(mChatRoomMetaDataEventListener);
        }
    }

    /**
     * Called when ChatRoom object got loaded from Firebase Database
     */
    private void initViewsWithChatRoom(ChatRoom chatRoom) {
        ChatRoomDetailsListAdapter adapter = new ChatRoomDetailsListAdapter(chatRoom, this);
        mChatInfoList.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        detachListeners();
    }

    /**
     * Detaching listeners from Firebase Database Listeners and making it null
     */
    private void detachListeners() {
        if(mChatRoomMetaDataEventListener!=null){
            mChatRoomMetaDataReference.removeEventListener(mChatRoomMetaDataEventListener);
            mChatRoomMetaDataEventListener=null;
        }
    }

    /**
     * Making alert dialog to edit text (in admin mode)
     */
    private void makeAlertDialog(String text, int titleRes, EditDialogListener dialogListener) {
        if(getContext()==null){return;}

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        final EditText edittext = new EditText(getContext());
        edittext.setText(text);
        alert
                .setTitle(titleRes)
                .setView(edittext)
                .setPositiveButton(R.string.chat_info_edit_button_ok,
                        (dialog, which) -> dialogListener.onOkClicked(edittext.getText().toString()));
        alert.show();
    }

    /**
     * if is admin mode - make dialog to change chat name
     */
    @Override
    public void onChatNameClick(String name, boolean isAdminMode) {
        if(!isAdminMode){ return; }
        makeAlertDialog(name,R.string.chat_info_edit_name, editedText -> mChatRoomMetaDataReference
                .child(FirebaseContract.CHILD_ROOM_NAME)
                .setValue(editedText));
    }


    /**
     * if is admin mode - make dialog to change chat description
     */
    @Override
    public void onChatDescrClick(String description, boolean isAdminMode) {
        if(!isAdminMode){ return; }
        makeAlertDialog(description, R.string.chat_info_edit_description, editedText -> mChatRoomMetaDataReference
                .child(FirebaseContract.CHILD_ROOM_DESCRIPTION)
                .setValue(editedText));
    }

    @Override
    public void onLocationClick(boolean isAdminMode) {

    }

    /**
     * if is admin mode - change type of chatroom
     */
    @Override
    public void onTypeClick(int oldType, boolean isAdminMode) {
        if(!isAdminMode){ return; }
        int newType = oldType==ChatRoom.TYPE_PRIVATE?
                ChatRoom.TYPE_PUBLIC:ChatRoom.TYPE_PRIVATE;
        mChatRoomMetaDataReference
                .child(FirebaseContract.CHILD_ROOM_TYPE)
                .setValue(newType);
    }

    private interface EditDialogListener{

        void onOkClicked(String editedText);
    }


}
