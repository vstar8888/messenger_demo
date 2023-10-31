package ru.demo.messenger.chats.fullscreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;

import biz.growapp.base.BaseAppActivity;
import butterknife.BindView;
import me.relex.photodraweeview.PhotoDraweeView;
import ru.demo.messenger.R;

public class PictureViewActivity extends BaseAppActivity {
    private static final String EXTRA_URL = "extraUrl";

    public static Intent navigateToFullscreenImage(@NonNull Context context, String url) {
        final Intent intent = new Intent(context, PictureViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    @BindView(R.id.pdvPhoto) PhotoDraweeView pdvPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        String photoUrl = getIntent().getStringExtra(EXTRA_URL);
        if (TextUtils.isEmpty(photoUrl)) {
            finish();
        }
        final boolean isFile = photoUrl.startsWith("/");
        if (isFile) {
            photoUrl = Uri.fromFile(new File(photoUrl)).toString();
        }

        final PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        controller.setUri(photoUrl);
        controller.setOldController(pdvPhoto.getController());
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null || pdvPhoto == null) {
                    return;
                }
                pdvPhoto.update(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });
        pdvPhoto.setController(controller.build());
    }
}
