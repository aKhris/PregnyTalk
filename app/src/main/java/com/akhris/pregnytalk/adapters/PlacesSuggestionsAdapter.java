package com.akhris.pregnytalk.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akhris.pregnytalk.R;
import com.google.android.gms.location.places.AutocompletePrediction;

import java.util.Iterator;

/**
 * Adapter for PlacesSearchView suggestions.
 * Make instance of MatrixCursor to allow use CursorAdapter without any database.
 */
public class PlacesSuggestionsAdapter extends CursorAdapter {

    private static final String IMAGE_ROW_ID ="-1";

    private static final String COLUMN_ROW_ID=BaseColumns._ID;
    public static final String COLUMN_SUGGEST_TEXT_1="suggest_text_1";
    private static final String COLUMN_SUGGEST_TEXT_2="suggest_text_2";
    public static final String COLUMN_PLACE_ID="place_id";
    
    private static final String[] SEARCH_SUGGEST_COLUMNS = {
            COLUMN_ROW_ID,
            COLUMN_SUGGEST_TEXT_1,
            COLUMN_SUGGEST_TEXT_2,
            COLUMN_PLACE_ID
    };

    public PlacesSuggestionsAdapter(Context context, Iterator<AutocompletePrediction> iterator) {
        super(context, makeMatrixCursorFromIterator(iterator),1);
    }

    /**
     * Making Matrix Cursor Object filled with Places data.
     * @param iterator - Some collection of places data got from Places API
     */
    private static MatrixCursor makeMatrixCursorFromIterator(Iterator<AutocompletePrediction> iterator){
        MatrixCursor matrixCursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS);
        while(iterator.hasNext()){
            AutocompletePrediction prediction = iterator.next();
            matrixCursor.addRow(new String[]{
                    String.valueOf(matrixCursor.getCount()),
                    prediction.getPrimaryText(null).toString(),
                    prediction.getSecondaryText(null).toString(),
                    prediction.getPlaceId()
            });
        }
        // Adding a row to add "Powered by Google" image on a last position
        matrixCursor.addRow(new String[]{
                IMAGE_ROW_ID,"","",""
        });
        return matrixCursor;
    }


    /**
     * Creating a View for row
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if(cursor.getString(cursor.getColumnIndex(COLUMN_ROW_ID)).equals(IMAGE_ROW_ID)){
            return LayoutInflater.from(context).inflate(R.layout.place_autocomplete_item_powered_by_google, parent, false);
        }
        return LayoutInflater.from(context).inflate(R.layout.place_autocomplete_item_prediction, parent, false);
    }

    /**
     * Binding a View with data from cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ProgressBar progressBar = view.findViewById(R.id.place_autocomplete_progress);
        progressBar.setVisibility(View.GONE);

        if(cursor.getString(cursor.getColumnIndex(COLUMN_ROW_ID)).equals(IMAGE_ROW_ID)){return;}

        TextView primaryText = view.findViewById(R.id.place_autocomplete_prediction_primary_text);
        TextView secondaryText = view.findViewById(R.id.place_autocomplete_prediction_secondary_text);

        primaryText.setText(cursor.getString(cursor.getColumnIndex(COLUMN_SUGGEST_TEXT_1)));
        secondaryText.setText(cursor.getString(cursor.getColumnIndex(COLUMN_SUGGEST_TEXT_2)));
    }

    /**
     * Check if row contains "Powered by Google" image
     */
    public static boolean checkForPoweredByGoogle(Cursor cursor, int position){
        //noinspection SimplifiableIfStatement
        if (cursor.moveToPosition(position)){
            return cursor.getString(cursor.getColumnIndex(COLUMN_ROW_ID)).equals(IMAGE_ROW_ID);
        }
        return false;
    }
}
