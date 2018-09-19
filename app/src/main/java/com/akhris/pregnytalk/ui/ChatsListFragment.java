package com.akhris.pregnytalk.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.ChatsListAdapter;
import com.akhris.pregnytalk.adapters.ItemClickListener;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment representing list of chats the user takes part in (showed on the app start)
 */
public class ChatsListFragment extends NavigationFragment
    implements CreateChatFragment.Callback, ItemClickListener, SwipeableRecyclerView.SwipeCallbacks, NavigationManagerCallback {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.rv_chats_list)
    SwipeableRecyclerView mChatsList;
    @Nullable @BindView(R.id.fl_chat_container) FrameLayout mChatContainer;

    // Argument passed to new instance of a fragment
    private static final String ARG_CHAT_ROOM_ID = "chat_room_id";
    private String mNavigateToChatRoomId;

    // Firebase
    private DatabaseReference mRoomMetaDataReference;
    private ChildEventListener mUserRoomIdsChildListener;
    private Query mUserChatsQuery;

    // RecyclerView's adapter
    private ChatsListAdapter mAdapter;

    // Creating new instance of fragment with passing chatroom's ID as a parameter
    // (used to load a chat room right after loading it - in Master-Detail-Flow mode)
    // To show a chat list only just call "new ChatsListFragment()"
    public static ChatsListFragment newInstance(@Nullable String chatRoomId) {
        Bundle args = new Bundle();
        if(chatRoomId!=null){
        args.putString(ARG_CHAT_ROOM_ID, chatRoomId);
        }
        ChatsListFragment fragment = new ChatsListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ChatsListFragment() {
        // Required empty public constructor
    }

    /**
     * Returning an instance of ChatRoom class with chatroom id assigned
     * or null if it doesn't exist
     */
    @Nullable
    private ChatRoom getChatRoomFromDataSnapshot(DataSnapshot dataSnapshot){
        if(dataSnapshot==null){return null;}
        ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
        if(chatRoom==null){return null;}
        chatRoom.setChatRoomId(dataSnapshot.getKey());
        return chatRoom;
    }


    @Override
    Toolbar getToolbar() {
        return mToolbar;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null && getArguments().containsKey(ARG_CHAT_ROOM_ID)){
            mNavigateToChatRoomId = getArguments().getString(ARG_CHAT_ROOM_ID);
        }
        mAdapter = new ChatsListAdapter(this);
        setupReferences();
    }

    /**
     * Initializing views here with adding "swipe-to-delete" functionality by using
     * Custom subclass of RecyclerView - SwipeableRecyclerView
     * and implementing SwipeCallbacks by the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chats_list, container, false);
        ButterKnife.bind(this, rootView);
        mToolbar.setTitle(R.string.toolbar_title_chatslistfragment);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mChatsList.setAdapter(mAdapter);
        mChatsList.initSwiping(this);
        return rootView;
    }

    /**
     * Clicking on floating action button: show dialog to add new chat
     */
    @OnClick(R.id.fab_add_new_chat)
    public void addNewChat(){
        CreateChatFragment
                .newInstance(null)
                .show(getChildFragmentManager(), CreateChatFragment.class.getSimpleName());
    }

    /**
     * Callback from CreateChatFragment dialog
     * @param chatRoom newly created ChatRoom instance
     */
    @Override
    public void onNewChatroomAdded(ChatRoom chatRoom) {
        mRoomMetaDataReference.push().setValue(chatRoom);
    }

    /**
     * Firebase references initialization.
     * Solution of how to query by key when it's value doesn't matter got here:
     * https://stackoverflow.com/a/45992586/7635275
     */
    private void setupReferences() {
        mRoomMetaDataReference =
                FirebaseContract
                .getRoomsMetaDataReference();

        mUserChatsQuery =
                mRoomMetaDataReference
                        .orderByChild(FirebaseContract.CHILD_ROOM_USERS_MAP+"/"+MainActivity.sMyUid)
                        .startAt("-");
    }

    private void attachListeners() {
        if(mUserRoomIdsChildListener==null){
            mUserRoomIdsChildListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String chatRoomId = dataSnapshot.getKey();
                    if(chatRoomId==null){return;}
                    mRoomMetaDataReference
                            .child(chatRoomId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ChatRoom chatRoom = getChatRoomFromDataSnapshot(dataSnapshot);
                                    if(chatRoom==null){return;}
                                    if(chatRoom.getName()==null || chatRoom.getName().length()==0){
                                        String anotherUserName="";
                                        for (Map.Entry<String, String> userEntry:chatRoom.getUsersMap().entrySet()) {
                                            if(!userEntry.getKey().equals(MainActivity.sMyUid)){
                                                anotherUserName = userEntry.getValue();
                                            }
                                        }
                                        if(isAdded()) {
                                            chatRoom.setName(String.format(getString(R.string.chat_with_format_string), anotherUserName));
                                        }
                                    }
                                    mAdapter.addChatRoom(chatRoom);

                                    if (mChatContainer!=null && chatRoomId.equals(mNavigateToChatRoomId)){
                                        getNavigationManager().navigateToChat(chatRoomId, getChildFragmentManager());
                                        mNavigateToChatRoomId = null;
                                    }
                                }

                                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    ChatRoom chatRoom = getChatRoomFromDataSnapshot(dataSnapshot);
                    if(chatRoom!=null) {
                        mAdapter.updateChatRoom(chatRoom);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    ChatRoom chatRoom = getChatRoomFromDataSnapshot(dataSnapshot);
                    if(chatRoom!=null) {
                        mAdapter.removeChatRoom(chatRoom);
                    }
                }
                @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
        }

        mUserChatsQuery
                .addChildEventListener(mUserRoomIdsChildListener);
    }

    /**
     * Detaching Firebase Reference listeners and making it null
     */
    private void detachListeners() {
        if(mUserRoomIdsChildListener!=null){
            mUserChatsQuery.removeEventListener(mUserRoomIdsChildListener);
            mUserRoomIdsChildListener = null;
        }
    }

    /**
     * Callback for ChatsListAdapter
     */
    @Override
    public void onItemClick(int position) {
        String chatRoomId = mAdapter.getChatRoomId(position);
        if(mChatContainer==null) {
            getNavigationManager().navigateToChat(chatRoomId);
        } else {
            getNavigationManager().navigateToChat(chatRoomId, getChildFragmentManager());
        }
    }

    /**
     * Callback for SwipeableRecyclerView.
     * Make actual deleting when SnackBar is dismissed not by user clicking on "undo" action.
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        String id = mAdapter.getChatRoomId(position);
        ChatRoom chatRoom = mAdapter.getChatRoom(position);
        mAdapter.removeChatRoom(position);
        String deleteString = String.format(getString(R.string.chatroom_deleted_snackbar_text), chatRoom.getName());
        Snackbar deleteChatRoomBar = Snackbar.make(mChatsList, deleteString, Snackbar.LENGTH_LONG);
        deleteChatRoomBar.setAction(R.string.snackbar_undo, v -> {
                    mAdapter.addChatRoom(chatRoom);
                }
            );
        deleteChatRoomBar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if(event==DISMISS_EVENT_ACTION){ return; }
                mRoomMetaDataReference
                .child(id)
                .child(FirebaseContract.CHILD_ROOM_USERS_MAP)
                .child(MainActivity.sMyUid).removeValue();
                deleteChatRoomBar.removeCallback(this);
            }
        });
        deleteChatRoomBar.show();
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


}
