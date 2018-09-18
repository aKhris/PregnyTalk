package com.akhris.pregnytalk.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.Child;
import com.akhris.pregnytalk.contract.PlaceData;
import com.akhris.pregnytalk.contract.User;
import com.akhris.pregnytalk.utils.DateUtils;

import java.util.ArrayList;
import java.util.Map;

public class UserDetailsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemClickListener, ChildrenClickListener, ChildrenListAdapter.ChildCallback {

    private static final int POSITION_DATE_OF_BIRTH=0;
    private static final int POSITION_ESTIMATED_DATE=1;
    private static final int POSITION_LOCATION=2;
    private static final int POSITION_HOSPITAL=3;
    private static final int POSITION_CHILDREN=4;

    private static final int LIST_ITEM_COUNT=5;

    private static final int VIEWTYPE_NOT_CHILDREN=0;
    private static final int VIEWTYPE_CHILDREN=1;

    private User mUser;
    private boolean isEditable;

    private UserDetailsCallback mCallback;

    public void setCallback(UserDetailsCallback callback) {
        this.mCallback = callback;
    }



    public UserDetailsListAdapter(User user, boolean isEditable) {
        this.mUser = user;
        this.isEditable = isEditable;
    }

    public void swipeUser(User user){
        this.mUser = user;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==POSITION_CHILDREN){
            return VIEWTYPE_CHILDREN;
        }
        return VIEWTYPE_NOT_CHILDREN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEWTYPE_CHILDREN:
                return ViewHolderFactory.onCreateChildrenItemViewHolder(parent, this);
            case VIEWTYPE_NOT_CHILDREN:
                return ViewHolderFactory.onCreateTwoLineWithIconViewHolder(parent, this);
                default:
                    throw new UnsupportedOperationException("Not yet implemented");
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (position){
            case POSITION_DATE_OF_BIRTH:
                bindUserDateOfBirth((ViewHolderFactory.TwoLineWithIconItemViewHolder) holder);
                break;
            case POSITION_ESTIMATED_DATE:
                bindUserEstimatedDate((ViewHolderFactory.TwoLineWithIconItemViewHolder) holder);
                break;
            case POSITION_CHILDREN:
                bindUserChildren((ViewHolderFactory.ChildrenItemViewHolder) holder);
                break;
            case POSITION_HOSPITAL:
                bindUserHospital((ViewHolderFactory.TwoLineWithIconItemViewHolder) holder);
                break;
            case POSITION_LOCATION:
                bindUserLocation((ViewHolderFactory.TwoLineWithIconItemViewHolder) holder);

        }
    }

    private void bindUserLocation(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.icon.setImageResource(R.drawable.ic_home_40dp);
        holder.bottomText.setText(R.string.user_info_title_location);
        final PlaceData userLocationPlaceData = mUser.getUserLocationPlaceData();
        if(userLocationPlaceData!=null){
            holder.topText.setText(userLocationPlaceData.getName());
        } else {
            holder.topText.clearComposingText();
        }
    }

    private void bindUserHospital(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        final PlaceData hospitalPlaceData = mUser.getHospitalLocationPlaceData();
        if(hospitalPlaceData!=null){
            holder.topText.setText(hospitalPlaceData.getName());
        } else {
            holder.topText.clearComposingText();
        }
        holder.bottomText.setText(R.string.user_info_title_hospital);
        holder.icon.setImageResource(R.drawable.ic_local_hospital_40dp);

    }

    private void bindUserChildren(ViewHolderFactory.ChildrenItemViewHolder holder) {
//        iconId=R.drawable.ic_child_friendly_black_24dp;
//        bottomString=context.getString(R.string.user_info_title_children);
        int boysCount=0;
        int girlsCount=0;
        String initString = holder.itemView.getContext().getString(R.string.children_add);
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
        holder.boysCount.setText(
                boysCount==0?initString:String.valueOf(boysCount)
        );

        holder.girlsCount.setText(
                girlsCount==0?initString:String.valueOf(girlsCount)
        );
        if(isEditable && mUser.getChildren()!=null){
            holder.childrenList.setVisibility(View.VISIBLE);
            Context context = holder.itemView.getContext();
            ChildrenListAdapter adapter = new ChildrenListAdapter(new ArrayList<>(mUser.getChildren().values()), this);
            holder.childrenList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.childrenList.setAdapter(adapter);
        } else {
            holder.childrenList.setVisibility(View.GONE);
        }
    }

    private void bindUserEstimatedDate(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.icon.setImageResource(R.drawable.ic_baseline_calendar_today_24px);
        holder.bottomText.setText(R.string.user_info_title_edd);
        holder.topText.setText(DateUtils.formatDateFromMillis(mUser.getEstimatedDateMillis()));
    }

    private void bindUserDateOfBirth(ViewHolderFactory.TwoLineWithIconItemViewHolder holder) {
        holder.icon.setImageResource(R.drawable.ic_baseline_calendar_today_24px);
        holder.bottomText.setText(R.string.user_info_title_date_of_birth);
        holder.topText.setText(DateUtils.formatDateFromMillis(mUser.getBirthDateMillis()));
    }


    private void setTexts(ViewHolderFactory.TwoLineWithIconItemViewHolder holder, String textTop, String textBottom){
        if(textTop!=null){
            holder.topText.setText(textTop);
        }
        if(textBottom!=null){
            holder.bottomText.setText(textBottom);
        }
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
                break;
        }
    }

    @Override
    public void onAddBoyClick() {
        mCallback.onAddChildClick(Child.SEX_MALE);
    }

    @Override
    public void onAddGirlClick() {
        mCallback.onAddChildClick(Child.SEX_FEMALE);
    }

    @Override
    public void onChildClick(Child child) {

    }

    public interface UserDetailsCallback{
        void onUserLocationClick(PlaceData placeData);
        void onHospitalLocationClick(PlaceData placeData);
        void onBirthDateClick(Long birthDateMillis);
        void onEstimatedDateClick(Long estimatedDateMillis);
        void onAddChildClick(String sex);
    }

}
