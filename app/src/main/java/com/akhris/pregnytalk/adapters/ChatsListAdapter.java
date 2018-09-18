package com.akhris.pregnytalk.adapters;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ViewGroup;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsListAdapter extends RecyclerView.Adapter<ViewHolderFactory.ChatsListItemHolder>{

    private List<ChatRoom> chatRooms;
    private ItemClickListener itemClickListener;
    private boolean mWasBounced=false;

    public ChatsListAdapter(ItemClickListener clickListener){
        chatRooms = new ArrayList<>();
        this.itemClickListener = clickListener;
    }

    public void addChatRoom(ChatRoom chatRoom){
        if(chatRoom==null){return;}
        List<ChatRoom> newList = new ArrayList<>(chatRooms);
        newList.add(chatRoom);
        updateList(newList);
    }

    public void removeChatRoom(ChatRoom chatRoom) {
        if(chatRoom==null){return;}
        List<ChatRoom> newList = new ArrayList<>(chatRooms);
        newList.remove(chatRoom);
        updateList(newList);
    }

    public void updateChatRoom(ChatRoom chatRoom) {
        if(chatRoom==null){return;}
        List<ChatRoom> newList = new ArrayList<>(chatRooms);
        int index = newList.indexOf(chatRoom);
        if(index==-1){return;}
        newList.set(index, chatRoom);
        updateList(newList);
    }

    private void updateList(List<ChatRoom> newList){
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new ChatsListCallback(chatRooms, newList), true);
        chatRooms.clear();
        chatRooms.addAll(newList);
        result.dispatchUpdatesTo(this);
    }

    public ChatRoom getChatRoom(int position){
        return chatRooms.get(position);
    }

    public String getChatRoomId(int position){
        return chatRooms.get(position).getChatRoomId();
    }

    @NonNull
    @Override
    public ViewHolderFactory.ChatsListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateChatsListItemViewHolder(parent, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFactory.ChatsListItemHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        holder.chatName.setText(chatRoom.getName());
        if(chatRoom.getLastMessage()!=null){
            holder.chatLastMessageText.setText(chatRoom.getLastMessage().getMessage());
            holder.chatTimeStamp.setText(DateUtils.formatTimeFromMillis(chatRoom.getLastMessage().getTimeStamp()));
            holder.chatUserName.setText(chatRoom.getLastMessage().getUserName());
        } else {
            holder.chatLastMessageText.setText(R.string.chat_room_created);
            holder.chatTimeStamp.setText(DateUtils.formatTimeFromMillis(chatRoom.getCreatedAt()));
        }

        if(!mWasBounced) {
            holder.bounce();
            mWasBounced = true;
        }
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public void clear() {
        chatRooms.clear();
        notifyDataSetChanged();
    }


    class ChatsListCallback extends DiffUtil.Callback{

        private List<ChatRoom> oldList;
        private List<ChatRoom> newList;

        ChatsListCallback(List<ChatRoom> oldList, List<ChatRoom> newList) {
            this.oldList = oldList;
            this.newList = newList;
            Collections.sort(this.newList);
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ChatRoom oldRoom = oldList.get(oldItemPosition);
            ChatRoom newRoom = newList.get(newItemPosition);
            return oldRoom.getChatRoomId().equals(newRoom.getChatRoomId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ChatRoom oldRoom = oldList.get(oldItemPosition);
            ChatRoom newRoom = newList.get(newItemPosition);
            if(newRoom.getLastMessage()==null || oldRoom.getLastMessage()==null){return false;}
            return oldRoom.getName().equals(newRoom.getName())
                    && oldRoom.getLastMessage().equals(newRoom.getLastMessage());
        }
    }
}
