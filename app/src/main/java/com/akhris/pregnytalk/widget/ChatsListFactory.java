package com.akhris.pregnytalk.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.Message;
import com.akhris.pregnytalk.utils.DateUtils;

import java.util.List;

public class ChatsListFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final int ID_CHATNAME = R.id.widget_tv_chat_name;
    public static final int ID_TIMESTAMP = R.id.widget_tv_chat_message_timestamp;
    public static final int ID_LASTMESSAGE = R.id.widget_tv_chat_last_message;
    public static final int ID_USERNAME = R.id.widget_tv_chat_user_name;

    private List<ChatRoom> mChatRooms;
    private Context mContext;

    public ChatsListFactory(Context context, List<ChatRoom> chatRooms) {
        this.mContext = context;
        this.mChatRooms = chatRooms;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Log.d("ChatsListFactory", "onDataSetChanged: ");
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if(mChatRooms==null){ return 0; }
        return mChatRooms.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        ChatRoom chatRoom = mChatRooms.get(position);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_chats_list_item);
        views.setTextViewText(ID_CHATNAME, chatRoom.getNameExtended(mContext.getString(R.string.chatroom_name_format_string)));
        Message lastMessage = chatRoom.getLastMessage();
        if(lastMessage!=null) {
            views.setTextViewText(ID_LASTMESSAGE, lastMessage.getMessage());
            views.setTextViewText(ID_TIMESTAMP, DateUtils.formatTimeFromMillis(lastMessage.getTimeStamp()));
            views.setTextViewText(ID_USERNAME, lastMessage.getUserName());
        } else {
            views.setTextViewText(ID_LASTMESSAGE, mContext.getString(R.string.chat_room_created));
            views.setTextViewText(ID_TIMESTAMP, DateUtils.formatTimeFromMillis(chatRoom.getCreatedAt()));
        }

        Intent clickIntent = new Intent();
        clickIntent.putExtra(PregnyChatsListWidget.ITEM_CHATROOM_ID, chatRoom.getChatRoomId());
        views.setOnClickFillInIntent(R.id.widget_ll_root_layout, clickIntent);
        return views;
    }

    @Override public RemoteViews getLoadingView() { return null; }
    @Override public int getViewTypeCount() { return 1; }
    @Override public long getItemId(int position) { return position; }
    @Override public boolean hasStableIds() { return true; }
}
