package com.akhris.pregnytalk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.firebase.ui.auth.AuthUI;

import butterknife.BindView;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Toolbar toolbar;

    private NavigationCallbacks callbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationCallbacks) {
            this.callbacks = (NavigationCallbacks) context;
        } else {
            throw new UnsupportedOperationException(context.toString() + " must implement NavigationCallbacks!");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_title_settingsfragment);
        getPreferenceScreen().findPreference("key_logout").setOnPreferenceClickListener(preference -> {
            AuthUI.getInstance().signOut(getContext());
            return true;
        });

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onResume() {
        super.onResume();
        callbacks.bindToolbar(toolbar, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        callbacks.unbindToolbar();
    }
}
