package ru.demo.messenger.helpers;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private final OnItemClickListener mListener;
    private final GestureDetector mGestureDetector;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public RecyclerItemClickListener(Context context, OnItemClickListener mListener) {
        this.mListener = mListener;
        this.mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    public RecyclerItemClickListener(OnItemClickListener mListener, GestureDetector mGestureDetector) {
        this.mListener = mListener;
        this.mGestureDetector = mGestureDetector;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        final View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            int position = rv.getChildAdapterPosition(child);
            mListener.onItemClick(child, position);
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
