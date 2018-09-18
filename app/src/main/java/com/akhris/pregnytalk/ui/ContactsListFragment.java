package com.akhris.pregnytalk.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.ContactsItemClickListener;
import com.akhris.pregnytalk.adapters.ContactsListAdapter;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.akhris.pregnytalk.contract.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment representing list of contacts.
 * Used in ContactsFragment and in ChatInfoFragment.
 */
public class ContactsListFragment extends Fragment
        implements ContactsItemClickListener, SwipeableRecyclerView.SwipeCallbacks {

    @BindView(R.id.rv_contacts_list)
    SwipeableRecyclerView mContactsList;
    @BindView(R.id.pb_contacts_list_progressbar) ProgressBar mProgressBar;

    // Argument passed to new instance of a fragment
    private static final String ARG_CHILD_PATH = "child_path";
    private String mChildPath;

    //Firebase
    private Query mContactsQuery;
    private ContactsListAdapter mAdapter;
    private ChildEventListener mMyContactsListener;
    private DatabaseReference mRoomMetaDataReference;

    //Callback is used to navigate to chat when clicked on a chat button in a contacts list
    private NavigationManagerCallback mCallback;

    /**
     * Making new instance of ContactsListFragment
     * @param childPath - Firebase database path to search users in.
     *                  Since the fragment is used to show users in user's contact list and users
     *                  of the chat room, this parameter can be one of the following:
     *                  - "room-meta-data/<ROOM ID>/usersMap"
     *                  - "users/<USER ID>/contacts"
     * @return new instance of ContactsListFragment
     */
    public static ContactsListFragment newInstance(@Nullable String childPath) {
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_PATH, childPath);
        ContactsListFragment fragment = new ContactsListFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Ensuring that all needed data is provided.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null && getArguments().containsKey(ARG_CHILD_PATH)){
            mChildPath = getArguments().getString(ARG_CHILD_PATH);
        } else {
            throw new UnsupportedOperationException("The path to contacts list must be provided!");
        }
        if(getParentFragment()!=null && getParentFragment() instanceof NavigationManagerCallback){
            mCallback = (NavigationManagerCallback) getParentFragment();
        } else {
            throw new UnsupportedOperationException("Parent should implement NavigationManagerCallback!");
        }
    }


    /**
     * Initializing the RecyclerView
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts_list, container, false);
        ButterKnife.bind(this, rootView);
        mAdapter = new ContactsListAdapter(this);
        mContactsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mContactsList.setAdapter(mAdapter);
        mContactsList.initSwiping(this);
        setupReferences();
        return rootView;
    }

    /**
     * Firebase initialization
     */
    private void setupReferences() {
        //Make reference to given contacts map <String, String>
        //with UserId as a Key String and user name as a Value String
        DatabaseReference mUserContactsReference =
                FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child(mChildPath);

        //Ordering this contacts by name (Value String)
        mContactsQuery =
                mUserContactsReference
                        .orderByValue();

        //Make chatrooms reference to handle clicks on "send message button"
        mRoomMetaDataReference =
                FirebaseContract
                        .getRoomsMetaDataReference();



    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.VISIBLE);
        setListeners();
    }

    /**
     * Making listener (if it is null) and attaching it to reference
     */
    private void setListeners() {
        if(mMyContactsListener==null) {
            mMyContactsListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //Get User Id from given Contacts Map as a Key String
                    String userId = dataSnapshot.getKey();
                    if(userId==null){return;}

                    //Making new reference to user's info with such ID
                    final DatabaseReference contactReference = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child(FirebaseContract.CHILD_USERS)
                            .child(userId);

                    //Get the information about this user from that reference
                    contactReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Add this user to the RecyclerView
                            User user = dataSnapshot.getValue(User.class);
                            if(user!=null){
                                user.setuId(userId);
                                mAdapter.addUser(user);
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }

                @Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                @Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
            };
            mContactsQuery.addChildEventListener(mMyContactsListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        removeListeners();
        mAdapter.clear();
    }

    //Detaching listener and deleting it
    private void removeListeners() {
        if(mMyContactsListener!=null){
            mContactsQuery.removeEventListener(mMyContactsListener);
            mMyContactsListener=null;
        }
    }

    /**
     *  Handling clicking on send message button
     *  Solution of implementing filtering like that got here:
     *  https://stackoverflow.com/a/45992586/7635275
     *  */
    @Override
    public void onSendMessageClick(int position) {
        //Get user to start a chat with from adapter (for given position)
        User user = mAdapter.getUser(position);

        //Querying for a chatroom with sMyUid in contacts list
        mRoomMetaDataReference
                .orderByChild("usersMap/"+ MainActivity.sMyUid)
                .startAt("-")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Then make remaining filtering here:
                        boolean chatRoomFound=false;
                        for(DataSnapshot childSnapshot:dataSnapshot.getChildren()){
                            //Iterating through chatrooms with sMyUid in the contacts
                            ChatRoom chatRoom = childSnapshot.getValue(ChatRoom.class);
                            if(chatRoom==null){continue;}
                            if(chatRoom.getType()==ChatRoom.TYPE_PUBLIC){continue;}     //We're looking only for private chats
                            if(chatRoom.getUsersMap().size()!=2){continue;}             //with two users
                            if(chatRoom.getUsersMap().containsKey(user.getuId())){      //with one of them - user from the contacts list
                                // We found private chat room with two users: me and "user"
                                // Navigate to this chat:
                                chatRoomFound = true;
                                navigateToPrivateChatRoom(childSnapshot.getKey());
                                break;
                            }
                        }
                        if(!chatRoomFound){
                            //No chat room was found, so create one!
                            createNewPrivateChatRoom(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Navigate to newly added or existing private chat room.
     * If current device is tablet - navigate to ChatsList and open inside it ChatRoom or
     * if current device is not a tablet - just open this chat.
     * @param chatRoomId - id of the chat room to navigate to
     */
    private void navigateToPrivateChatRoom(String chatRoomId) {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if(isTablet){
            mCallback
                    .getNavigationManager()
                    .navigateToChatsList(chatRoomId);
        } else {
            mCallback
                    .getNavigationManager()
                    .navigateToChat(chatRoomId);
        }
    }

    /**
     * Creating new private chat room with given user:
     * 1. Making instance of ChatRoom class and filling in the usersmap;
     * 2. Making new record in the Firebase Database and get the key;
     * 3. Saving chatroom there;
     * 4. Navigate to newly created chatroom;
     * @param anotherUser - user to make private chat with
     */
    private void createNewPrivateChatRoom(User anotherUser) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setType(ChatRoom.TYPE_PRIVATE);
        HashMap<String, String> usersMap = new HashMap<>();
        usersMap.put(anotherUser.getuId(), anotherUser.getName());
        usersMap.put(MainActivity.sMyUid, MainActivity.sMe.getName());
        chatRoom.setUsersMap(usersMap);
        chatRoom.setCreatedAt(System.currentTimeMillis());
        String addedChatroomId = mRoomMetaDataReference.push().getKey();
        if(addedChatroomId==null){return;}

        mRoomMetaDataReference
                .child(addedChatroomId)
                .setValue(chatRoom);

        navigateToPrivateChatRoom(addedChatroomId);
    }

    /**
     * Showing user info when clicking on list item
     */
    @Override
    public void onItemClick(int position) {
        User user = mAdapter.getUser(position);
        String uId = user.getuId();
        if(uId==null){return;}
        startActivity(UserInfoActivity.getUserIntent(getContext(), uId));
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
