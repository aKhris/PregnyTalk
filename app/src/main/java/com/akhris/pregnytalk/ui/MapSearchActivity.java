package com.akhris.pregnytalk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.PlaceData;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity representing Google Map with a SearchView above and a button to confirm selected place
 * under the map.
 * Used to choose user's home or hospital location, or while creating new chat.
 * Contains ImprovedMapFragment as a main Map part.
 */
public class MapSearchActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ImprovedMapFragment.MapCallback{

    @BindView(R.id.psv_map_search) PlacesSearchView searchView;
    @BindView(R.id.b_choose_place) Button bChoosePlace;

    private final static int VERTICAL_PADDING=150;

    ImprovedMapFragment improvedMapFragment;


    // Extra data that can be passed in starting intent
    public static final String EXTRA_PLACE_DATA = "place_data";
    public static final String EXTRA_ICON_ID = "icon_id";
    private PlaceData mPlaceData;
    private int mIconId = ImprovedMapFragment.STANDARD_ICON;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);
        ButterKnife.bind(this);
        searchView.setIconifiedByDefault(false);
        improvedMapFragment = ImprovedMapFragment.newInstance(false);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_map_fragment_container, improvedMapFragment, ImprovedMapFragment.class.getSimpleName())
                .commit();
        // set padding - to make on-the-map UI not obscured under top toolbar and the bottom button.
        improvedMapFragment.setVerticalPadding(VERTICAL_PADDING);
        improvedMapFragment.getMapAsync(this);
        improvedMapFragment.withPlacesSearchView(searchView);
        if(getIntent()!=null && getIntent().hasExtra(EXTRA_ICON_ID)){
            mIconId = getIntent().getIntExtra(EXTRA_ICON_ID, ImprovedMapFragment.STANDARD_ICON);
        }

    }

    /**
     * If activity was called with some PlaceData - show marker of that place and zoom there.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(getIntent()!=null){
            mPlaceData = (PlaceData) getIntent().getSerializableExtra(EXTRA_PLACE_DATA);
            if(mPlaceData!=null){
                improvedMapFragment
                        .pinMarkerToMap(
                                mPlaceData.getLatLng(),
                                mPlaceData.getName(),
                                mIconId,
                                true,
                                true);
                searchView.setQuery(mPlaceData.getName(), false);
            }
        }
    }

    /**
     * Called when button is clicked.
     * The PlaceData instance is returned in the starting intent.
     */
    @OnClick(R.id.b_choose_place)
    public void onChoosePlaceClick(){
        getIntent().putExtra(EXTRA_PLACE_DATA, mPlaceData);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    /**
     * Updating PlaceData instance with data of the place the user clicked in on a map.
     */
    private void updatePlaceData(Place place){
        LatLng latLng = place.getLatLng();
        mPlaceData = new PlaceData(latLng.latitude, latLng.longitude, place.getName().toString());
    }

    /**
     * ImprovedMapFragment callback that returns Place object when user clicks on a map.
     */
    @Override
    public void onMapClick(Place place) {
        updatePlaceData(place);
        bChoosePlace.setEnabled(true);
    }
}
