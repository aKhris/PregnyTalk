package com.akhris.pregnytalk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.adapters.AdaptersClickListeners.ContactsItemClickListener;
import com.akhris.pregnytalk.contract.User;
import com.akhris.pregnytalk.utils.ImageUtils;
import com.akhris.pregnytalk.utils.SharedPrefUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView's adapter representing contacts list
 */
public class ContactsListAdapter extends RecyclerView.Adapter<ViewHolderFactory.ContactsItemHolder> {


    private List<User> mContacts;
    // Callbacks for contact's clicks (to send a message or to show user info)
    private ContactsItemClickListener mContactsItemClickListener;

    // A flag to show helper's bouncing only one time
    private boolean wasBouncedAfterAdapterCreation=false;

    // If it is contacts list in user/contacts path mSwipeable is true.
    // If it is contacts list of the chatroom mSwipeable is false.
    private final boolean mSwipeable;


    public ContactsListAdapter(ContactsItemClickListener mContactsItemClickListener, boolean swipeable) {
        this.mContactsItemClickListener = mContactsItemClickListener;
        this.mContacts = new ArrayList<>();
        this.mSwipeable = swipeable;
    }

    @NonNull
    @Override
    public ViewHolderFactory.ContactsItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateContactsItemViewHolder(parent, mContactsItemClickListener);
    }

    /**
     * Binding contact's item to User information: name and photo (if exists).
     * Also bouncing the view to show user that it is swipeable.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolderFactory.ContactsItemHolder holder, int position) {
        User user = mContacts.get(position);
        holder.userName.setText(user.getName());
        holder.userPhoto.setImageResource(R.drawable.ic_person_black_24dp);
        if(user.getPictureUrl()!=null && user.getPictureUrl().length()>0){
            Picasso.get()
                    .load(user.getPictureUrl())
                    .fit()
                    .transform(new ImageUtils.CircleTransform())
                    .into(holder.userPhoto);
        }
        if(user.getuId().equals(MainActivity.sMyUid)){
            holder.userName.append(" ");
            holder.userName.append(holder.itemView.getContext().getString(R.string.contacts_you));
            holder.sendMessage.setVisibility(View.INVISIBLE);
        } else {
            holder.sendMessage.setVisibility(View.VISIBLE);
        }

        // Check if there was no more than 3 bounces of that adapter during all time
        // (not to annoy user with bouncing - just to show that it is possible)
        if(mSwipeable && !SharedPrefUtils.wasBounced(holder.itemView.getContext(), this.getClass()) && !wasBouncedAfterAdapterCreation){
            holder.bounce();
            wasBouncedAfterAdapterCreation = true;
        }

        if(holder.wasSwiped){
            holder.releaseSwiped();
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public User getUser(int position) {
        return mContacts.get(position);
    }

    public void addUser(User user) {
        mContacts.add(user);
        notifyItemInserted(mContacts.size()-1);
    }

    public void clear() {
        this.mContacts.clear();
        notifyDataSetChanged();
    }

    public void removeUser(int position) {
        mContacts.remove(position);
        notifyItemRemoved(position);
    }
}
