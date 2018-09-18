package com.akhris.pregnytalk.adapters;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.Child;
import com.akhris.pregnytalk.utils.DateUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChildrenListAdapter extends RecyclerView.Adapter<ChildrenListAdapter.ChildHolder>{

    private List<Child> mChildren;
    private ChildCallback mChildCallback;

    public ChildrenListAdapter(List<Child> mChildren, ChildCallback mChildCallback) {
        this.mChildren = mChildren;
        this.mChildCallback = mChildCallback;
    }

    @NonNull
    @Override
    public ChildHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChildHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.children_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChildHolder holder, int position) {
        holder.position.setText(String.valueOf(position+1));
        Child child = mChildren.get(position);
        holder.name.setText(child.getName());
        holder.dateOfBirth.setText(DateUtils.formatDateFromMillis(child.getBirthDateMillis()));
        holder.setItemColor(
                child.getSex().equals(Child.SEX_FEMALE)?
                        R.color.babyGirl:
                        R.color.babyBoy
        );
    }

    @Override
    public int getItemCount() {
        return mChildren.size();
    }


    class ChildHolder extends ViewHolderFactory.WithBackgroundHolder{
        @BindView(R.id.tv_children_list_position) TextView position;
        @BindView(R.id.tv_children_list_name) TextView name;
        @BindView(R.id.tv_children_list_date_of_birth) TextView dateOfBirth;

        public ChildHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v->mChildCallback.onChildClick(mChildren.get(getAdapterPosition())));
        }

        void setItemColor(@ColorRes int colorResId){
            int color = ContextCompat.getColor(itemView.getContext(), colorResId);
            this.position.setTextColor(color);
            this.name.setTextColor(color);
            this.dateOfBirth.setTextColor(color);
        }
    }

    public interface ChildCallback{
        void onChildClick(Child child);
    }


}
