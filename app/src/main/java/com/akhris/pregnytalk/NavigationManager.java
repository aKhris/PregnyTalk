package com.akhris.pregnytalk;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.akhris.pregnytalk.ui.ChatFragment;
import com.akhris.pregnytalk.ui.ChatInfoFragment;
import com.akhris.pregnytalk.ui.ChatsListFragment;
import com.akhris.pregnytalk.ui.ContactsFragment;
import com.akhris.pregnytalk.ui.ContactsListFragment;
import com.akhris.pregnytalk.ui.MapAndListFragment;
import com.akhris.pregnytalk.ui.SettingsFragment;
import com.mikepenz.aboutlibraries.LibsBuilder;

/**
 * Class to handle navigation in an app
 * Inspired by:
 * https://medium.com/@bherbst/managing-the-fragment-back-stack-373e87e4ff62
 * https://www.toptal.com/android/android-fragment-navigation-pattern
 */
public class NavigationManager {

    private final static int S_CONTAINER_ID = R.id.main_fragments_container;
    private final static String BACK_STACK_ROOT_TAG = "root_fragment";

    private FragmentManager mFragmentManager;



    NavigationManager(FragmentManager fragmentManager) {
        this.mFragmentManager = fragmentManager;
    }

    /**
     * Main navigation function
     * @param fragment - fragment to show inside the container
     * @param isRoot - true to make this fragment - the root fragment
     */
    private void navigateTo(Fragment fragment, boolean isRoot){
        if(mFragmentManager==null){return;}
        mFragmentManager
                .beginTransaction()
                .replace(S_CONTAINER_ID, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(isRoot?BACK_STACK_ROOT_TAG:null)
                .commit();
        mFragmentManager.executePendingTransactions();
    }

    /**
     * Pop all fragments from the stack (including root fragment)
     */
    public void popAll(){
        if(mFragmentManager==null){return;}
        mFragmentManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    /**
     * Pop all fragment from the stack except root
     */
    private void popAllButRoot(){
        if(mFragmentManager==null){return;}
        mFragmentManager.popBackStack(BACK_STACK_ROOT_TAG, 0);
    }

    /**
     * Navigate to ChatsListFragment
     */
    public void navigateToChatsList(){
        Fragment fragment = new ChatsListFragment();
        popAll();
        navigateTo(fragment, true);
    }

    /**
     * Navigate to ChatsListFragment and open chatroom with chatRoomId
     * (for tablets with Master-Detail-Flow
     * @param chatRoomId - chatroom to open after loading it in the chats list
     */
    public void navigateToChatsList(String chatRoomId){
        Fragment fragment = ChatsListFragment.newInstance(chatRoomId);
        popAll();
        navigateTo(fragment, true);
    }

    /**
     * Navigate to ContactsFragment
     */
    public void navigateToContacts(){
        Fragment fragment = new ContactsFragment();
        popAllButRoot();
        navigateTo(fragment, false);
    }

    /**
     * Navigate to MapAndListFragment
     */
    public void navigateToMapAndList(){
        Fragment fragment = new MapAndListFragment();
        popAllButRoot();
        navigateTo(fragment, false);
    }

    /**
     * Navigate to SettingsFragment
     */
    public void navigateToSettings(){
        Fragment fragment = new SettingsFragment();
        popAllButRoot();
        navigateTo(fragment, false);
    }

    /**
     * Navigate to ChatFragment
     * @param chatRoomID - the id of the chatroom in the Firebase Database
     */
    public void navigateToChat(String chatRoomID){
            Fragment fragment = ChatFragment.newInstance(chatRoomID, false);
            popAllButRoot();
            navigateTo(fragment, false);
    }

    /**
     * Navigate to chat inside ChatListFragment.
     * Used on the tablets.
     * @param chatRoomID - the id of the chatroom in the Firebase Database
     * @param childFragmentManager - Fragment Manager got from ChatListFragment
     */
    public void navigateToChat(String chatRoomID, @Nullable FragmentManager childFragmentManager){
        if(childFragmentManager==null) {
           navigateToChat(chatRoomID);
        } else {
            Fragment fragment = ChatFragment.newInstance(chatRoomID, true);
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_chat_container, fragment)
                    .commit();
        }
    }

    /**
     * Navigate to List of Contacts inside ContactsFragment or ChatInfoFragment.
     * @param childPath - path in Firebase Database to search contacts in
     * @param containerId - id of the container to load ContactsFragment into
     * @param childFragmentManager - FragmentManager of ContactsFragment or ChatInfoFragment
     */
    public void navigateToContactsList(String childPath, @IdRes int containerId, FragmentManager childFragmentManager){
        ContactsListFragment contactsListFragment = ContactsListFragment.newInstance(childPath);
        childFragmentManager
                .beginTransaction()
                .replace(containerId, contactsListFragment)
                .commit();
    }

    /**
     * Navigate to Chat info
     * @param chatRoomID - the id of the chatroom in the Firebase Database
     */
    public void navigateToChatInfo(String chatRoomID){
        Fragment fragment = ChatInfoFragment.newInstance(chatRoomID);
        navigateTo(fragment, false);
    }

    /**
     * Navigate to About screen where 3rd party libraries and their licenses are shown
     */
    public void navigateToAbout(Context context) {
        new LibsBuilder()
                .withActivityTitle(context.getString(R.string.nav_title_about))
                .withActivityTheme(R.style.AppTheme)
                .start(context);
    }

    /**
     * Navigate to the previous fragment in the stack.
     * Returns  true if there was fragment to navigate to.
     *          false if there was no fragments;
     */
    public boolean navigateBack(){
        int entryCount = mFragmentManager.getBackStackEntryCount();
        if(entryCount>1) {
            mFragmentManager.popBackStack();
            return true;
        } else {
            return false;
        }
    }


}
