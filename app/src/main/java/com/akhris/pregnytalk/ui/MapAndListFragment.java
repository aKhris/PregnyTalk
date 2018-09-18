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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapAndListFragment extends NavigationFragment
        implements ImprovedMapFragment.ChatsOnMapCallback{


    @BindView(R.id.tl_maps_list_tabs) TabLayout mTabLayout;
    @BindView(R.id.vp_maps_list_pager) ViewPager mViewPager;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private PlacesSearchView searchView;

//    private GoogleMap mMap;


    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
//            mMap = googleMap;
//            mMap.setOnMapClickListener(this);
            // Add a marker in Sydney and move the camera
//            LatLng sydney = new LatLng(-34, 151);
//            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    };
    private MyPagerAdapter mAdapter;

    public MapAndListFragment() {
        // Required empty public constructor
    }

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_map, menu);
        searchView = (PlacesSearchView) menu.findItem(R.id.action_search)
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

    private void setUpSearchView(){
        if(searchView!=null && mAdapter!=null){
            getMapFragment()
                    .withPlacesSearchView(searchView);
        }
    }

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
        mTabLayout
                .getTabAt(MyPagerAdapter.PAGE_INDEX_LIST)
                .setText(pageTitle);
    }


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
