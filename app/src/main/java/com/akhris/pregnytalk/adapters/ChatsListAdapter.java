package com.akhris.pregnytalk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.ViewGroup;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.AdaptersClickListeners.ItemClickListener;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.utils.DateUtils;
import com.akhris.pregnytalk.utils.SharedPrefUtils;

/**
 * RecyclerView's adapter implementation for list of chats
 */
public class ChatsListAdapter extends RecyclerView.Adapter<ViewHolderFactory.ChatsListItemHolder>{

    // Using SortedList to automatically sort ChatRooms mainly by the Message time
    // to display chat room with the last message - at the top of the list.
    // For more information about sorting refer to ChatRoom.compareTo() method
    private SortedList<ChatRoom> mChatRooms;
    private ItemClickListener itemClickListener;
    private boolean wasBouncedAfterAdapterCreation=false;

    public ChatsListAdapter(ItemClickListener clickListener){
        SortedListAdapterCallback<ChatRoom> mSortedListCallback = new SortedListAdapterCallback<ChatRoom>(this) {
            @Override
            public int compare(ChatRoom o1, ChatRoom o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(ChatRoom oldRoom, ChatRoom newRoom) {
                if (newRoom.getLastMessage() == null || oldRoom.getLastMessage() == null) {
                    return false;
                }
                return oldRoom.getName().equals(newRoom.getName())
                        && oldRoom.getLastMessage().equals(newRoom.getLastMessage());
            }

            @Override
            public boolean areItemsTheSame(ChatRoom item1, ChatRoom item2) {
                return item1.getChatRoomId().equals(item2.getChatRoomId());
            }
        };
        mChatRooms = new SortedList<>(ChatRoom.class, mSortedListCallback);
        this.itemClickListener = clickListener;
    }

    public void addChatRoom(ChatRoom chatRoom){
        if(chatRoom==null){return;}
        mChatRooms.add(chatRoom);
    }


    public void removeChatRoom(ChatRoom chatRoom) {
        if(chatRoom==null){return;}
        mChatRooms.remove(chatRoom);
    }

    public void removeChatRoom(int position){
        mChatRooms.removeItemAt(position);
    }

    public void updateChatRoom(ChatRoom chatRoom) {
        if(chatRoom==null){return;}
        int index = findIndexManually(chatRoom);
        if(index==SortedList.INVALID_POSITION){return;}
        mChatRooms.updateItemAt(index, chatRoom);
    }

    /**
     * This method is used when the updating chatroom is needed,
     * since it's usually needed when new message is arrived, standard SortedList.indexOf
     * returns INVALID_POSITION, maybe because of that it mainly use last message timestamp as a
     * comparing property.
     * @param chatRoom - ChatRoom object to find index for
     * @return index found for ChatRoom object or INVALID_POSITION
     */
    private int findIndexManually(ChatRoom chatRoom){
        for (int i = 0; i < mChatRooms.size(); i++) {
            ChatRoom cr = mChatRooms.get(i);
            if(cr.getChatRoomId().equals(chatRoom.getChatRoomId())){
                return i;
            }
        }
        return SortedList.INVALID_POSITION;
    }

    public ChatRoom getChatRoom(int position){
        return mChatRooms.get(position);
    }

    public String getChatRoomId(int position){
        return mChatRooms.get(position).getChatRoomId();
    }

    @NonNull
    @Override
    public ViewHolderFactory.ChatsListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateChatsListItemViewHolder(parent, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFactory.ChatsListItemHolder holder, int position) {
        ChatRoom chatRoom = mChatRooms.get(position);
        holder.chatName.setText(chatRoom.getNameExtended(holder.itemView.getContext().getString(R.string.chatroom_name_format_string)));
        if(chatRoom.getLastMessage()!=null){
            holder.chatLastMessageText.setText(chatRoom.getLastMessage().getMessage());
            holder.chatTimeStamp.setText(DateUtils.formatTimeFromMillis(chatRoom.getLastMessage().getTimeStamp()));
            holder.chatUserName.setText(chatRoom.getLastMessage().getUserName());
        } else {
            holder.chatLastMessageText.setText(R.string.chat_room_created);
            holder.chatTimeStamp.setText(DateUtils.formatTimeFromMillis(chatRoom.getCreatedAt()));
        }

        if(!SharedPrefUtils.wasBounced(holder.itemView.getContext(), this.getClass()) && !wasBouncedAfterAdapterCreation){
            holder.bounce();
            wasBouncedAfterAdapterCreation = true;
        }
        if(holder.wasSwiped){
            holder.releaseSwiped();
        }
    }

    @Override
    public int getItemCount() {
        return mChatRooms.size();
    }

    public void clear() {
        mChatRooms.clear();
    }

}
