package com.akhris.pregnytalk.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.PlaceData;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapSearchActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.InfoWindowAdapter,
        ImprovedMapFragment.MapCallback{

    @BindView(R.id.psv_map_search) PlacesSearchView searchView;
    @BindView(R.id.b_choose_place) Button bChoosePlace;
    ImprovedMapFragment improvedMapFragment;

    public static final String EXTRA_PLACE_DATA = "place_data";

    @Nullable
    private String mPlaceId;

    private PlaceData mPlaceData;

    private GoogleMap mMap;

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

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        if(getIntent()!=null){
            mPlaceData = (PlaceData) getIntent().getSerializableExtra(EXTRA_PLACE_DATA);
            if(mPlaceData!=null){
                improvedMapFragment
                        .addMarkerToMap(
                                mPlaceData.getLatLng(),
                                mPlaceData.getName(),
                                null,
                                ImprovedMapFragment.NO_ICON,
                                true);
                searchView.setQuery(mPlaceData.getName(), false);
            }
        }
    }





    @Override
    // Return null here, so that getInfoContents() is called next.
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Inflate the layouts for the info window, title and snippet.
        View infoWindow = getLayoutInflater().inflate(R.layout.search_map_marker_info_contents, null);

        TextView title = infoWindow.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = infoWindow.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());

        return infoWindow;
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
