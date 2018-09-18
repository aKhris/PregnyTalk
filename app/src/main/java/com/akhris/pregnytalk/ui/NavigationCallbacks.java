package com.akhris.pregnytalk.ui;

import android.support.v7.widget.Toolbar;

import com.akhris.pregnytalk.NavigationManager;

/**
 * This callbacks are used from NavigationFragment subclasses
 */
public interface NavigationCallbacks extends NavigationManagerCallback {
    /**
     * Make toolbar from NavigationFragment subclass the action bar of the Activity
     * @param toolbar - toolbar of NavigationFragment subclass
     * @param withBackButton - true to use back button
     *                       - false to use "hamburger" button
     */
    void bindToolbar(Toolbar toolbar, boolean withBackButton);

    /**
     * Detach toolbar from Activity
     */
    void unbindToolbar();

}
