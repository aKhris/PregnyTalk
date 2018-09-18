package com.akhris.pregnytalk.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.utils.DateUtils;

public class ChatRoomDetailsListAdapter  extends RecyclerView.Adapter<ViewHolderFactory.TwoLineWithIconItemViewHolder> implements ItemClickListener {

    private static final int POSITION_CHAT_NAME=0;
    private static final int POSITION_CHAT_DESCRIPTION=1;
    private static final int POSITION_CREATED_AT=2;
    private static final int POSITION_LOCATION=3;
    private static final int POSITION_TYPE=4;

    private static final int LIST_ITEM_COUNT=5;

    private ChatRoom mChatRoom;
    private Callback mCallback;

    private boolean isAdminMode;

    public ChatRoomDetailsListAdapter(ChatRoom chatRoom, Callback callback) {
        this.mChatRoom = chatRoom;
        this.mCallback = callback;
        if(mChatRoom.getAdminId()!=null) {
            isAdminMode = mChatRoom.getAdminId().equals(MainActivity.sMyUid);
        } else {
            isAdminMode = true;
        }
    }

    @NonNull
    @Override
    public ViewHolderFactory.TwoLineWithIconItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateTwoLineWithIconViewHolder(parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFactory.TwoLineWithIconItemViewHolder holder, int position) {
        switch (position){
            case POSITION_CHAT_NAME:
                bindChatName(holder);
                break;
            case POSITION_CHAT_DESCRIPTION:
                bindChatDescription(holder);
                break;
            case POSITION_CREATED_AT:
                bindChatCreatedAt(holder);
                break;
            case POSITION_LOCATION:
                bindChatLocation(holder);
                break;
            case POSITION_TYPE:
                bindChatType(holder);
                break;
        }
    }

    private void bindChatType(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_chat_type_title);
        holder.topText.setText(
                mChatRoom.getType()==ChatRoom.TYPE_PUBLIC?
                R.string.chat_info_chat_type_public:
                R.string.chat_info_chat_type_private
        );
        holder.icon.setImageResource(
                mChatRoom.getType()==ChatRoom.TYPE_PUBLIC?
                        R.drawable.ic_visibility_black_24dp:
                        R.drawable.ic_visibility_off_black_24dp
        );
    }

    private void bindChatLocation(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_location_title);
        holder.icon.setImageResource(R.drawable.ic_location_on_black_24dp);
        if (mChatRoom.getLocation() == null || mChatRoom.getLocation().getName()==null || mChatRoom.getLocation().getName().length() == 0) { return; }
        holder.topText.setText(mChatRoom.getLocation().getName());
    }

    private void bindChatCreatedAt(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_created_at);
        holder.topText.setText(String.format("%s %s", DateUtils.formatDateFromMillis(mChatRoom.getCreatedAt()), DateUtils.formatTimeFromMillis(mChatRoom.getCreatedAt())));
        holder.icon.setImageResource(R.drawable.ic_baseline_calendar_today_24px);
    }

    private void bindChatDescription(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_description);
        if (mChatRoom.getDescription() == null || mChatRoom.getDescription().length() == 0) { return; }
        holder.topText.setText(mChatRoom.getDescription());
    }

    private void bindChatName(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_name);
        if (mChatRoom.getName()==null || mChatRoom.getName().length()==0) { return; }
        holder.topText.setText(mChatRoom.getName());
    }

    private void bindChatUsers(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.topText.setText(R.string.chat_info_users);
        int usersCount=0;
        if(mChatRoom.getUsersMap()!=null){
            usersCount = mChatRoom.getUsersMap().size();
        }
        Context context = holder.itemView.getContext();

        holder.bottomText.setText(
                String.format(
                        context.getString(R.string.chat_info_users_total_count_format_string),
                        usersCount)
        );
    }


    @Override
    public int getItemCount() {
        return LIST_ITEM_COUNT;
    }

    @Override
    public void onItemClick(int position) {
        if(mCallback==null){return;}
        switch (position){
            case POSITION_CHAT_NAME:
                mCallback.onChatNameClick(mChatRoom.getName(), isAdminMode);
                break;
            case POSITION_CHAT_DESCRIPTION:
                mCallback.onChatDescrClick(mChatRoom.getDescription(), isAdminMode);
                break;
            case POSITION_LOCATION:
                mCallback.onLocationClick(isAdminMode);
                break;
            case POSITION_TYPE:
                mCallback.onTypeClick(mChatRoom.getType(), isAdminMode);
                break;
        }
    }

    public interface Callback{

        void onChatNameClick(String name, boolean isAdminMode);

        void onChatDescrClick(String description, boolean isAdminMode);

        void onLocationClick(boolean isAdminMode);

        void onTypeClick(int oldType, boolean isAdminMode);

    }
}
