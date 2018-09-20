package com.akhris.pregnytalk.adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akhris.pregnytalk.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Util class for handling view holder's operations
 */
public class ViewHolderFactory {

    /**
     * Creating standard two lined list item with icon on the left
     */
    public static TwoLineWithIconItemViewHolder onCreateTwoLineWithIconViewHolder(@NonNull ViewGroup parent, ItemClickListener itemClickListener) {

                return new TwoLineWithIconItemViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_two_line_icon_secondary_text, parent, false), itemClickListener);
    }

    /**
     * Creating view holder for chats list
     */
    public static ChatsListItemHolder onCreateChatsListItemViewHolder(@NonNull ViewGroup parent, ItemClickListener itemClickListener){
        return new ChatsListItemHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chats_list_item, parent, false), itemClickListener);
    }

    /**
     * Creating view holder for chats list in the MapAndListFragment
     */
    public static ChatsListItemFromMapHolder onCreateChatsListFromMapItemViewHolder(@NonNull ViewGroup parent, ItemClickListener itemClickListener){
        return new ChatsListItemFromMapHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chats_list_item, parent, false), itemClickListener);
    }

    /**
     * Creating view holder for message in the chat
     */
    public static MessageItemHolder onCreateMessageItemViewHolder(@NonNull ViewGroup parent, MessageClickListener messageClickListener){
        return new MessageItemHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_list_item, parent, false),
                messageClickListener
        );
    }

    /**
     * Creating view holder for contacts list
     */
    public static ContactsItemHolder onCreateContactsItemViewHolder(@NonNull ViewGroup parent, ContactsItemClickListener contactsItemClickListener){
        return new ContactsItemHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.contacts_list_item, parent, false),
                contactsItemClickListener
        );
    }

    /**
     * Creating view holder for children list
     */
    public static ChildViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, ItemClickListener itemClickListener) {
        return new ChildViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.children_list_item, parent, false), itemClickListener);
    }

    /**
     * Implementation of view holders      ************************
     */

    public static class TwoLineWithIconItemViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.tv_two_line_item_item_text_top) TextView topText;
        @BindView(R.id.tv_two_line_item_text_bottom) TextView bottomText;
        @BindView(R.id.iv_two_line_item_icon) ImageView icon;

        TwoLineWithIconItemViewHolder(View itemView, final ItemClickListener itemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(getAdapterPosition()));
        }
    }


    public static class ChatsListItemHolder extends WithBackgroundHolder{
        @BindView(R.id.tv_chat_name)                TextView chatName;
        @BindView(R.id.tv_chat_user_name)           TextView chatUserName;
        @BindView(R.id.tv_chat_last_message)        TextView chatLastMessageText;
        @BindView(R.id.tv_chat_message_timestamp)   TextView chatTimeStamp;

        ChatsListItemHolder(View itemView, final ItemClickListener itemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(getAdapterPosition()));
        }
    }

    public static class ChatsListItemFromMapHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_chat_name)                TextView chatUsersCount;
        @BindView(R.id.tv_chat_user_name)           TextView chatName;
        @BindView(R.id.tv_chat_last_message)        TextView chatLocation;

        ChatsListItemFromMapHolder(View itemView, final ItemClickListener itemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(getAdapterPosition()));
        }
    }

    public static class MessageItemHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.cv_message_root_card) CardView rootCard;
        @BindView(R.id.tv_message_user_name) TextView userName;
        @BindView(R.id.tv_message_text) TextView messageText;
        @BindView(R.id.tv_message_timestamp) TextView timeStamp;
        @BindView(R.id.iv_message_picture) ImageView picture;
        @BindView(R.id.pb_message_progressbar) ProgressBar progressBar;

        MessageItemHolder(View itemView, MessageClickListener messageClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            userName.setOnClickListener(v->messageClickListener.onUserNameClick(getAdapterPosition()));
        }
    }

    public static class ContactsItemHolder extends WithBackgroundHolder{
        @BindView(R.id.iv_contacts_photo) ImageView userPhoto;
        @BindView(R.id.tv_contacts_name) TextView userName;
        @BindView(R.id.iv_contacts_send_message) ImageView sendMessage;



        ContactsItemHolder(View itemView, ContactsItemClickListener contactsItemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v->contactsItemClickListener.onItemClick(getAdapterPosition()));
            sendMessage.setOnClickListener(v->contactsItemClickListener.onSendMessageClick(getAdapterPosition()));
        }
    }

    public static class ChildViewHolder extends WithBackgroundHolder{

        @BindView(R.id.tv_two_line_item_item_text_top) TextView topText;
        @BindView(R.id.tv_two_line_item_text_bottom) TextView bottomText;
        @BindView(R.id.iv_two_line_item_icon) ImageView icon;

        ChildViewHolder(View itemView, final ItemClickListener itemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(getAdapterPosition()));
        }
    }

    /**
     * Base View Holder class that is used for "swipe to delete" functionality.
     * It ensures that Swipeable View Holder contains foreground view.
     * And also it has methods to animate bouncing and to release swiped state.
     * To use it just extend ViewHolder from this class and
     * add foreground view with id=R.id.foreground to item layout.
     */
    public static class WithBackgroundHolder extends RecyclerView.ViewHolder{

        public boolean wasSwiped = false;

        public ViewGroup foreground;

        WithBackgroundHolder(View itemView) {
            super(itemView);
            foreground = itemView.findViewById(R.id.foreground);
            if(foreground==null){
                throw new UnsupportedOperationException("To use WithBackgroundHolder itemview must contain foreground view with id=R.id.foreground!");
            }
        }

        public void bounce(){
            AnimatorSet bounceAnimator =
                    (AnimatorSet) AnimatorInflater.loadAnimator(itemView.getContext(), R.animator.foreground_bounce);
            bounceAnimator.setTarget(foreground);
            bounceAnimator.start();
        }

        public void releaseSwiped(){
            wasSwiped = false;
            foreground.animate().translationX(0f).start();
        }
    }
}
