package com.akhris.pregnytalk.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.ChatsListFromMapAdapter;
import com.akhris.pregnytalk.contract.ChatRoom;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *  Fragment representing chats showed on a map in a form of the list.
 *  Contains just the RecyclerView for chats list.
 */
public class ChatLocationListFragment extends Fragment {

    @BindView(R.id.rv_chats_location_list) RecyclerView rvChatsOnMapList;

    private ChatsListFromMapAdapter mAdapter;


    public ChatLocationListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_location_list, container, false);
        ButterKnife.bind(this, rootView);
        this.mAdapter = new ChatsListFromMapAdapter();
        rvChatsOnMapList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvChatsOnMapList.setAdapter(mAdapter);
        return rootView;
    }


    /**
     * Method is called when user moves the map camera
     * @param chatRooms - List of Chatrooms that are currently visible on a map
     */
    public void swipeList(List<ChatRoom> chatRooms){
        mAdapter.swipeChatRooms(chatRooms);
    }


}
