package com.akhris.pregnytalk.contract;

import com.akhris.pregnytalk.ui.PlacesSearchView;
import com.google.android.gms.location.places.Place;

public interface MapsContract {

    /**
     * Contract that is common for MapAndListFragment and MapSearchActivity:
     * - Map has to work with PlacesSearchView;
     * - Map has to handle clicks and return Place object for clicked place;
     * - Map has
     */
    interface CommonMapsContract {
        void withPlacesSearchView(PlacesSearchView searchView);
        void onMapClicked(Place place);
    }
}
