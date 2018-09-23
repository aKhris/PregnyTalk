package com.akhris.pregnytalk.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.AdaptersClickListeners.MessageClickListener;
import com.akhris.pregnytalk.contract.Message;
import com.akhris.pregnytalk.utils.DateUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing adapter for list of messages inside chat room
 */
public class MessagesListAdapter extends RecyclerView.Adapter<ViewHolderFactory.MessageItemHolder>{

    private List<Message> messagesList;
    // Callback that fired when user clicks on user name shown on the message view - to show
    // UserInfoActivity with selected user.
    private MessageClickListener mMessageClickListener;

    public MessagesListAdapter(MessageClickListener mMessageClickListener) {
        messagesList = new ArrayList<>();
        this.mMessageClickListener = mMessageClickListener;
    }

    public void addMessage(Message message){
        messagesList.add(message);
        notifyItemInserted(messagesList.size());
    }

    @NonNull
    @Override
    public ViewHolderFactory.MessageItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateMessageItemViewHolder(parent, mMessageClickListener);
    }

    /**
     * Binding message views to Message object: user name, message text, timestamp and
     * picture (if exists)
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolderFactory.MessageItemHolder holder, int position) {
        Context context = holder.itemView.getContext();

        Message message = messagesList.get(position);

        holder.userName.setText(message.getUserName());
        holder.messageText.setText(message.getMessage());
        holder.timeStamp.setText(DateUtils.formatTimeFromMillis(message.getTimeStamp()));
        if(message.getPictureUrl()!=null && message.getPictureUrl().length()>0) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.picture.setVisibility(View.VISIBLE);
            int picWidth = context.getResources().getInteger(R.integer.message_pic_width);
            int picHeight = context.getResources().getInteger(R.integer.message_pic_height);
            Picasso.get()
                    .load(message.getPictureUrl())
                    .resize(picWidth, picHeight)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(holder.picture, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            holder.progressBar.setVisibility(View.GONE);
                            holder.picture.setVisibility(View.GONE);
                        }
                    });
        } else {
            holder.picture.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }

        // Set message alignment:
        // From the right - for outcoming messages;
        // From the left - for incoming;
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = message.getUserId().equals(MainActivity.sMyUid) ? Gravity.END : Gravity.START;
        holder.rootCard.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Message getMessage(int position) {
        return messagesList.get(position);
    }

    public void clear() {
        this.messagesList.clear();
        notifyDataSetChanged();
    }
}
