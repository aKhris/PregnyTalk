package com.akhris.pregnytalk.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.akhris.pregnytalk.App;
import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.akhris.pregnytalk.utils.ImageUtils;
import com.akhris.pregnytalk.utils.LoadPlaceTask;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE;

/**
 * Subclass of SupportMapFragment with improved functionality
 */
public class ImprovedMapFragment extends SupportMapFragment
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        PlacesSearchView.Callback,
        LoaderManager.LoaderCallbacks<Void>,
        LoadPlaceTask.Callback,
        GoogleMap.OnCameraIdleListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    public static final int NO_ICON=-1;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    private static final String LOADER_BUNDLE_LATLNG = "bundle_latlng";

    // Argument passed to new instance of a fragment
    private static final String ARG_SHOW_CHAT_MARKERS = "bundle_show_chat_markers";
    private boolean showChatMarkers=false;


    private static final int PLACE_LOADER_ID = 1;



    private PlacesSearchView placesSearchView;



    //Firebase
    private DatabaseReference mRoomMetaDataReference;
    private FirebaseDatabase mFirebaseDatabase;

    //Google Map
    private OnMapReadyCallback outerMapReadyCallback;
    private GoogleMap mMap;
    private MapCallback mapCallback;
    private ChatsOnMapCallback chatsOnMapCallback;


    private int verticalPadding=0;

    private boolean mIsMovedByGesture;
    private Marker mMarker;
    private HashMap<Marker, ChatRoom> mChatMarkerMap;


    /**
     * Creating new instance of ImprovedMapFragment
     * @param showChatMarkers - true to show chats on a map
     *                        - false not to show them
     */
    public static ImprovedMapFragment newInstance(boolean showChatMarkers) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_CHAT_MARKERS, showChatMarkers);
        ImprovedMapFragment fragment = new ImprovedMapFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MapCallback){
            this.mapCallback = (MapCallback)context;
        }
        if(context instanceof ChatsOnMapCallback) {
            this.chatsOnMapCallback = (ChatsOnMapCallback) context;
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if(getParentFragment()!=null){
            if(getParentFragment() instanceof MapCallback){
                this.mapCallback = (MapCallback)getParentFragment();
            }
            if(getParentFragment() instanceof ChatsOnMapCallback){
                this.chatsOnMapCallback = (ChatsOnMapCallback) getParentFragment();
            }

        }
        mChatMarkerMap = new HashMap<>();
        if(getArguments()!=null){
            showChatMarkers = getArguments().getBoolean(ARG_SHOW_CHAT_MARKERS, false);
        }
        setupChatRoomsReferences();
    }

    private void setupChatRoomsReferences() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mRoomMetaDataReference = mFirebaseDatabase
                .getReference()
                .child(FirebaseContract.CHILD_ROOM_META_DATA);
    }

    public void withPlacesSearchView(PlacesSearchView placesSearchView){
        this.placesSearchView = placesSearchView;
        this.placesSearchView.setmCallback(this);
    }

    @Override
    public void getMapAsync(OnMapReadyCallback onMapReadyCallback) {
        outerMapReadyCallback = onMapReadyCallback;
        super.getMapAsync(this);
    }

    public void setVerticalPadding(int verticalPadding) {
        this.verticalPadding = verticalPadding;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(App.getApp(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.setPadding(0,verticalPadding,0,verticalPadding);   //left, top, right, bottom
        }
    }


    public void zoomToLocation(LatLng latLng){
        CameraPosition cameraPosition = new CameraPosition
                .Builder().target(latLng).zoom(16).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode!=LOCATION_PERMISSION_REQUEST_CODE){return;}

        if (grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        this.mMap.setOnMapClickListener(this);
        enableMyLocation();
        if(showChatMarkers){
            this.mMap.setOnCameraIdleListener(this);
            this.mMap.setOnCameraMoveStartedListener(reason -> {
                if(reason==REASON_GESTURE){
                    mIsMovedByGesture=true;
                }
            });
        }
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);
        outerMapReadyCallback.onMapReady(googleMap);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(LOADER_BUNDLE_LATLNG, latLng);
        getLoaderManager().restartLoader(PLACE_LOADER_ID,bundle,this);
    }



    public void showPlaceOnMap(Place place, boolean zoomTo) {
        LatLng latLng = place.getLatLng();
        if(mMarker!=null) {
            mMarker.remove();
            mMarker = null;
        }
        String snippet = "";
        if(place.getAddress()!=null) {
            snippet = place.getAddress().toString();
        }
        if(place.getAttributions()!=null && place.getAttributions().length()>0){
            snippet+="\n"+place.getAttributions();
        }
        mMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title(place.getName().toString())
                        .snippet(snippet)
        );

        if(zoomTo){
            zoomToLocation(latLng);
        }
    }

    public void addMarkerToMap(LatLng latLng, String title, @Nullable String id, int iconId, boolean zoomTo){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .anchor(0f, 1f);

        if(iconId!=NO_ICON){
            markerOptions.icon(
                    ImageUtils.bitmapDescriptorFromVector(getContext(), iconId));
        }

        mMap.addMarker(markerOptions);

        if(zoomTo){
            zoomToLocation(latLng);
        }
    }

    public void addChatMarkerToMap(ChatRoom chatRoom){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(chatRoom.getLocation().getLatLng())
                .title(chatRoom.getName());

        int iconRes = R.drawable.ic_chat_bubble_add_24dp;

        if(isMeInChatRoom(chatRoom)){
            iconRes = R.drawable.ic_chat_bubble_24dp;
        }

        markerOptions.icon(
                    ImageUtils.bitmapDescriptorFromVector(getContext(), iconRes))
                    .anchor(0f, 1f);;

        Marker marker = mMap.addMarker(markerOptions);

        mChatMarkerMap.put(marker, chatRoom);

    }


    private boolean isMeInChatRoom(ChatRoom chatRoom){
        if(chatRoom.getUsersMap()==null){return false;}
        return chatRoom.getUsersMap().containsKey(MainActivity.sMyUid);
    }

    @Override
    public void onPlaceSuggested(String placeID) {
        App.getApp().getAppComponent()
                .getGeoDataClient()
                .getPlaceById(placeID)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        PlaceBufferResponse places = task.getResult();
                        Place myPlace = places.get(0);
                        showPlaceOnMap(myPlace, true);
                        places.release();
                    }
                });
    }

    @NonNull
    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        LatLng latLng = args.getParcelable(LOADER_BUNDLE_LATLNG);
        return new LoadPlaceTask(getContext(),latLng, this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Void> loader, Void data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Void> loader) {

    }

    @Override
    public void onPlaceLoaded(Place place) {
        if(mapCallback!=null){
            mapCallback.onMapClick(place);
        }
        showPlaceOnMap(place, false);
        if(placesSearchView!=null) {
            placesSearchView.setQuery(place.getName(), false);
        }
    }

    @Override
    public void onCameraIdle() {
        if(mMap==null){return;}
        if(!showChatMarkers){return;}
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        setupChatRoomsVisibleOnMapQuery(bounds);
    }

    private void setupChatRoomsVisibleOnMapQuery(LatLngBounds bounds) {

        Query visibleChatsQuery =
                mRoomMetaDataReference
                .orderByChild("location/lat")
                .startAt(bounds.southwest.latitude)
                .endAt(bounds.northeast.latitude);

        visibleChatsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, ChatRoom> filteredChatRooms = new HashMap<>();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = childSnapshot.getValue(ChatRoom.class);
                    if (chatRoom == null) { continue; }
                    if  (chatRoom.getType()==ChatRoom.TYPE_PRIVATE){ continue; }
                    String key = childSnapshot.getKey();
                    chatRoom.setChatRoomId(key);
                    double lng = chatRoom.getLocation().getLng();
                    if (longitudeFilter(lng, bounds.southwest, bounds.northeast)) {
                        filteredChatRooms.put(key, chatRoom);
                    }
                }
                showChatsOnMap(filteredChatRooms);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showChatsOnMap(Map<String, ChatRoom> filteredChatRooms) {
        if(mMap==null){return;}
        if(mIsMovedByGesture) {
            mMap.clear();
        }
        for (Map.Entry<String, ChatRoom> chatRoomEntry:filteredChatRooms.entrySet()) {
            addChatMarkerToMap(chatRoomEntry.getValue());
        }
        mIsMovedByGesture=false;
        if(chatsOnMapCallback!=null){
            chatsOnMapCallback
                    .onCameraMoved(new ArrayList<>(filteredChatRooms.values()));
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View infoWindow = null;
        if(mChatMarkerMap.containsKey(marker)){
            //inflate info window for chat markers
            infoWindow = getLayoutInflater().inflate(R.layout.chat_map_marker_info_contents, null);

            TextView title = infoWindow.findViewById(R.id.tv_marker_info_title);
            TextView placeName = infoWindow.findViewById(R.id.tv_marker_info_place_name);
            TextView usersCountText = infoWindow.findViewById(R.id.tv_marker_info_users_count);
            TextView joinChat = infoWindow.findViewById(R.id.tv_marker_info_join_chat);

            ChatRoom chatRoom = mChatMarkerMap.get(marker);

            if(isMeInChatRoom(chatRoom)){
                joinChat.setText("");
            }

            title.setText(chatRoom.getName());
            placeName.setText(chatRoom.getLocation().getName());

            int usersCount=0;
            if(chatRoom.getUsersMap()!=null){
                usersCount=chatRoom.getUsersMap().size();
            }
            String usersCountString = String.format(getString(R.string.users_count_format_string),usersCount );
            usersCountText.setText(usersCountString);

        } else {
            //inflate info window for just a click marker
            // TODO: 31.08.18 Make new chat at this place! 
            infoWindow = getLayoutInflater().inflate(R.layout.search_map_marker_info_contents, null);

            TextView title = infoWindow.findViewById(R.id.title);
            TextView snippet = infoWindow.findViewById(R.id.snippet);

            title.setText(marker.getTitle());
            snippet.setText(marker.getSnippet());
        }
        // Inflate the layouts for the info window, title and snippet.



        return infoWindow;
    }

    private void onChatInfoWindowClick(ChatRoom chatRoom) {
        if(MainActivity.sMe==null){return;}
        if(isMeInChatRoom(chatRoom)){
            //User is already in the room

        } else {
            //User is going to get into the room
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.chat_join_dialog_title)
                    .setMessage(chatRoom.getName())
                    .setPositiveButton(R.string.chat_join_dialog_positive_button, (dialog, which) -> {
                        addUserToChatRoom(chatRoom);
                    });
            builder.create().show();
        }
    }

    private void addUserToChatRoom(ChatRoom chatRoom) {

        mRoomMetaDataReference
                .child(chatRoom.getChatRoomId())
                .child(FirebaseContract.CHILD_ROOM_USERS_MAP)
                .child(MainActivity.sMyUid)
                .setValue(MainActivity.sMe.getName());
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(mChatMarkerMap.containsKey(marker)){
            onChatInfoWindowClick(mChatMarkerMap.get(marker));
        }
    }

    public interface MapCallback{
        void onMapClick(Place place);
    }

    public interface ChatsOnMapCallback{
        void onCameraMoved(List<ChatRoom> chatRooms);
    }


    private boolean longitudeFilter(double lng, LatLng southwest, LatLng northeast) {
        if (southwest.longitude <= northeast.longitude) {
            return southwest.longitude <= lng && lng <= northeast.longitude;
        } else {
            return southwest.longitude <= lng || lng <= northeast.longitude;
        }
    }

}
