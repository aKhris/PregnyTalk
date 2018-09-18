package com.akhris.pregnytalk.dagger;

import android.content.Context;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;

import dagger.Module;
import dagger.Provides;

/**
 * Created by anatoly on 11.03.18.
 */
@Module
public class AppModule {
    private Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    @AppScope
    Context getContext(){
        return context;
    }

    @Provides
    @AppScope
    GeoDataClient getGeoDataClient(Context context){
        return Places.getGeoDataClient(context);
    }

}
