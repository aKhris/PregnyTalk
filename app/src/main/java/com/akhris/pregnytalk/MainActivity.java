package com.akhris.pregnytalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.akhris.pregnytalk.contract.FirebaseContract;
import com.akhris.pregnytalk.contract.User;
import com.akhris.pregnytalk.ui.NavigationCallbacks;
import com.akhris.pregnytalk.ui.UserInfoActivity;
import com.akhris.pregnytalk.utils.ImageUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * MainActivity class.
 * Layout of MainActivity contains NavigationDrawer and a container for fragments.
 * Each fragment has it's own implementation of Toolbar, so MainActivity uses toolbar of currently
 * visible fragment via NavigationCallbacks (bindToolbar() and unbindToolbar() methods).
 *
 * Each Fragment that can be displayed in MainActivity's container has to extend NavigationFragment
 * abstract class that handles the usage of NavigationCallbacks methods.
 *
 * The third NavigationCallback method is getNavigationManager() which returns the instance of
 * NavigationManager class that is initialized in onCreate() method of MainActivity.
 *
 * NavigationManager class handles the navigation between the fragments, and since every fragment
 * get the instance of it via callback, we can change fragments from inside the fragments.
 * It is used when choosing a chat in a chatlist or clicking a chat-info button on the toolbar
 * of the chat fragment for example.
 */
