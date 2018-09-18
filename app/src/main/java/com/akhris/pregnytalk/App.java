package com.akhris.pregnytalk;

import android.app.Application;

import com.akhris.pregnytalk.dagger.AppComponent;
import com.akhris.pregnytalk.dagger.AppModule;
import com.akhris.pregnytalk.dagger.DaggerAppComponent;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Application class.
 * Used with Dagger to make the following components accessible from different parts of the app:
 * -GeoDataClient;
 */
public class App extends Application{

    private static App app;
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        appComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static App getApp() {
        return app;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
