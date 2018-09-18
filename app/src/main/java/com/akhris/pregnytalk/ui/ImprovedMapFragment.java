package com.akhris.pregnytalk.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akhris.pregnytalk.App;
import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.akhris.pregnytalk.contract.PlaceData;
import com.akhris.pregnytalk.utils.ImageUtils;
import com.akhris.pregnytalk.utils.LoadPlaceTask;
import com.akhris.pregnytalk.utils.NetworkUtils;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
        GoogleMap.OnCameraIdleListener,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener,
        CreateChatFragment.Callback{

    // Constant field to distinguish between icon res and standard marker icon.
    public static final int STANDARD_ICON =-1;

    // Request code for request permission (FINE_LOCATION) dialog
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    // Fields for LoadPlaceTask
    private static final String LOADER_BUNDLE_LATLNG = "bundle_latlng";
    private static final int PLACE_LOADER_ID = 1;

    // Argument passed to new instance of a fragment
    private static final String ARG_SHOW_CHAT_MARKERS = "bundle_show_chat_markers";
    private boolean mShowChatMarkers =false;

    // Instance of outer custom SearchView to use with this map
    private PlacesSearchView mPlacesSearchView;

    // Firebase
    private DatabaseReference mRoomMetaDataReference;

    // Google Map
    private OnMapReadyCallback outerMapReadyCallback;
    private GoogleMap mMap;

    // MapCallback is used when user taps on Google Map, LoadPlaceTask loads Place
    // using coordinates of user's tap. The Place returns via MapCallback.
    private MapCallback mapCallback;

    // ChatsOnMapCallback is used to get list of currently visible ChatRooms to
    // fill in the List of chats tab in the MapAndListFragment.
    // So user can see all chats information on the map in the form of the list without
    // need to tap on each chat marker on the map.
    private ChatsOnMapCallback chatsOnMapCallback;

    // Vertical Padding for the Google Map UI (location button, etc)
    private int verticalPadding=0;

    // Flag to distinguish between map camera moving by user gesture or by animation.
    private boolean mIsMovedByGesture;

    // Save the search or tap-on-map result marker in mMarker to remove it when new search/tap
    // marker is going to be added and not to remove all other Markers (i.e. chat markers) by calling
    // mMap.clear() method.
    private Marker mMarker;

    // Save ChatRoom and corresponding Marker to Map<> object to make possible fill in the info
    // window with chat information while tapping on some marker on the Google Map.
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
            mShowChatMarkers = getArguments().getBoolean(ARG_SHOW_CHAT_MARKERS, false);
        }

        setupChatRoomsReference();
    }

    /**
     * Initialize Firebase Database Reference to the whole list of chats.
     * It's going to get queried and filtered while camera moves.
     */
    private void setupChatRoomsReference() {
        mRoomMetaDataReference =
                FirebaseDatabase
                        .getInstance()
                .getReference()
                .child(FirebaseContract.CHILD_ROOM_META_DATA);
    }

    /**
     * Binding outer PlacesSearchView instance to use with this map.
     * Setting callback makes possible to
     * @param placesSearchView
     */
    public void withPlacesSearchView(PlacesSearchView placesSearchView){
        this.mPlacesSearchView = placesSearchView;
        this.mPlacesSearchView.setmCallback(this);
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
        if(mShowChatMarkers){
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
        if(getContext()!=null && NetworkUtils.isOnline(getContext())) {
            getLoaderManager().restartLoader(PLACE_LOADER_ID, bundle, this);
        } else {
            Toast.makeText(getContext(), R.string.warning_check_internet, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Show marker on Google Map for given Place object.
     * Filling in the Snippet and Name to show it on the info window
     * after user clicks on marker
     * Marker object is saved here in mMarker field - to remove previous marker and show therefore
     * one marker at a time.
     * @param place Place object to show marker for
     * @param zoomTo true if zoom to the marker is needed
     */
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

    /**
     * Show marker on a map, but since it's not saved in mMarker variable, it's not removing on clicks.
     * It's used to set existing previous location while user can pick another one from
     * User Info Activity.
     * @param latLng - coordinates of Marker
     * @param title - Title of Marker
     * @param iconId - Icon of the Marker
     * @param zoomTo - if true zoom to the Marker
     * @param isTransparent - if true set the opacity of 50% of the Marker
     */
    public void pinMarkerToMap(LatLng latLng, String title, int iconId, boolean zoomTo, boolean isTransparent){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .anchor(0f, 1f);

        if(iconId!= STANDARD_ICON){
            markerOptions.icon(ImageUtils.bitmapDescriptorFromVector(getContext(), iconId));
            markerOptions.anchor(0.5f, 0.5f);
        }

        if(isTransparent){
            markerOptions.alpha(0.5f);
        }

        mMap.addMarker(markerOptions);

        if(zoomTo){
            zoomToLocation(latLng);
        }
    }

    /**
     * Adding Marker to a Map representing a Chat.
     * @param chatRoom - ChatRoom object to draw a Marker to.
     */
    public void addChatMarkerToMap(ChatRoom chatRoom){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(chatRoom.getLocation().getLatLng())
                .title(chatRoom.getName());
        // Show a marker with a "plus" sign if user is not in the room
        // meaning that user can join the chat
        int iconRes;
        if(isMeInChatRoom(chatRoom)){
            iconRes = R.drawable.ic_chat_bubble_24dp;
        } else {
            iconRes = R.drawable.ic_chat_bubble_add_24dp;
        }
        markerOptions.icon(
                    ImageUtils.bitmapDescriptorFromVector(getContext(), iconRes))
                    .anchor(0f, 1f);
        Marker marker = mMap.addMarker(markerOptions);
        // Put added marker to a HashMap to fill info window with ChatRoom parameters
        // if user taps on the marker.
        mChatMarkerMap.put(marker, chatRoom);
    }

    /**
     * Check if current User is in given ChatRoom
     * to show a "plus" sign on a chat marker if it's not there
     * @param chatRoom given Chatroom to find user in
     * @return true if current User is in Chatroom
     *          false if not
     */
    private boolean isMeInChatRoom(ChatRoom chatRoom){
        if(chatRoom.getUsersMap()==null){return false;}
        return chatRoom.getUsersMap().containsKey(MainActivity.sMyUid);
    }

    /**
     * Callback from mPlacesSearchView
     * Get Place object for given Place ID using Places API and then
     * show marker on the web with zooming to it
     * @param placeID - returned Place ID from mPlacesSearchView
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
                        showPlaceOnMap(myPlace.freeze(), true);
                        places.release();
                    }
                });
    }

    /** Loader callbacks **/
    @NonNull
    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        LatLng latLng = args.getParcelable(LOADER_BUNDLE_LATLNG);
        return new LoadPlaceTask(getContext(),latLng, this);
    }

    @Override public void onLoadFinished(@NonNull Loader<Void> loader, Void data) { }
    @Override public void onLoaderReset(@NonNull Loader<Void> loader) { }

    /**
     * Callback from LoadPlaceTask.
     * LoadPlaceTask is started when user clicks on a map.
     * It loads Place ID for that place using GeoCoding API and returns it using this method.
     * Here we can return further given Place to mapCallback object.
     * The clicked place is marked with a Marker without zooming.
     * If there is PlacesSearchView binded, we fill in the query text with given place's name.
     * @param place - Place object returned from GeoCoding API
     */
    @Override
    public void onPlaceLoaded(Place place) {
        if(mapCallback!=null){
            mapCallback.onMapClick(place);
        }
        showPlaceOnMap(place, false);
        if(mPlacesSearchView !=null) {
            mPlacesSearchView.setQuery(place.getName(), false);
        }
    }


    /**
     * Method of OnCameraIdleListener.
     * Using it to show chat markers (if this option is turned on by setting mShowChatMarkers to true)
     * when camera is getting stopped.
     */
    @Override
    public void onCameraIdle() {
        if(mMap==null){return;}
        if(!mShowChatMarkers){return;}
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        setupChatRoomsVisibleOnMapQuery(bounds);
    }

    /**
     * Get the list of ChatRooms for given map visible region.
     * 1. Making a query to filter by latitude on a server.
     * (it's easier than filter by longitude)
     * 2. Get results once.
     * 3. Filter the results by type (PUBLIC) and longitude and if it fits the given range
     * - put it to filtered HashMap<ChatRoomId, ChatRoom>.
     * 4. Pass the filtered HashMap to a method to show them on a Google Map.
     * @param bounds - SouthWest and NorthEast coordinates of a Map that currently is showed on the
     *               screen.
     */
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
                    double lng = chatRoom.getLocation().getLng();
                    if (longitudeFilter(lng, bounds.southwest, bounds.northeast)) {
                        String key = childSnapshot.getKey();
                        chatRoom.setChatRoomId(key);
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

    /**
     * Method to show currently visible public chats on a Map.
     * @param filteredChatRooms - HashMap with ChatRoom objects to show on a map
     *                          with ChatRoom Id as a Key and ChatRoom as a value
     */
    private void showChatsOnMap(Map<String, ChatRoom> filteredChatRooms) {
        if(mMap==null){return;}
        // If user moved the map by finger - clear the map at first
        if(mIsMovedByGesture) {
            mMap.clear();
        }
        // iterate through the map to show all chatrooms
        for (Map.Entry<String, ChatRoom> chatRoomEntry:filteredChatRooms.entrySet()) {
            addChatMarkerToMap(chatRoomEntry.getValue());
        }
        // reset the moved flag
        mIsMovedByGesture=false;
        // return the ArrayList of ChatRoom objects to fill in corresponding List of currently visible
        // Chatrooms on a map
        if(chatsOnMapCallback!=null){
            chatsOnMapCallback
                    .onCameraMoved(new ArrayList<>(filteredChatRooms.values()));
        }
    }

    @Override public View getInfoWindow(Marker marker) { return null; }

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
            infoWindow = getLayoutInflater().inflate(R.layout.search_map_marker_info_contents, null);

            TextView title = infoWindow.findViewById(R.id.title);
            TextView snippet = infoWindow.findViewById(R.id.snippet);
            TextView createChat = infoWindow.findViewById(R.id.tv_marker_info_create_chat);


            title.setText(marker.getTitle());
            snippet.setText(marker.getSnippet());
            if(!mShowChatMarkers){
                createChat.setText("");
            }

        }
        // Inflate the layouts for the info window, title and snippet.



        return infoWindow;
    }

    private void onChatInfoWindowClick(ChatRoom chatRoom) {
        if(MainActivity.sMe==null){return;}
        if(!isMeInChatRoom(chatRoom)) {
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
        } else {
            if(mShowChatMarkers) {
                onCreateNewChatInfoWindowClick(marker);
            }
        }
    }

    private void onCreateNewChatInfoWindowClick(Marker marker) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(marker.getTitle());
        LatLng latLng = marker.getPosition();
        chatRoom.setLocation(new PlaceData(latLng.latitude, latLng.longitude, marker.getSnippet()));
        CreateChatFragment
                .newInstance(chatRoom)
                .show(getChildFragmentManager(), CreateChatFragment.class.getSimpleName());
    }

    /**
     * Callback for CreateChatFragment.
     * Called when user clicks on info window of a not-chat-marker
     * @param chatRoom
     */
    @Override
    public void onNewChatroomAdded(ChatRoom chatRoom) {

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
