package com.akhris.pregnytalk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.akhris.pregnytalk.NavigationManager;

/**
 * Base class of all the fragments that may be shown inside MainActivity's container
 * Handles operation with NavigationManager and binding/unbinding toolbar to MainActivity.
 *
 * To make possible Master-Detail-Flow navigation on a tablet NavigationCallbacks are got from
 * the ParentFragment also. It is used when ChatFragment is put inside ChatListFragment. In such
 * configuration ChatFragment does not have it's own toolbar (getToolbar() returns null), but since
 * ChatFragment is using NavigationManager to show ChatRoomInfo, it uses NavigationCallbacks from
 * it's parent - ChatFragment.
 */
public abstract class NavigationFragment extends Fragment {

    /**
     * Every subclass can have it's own Toolbar or have no Toolbar at all
     * @return Toolbar instance or null
     */
    @Nullable
    abstract Toolbar getToolbar();

    NavigationCallbacks mCallbacks;

    /**
     * By default subclass doesn't have back button on a toolbar and "hamburger" is shown there.
     * To use back button subclass should override this method and return true.
     * @return  true - to show back button
     *          false - to show "hamburger" button
     */
    public boolean withBackButton(){
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationCallbacks) {
            this.mCallbacks = (NavigationCallbacks) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getParentFragment()!=null && getParentFragment() instanceof NavigationCallbacks){
            this.mCallbacks = (NavigationCallbacks) getParentFragment();
        }

//        if(mCallbacks ==null){
//            throw new UnsupportedOperationException("Parent should implement NavigationCallbacks!");
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getToolbar()==null){return;}
        if(mCallbacks ==null){
            getToolbar().setVisibility(View.GONE);
            return;
        }
        mCallbacks.bindToolbar(getToolbar(), withBackButton());
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mCallbacks ==null){return;}
        mCallbacks.unbindToolbar();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mCallbacks = null;
    }

    /**
     * Every subclass can get an instance of NavigationManager
     */
    public NavigationManager getNavigationManager() {
        return mCallbacks.getNavigationManager();
    }
}
