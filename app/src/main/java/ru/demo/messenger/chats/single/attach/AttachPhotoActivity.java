package ru.demo.messenger.chats.single.attach;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import biz.growapp.base.BaseAppActivity;
import biz.growapp.base.helpers.MediaFilePicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.demo.messenger.R;
import ru.demo.messenger.utils.VectorUtils;

public class AttachPhotoActivity extends BaseAppActivity implements MediaFilePicker.OnFilePickerListener {

    private static final String TAG = AttachPhotoActivity.class.getSimpleName();

    private static final int COUNT_COLUMNS = 3;
    private static final String EXTRA_OUT_PATH_IMAGES = "pathImages";

    private static final String ARG_MEDIA_PICKER = "media_picker";

    @NonNull
    public static Intent getIntent(@NonNull Context context) {
        return new Intent(context, AttachPhotoActivity.class);
    }

    @Nullable
    public static ArrayList<String> unpackImagesPath(@NonNull Intent data) {
        return data.getStringArrayListExtra(EXTRA_OUT_PATH_IMAGES);
    }

    @BindView(R.id.vTouchOutside) View vTouchOutside;
    @BindView(R.id.rvPhotos) RecyclerView rvPhotos;
    @BindView(R.id.ablAppBar) AppBarLayout ablAppBar;
    @BindView(R.id.tvAttach) TextView tvAttach;

    private BottomSheetBehavior<View> sheetBehavior;

    private GalleryPhotoAdapter adapter;

    private MediaFilePicker photoPicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attach_photo);
        overridePendingTransition(R.anim.slide_in_bottom, 0);

        Bundle bundle = null;
        if (savedInstanceState != null) {
            bundle = savedInstanceState.getBundle(ARG_MEDIA_PICKER);
        }
        photoPicker = new MediaFilePicker(this, this, bundle);

        //noinspection ConstantConditions
        getToolbar().setNavigationIcon(VectorUtils.getVectorDrawable(this, R.drawable.ic_close));

        View slidingView = findViewById(R.id.vgPhotosContainer);
        sheetBehavior = BottomSheetBehavior.from(slidingView);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        closeBottomSheet();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        setStatusBarDim(false);
                        break;
                    default:
                        setStatusBarDim(true);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (bottomSheet.getTop() <= ablAppBar.getBottom()) {
                    final int height = ablAppBar.getHeight();
                    float diff = ((height - (bottomSheet.getTop() - ablAppBar.getTop())) / (height / 100f)) / 100;
                    bottomSheet.setPadding(0, (int) (height * diff), 0, slidingView.getPaddingBottom());
//                    bottomSheet.setPadding(0, (int) (height * diff), 0, 0);
                    ablAppBar.setVisibility(View.VISIBLE);
                    ablAppBar.setAlpha(diff);
                } else {
                    if (slidingView.getPaddingTop() != 0) {
                        slidingView.setPadding(0, 0, 0, slidingView.getPaddingBottom());
//                        rvPhotos.setPadding(0, 0, 0, 0);
                    }
                    ablAppBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        setStatusBarDim(true);

        initRecyclerView();

        setupVectorDrawables();
    }

    private void initRecyclerView() {
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, COUNT_COLUMNS);
        rvPhotos.setLayoutManager(gridLayoutManager);
        adapter = new GalleryPhotoAdapter(this, COUNT_COLUMNS, new GalleryPhotoAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(String path, boolean isAdd, int count) {
                if (count == 0) {
                    tvAttach.setText(R.string.chat_action_cancel);
                } else {
                    final String string = getString(R.string.chat_action_attach, count);
                    // FIXME: 16.12.16 why not work???
                    SpannableString spannableString = new SpannableString(string);
                    spannableString.setSpan(new RoundedBackgroundSpan(AttachPhotoActivity.this, R.color.white, R.color.main_blue),
                            string.indexOf(' ') + 1, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tvAttach.setText(spannableString);
                }
            }
        });
        rvPhotos.setAdapter(adapter);
    }

    private void setupVectorDrawables() {
        final Drawable takePhotoIcon = VectorUtils.getVectorDrawable(this, R.drawable.ic_take_photo);
        ((TextView)findViewById(R.id.tvTakePicture)).setCompoundDrawablesWithIntrinsicBounds(takePhotoIcon, null, null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter.isEmpty()) {
            List<String> listImagesPath = getImagesPath(this);
            removeBrokenImages(listImagesPath);
            adapter.addAll(listImagesPath);
        }
    }

    private List<String> getImagesPath(@NonNull Context context) {
        Uri uri;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        Cursor cursor;
        int column_index_data;
        String pathOfImage;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA};

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);

        if (cursor != null) {
            try {
                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                while (cursor.moveToNext()) {
                    pathOfImage = cursor.getString(column_index_data);

                    if (!listOfAllImages.contains(pathOfImage)) {
                        listOfAllImages.add(0, pathOfImage);
                    }
                }
                return listOfAllImages;
            } finally {
                cursor.close();
            }
        }
        return Collections.emptyList();
    }

    private void removeBrokenImages(List<String> listImagesPath) {
        Iterator<String> iterator = listImagesPath.iterator();
        while (iterator.hasNext()) {
            String path = iterator.next();
            File file = new File(path);
            if (file.length() == 0) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(ARG_MEDIA_PICKER, photoPicker.saveState());
        super.onSaveInstanceState(outState);
    }

    @OnClick(R.id.tvAttach)
    void onAttachClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        finishWithResultImages(adapter.getSelectedPhotos());
    }

    private void finishWithResultImages(ArrayList<String> imagePaths) {
        final Intent data = new Intent();
        data.putStringArrayListExtra(EXTRA_OUT_PATH_IMAGES, imagePaths);
        setResult(RESULT_OK, data);
        finish();
    }

    @OnClick(R.id.vgTakePicture)
    protected void onTakePictureClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        photoPicker.requestCameraIntent();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }

    @OnClick(R.id.vTouchOutside)
    void closeBottomSheet() {
        finish();
    }

    @SuppressWarnings("ResourceAsColor")
    private void setStatusBarDim(boolean dim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dim ? Color.TRANSPARENT :
                    ContextCompat.getColor(this, getThemedResId(R.attr.colorPrimaryDark)));
        }
    }

    private int getThemedResId(@AttrRes int attr) {
        TypedArray a = getTheme().obtainStyledAttributes(new int[]{attr});
        int resId = a.getResourceId(0, 0);
        a.recycle();
        return resId;
    }

    @Override
    public void onFilePicked(File file) {
        if (file == null) {
            return;
        }
        final ArrayList<String> selectedPhotos = new ArrayList<>(adapter.getSelectedPhotos());
        selectedPhotos.add(file.getAbsolutePath());
        finishWithResultImages(selectedPhotos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!photoPicker.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
