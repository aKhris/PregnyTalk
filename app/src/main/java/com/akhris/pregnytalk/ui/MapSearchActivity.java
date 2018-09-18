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

public class MapSearchActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ImprovedMapFragment.MapCallback{

    @BindView(R.id.psv_map_search) PlacesSearchView searchView;
    @BindView(R.id.b_choose_place) Button bChoosePlace;
    ImprovedMapFragment improvedMapFragment;


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
        improvedMapFragment.setVerticalPadding(150);
        improvedMapFragment.getMapAsync(this);
        improvedMapFragment.withPlacesSearchView(searchView);
        if(getIntent()!=null && getIntent().hasExtra(EXTRA_ICON_ID)){
            mIconId = getIntent().getIntExtra(EXTRA_ICON_ID, ImprovedMapFragment.STANDARD_ICON);
        }

    }

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

    @OnClick(R.id.b_choose_place)
    public void onChoosePlaceClick(){
        getIntent().putExtra(EXTRA_PLACE_DATA, mPlaceData);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private void updatePlaceData(Place place){
        LatLng latLng = place.getLatLng();
        mPlaceData = new PlaceData(latLng.latitude, latLng.longitude, place.getName().toString());
    }


    @Override
    public void onMapClick(Place place) {
        updatePlaceData(place);
        bChoosePlace.setEnabled(true);
    }
}
