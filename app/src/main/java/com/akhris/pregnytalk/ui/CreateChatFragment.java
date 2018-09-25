package com.akhris.pregnytalk.ui;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

import com.akhris.pregnytalk.App;
import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.PlaceData;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog for creating new chat allowing user to set name of the chat and its location.
 */
public class CreateChatFragment extends DialogFragment implements PlacesSearchView.Callback {

    @BindView(R.id.psv_new_chat_place_search) PlacesSearchView mPlaceSearch;
    @BindView(R.id.et_new_chat_name) EditText mChatName;
    @BindView(R.id.ib_new_chat_map) ImageButton mMapButton;

    private static final int RC_MAP_SEARCH_USER_LOCATION = 10;

    // Argument passed to new instance of a fragment
    private static final String BUNDLE_CHAT_ROOM = "bundle_chat_room";
    private ChatRoom mChatRoom;

    private View mRootView;
    private Callback mCallback;

    private boolean mShowMapIcon=true;

    /**
     * Creating new instance of CreateChatFragment
     * @param chatRoom - pass ChatRoom to edit it's name or location
     *                 - pass null to make new ChatRoom
     */
    public static CreateChatFragment newInstance(@Nullable ChatRoom chatRoom) {
        CreateChatFragment fragment = new CreateChatFragment();
        if(chatRoom!=null) {
            Bundle args = new Bundle();
            args.putSerializable(BUNDLE_CHAT_ROOM, chatRoom);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public CreateChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Callback){
            mCallback = (Callback)context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null && getArguments().containsKey(BUNDLE_CHAT_ROOM)){
            mChatRoom = (ChatRoom) getArguments().getSerializable(BUNDLE_CHAT_ROOM);
            mShowMapIcon=false;
        } else {
            mChatRoom = new ChatRoom();
        }
        checkChatRoom();
        if(getParentFragment() instanceof Callback){
            mCallback = (Callback) getParentFragment();
        }

        if(mCallback==null){
            String contextString = getContext()==null?"":(getContext().toString()+" ");
            String parentFragmentString = getParentFragment()==null?"":(getParentFragment().toString()+" ");
            throw new UnsupportedOperationException(
                    String.format("Either context %sor parent fragment %sshould implement Callback",
                            contextString, parentFragmentString)
            );
        }
    }

    /**
     * Making Dialog without additional space for title (because we have the custom one).
     * Solution got here:
     * https://stackoverflow.com/a/15279400/7635275
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if(dialog.getWindow()==null){return dialog;}
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /**
     * Checking if ChatRoom has all sufficient data.
     * Since we are creating new chat room here it must have user's id as admin id
     * and have user in the usersMap.
     */
    private void checkChatRoom() {
        mChatRoom.setAdminId(MainActivity.sMyUid);
        HashMap<String, String> usersMap = new HashMap<>();
        usersMap.put(MainActivity.sMyUid, MainActivity.sMe.getName());
        mChatRoom.setUsersMap(usersMap);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_create_chat, container, false);
        ButterKnife.bind(this, mRootView);
            if(mChatRoom.getLocation()!=null) {
                mPlaceSearch.setQuery(mChatRoom.getLocation().getName(), false);
            }
        mPlaceSearch.setmCallback(this);
            if(!mShowMapIcon){
                mMapButton.setVisibility(View.GONE);
            }
        return mRootView;
    }

    /**
     * Opening MapSearchActivity when user clicks on a map button
     */
    @OnClick(R.id.ib_new_chat_map)
    public void openMap(){
        Intent intent = new Intent(getContext(), MapSearchActivity.class);
        intent.putExtra(MapSearchActivity.EXTRA_PLACE_DATA, mChatRoom.getLocation());
        startActivityForResult(intent, RC_MAP_SEARCH_USER_LOCATION);
    }

    /**
     * Verifying given data. If it's ok - return new ChatRoom via callback.
     * If not - show Snackbar with warnings.
     */
    @OnClick(R.id.b_chat_info_ok)
    public void addNewChat(){
        String snackText="";
        if(mChatName.getText().length()==0){
            snackText=getString(R.string.chat_info_empty_name_message);
        }
        if(mPlaceSearch.getQuery().length()==0){
            if(snackText.length()>0){
                snackText+=" ";
                snackText+=getString(R.string.chat_info_empty_location_and_message);

            } else {
                snackText+=getString(R.string.chat_info_empty_location_message);
            }
        }
        if(snackText.length()>0) {
            Snackbar.make(mRootView, snackText, Snackbar.LENGTH_SHORT).show();
        } else {
            prepareChatRoom();
            mCallback.onNewChatroomAdded(mChatRoom);
            dismiss();
        }
    }

    private void prepareChatRoom(){
        mChatRoom.setName(mChatName.getText().toString());
        mChatRoom.setCreatedAt(System.currentTimeMillis());
    }

    /**
     * Returning result from MapSearchActivity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){return;}
        switch (requestCode){
            case RC_MAP_SEARCH_USER_LOCATION:
                if(data!=null) {
                    PlaceData placeData = (PlaceData)data.getSerializableExtra(MapSearchActivity.EXTRA_PLACE_DATA);
                    mChatRoom.setLocation(placeData);
                    mPlaceSearch.setQuery(placeData.getName(), false);
                }
                break;
        }
    }

    /**
     * Callback from PlacesSearchView
     * Getting Place object for given Place ID using Places API and saving
     * it's latitude, longitude and name to ChatRoom instance.
     */
    @Override
    public void onPlaceSuggested(String placeID) {
        App.getApp().getAppComponent()
                .getGeoDataClient()
                .getPlaceById(placeID)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);
                        LatLng latLng = myPlace.getLatLng();
                        mChatRoom.setLocation(new PlaceData(latLng.latitude, latLng.longitude, myPlace.getName().toString()));
                        places.release();
                    }
                });
    }


    public interface Callback{
        void onNewChatroomAdded(ChatRoom chatRoom);
    }

}
