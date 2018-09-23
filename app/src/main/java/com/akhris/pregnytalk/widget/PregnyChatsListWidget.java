package com.akhris.pregnytalk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;

import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 */
public class PregnyChatsListWidget extends AppWidgetProvider {

    private static final String ACTION_ON_CLICK = "com.akhris.pregnytalk.action.on_item_click";
    public static final String ITEM_CHATROOM_ID = "item_chatroom_id";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, ArrayList<ChatRoom> chatRoomList) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pregny_chats_list_widget);

        // Setting remote adapter for listview:
        Intent intent = new Intent(context, ChatsListService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatsListService.EXTRA_CHATROOMS_LIST, chatRoomList);
        intent.putExtra(ChatsListService.BUNDLE_CHATROOMS_LIST, bundle);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
        views.setRemoteAdapter(R.id.widget_lv_chats_list, intent);

        Intent startActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_tv_title, pendingIntent);

        Intent listClickIntent = new Intent(context, PregnyChatsListWidget.class);
        listClickIntent.setAction(ACTION_ON_CLICK);
        PendingIntent listClickPIntent = PendingIntent.getBroadcast(context, 0,
                listClickIntent, 0);
        views.setPendingIntentTemplate(R.id.widget_lv_chats_list, listClickPIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        GetChatroomsListService.startActionUpdateWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction()==null){return;}
        if (intent.getAction().equals(ACTION_ON_CLICK)){
            String chatRoomId = intent.getStringExtra(ITEM_CHATROOM_ID);
            if(chatRoomId!=null && chatRoomId.length()>0) {
                MainActivity.startActivityAndShowChat(context, chatRoomId);
            }
        }
    }
}

