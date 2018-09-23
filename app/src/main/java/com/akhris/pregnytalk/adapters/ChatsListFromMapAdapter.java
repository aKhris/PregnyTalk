package com.akhris.pregnytalk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;

import java.util.ArrayList;
import java.util.List;

public class ChatsListFromMapAdapter extends RecyclerView.Adapter<ViewHolderFactory.ChatsListItemFromMapHolder> implements AdaptersClickListeners.ItemClickListener {

    private List<ChatRoom> mChatRooms;

    public ChatsListFromMapAdapter() {
        this.mChatRooms = new ArrayList<>();
        setHasStableIds(true);
    }

    public void swipeChatRooms(List<ChatRoom> chatRooms){
        this.mChatRooms = chatRooms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolderFactory.ChatsListItemFromMapHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateChatsListFromMapItemViewHolder(parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFactory.ChatsListItemFromMapHolder holder, int position) {
        ChatRoom chatRoom = mChatRooms.get(position);
        int userCount = 0;
        if(chatRoom.getUsersMap()!=null){
            userCount = chatRoom.getUsersMap().size();
        }
        String locString = chatRoom.getLocation().getName();
        String userCountString =
                String.format(holder.itemView.getContext().getString(R.string.users_count_format_string), userCount);

        holder.chatUsersCount.setText(userCountString);
        holder.chatLocation.setText(locString);
        holder.chatName.setText(chatRoom.getName());
    }

    @Override
    public int getItemCount() {
        return mChatRooms.size();
    }

    @Override
    public void onItemClick(int position) {

    }
}
