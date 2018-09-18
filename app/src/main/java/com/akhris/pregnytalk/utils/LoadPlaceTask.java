package com.akhris.pregnytalk.utils;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.akhris.pregnytalk.App;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.model.LatLng;

/**
 * AsyncTaskLoader to load Place from given PlaceId
 */
public class LoadPlaceTask extends AsyncTaskLoader<Void> {

    private LatLng mLatLng;
    private Callback mCallback;

    public LoadPlaceTask(Context context, LatLng latLng, Callback callback) {
        super(context);
        this.mLatLng = latLng;
        this.mCallback = callback;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Void loadInBackground() {
        String placeId = PlaceUtils.getPlaceIdFromLatLng(getContext(), mLatLng);
        if(placeId==null || placeId.length()==0){return null;}
        GeoDataClient geoDataClient = App.getApp().getAppComponent().getGeoDataClient();
        geoDataClient
                .getPlaceById(placeId)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        PlaceBufferResponse places = task.getResult();
                        Place loadedPlace = places.get(0);
                        mCallback.onPlaceLoaded(loadedPlace);
                        places.release();
                    }
                });
        return null;
    }

    public interface Callback{
        void onPlaceLoaded(Place place);
    }
}
