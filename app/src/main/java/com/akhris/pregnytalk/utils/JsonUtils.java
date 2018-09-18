package com.akhris.pregnytalk.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Util class for handling JSON operations.
 * Used with Places API
 */
public class JsonUtils {

    private static final String KEY_RESULTS="results";
    private static final String KEY_STATUS="status";
    private static final String KEY_PLACE_ID="place_id";

    private static final String RESULT_OK="OK";

    /**
     * Returns place ID from http-response
     */
    public static String parsePlaceId(String response) {
        String placeID="";
        try {
            JSONObject responseJSON = new JSONObject(response);
            String status = responseJSON.optString(KEY_STATUS);
            if(!status.equals(RESULT_OK)){return placeID;}
            JSONArray resultsJSON = responseJSON.getJSONArray(KEY_RESULTS);
            JSONObject addressComponentJson = resultsJSON.getJSONObject(0);
            placeID = addressComponentJson.optString(KEY_PLACE_ID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return placeID;
    }
}
