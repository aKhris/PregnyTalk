package com.akhris.pregnytalk.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GetChatroomsListService extends IntentService {

    public static final String ACTION_UPDATE_WIDGETS = "com.akhris.pregnytalk.action.update_widgets";

    private static final String SERVICE_NAME = GetChatroomsListService.class.getSimpleName();

    public GetChatroomsListService() {
        super(SERVICE_NAME);
    }

    public static void startActionUpdateWidgets(Context context){
        Intent intent = new Intent(context, GetChatroomsListService.class);
        intent.setAction(ACTION_UPDATE_WIDGETS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent==null || intent.getAction()==null){return;}
        if(intent.getAction().equals(ACTION_UPDATE_WIDGETS)){
            handleActionUpdateWidgets();
        }
    }

    private void handleActionUpdateWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PregnyChatsListWidget.class));

        Query mUserChatsQuery =
                FirebaseContract
                        .getRoomsMetaDataReference()
                        .orderByChild(FirebaseContract.CHILD_ROOM_USERS_MAP+"/"+ MainActivity.sMyUid)
                        .startAt("-");
        mUserChatsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ChatRoom> chatRooms = new ArrayList<>();
                for (DataSnapshot chatRoomSnapshot:dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = chatRoomSnapshot.getValue(ChatRoom.class);
                    if(chatRoom!=null) {
                        chatRoom.setChatRoomId(chatRoomSnapshot.getKey());
                        chatRooms.add(chatRoom);
                    }
                }
                if(chatRooms.size()>0){
                    for (int appWidgetId:appWidgetIds) {
                        PregnyChatsListWidget.updateAppWidget(GetChatroomsListService.this, appWidgetManager, appWidgetId, chatRooms);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
