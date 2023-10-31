package ru.demo.messenger.helpers;

import android.app.Activity;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import ru.demo.messenger.BuildConfig;
import ru.demo.messenger.R;

public class ViewStateSwitcher {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({State.STATE_MAIN, State.STATE_LOADING, State.STATE_EMPTY, State.STATE_ERROR, State.STATE_NOT_FOUND})
    public @interface State {
        String STATE_MAIN = "main";
        String STATE_LOADING = "loading";
        String STATE_EMPTY = "empty";
        String STATE_ERROR = "error";
        String STATE_NOT_FOUND = "not_found";
    }

    private static final int DEFAULT_ANIMATION_DURATION_MS = 200;

    private final LayoutInflater layoutInflater;
    /**
     * Container containing all view states
     */
    private final ViewGroup container;
    /**
     * View that is currently displayed
     */
    private View currentView;
    private final Map<String, ViewInfo> states = new HashMap<>();

    private final int animationDuration;
    private boolean isAnimating;
    /**
     * View to which we're animating to
     */
    private View animatingView;

    public ViewStateSwitcher(Activity activity, @IdRes int targetViewId) {
        this(activity, activity.findViewById(targetViewId));
    }

    public ViewStateSwitcher(Activity activity, View targetView) {
        layoutInflater = activity.getLayoutInflater();
        states.put(State.STATE_MAIN, new ViewInfo(0, targetView));
        currentView = targetView;
        animationDuration = DEFAULT_ANIMATION_DURATION_MS;
        container = (ViewGroup) targetView.getParent();
    }

    public void addViewState(@State String state, View stateView) {
        if (BuildConfig.DEBUG && states.containsKey(state)) {
            throw new IllegalStateException(String.format("State %s already added", state));
        }
        stateView.setVisibility(View.GONE);
        states.put(state, new ViewInfo(0, stateView));
    }

    public void addViewState(@State String state, @LayoutRes int layoutId) {
        if (BuildConfig.DEBUG && states.containsKey(state)) {
            throw new IllegalStateException(String.format("State %s already added", state));
        }
        states.put(state, new ViewInfo(layoutId, null));
    }

    public void switchToState(@State String state, boolean animate) {
        final ViewInfo viewInfo = states.get(state);
        if (BuildConfig.DEBUG && viewInfo == null) {
            throw new IllegalStateException(String.format("State %s was not added", state));
        }

        if (isAnimating) {
            clearAnimations();
        }
        if (viewInfo.view == null) {
            viewInfo.view = layoutInflater.inflate(viewInfo.layoutId, container, false);
        }
        final View nextView = viewInfo.view;
        if (nextView == currentView) {
            return;
        }
        if (nextView.getParent() != container) {
            container.addView(nextView);
        }
        if (animate) {
            animate(nextView);
        } else {
            show(nextView);
        }
    }

    private void clearAnimations() {
        if (animatingView.getAnimation() != null) {
            animatingView.getAnimation().setAnimationListener(null);
        }
        currentView.clearAnimation();
        animatingView.clearAnimation();

        currentView.setVisibility(View.GONE);
        animatingView.setVisibility(View.VISIBLE);
        currentView = animatingView;
        isAnimating = false;
    }

    private void show(@NonNull View nextView) {
        currentView.setVisibility(View.GONE);
        nextView.setVisibility(View.VISIBLE);
        currentView = nextView;
    }

    private void animate(@NonNull final View nextView) {
        isAnimating = true;
        animatingView = nextView;
        currentView.setVisibility(View.VISIBLE);
        nextView.setVisibility(View.VISIBLE);

        final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setFillAfter(true);
        fadeOut.setDuration(animationDuration);

        final AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setFillAfter(true);
        fadeIn.setDuration(animationDuration);
        fadeIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimations();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        currentView.startAnimation(fadeOut);
        nextView.startAnimation(fadeIn);
    }

    private static class ViewInfo {
        @LayoutRes
        int layoutId;
        View view;

        private ViewInfo(@LayoutRes int layoutId, View view) {
            this.layoutId = layoutId;
            this.view = view;
        }
    }

    public View inflateStateView(int layout) {
        return layoutInflater.inflate(layout, container, false);
    }

    /**
     * Crate ViewSwitcher with added default {@link ViewStateSwitcher.State#STATE_LOADING}
     *
     * @param activity targetActivity
     * @param mainView view with main content
     * @return ViewSwitcher
     */
    public static ViewStateSwitcher createStandardSwitcher(
            @NonNull Activity activity, @NonNull View mainView) {
        final ViewStateSwitcher result = new ViewStateSwitcher(activity, mainView);
        result.addStandardLoading();
        return result;
    }

    public static ViewStateSwitcher createStandardSwitcher(@NonNull Activity activity,
                                                           @IdRes int targetViewId) {
        final ViewStateSwitcher result = new ViewStateSwitcher(activity, targetViewId);
        result.addStandardLoading();
        return result;
    }

    private void addStandardLoading() {
        addViewState(State.STATE_LOADING, inflateStateView(R.layout.loading_state_view));
    }

    @NonNull
    public static TextView addTextState(@NonNull ViewStateSwitcher viewStateSwitcher,
                                        @State String state, String text) {
        final View emptyView = viewStateSwitcher.layoutInflater.inflate(R.layout.empty_state_view, viewStateSwitcher.container, false);
        final TextView textView = (TextView) emptyView.findViewById(R.id.tvMessage);
        textView.setText(text);
        viewStateSwitcher.addViewState(state, emptyView);
        return textView;
    }

    @NonNull
    public static TextView addStandardErrorView(@NonNull ViewStateSwitcher stateSwitcher,
                                                String defaultMessage, String retryButtonName,
                                                View.OnClickListener onRetryClickListener) {
        final View errorView = stateSwitcher.inflateStateView(R.layout.error_state_view);
        stateSwitcher.addViewState(State.STATE_ERROR, errorView);

        final Button btnRetry = (Button) errorView.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(onRetryClickListener);
        btnRetry.setText(retryButtonName);

        final TextView tvMessage = (TextView) errorView.findViewById(R.id.tvMessage);
        tvMessage.setText(defaultMessage);

        return tvMessage;
    }
}
