package com.akhris.pregnytalk.ui;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.UserDetailsListAdapter;
import com.akhris.pregnytalk.contract.Child;
import com.akhris.pregnytalk.contract.FirebaseContract;
import com.akhris.pregnytalk.contract.PlaceData;
import com.akhris.pregnytalk.contract.User;
import com.akhris.pregnytalk.utils.DateUtils;
import com.akhris.pregnytalk.utils.ImageUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity representing user info
 */
public class UserInfoActivity extends AppCompatActivity implements UserDetailsListAdapter.UserDetailsCallback,
AddChildFragment.Callback
{

    private static final int RC_PHOTO_PICKER = 100;
    private static final int RC_MAP_SEARCH_USER_LOCATION = 201;
    private static final int RC_MAP_SEARCH_HOSPITAL_LOCATION = 202;
    private static final long AFTER_TEXT_CHANGED_DELAY_MILLIS = 2000;

    @BindView(R.id.rv_user_info_details_list) RecyclerView userInfoList;
    @BindView(R.id.iv_user_info_picture) ImageView userInfoPicture;
    @BindView(R.id.et_user_info_user_name) EditText userInfoName;
    @BindView(R.id.iv_user_info_add_to_contacts) ImageView addToContacts;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    public final static String EXTRA_USER_ID="user_id";
    private final static String TAG = "UserInfoActivity";

    private UserDetailsListAdapter mAdapter;

    private boolean mIsEditMode;
    private String mUid;
    private User mUser;

    private DatabaseReference mUserReference;
    private DatabaseReference mUserInMyContactsReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mUserAvatarReference;
    private Uri mCropImageUri;
    private ValueEventListener mUserEventListener;
    private ValueEventListener mMyContactsListener;
    private Query mThisContactInMyContactsQuery;
    private boolean mInContacts;

    public static Intent getUserIntent(Context context, String userId){
        Intent intent = new Intent(context, UserInfoActivity.class);
        intent.putExtra(UserInfoActivity.EXTRA_USER_ID, userId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        if(getIntent()==null){finish();}
        if(!getIntent().hasExtra(EXTRA_USER_ID)){finish();}
        mUid = getIntent().getStringExtra(EXTRA_USER_ID);
        if(mUid==null){return;}
        mUser = new User();
        mIsEditMode = mUid.equals(MainActivity.sMyUid);
        initUI();
        setupUserReference();
    }

    @OnClick(R.id.iv_user_info_picture)
    public void addPicture(){
        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){return;}
        switch (requestCode){
            case RC_MAP_SEARCH_USER_LOCATION:
                if(data!=null) {
                    PlaceData placeData = (PlaceData)data.getSerializableExtra(MapSearchActivity.EXTRA_PLACE_DATA);
                    mUserReference.child(FirebaseContract.CHILD_USER_USER_LOCATION_PLACEDATA).setValue(placeData);
                }
                break;
            case RC_MAP_SEARCH_HOSPITAL_LOCATION:
                if(data!=null){
                    PlaceData placeData = (PlaceData)data.getSerializableExtra(MapSearchActivity.EXTRA_PLACE_DATA);
                    mUserReference.child(FirebaseContract.CHILD_USER_HOSPITAL_LOCATION_PLACEDATA).setValue(placeData);
                }
                break;
//            case RC_PHOTO_PICKER:
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri selectedImageUri = result.getUri();
//                Uri selectedImageUri = data.getData();
                final StorageReference photoRef = mUserAvatarReference.child(selectedImageUri.getLastPathSegment());
                photoRef.putFile(selectedImageUri).addOnSuccessListener(this, taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    mUserReference.child(FirebaseContract.CHILD_USER_PICTURE_URL).setValue(uri.toString());
                }));
                break;
            case CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE:
                Uri imageUri = CropImage.getPickImageResultUri(this, data);

                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    mCropImageUri = imageUri;
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else {
                    // no permissions required or already granted, can start crop image activity
                    startCropImageActivity(imageUri);
                }
                break;
        }

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }

    private void setupUserReference() {
        mUserReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(FirebaseContract.CHILD_USERS)
                .child(mUid);

        mFirebaseStorage = FirebaseStorage.getInstance();

        mUserAvatarReference = mFirebaseStorage
                .getReference()
                .child(FirebaseContract.STORAGE_CHILD_USER_AVATARS)
                .child(mUid);

        mUserInMyContactsReference =
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(FirebaseContract.CHILD_USERS)
                        .child(MainActivity.sMyUid)
                        .child(FirebaseContract.CHILD_USER_CONTACTS)
                        .child(mUid);


        setListeners();
    }



    @Override
    public void onUserLocationClick(PlaceData placeData) {
        Intent intent = new Intent(this, MapSearchActivity.class);
        intent.putExtra(MapSearchActivity.EXTRA_PLACE_DATA, placeData);
        startActivityForResult(intent, RC_MAP_SEARCH_USER_LOCATION);
    }

    @Override
    public void onHospitalLocationClick(PlaceData placeData) {
        Intent intent = new Intent(this, MapSearchActivity.class);
        intent.putExtra(MapSearchActivity.EXTRA_PLACE_DATA, placeData);
        startActivityForResult(intent, RC_MAP_SEARCH_HOSPITAL_LOCATION);
    }

    @Override
    public void onBirthDateClick(Long birthDateMillis) {
        DateUtils.showDatePicker(
                this,
                    birthDateMillis,
                timeInMillis ->
                        mUserReference
                                .child(FirebaseContract.CHILD_USER_BIRTH_DATE_MILLIS)
                                .setValue(timeInMillis)
        );
    }

    @Override
    public void onEstimatedDateClick(Long estimatedDateMillis) {
        DateUtils.showDatePicker(
                this,
                estimatedDateMillis,
                timeInMillis ->
                        mUserReference
                                .child(FirebaseContract.CHILD_USER_ESTIMATED_DATE_MILLIS)
                                .setValue(timeInMillis)
        );
    }

    @Override
    public void onAddChildClick(String sex) {
        AddChildFragment
                .newInstance(sex)
                .show(getSupportFragmentManager(), "add_child_dialog");
    }

    private void saveUserName(){
        String name = userInfoName.getText().toString();
        if(!name.equals(mUser.getName())) {
            mUserReference
                    .child(FirebaseContract.CHILD_USER_NAME)
                    .setValue(name);
        }
    }


    private void setListeners(){
        if(mUserEventListener==null){
            mUserEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        mUser = dataSnapshot.getValue(User.class);
                        if(mAdapter==null){
                            initAdapter();
                        } else {
                            mAdapter.swipeUser(mUser);
                        }
                        if(mUser.getPictureUrl()!=null && mUser.getPictureUrl().length()>0) {
                            Picasso.get()
                                    .load(mUser.getPictureUrl())
                                    .fit()
                                    .transform(new ImageUtils.CircleTransform())
                                    .into(userInfoPicture);
                        }
                        if(!mUser.getName().equals(userInfoName.getText().toString())) {
                            userInfoName.setText(mUser.getName());
                        }
                        checkEditMode();
                    } else { //Called when there is no User in the database
                        if(mIsEditMode) {
                            mUserReference.push().setValue(mUser);
                        } else {
                            finish();
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mUserReference.addValueEventListener(mUserEventListener);
        }

        if(mMyContactsListener==null){
            mMyContactsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()==null){
                        //no such user in contacts
                        mInContacts=false;
                    } else {
                        //there is such user in contacts
                        mInContacts=true;
                    }
                    checkContactsList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mUserInMyContactsReference.addValueEventListener(mMyContactsListener);
        }



    }

    private void checkContactsList() {
        if(mIsEditMode){return;}
        if(mInContacts){
            //already in contacts list
            addToContacts.setImageResource(R.drawable.ic_person_black_24dp);
        } else {
            //not yet in contacts list
            addToContacts.setImageResource(R.drawable.ic_person_add_black_24dp);
        }
    }



    @OnClick(R.id.iv_user_info_add_to_contacts)
    public void onAddToContactsClick(){
        if(mInContacts){
            //remove from contacts
            mUserInMyContactsReference
                    .removeValue();
        } else {
            //add to contacts
           mUserInMyContactsReference
                    .setValue(mUser.getName());
        }
    }

    private void checkEditMode() {
        if(mIsEditMode){
            addToContacts.setVisibility(View.INVISIBLE);
        } else {
            makeEditTextsNotEditable();
        }
    }

    /**
     * Making edittext(s) look like a textview - for another user's info.
     * Solution got here:
     * https://stackoverflow.com/a/29048310/7635275
     */
    private void makeEditTextsNotEditable() {
        userInfoName.setCursorVisible(false);
        userInfoName.setLongClickable(false);
        userInfoName.setClickable(false);
        userInfoName.setFocusable(false);
        userInfoName.setSelected(false);
        userInfoName.setKeyListener(null);
        userInfoName.setBackgroundResource(android.R.color.transparent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsEditMode){
            saveUserName();
        }
        removeListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListeners();
    }

    private void removeListeners() {
        if(mUserEventListener!=null){
            mUserReference.removeEventListener(mUserEventListener);
            mUserEventListener = null;
        }
        if(mMyContactsListener!=null){
            mUserInMyContactsReference.removeEventListener(mMyContactsListener);
            mMyContactsListener = null;
        }
    }

    /**
     * Autosaving after some time from finishing text changing got here:
     * https://stackoverflow.com/a/35268540/7635275
     */
    private void initUI(){
//        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(v->finish());
        userInfoName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
            Runnable workRunnable;
            @Override public void afterTextChanged(Editable s) {
                handler.removeCallbacks(workRunnable);
                workRunnable = () -> saveUserName();
                handler.postDelayed(workRunnable, AFTER_TEXT_CHANGED_DELAY_MILLIS /*delay*/);
            }

        });
    }

    private void initAdapter(){
        mAdapter = new UserDetailsListAdapter(mUser, mIsEditMode);
        mAdapter.setCallback(UserInfoActivity.this);
        userInfoList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        userInfoList.setAdapter(mAdapter);
    }


    @Override
    public void onChildAdded(Child child) {
        mUserReference
                .child(FirebaseContract.CHILD_USER_CHILDREN)
                .push()
                .setValue(child);
    }
}