public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,    // Listens to navigation menu clicks
        NavigationCallbacks {      // Used to interact with NavigationFragment subclasses

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    //Authentication data. Initialized after successful authentication.
    public static String sMyUid="";
    public static User sMe;

    //Used to determine if the list of messages was shown not to show it again after device rotation
    private static final String BUNDLE_WAS_MESSAGES_LIST_SHOWN = "was_messages_list_shown";
    private boolean isMessagesListShown=false;

    //Request codes
    private static final int RC_SIGN_IN = 100;
    private static final int RC_USER_INFO_CHANGE = 200;

    //Views inside NavigationDrawer
    private ImageView   mHeaderPhoto;
    private TextView    mHeaderUsername;
    private TextView    mHeaderUserAuthInfo;
    private ImageView   mHeaderUserInfoButton;

    private ActionBarDrawerToggle mToggle;

    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserReference;
    private ValueEventListener mUserValueEventListener;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //NavigationManager instance that is shared between all NavigationFragment subclasses
    private NavigationManager mNavigationManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        restoreState(savedInstanceState);
        initNavigation();
        initFirebase();
    }

    /**
     * Initialization of FirebaseAuth (for authentication) and FirebaseDatabase (for operating with
     * user data).
     * Enable of database persistence for offline access.
     */
    private void initFirebase(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuthStateListener = firebaseAuth -> {
            final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser!=null){
                //user is signed in
                onSignedInInit(firebaseUser);
            } else {
                onSignedOutCleanup();
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.PhoneBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .build(),
                        RC_SIGN_IN);
            }
        };
    }

    /**
     * Initialization of views inside NavigationView, setting listeners for navigation menu and
     * making instance of NavigationManager.
     */
    private void initNavigation(){
        mNavigationManager = new NavigationManager(getSupportFragmentManager());
        navigationView.setNavigationItemSelectedListener(this);
        mHeaderPhoto = navigationView.getHeaderView(0).findViewById(R.id.nav_photoview);
        mHeaderUsername = navigationView.getHeaderView(0).findViewById(R.id.nav_user_name);
        mHeaderUserAuthInfo = navigationView.getHeaderView(0).findViewById(R.id.nav_user_auth_info);
        mHeaderUserInfoButton = navigationView.getHeaderView(0).findViewById(R.id.iv_nav_user_info_button);
        mHeaderUserInfoButton.setOnClickListener(v-> callSelfUserInfo());
    }

    /**
     * Handling restoring data
     * @param savedInstanceState - bundle that is passed to onCreate() method
     */
    private void restoreState(Bundle savedInstanceState) {
        if(savedInstanceState==null){return;}
        if(savedInstanceState.containsKey(BUNDLE_WAS_MESSAGES_LIST_SHOWN)){
            isMessagesListShown = savedInstanceState.getBoolean(BUNDLE_WAS_MESSAGES_LIST_SHOWN);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_WAS_MESSAGES_LIST_SHOWN, isMessagesListShown);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handling activities results.
     * If user is not signed in - finish MainActivity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RC_SIGN_IN:
                if (resultCode == RESULT_CANCELED){
                    finish();
                }
                break;
            case RC_USER_INFO_CHANGE:

                break;
        }
    }

    /**
     * Handling navigation view item clicks here.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_contacts:
                mNavigationManager.navigateToContacts();
                break;
            case R.id.nav_map:
                mNavigationManager.navigateToMapAndList();
                break;
            case R.id.nav_messages:
                mNavigationManager.navigateToChatsList();
                break;
            case R.id.nav_settings:
                mNavigationManager.navigateToSettings();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Called from onResume method of NavigationFragment.
     * @param toolbar the toolbar of a Fragment that is currently showing inside container.
     * @param withBackButton if true, the back button will be showed.
     *                       If false - show drawer "hamburger"
     */
    @Override
    public void bindToolbar(Toolbar toolbar, boolean withBackButton) {
        setSupportActionBar(toolbar);

        if(withBackButton){
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            toolbar.setNavigationOnClickListener(v -> mNavigationManager.navigateBack());
        } else {

            mToggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(mToggle);
            mToggle.syncState();
        }
    }

    /**
     * Called form onPause method of NavigationFragment
     */
    @Override
    public void unbindToolbar() {
        setSupportActionBar(null);
        drawer.removeDrawerListener(mToggle);
        mToggle=null;
    }

    /**
     * Called when user is successfully signed in.
     * Initializing static user field sMyUid.
     */
    private void onSignedInInit(FirebaseUser firebaseUser) {

        sMyUid=firebaseUser.getUid();
        mUserReference = mFirebaseDatabase
                .getReference()
                .child(FirebaseContract.CHILD_USERS)
                .child(sMyUid);

        if(mUserValueEventListener==null){
            mUserValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        //create new user
                        callSelfUserInfo();
                    } else {
                        //the user exists
                        setCurrentUser(dataSnapshot.getValue(User.class));
                        if(!isMessagesListShown) {
                            mNavigationManager.navigateToChatsList();
                            isMessagesListShown = true;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }

        mUserReference.addListenerForSingleValueEvent(mUserValueEventListener);
    }

    /**
     * Updating user information in NavigationView and in static field sMe
     * @param user - instance of User class
     */
    private void setCurrentUser(User user) {
        if(user==null){return;}
        sMe = user;
        updateNavigationHeader();
    }


    /**
     * Updating user information in NavigationView
     */
    private void updateNavigationHeader() {
        if(sMe.getPictureUrl()!=null && sMe.getPictureUrl().length()>0) {
            Picasso.get()
                    .load(sMe.getPictureUrl())
                    .fit()
                    .transform(new ImageUtils.CircleTransform())
                    .into(mHeaderPhoto);
        }
        mHeaderUsername.setText(sMe.getName());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String authInfo="";
        if(firebaseUser.getEmail()!=null && firebaseUser.getEmail().length()>0){
            authInfo+=firebaseUser.getEmail();
        }
        if(firebaseUser.getPhoneNumber()!=null && firebaseUser.getPhoneNumber().length()>0){
            authInfo+="\n"+firebaseUser.getPhoneNumber();
        }
        mHeaderUserAuthInfo.setText(authInfo);
    }

    /**
     * Called when user is signed out
     */
    private void onSignedOutCleanup(){
        sMyUid = null;
        sMe = null;
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if(mUserValueEventListener!=null) {
            mUserReference.removeEventListener(mUserValueEventListener);
            mUserValueEventListener = null;
        }
    }

    /**
     * Opening UserInfoActivity for user sMyUid with some transition animation
     */
    private void callSelfUserInfo() {
        if(sMyUid==null){return;}
        if(sMyUid.length()==0){return;}

        Pair<View, String> p1 = Pair.create(mHeaderPhoto, getString(R.string.transition_user_info_photo));
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, p1);
        ActivityCompat.startActivityForResult(this, UserInfoActivity.getUserIntent(this, sMyUid), RC_USER_INFO_CHANGE, options.toBundle());
    }

    /**
     * Part of NavigationCallbacks
     */
    @Override
    public NavigationManager getNavigationManager() {
        return mNavigationManager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        detachDatabaseReadListener();
    }

}
