package com.akhris.pregnytalk.adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akhris.pregnytalk.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewHolderFactory {


    public static TwoLineWithIconItemViewHolder onCreateTwoLineWithIconViewHolder(@NonNull ViewGroup parent, ItemClickListener itemClickListener) {

                return new TwoLineWithIconItemViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_two_line_icon_secondary_text, parent, false), itemClickListener);
    }

    public static ChatsListItemHolder onCreateChatsListItemViewHolder(@NonNull ViewGroup parent, ItemClickListener itemClickListener){
        return new ChatsListItemHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chats_list_item, parent, false), itemClickListener);
    }

    public static ChatsListItemFromMapHolder onCreateChatsListFromMapItemViewHolder(@NonNull ViewGroup parent, ItemClickListener itemClickListener){
        return new ChatsListItemFromMapHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chats_list_item, parent, false), itemClickListener);
    }

    public static MessageItemHolder onCreateMessageItemViewHolder(@NonNull ViewGroup parent, MessageClickListener messageClickListener){
        return new MessageItemHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_list_item, parent, false),
                messageClickListener
        );
    }

    public static ContactsItemHolder onCreateContactsItemViewHolder(@NonNull ViewGroup parent, ContactsItemClickListener contactsItemClickListener){
        return new ContactsItemHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.contacts_list_item, parent, false),
                contactsItemClickListener
        );
    }

    public static ChildrenItemViewHolder onCreateChildrenItemViewHolder(@NonNull ViewGroup parent, ChildrenClickListener childrenClickListener){
        return new ChildrenItemViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_two_line_children_material, parent, false),
                childrenClickListener
        );
    }

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

    public static class ChildrenItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_boys_count) TextView boysCount;
        @BindView(R.id.tv_girls_count) TextView girlsCount;
        @BindView(R.id.iv_babyboy) ImageView babyBoyIcon;
        @BindView(R.id.iv_babygirl) ImageView babyGirlIcon;
        @BindView(R.id.rv_children_list) RecyclerView childrenList;

        public ChildrenItemViewHolder(View itemView, final ChildrenClickListener childrenClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            babyBoyIcon.setOnClickListener(v->childrenClickListener.onAddBoyClick());
            babyGirlIcon.setOnClickListener(v->childrenClickListener.onAddGirlClick());
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
//        @BindView(R.id.tv_chat_message_timestamp)   TextView chatTimeStamp;

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
    
    public static class WithBackgroundHolder extends RecyclerView.ViewHolder{

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
    }
}
