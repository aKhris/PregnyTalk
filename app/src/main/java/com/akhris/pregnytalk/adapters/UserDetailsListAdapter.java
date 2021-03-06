package com.akhris.pregnytalk.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.ViewHolderFactory.TwoLineWithIconItemViewHolder;
import com.akhris.pregnytalk.contract.Child;
import com.akhris.pregnytalk.contract.PlaceData;
import com.akhris.pregnytalk.contract.User;
import com.akhris.pregnytalk.utils.DateUtils;

import java.util.Map;

/**
 * Adapter representing user details in a list form
 */
public class UserDetailsListAdapter extends RecyclerView.Adapter<TwoLineWithIconItemViewHolder>
        implements AdaptersClickListeners.ItemClickListener {

    // Position constants
    private static final int POSITION_DATE_OF_BIRTH=0;
    private static final int POSITION_ESTIMATED_DATE=1;
    private static final int POSITION_LOCATION=2;
    private static final int POSITION_HOSPITAL=3;
    private static final int POSITION_CHILDREN=4;

    // Not using a List<> variable here, but every row is delivered from the appropriate
    // property of a User object. So it is needed to define item count manually.
    private static final int LIST_ITEM_COUNT=5;

    private User mUser;

    // True if User is watching self info
    private boolean isEditable;

    private UserDetailsCallback mCallback;

    public void setCallback(UserDetailsCallback callback) {
        this.mCallback = callback;
    }

    public UserDetailsListAdapter(User user) {
        this.mUser = user;
        this.isEditable = user.getuId().equals(MainActivity.sMyUid);
    }

    /**
     * Called when some user details are changed and there is need to refresh the list
     * @param user - new changed user
     */
    public void swipeUser(User user){
        this.mUser = user;
        notifyDataSetChanged();
    }

    /**
     * Using standard Material two lined list item with icon on the left for every row
     */
    @NonNull
    @Override
    public TwoLineWithIconItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return ViewHolderFactory.onCreateTwoLineWithIconViewHolder(parent, this);

    }

    /**
     * Splitting binding view holder depending on kind of info it's representing to different methods
     */
    @Override
    public void onBindViewHolder(@NonNull TwoLineWithIconItemViewHolder holder, int position) {
        switch (position){
            case POSITION_DATE_OF_BIRTH:
                bindUserDateOfBirth(holder);
                break;
            case POSITION_ESTIMATED_DATE:
                bindUserEstimatedDate(holder);
                break;
            case POSITION_CHILDREN:
                bindUserChildren(holder);
                break;
            case POSITION_HOSPITAL:
                bindUserHospital(holder);
                break;
            case POSITION_LOCATION:
                bindUserLocation(holder);

        }
    }

    /**
     * Set user location information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindUserLocation(TwoLineWithIconItemViewHolder holder) {
        holder.icon.setImageResource(R.drawable.ic_home_40dp);
        holder.bottomText.setText(R.string.user_info_title_location);
        final PlaceData userLocationPlaceData = mUser.getUserLocationPlaceData();
        if(userLocationPlaceData!=null){
            holder.topText.setText(userLocationPlaceData.getName());
        } else {
            holder.topText.clearComposingText();
        }
    }

    /**
     * Set user's hospital location information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindUserHospital(TwoLineWithIconItemViewHolder holder) {
        final PlaceData hospitalPlaceData = mUser.getHospitalLocationPlaceData();
        if(hospitalPlaceData!=null){
            holder.topText.setText(hospitalPlaceData.getName());
        } else {
            holder.topText.clearComposingText();
        }
        holder.bottomText.setText(R.string.user_info_title_hospital);
        holder.icon.setImageResource(R.drawable.ic_local_hospital_40dp);

    }

    /**
     * Set user's children information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindUserChildren(TwoLineWithIconItemViewHolder holder) {
        Context context = holder.itemView.getContext();
        int boysCount=0;
        int girlsCount=0;
        if(mUser.getChildren()!=null && mUser.getChildren().size()>0) {
            for (Map.Entry<String, Child> entry : mUser.getChildren().entrySet()) {
                Child child = entry.getValue();
                if(child.getSex().equals(Child.SEX_FEMALE)){
                    girlsCount++;
                } else {
                    boysCount++;
                }
            }
        }
        holder.icon.setImageResource(R.drawable.ic_child_friendly_black_24dp);
        holder.bottomText.setText(R.string.user_info_title_children);
        String boysString = boysCount==0?"": String.format(context.getString(R.string.user_info_boys_count_format), boysCount);
        String girlsString = girlsCount==0?"": String.format(context.getString(R.string.user_info_girls_count_format), girlsCount);
        holder.topText.setText(String.format("%s %s", boysString, girlsString));
    }

    /**
     * Set user's estimated date information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindUserEstimatedDate(TwoLineWithIconItemViewHolder holder) {
        holder.icon.setImageResource(R.drawable.ic_baseline_calendar_today_24px);
        holder.bottomText.setText(R.string.user_info_title_edd);
        holder.topText.setText(DateUtils.formatDateFromMillis(mUser.getEstimatedDateMillis()));
    }

    /**
     * Set user's date of birth information to a specified row.
     * @param holder - View Holder that has to be populated with data
     */
    private void bindUserDateOfBirth(TwoLineWithIconItemViewHolder holder) {
        holder.icon.setImageResource(R.drawable.ic_baseline_calendar_today_24px);
        holder.bottomText.setText(R.string.user_info_title_date_of_birth);
        holder.topText.setText(DateUtils.formatDateFromMillis(mUser.getBirthDateMillis()));
    }


    @Override
    public int getItemCount() {
        return LIST_ITEM_COUNT;
    }

    @Override
    public void onItemClick(int position) {
        switch(position){
            case POSITION_DATE_OF_BIRTH:
                if(isEditable) {
                    mCallback.onBirthDateClick(mUser.getBirthDateMillis());
                }
                break;
            case POSITION_ESTIMATED_DATE:
                if(isEditable) {
                    mCallback.onEstimatedDateClick(mUser.getEstimatedDateMillis());
                }
                break;
            case POSITION_LOCATION:
                mCallback.onUserLocationClick(mUser.getUserLocationPlaceData());
                break;
            case POSITION_HOSPITAL:
                mCallback.onHospitalLocationClick(mUser.getHospitalLocationPlaceData());
                break;
            case POSITION_CHILDREN:
                mCallback.onAddChildClick();
                break;
        }
    }

    /**
     * Callbacks for interaction with this details list
     */
    public interface UserDetailsCallback{
        void onUserLocationClick(PlaceData placeData);
        void onHospitalLocationClick(PlaceData placeData);
        void onBirthDateClick(Long birthDateMillis);
        void onEstimatedDateClick(Long estimatedDateMillis);
        void onAddChildClick();
    }

}
