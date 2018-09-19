package com.akhris.pregnytalk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.akhris.pregnytalk.MainActivity;
import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.User;
import com.akhris.pregnytalk.utils.ImageUtils;
import com.akhris.pregnytalk.utils.SharedPrefUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ContactsListAdapter extends RecyclerView.Adapter<ViewHolderFactory.ContactsItemHolder> {

    private List<User> mContacts;
    private ContactsItemClickListener mContactsItemClickListener;
    private boolean wasBouncedAfterAdapterCreation=false;

    public ContactsListAdapter(ContactsItemClickListener mContactsItemClickListener) {
        this.mContactsItemClickListener = mContactsItemClickListener;
        this.mContacts = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolderFactory.ContactsItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolderFactory.onCreateContactsItemViewHolder(parent, mContactsItemClickListener);
    }

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
            holder.sendMessage.setVisibility(View.INVISIBLE);
        } else {
            holder.sendMessage.setVisibility(View.VISIBLE);
        }

        if(!SharedPrefUtils.wasBounced(holder.itemView.getContext(), this.getClass()) && !wasBouncedAfterAdapterCreation){
            holder.bounce();
            wasBouncedAfterAdapterCreation = true;
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
}
