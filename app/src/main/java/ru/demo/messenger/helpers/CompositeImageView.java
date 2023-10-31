package ru.demo.messenger.helpers;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeImageView extends CircleFrameLayout {

    private static final int IMAGES_COUNT = 4;

    private final List<SimpleDraweeView> imageViews = new ArrayList<>(IMAGES_COUNT);

    private int viewSize;
    private int dividedViewSize;

    @Nullable
    private List<String> urls;
    @Nullable
    private PlaceholderCreator placeholderCreator;

    interface PlaceholderCreator {
        Drawable create(int position, int width, int height);
    }

    public CompositeImageView(Context context) {
        super(context);
        init();
    }

    public CompositeImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompositeImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        for (int i = 0; i < IMAGES_COUNT; i++) {
            final SimpleDraweeView imageView = new SimpleDraweeView(getContext());
            imageView.getHierarchy().setFadeDuration(0);
            addView(imageView);
            imageViews.add(imageView);
        }

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                viewSize = getWidth();
                dividedViewSize = Math.round(viewSize / (float) 2);
                recompose(urls);
                return true;
            }
        });
    }

    void setPlaceholderCreator(@Nullable PlaceholderCreator placeholderCreator) {
        this.placeholderCreator = placeholderCreator;
    }

    public void setImageUrl(@Nullable String url) {
        recompose(Collections.singletonList(url));
    }

    public void setImageUrls(@Nullable List<String> urls) {
        recompose(urls);
    }

    private void recompose(@Nullable List<String> urls) {
        this.urls = urls;
        clearCanvas();

        if (urls == null || urls.isEmpty()) {
            invalidateViewsVisibility(0);
            invalidate();
            return;
        }

        final int urlsCount = urls.size();
        if (urlsCount == 1) {
            invalidateViewsVisibility(1);

            setImageSizeWithGravity(0, viewSize, viewSize, Gravity.NO_GRAVITY);

            setImageUrl(0, urls.get(0));
        } else if (urlsCount == 2) {
            invalidateViewsVisibility(2);

            setImageSizeWithGravity(0, dividedViewSize, viewSize, Gravity.LEFT);
            setImageSizeWithGravity(1, dividedViewSize, viewSize, Gravity.RIGHT);

            setImageUrl(0, urls.get(0));
            setImageUrl(1, urls.get(1));
        } else if (urlsCount == 3) {
            invalidateViewsVisibility(3);

            setImageSizeWithGravity(0, dividedViewSize, viewSize, Gravity.LEFT);
            setImageSizeWithGravity(1, dividedViewSize, dividedViewSize, Gravity.RIGHT | Gravity.TOP);
            setImageSizeWithGravity(2, dividedViewSize, dividedViewSize, Gravity.RIGHT | Gravity.BOTTOM);

            setImageUrl(0, urls.get(0));
            setImageUrl(1, urls.get(1));
            setImageUrl(2, urls.get(2));
        } else {
            invalidateViewsVisibility(4);

            setImageSizeWithGravity(0, dividedViewSize, dividedViewSize, Gravity.LEFT | Gravity.TOP);
            setImageSizeWithGravity(1, dividedViewSize, dividedViewSize, Gravity.LEFT | Gravity.BOTTOM);
            setImageSizeWithGravity(2, dividedViewSize, dividedViewSize, Gravity.RIGHT | Gravity.TOP);
            setImageSizeWithGravity(3, dividedViewSize, dividedViewSize, Gravity.RIGHT | Gravity.BOTTOM);

            setImageUrl(0, urls.get(0));
            setImageUrl(1, urls.get(1));
            setImageUrl(2, urls.get(2));
            setImageUrl(3, urls.get(3));
        }
    }

    private void invalidateViewsVisibility(int visibleIndex) {
        for (int i = 0; i < IMAGES_COUNT; i++) {
            if (i < visibleIndex) {
                getChildAt(i).setVisibility(VISIBLE);
            } else {
                getChildAt(i).setVisibility(GONE);
            }
        }
    }

    private void setImageSizeWithGravity(int index, int width, int height, int gravity) {
        final SimpleDraweeView imageView = imageViews.get(index);
        final LayoutParams layoutParams = (LayoutParams) imageView.getLayoutParams();

        final boolean noChanges = layoutParams.gravity == gravity
                && layoutParams.width == width
                && layoutParams.height == height;
        if (noChanges) {
            return;
        }

        layoutParams.gravity = gravity;
        layoutParams.width = width;
        layoutParams.height = height;
        imageView.requestLayout();
    }

    private void setImageUrl(int index, @Nullable String url) {
        final SimpleDraweeView imageView = imageViews.get(index);

        if (placeholderCreator != null) {
            final ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            final Drawable placeHolder = placeholderCreator.create(index,
                    layoutParams.width,
                    layoutParams.height
            );
            imageView.getHierarchy().setPlaceholderImage(placeHolder);
        } else {
            imageView.getHierarchy().setPlaceholderImage(null);
        }
        if (url == null) {
            invalidate();
        }
        imageView.setController(
                Fresco.newDraweeControllerBuilder()
                        .setControllerListener(new BaseControllerListener<ImageInfo>() {
                            @Override
                            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo,
                                                        @Nullable Animatable animatable) {
                                invalidate();
                            }
                        })
                        .setOldController(imageView.getController())
                        .setUri(url)
                        .build()
        );
    }

}
