package com.akhris.pregnytalk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.utils.DateUtils;

/**
 * RecyclerView Adapter representing chat room details
 */
public class ChatRoomDetailsListAdapter  extends RecyclerView.Adapter<ViewHolderFactory.TwoLineWithIconItemViewHolder> implements ItemClickListener {

    // Position constants
    private static final int POSITION_CHAT_NAME=0;
    private static final int POSITION_CHAT_DESCRIPTION=1;
    private static final int POSITION_CREATED_AT=2;
    private static final int POSITION_LOCATION=3;
    private static final int POSITION_TYPE=4;

    // Not using a List<> variable here, but every row is delivered from the appropriate
    // property of a ChatRoom object. So it is needed to define item count manually.
    private static final int LIST_ITEM_COUNT=5;

    private ChatRoom mChatRoom;
    private Callback mCallback;

    private boolean isAdminMode;

    public ChatRoomDetailsListAdapter(ChatRoom chatRoom, Callback callback) {
        this.mChatRoom = chatRoom;
        this.mCallback = callback;
        // mChatRoom.getAdminId() == null means that this is a private room with two users
        // and no one is the admin, so any of them can change room details
        isAdminMode = mChatRoom.getAdminId() == null || mChatRoom.getAdminId().equals(MainActivity.sMyUid);
    }

    /**
     * Using standard Material two lined list item with icon on the left for every row
     */
    @NonNull
    @Override
    public ViewHolderFactory.TwoLineWithIconItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateTwoLineWithIconViewHolder(parent, this);
    }

    /**
     * Splitting binding view holder depending on kind of info it's representing to different methods
     */
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

    /**
     * Set chat type information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
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

    /**
     * Set chat location information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindChatLocation(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_location_title);
        holder.icon.setImageResource(R.drawable.ic_location_on_black_24dp);
        if (mChatRoom.getLocation() == null || mChatRoom.getLocation().getName()==null || mChatRoom.getLocation().getName().length() == 0) { return; }
        holder.topText.setText(mChatRoom.getLocation().getName());
    }

    /**
     * Set chat created at information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindChatCreatedAt(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_created_at);
        holder.topText.setText(String.format("%s %s", DateUtils.formatDateFromMillis(mChatRoom.getCreatedAt()), DateUtils.formatTimeFromMillis(mChatRoom.getCreatedAt())));
        holder.icon.setImageResource(R.drawable.ic_baseline_calendar_today_24px);
    }

    /**
     * Set chat description information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindChatDescription(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_description);
        if (mChatRoom.getDescription() == null || mChatRoom.getDescription().length() == 0) { return; }
        holder.topText.setText(mChatRoom.getDescription());
    }

    /**
     * Set chat name information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindChatName(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.bottomText.setText(R.string.chat_info_name);
        if (mChatRoom.getName()==null || mChatRoom.getName().length()==0) { return; }
        holder.topText.setText(mChatRoom.getName());
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

    /**
     * Callbacks for interaction with this details list
     */
    public interface Callback{
        void onChatNameClick(String name, boolean isAdminMode);
        void onChatDescrClick(String description, boolean isAdminMode);
        void onLocationClick(boolean isAdminMode);
        void onTypeClick(int oldType, boolean isAdminMode);
    }
}
