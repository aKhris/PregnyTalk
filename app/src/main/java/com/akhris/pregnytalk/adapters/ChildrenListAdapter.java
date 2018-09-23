package com.akhris.pregnytalk.adapters;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.Child;
import com.akhris.pregnytalk.contract.User;
import com.akhris.pregnytalk.utils.DateUtils;
import com.akhris.pregnytalk.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView's adapter representing ChildrenList
 */
public class ChildrenListAdapter extends RecyclerView.Adapter<ViewHolderFactory.ChildViewHolder> implements AdaptersClickListeners.ItemClickListener {

    private List<Child> mChildren;
    private ChildCallback mChildCallback;

    private boolean wasBouncedAfterAdapterCreation=false;

    public ChildrenListAdapter(List<Child> mChildren, ChildCallback mChildCallback) {
        this.mChildren = mChildren;
        this.mChildCallback = mChildCallback;
    }

    @NonNull
    @Override
    public ViewHolderFactory.ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateChildViewHolder(parent, this);
    }

    /**
     * Binding ChildViewHolder views to Child object: name, birth date
     * and icon colored with blue or pink
     * Also bouncing the view to show user that it is swipeable.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolderFactory.ChildViewHolder holder, int position) {
        Child child = mChildren.get(position);
        holder.topText.setText(child.getName());
        holder.bottomText.setText(DateUtils.formatDateFromMillis(child.getBirthDateMillis()));
        holder.icon.setImageResource(R.drawable.ic_child_friendly_black_24dp);
        int color = ContextCompat.getColor(holder.itemView.getContext(),
                child.getSex().equals(Child.SEX_FEMALE)?
                R.color.babyGirl:
                R.color.babyBoy);
        holder.icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        // Check if there was no more than 3 bounces of that adapter during all time
        // (not to annoy user with bouncing - just to show that it is possible)
        if(!SharedPrefUtils.wasBounced(holder.itemView.getContext(), this.getClass()) && !wasBouncedAfterAdapterCreation){
            holder.bounce();
            wasBouncedAfterAdapterCreation = true;
        }
    }

    @Override
    public int getItemCount() {
        return mChildren.size();
    }

    @Override
    public void onItemClick(int position) {
        mChildCallback.onChildClick(mChildren.get(position));
    }

    public Child getChild(int position) {
        return mChildren.get(position);
    }

    public void swipeChildren(User mUser) {
        if(mUser.getChildren()!=null){
            mChildren = new ArrayList<>(mUser.getChildren().values());
            notifyDataSetChanged();
        }
    }

    public interface ChildCallback{
        void onChildClick(Child child);
    }


}
