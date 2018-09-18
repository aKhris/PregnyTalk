package com.akhris.pregnytalk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import com.akhris.pregnytalk.adapters.ViewHolderFactory;

public class SwipeableRecyclerView extends RecyclerView {
    public SwipeableRecyclerView(Context context) {
        super(context);
    }

    public SwipeableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void initSwiping(SwipeCallbacks swipeCallbacks){
        RecyclerItemTouchHelperCallback itemTouchHelperCallback =
                new RecyclerItemTouchHelperCallback(0, ItemTouchHelper.LEFT, swipeCallbacks);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this);
    }

    class RecyclerItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        private SwipeCallbacks mCallbacks;

        public RecyclerItemTouchHelperCallback(int dragDirs, int swipeDirs, SwipeCallbacks mCallbacks) {
            super(dragDirs, swipeDirs);
            this.mCallbacks = mCallbacks;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mCallbacks.onSwiped(viewHolder, direction);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if(!(viewHolder instanceof ViewHolderFactory.WithBackgroundHolder)){return;}
            final View foregroundView = ((ViewHolderFactory.WithBackgroundHolder) viewHolder).foreground;
            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }
    }

    interface SwipeCallbacks{
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction);
    }
}
