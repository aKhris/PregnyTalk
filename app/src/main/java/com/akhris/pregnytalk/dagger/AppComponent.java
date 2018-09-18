package com.akhris.pregnytalk.dagger;

import android.content.Context;

import com.akhris.pregnytalk.NavigationManager;
import com.google.android.gms.location.places.GeoDataClient;

import dagger.Component;

/**
 * Created by anatoly on 11.03.18.
 */
@AppScope
@Component(modules = {AppModule.class})
public interface AppComponent {

    Context getContext();

    GeoDataClient getGeoDataClient();

}
