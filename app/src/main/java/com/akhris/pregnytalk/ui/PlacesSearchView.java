package com.akhris.pregnytalk.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.akhris.pregnytalk.adapters.PlacesSuggestionsAdapter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

/**
 * Customized SearchView that listens to user's input and makes suggestions of places
 * using Places API
 */
public class PlacesSearchView extends SearchView
        implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    private GeoDataClient mGeoDataClient;
    private Callback mCallback;


    public PlacesSearchView(Context context) {
        super(context);
        initSearch();
        centerHint();
    }

    public PlacesSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSearch();
        centerHint();
    }

    public PlacesSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSearch();
        centerHint();
    }

    public void setmCallback(Callback mCallback) {
        this.mCallback = mCallback;
    }


    private void initSearch(){
        mGeoDataClient = Places.getGeoDataClient(getContext());
        setOnQueryTextListener(this);
        setOnSuggestionListener(this);
    }

    /**
     * https://stackoverflow.com/a/43109316/7635275
     */
    private void centerHint(){
        int id = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = findViewById(id);
        if (searchEditText != null) {
            searchEditText.setGravity(Gravity.CENTER);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * When user changes text in search window create new PlacesSuggestionsAdapter
     * and attach it to PlacesSearchView;
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        mGeoDataClient
                .getAutocompletePredictions(newText, null, null)
                .addOnSuccessListener(autocompletePredictions -> setSuggestionsAdapter(new PlacesSuggestionsAdapter(getContext(), autocompletePredictions.iterator())));

        return true;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    /**
     * Called when user clicks on suggestion.
     * Checking if user clicks not on the "Powered By Google" image and then got the name of the Place
     * and its ID from the adapter.
     * Name is using to set in the search window, Place ID - returns in the callback.
     */
    @Override
    public boolean onSuggestionClick(int position) {
        Cursor cursor = getSuggestionsAdapter().getCursor();
        if(PlacesSuggestionsAdapter.checkForPoweredByGoogle(cursor, position)){return true;}
        cursor.moveToPosition(position);
        String suggestion = cursor.getString(cursor.getColumnIndex(PlacesSuggestionsAdapter.COLUMN_SUGGEST_TEXT_1));//1 is the index of col containing suggestion name.
        setQuery(suggestion,false);//setting suggestion
        hideKeyboardFrom(getContext(), this);
        if(mCallback!=null){
            mCallback.onPlaceSuggested(cursor.getString(cursor.getColumnIndex(PlacesSuggestionsAdapter.COLUMN_PLACE_ID)));
        }
        return true;
    }

    /**
     * Hiding keyboard when user click on a suggestion.
     * Got here:
     * https://stackoverflow.com/a/17789187/7635275
     */
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm!=null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public interface Callback{
        void onPlaceSuggested(String placeID);
    }
}
