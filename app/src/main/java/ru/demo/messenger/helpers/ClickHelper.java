package ru.demo.messenger.helpers;

import android.os.SystemClock;
import androidx.annotation.IdRes;
import android.view.View;

public class ClickHelper {
    private static final int MIS_CLICK_THRESHOLD_MS = 1000;
    // variable to track event time to prevent double click
    private long mLastClickTime = 0;
    @IdRes
    private int lastClickedViewId = View.NO_ID;

    public boolean isDoubleClicked(@IdRes int viewId) {
        if (lastClickedViewId != viewId) {
            lastClickedViewId = viewId;
            mLastClickTime = SystemClock.elapsedRealtime();
            return false;
        }
        lastClickedViewId = viewId;
        if (SystemClock.elapsedRealtime() - mLastClickTime < MIS_CLICK_THRESHOLD_MS) {
            return true;
        } else {
            mLastClickTime = SystemClock.elapsedRealtime();
            return false;
        }
    }

}
