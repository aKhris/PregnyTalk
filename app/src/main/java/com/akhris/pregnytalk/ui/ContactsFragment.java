package com.akhris.pregnytalk.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.FirebaseContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment containing only a list of contacts.
 * Since list of contacts is used also in chatroom info screen it's made in the form of fragment -
 * ContactsListFragment.
 * This class is wrapping ContactsListFragment to make it "navigationable".
 */
public class ContactsFragment extends NavigationFragment
        implements NavigationManagerCallback{

    @BindView(R.id.toolbar) Toolbar toolbar;

    public ContactsFragment() {
        // Required empty public constructor
    }


    /**
     * Simply loading ContactsListFragment into container view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, rootView);
        toolbar.setTitle(R.string.toolbar_title_contacts);
        String childPath = FirebaseContract.CHILD_USERS+"/"+MainActivity.sMyUid+"/"+FirebaseContract.CHILD_USER_CONTACTS;
        getNavigationManager()
                .navigateToContactsList(childPath, R.id.fl_contacts_container, getChildFragmentManager());
        return rootView;
    }


    @Override
    Toolbar getToolbar() {
        return toolbar;
    }
}
