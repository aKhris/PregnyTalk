package com.akhris.pregnytalk.utils;

import android.content.Context;
import android.net.Uri;

import com.akhris.pregnytalk.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Util class for getting PlaceID from HTTP-response.
 * Used with GeoCoding API
 */
public class PlaceUtils {

    private final static String GEOCODING_BASE_URL="https://maps.googleapis.com/maps/api/geocode/json";
    private final static String KEY_LATLNG="latlng";
    private final static String KEY_API_KEY="key";
    /*
    Example of url:
    https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=YOUR_API_KEY
     */

    /**
     * Returns Place ID from LatLng object
     * (that is got from clicking on GoogleMap object, for example)
     */
    public static String getPlaceIdFromLatLng(Context context, LatLng latLng){
        String placeId="";
        try {
            String response = NetworkUtils.getResponseFromHttpUrl(buildPlaceUrl(context, latLng));
            placeId = JsonUtils.parsePlaceId(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return placeId;
    }

    /**
     * Building a correct URL for given LatLng object to got HTTP-response via GeoCoding API
     */
    private static URL buildPlaceUrl(Context context, LatLng latLng){
        String latLngString = String.format(Locale.US,"%f,%f",latLng.latitude, latLng.longitude);

        Uri builtUri = Uri.parse(GEOCODING_BASE_URL).buildUpon()
                .appendQueryParameter(KEY_LATLNG,latLngString)
                .appendQueryParameter(KEY_API_KEY, context.getString(R.string.google_api_key))
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

}
