package com.akhris.pregnytalk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.utils.SharedPrefUtils;
import com.firebase.ui.auth.AuthUI;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

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
        getPreferenceScreen().findPreference(getString(R.string.pref_key_logout))
                .setOnPreferenceClickListener(this);
        getPreferenceScreen().findPreference(getString(R.string.pref_key_ui_hints_reset))
                .setOnPreferenceClickListener(this);

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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if(key.equals(getString(R.string.pref_key_logout))){
                AuthUI.getInstance().signOut(getContext());
                return true;
        }
        else if (key.equals(getString(R.string.pref_key_ui_hints_reset))){
            SharedPrefUtils.resetHelpers(getContext());
            notifyUser(getString(R.string.settings_hints_reset_message));
            return true;
        }
        return false;
    }

    private void notifyUser(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
