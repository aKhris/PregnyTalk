package com.akhris.pregnytalk.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.AdaptersClickListeners.ItemClickListener;
import com.akhris.pregnytalk.adapters.ChatsListAdapter;
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
import butterknife.Optional;

/**
 * Fragment representing list of chats the user takes part in (showed on the app start)
 */
public class ChatsListFragment extends NavigationFragment
    implements CreateChatFragment.Callback, ItemClickListener, SwipeableRecyclerView.SwipeCallbacks, NavigationManagerCallback, MenuItem.OnMenuItemClickListener {



    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.rv_chats_list) SwipeableRecyclerView mChatsList;
    @Nullable @BindView(R.id.nsv_chats_list) NestedScrollView mNestedChatsList;
    @Nullable @BindView(R.id.fl_chat_container) FrameLayout mChatContainer;
    @Nullable @BindView(R.id.tv_chats_list_hint) TextView mHint;

    // Argument passed to new instance of a fragment
    private static final String ARG_CHAT_ROOM_ID = "chat_room_id";
    private String mNavigateToChatRoomId;

    private static final String BUNDLE_CURRENT_CHAT_ROOM_ID = "current_chat_room_id";
    private String mCurrentChatRoomId;
    private String mLoadOnStartChatRoomId;
    private MenuItem mInfoItem;

    // Firebase
    private DatabaseReference mRoomMetaDataReference;
    private ChildEventListener mUserRoomIdsChildListener;
    private Query mUserChatsQuery;

    // RecyclerView's adapter
    private ChatsListAdapter mAdapter;

    // Saving scroll position on screen rotation
    private static final String BUNDLE_SCROLL_POSITION = "scroll_position";
    // Waiting this time before setting saved scroll position to NestedScrollView
    // During this time the list has to be populated with items
    private static final int DELAY_SCROLL_MILLIS = 1000;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Callback){
            mLoadOnStartChatRoomId = ((Callback)context).getChatIDToShowOnStart();
        }
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
        if(savedInstanceState!=null){
            mCurrentChatRoomId = savedInstanceState.getString(BUNDLE_CURRENT_CHAT_ROOM_ID, null);
        }
        mAdapter = new ChatsListAdapter(this);
        setupReferences();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chatslist_tablet, menu);
        menu.findItem(R.id.action_add_new_chat).setOnMenuItemClickListener(this);
        mInfoItem = menu.findItem(R.id.action_info);
        mInfoItem.setOnMenuItemClickListener(this);
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
        // Set menu instead of floating action button in tablet mode:
        setHasOptionsMenu(mChatContainer!=null);


        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mChatsList.setLayoutManager(manager);
        mChatsList.setItemAnimator(null);
        mChatsList.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mChatsList.getContext(), manager.getOrientation());
        mChatsList.initSwiping(this);
        mChatsList.addItemDecoration(dividerItemDecoration);

        if(savedInstanceState!=null && savedInstanceState.containsKey(BUNDLE_SCROLL_POSITION)) {
            if(mNestedChatsList!=null) {
                int[] scrollPos = savedInstanceState.getIntArray(BUNDLE_SCROLL_POSITION);
                if (scrollPos != null) {
                    mNestedChatsList.postDelayed(() -> mNestedChatsList.scrollTo(scrollPos[0], scrollPos[1]), DELAY_SCROLL_MILLIS);
                }
            } else {
                int visiblePos = savedInstanceState.getInt(BUNDLE_SCROLL_POSITION);
                if(visiblePos>RecyclerView.NO_POSITION){
                    mChatsList.postDelayed(()->manager.scrollToPosition(visiblePos), DELAY_SCROLL_MILLIS);
                }
            }
        }
        return rootView;
    }

    /**
     * Clicking on floating action button: show dialog to add new chat
     */
    @Optional
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
                    if(chatRoomId.equals(mLoadOnStartChatRoomId)){
                        navigateToChat(chatRoomId);
                        mLoadOnStartChatRoomId = null;
                        return;
                    }
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
                                        navigateToChat(chatRoomId);
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
        navigateToChat(chatRoomId);
    }

    private void navigateToChat(String chatRoomId){
        this.mCurrentChatRoomId = chatRoomId;
        if(mChatContainer==null) {
            getNavigationManager().navigateToChat(chatRoomId);
        } else {
            getNavigationManager().navigateToChat(chatRoomId, getChildFragmentManager());
        }
        if(mInfoItem!=null && !mInfoItem.isVisible()){
            mInfoItem.setVisible(true);
        }
        if(mHint!=null){
            mHint.setVisibility(View.GONE);
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
        deleteChatRoomBar.setAction(R.string.snackbar_undo, v -> mAdapter.addChatRoom(chatRoom)
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_CURRENT_CHAT_ROOM_ID, mCurrentChatRoomId);
        if(mNestedChatsList!=null) {
            outState.putIntArray(BUNDLE_SCROLL_POSITION, new int[]{mNestedChatsList.getScrollX(), mNestedChatsList.getScrollY()});
        } else {
            outState.putInt(BUNDLE_SCROLL_POSITION,
                    ((LinearLayoutManager)mChatsList.getLayoutManager()).findFirstVisibleItemPosition());
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId()==R.id.action_add_new_chat){
            addNewChat();
            return true;
        }
        if(item.getItemId()==R.id.action_info){
            if(mCurrentChatRoomId!=null){
                getNavigationManager().navigateToChatInfo(mCurrentChatRoomId);
            }
            return true;
        }
        return false;
    }

    /**
     * Callback to get Chatroom's ID from MainActivity when user clicks on a widget list
     * of chats and starts MainActivity with that chat id in the intent.
     */
    public interface Callback{
        String getChatIDToShowOnStart();
    }
}
