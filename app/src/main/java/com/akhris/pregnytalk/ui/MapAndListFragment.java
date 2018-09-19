package com.akhris.pregnytalk.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.ChatRoom;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment containing View Pager layout with two tabs: Maps and List of chats that are
 * currently displayed on a map.
 */
public class MapAndListFragment extends NavigationFragment
        implements ImprovedMapFragment.ChatsOnMapCallback{

    @BindView(R.id.tl_maps_list_tabs) TabLayout mTabLayout;
    @BindView(R.id.vp_maps_list_pager) ViewPager mViewPager;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private PlacesSearchView mSearchView;

    // Currently callback is empty. It's needed two fire getMapAsync() function of ImprovedMapFragment.
    private OnMapReadyCallback mapReadyCallback = googleMap -> { };

    // Instance of View Pager adapter.
    private MyPagerAdapter mAdapter;

    public MapAndListFragment() {
        // Required empty public constructor
    }

    /**
     * View initialization: tabs are setup to operate with ViewPagers
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_map_and_list, container, false);
        ButterKnife.bind(this, rootview);
        mAdapter = new MyPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        toolbar.setTitle(R.string.toolbar_title_mapsandlistfragment);
        setHasOptionsMenu(true);
        return rootview;
    }

    /**
     * Getting reference to custom PlacesSearchView inside menu.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_map, menu);
        mSearchView = (PlacesSearchView) menu.findItem(R.id.action_search)
                .getActionView();
        setUpSearchView();
    }

    @Override
    Toolbar getToolbar() {
        return toolbar;
    }

    private ImprovedMapFragment getMapFragment(){
        return (ImprovedMapFragment)mAdapter.instantiateItem(mViewPager, MyPagerAdapter.PAGE_INDEX_MAPS);
    }

    /**
     * Binding ImprovedMapFragment and PlacesSearchView that placed in the menu.
     */
    private void setUpSearchView(){
        if(mSearchView !=null && mAdapter!=null){
            getMapFragment()
                    .withPlacesSearchView(mSearchView);
        }
    }

    /**
     * Method of ImprovedMapFragment.ChatsOnMapCallback, called after User moves the map and
     * ImprovedMapFragment does all the work of querying all currently visible chatrooms from
     * the Firebase Database.
     * The list then send to RecyclerView's adapter inside ChatLocationListFragment.
     * @param chatRooms - List of currently visible chatrooms on the map.
     */
    @Override
    public void onCameraMoved(List<ChatRoom> chatRooms) {
        ((ChatLocationListFragment)
                mAdapter.instantiateItem(mViewPager, MyPagerAdapter.PAGE_INDEX_LIST))
                .swipeList(chatRooms);

        String pageTitle=
                String.format(
                        getString(R.string.viewpager_title_list_format),
                        chatRooms.size()
                );
        TabLayout.Tab tab = mTabLayout.getTabAt(MyPagerAdapter.PAGE_INDEX_LIST);
        if(tab!=null) {
                tab.setText(pageTitle);
        }
    }


    /**
     * Simple class of View Pager adapter
     */
    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        static final int PAGE_INDEX_MAPS = 0;
        static final int PAGE_INDEX_LIST = 1;


        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case PAGE_INDEX_MAPS:
                    ImprovedMapFragment mapFragment = ImprovedMapFragment.newInstance(true);
                    mapFragment.getMapAsync(mapReadyCallback);
                    return mapFragment;
                case PAGE_INDEX_LIST:
                    return new ChatLocationListFragment();
                    default:
                        throw new UnsupportedOperationException("This page not yet supported.");
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case PAGE_INDEX_MAPS:
                    return getString(R.string.viewpager_title_map);
                case PAGE_INDEX_LIST:
                    return getString(R.string.viewpager_title_list);
                    default:
                        throw new UnsupportedOperationException("This page not yet supported.");
            }
        }
    }

}
