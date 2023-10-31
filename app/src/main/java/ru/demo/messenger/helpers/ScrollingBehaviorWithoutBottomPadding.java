package ru.demo.messenger.helpers;

import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

// TODO: ET 24.11.16 This WA for viewpager bottom padding. Have some issue when app bar collapsed/expanded
// http://stackoverflow.com/questions/30777698/android-footer-scrolls-off-screen-when-used-in-coordinatorlayout
@SuppressWarnings("unused")
public class ScrollingBehaviorWithoutBottomPadding extends AppBarLayout.ScrollingViewBehavior {

    private AppBarLayout appBarLayout;

    public ScrollingBehaviorWithoutBottomPadding() {
        super();
    }

    public ScrollingBehaviorWithoutBottomPadding(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        if (appBarLayout == null) {
            appBarLayout = (AppBarLayout) dependency;
        }

        final boolean result = super.onDependentViewChanged(parent, child, dependency);
        final int bottomPadding = calculateBottomPadding(appBarLayout);
        final boolean paddingChanged = bottomPadding != child.getPaddingBottom();
        if (paddingChanged) {
            child.setPadding(
                    child.getPaddingLeft(),
                    child.getPaddingTop(),
                    child.getPaddingRight(),
                    bottomPadding);
            child.requestLayout();
        }
        return paddingChanged || result;
    }


    // Calculate the padding needed to keep the bottom of the view pager's content at the same location on the screen.
    private int calculateBottomPadding(AppBarLayout dependency) {
        final int totalScrollRange = dependency.getTotalScrollRange();
        return totalScrollRange + dependency.getTop();
    }
}