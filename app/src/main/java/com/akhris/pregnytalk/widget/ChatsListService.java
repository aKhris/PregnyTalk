package com.akhris.pregnytalk.widget;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViewsService;

import com.akhris.pregnytalk.contract.ChatRoom;

import java.util.ArrayList;

public class ChatsListService extends RemoteViewsService {

    public static final String EXTRA_CHATROOMS_LIST = "extra_chatrooms_list";
    public static final String BUNDLE_CHATROOMS_LIST = "bundle_chatrooms_list";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        if(!intent.hasExtra(BUNDLE_CHATROOMS_LIST)){return null;}
        Bundle bundle = intent.getBundleExtra(BUNDLE_CHATROOMS_LIST);
        @SuppressWarnings("unchecked")
        ArrayList<ChatRoom> chatRooms = (ArrayList<ChatRoom>)bundle.getSerializable(EXTRA_CHATROOMS_LIST);
        return new ChatsListFactory(this, chatRooms);
    }
}
