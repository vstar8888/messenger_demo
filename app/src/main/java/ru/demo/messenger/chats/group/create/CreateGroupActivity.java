package ru.demo.messenger.chats.group.create;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import biz.growapp.base.helpers.MediaFilePicker;
import biz.growapp.base.loading.BaseAppLoadingActivity;
import biz.growapp.base.utils.DraweeUtils;
import butterknife.BindView;
import butterknife.OnClick;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.single.SingleChatActivity;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.internal.di.Injection;
import ru.demo.messenger.utils.DisplayUtils;
import rx.android.schedulers.AndroidSchedulers;

public class CreateGroupActivity extends BaseAppLoadingActivity
        implements CreateGroupPresenter.CreateGroupView, PeopleAdapter.ItemClickListener,
        MediaFilePicker.OnFilePickerListener {

    public static final String EXTRA_GROUP_USERS = "extra.GROUP_USERS";

    private static final String ARG_MEDIA_PICKER = "ARG_MEDIA_PICKER";
    private static final String ARG_UPLOAD_FILE_URI = "ARG_UPLOAD_FILE_URI";

    private static final int ONE_ON_ONE_CHAT_SIZE = 1;
    private int groupAvatarSize;

    public static Intent getIntent(Context context, List<UserModel> users) {
        final Intent intent = new Intent(context, CreateGroupActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_GROUP_USERS, new ArrayList<>(users));
        return intent;
    }

    @BindView(R.id.rvUsers) RecyclerView rvUsers;
    @BindView(R.id.etGroupTitle) EditText etGroupTitle;
    @BindView(R.id.tilGroupTitle) TextInputLayout tilGroupTitle;
    @BindView(R.id.sdvGroupAvatar) SimpleDraweeView sdvGroupAvatar;
    @BindView(R.id.ivCamera) ImageView ivCamera;

    private PeopleAdapter adapter;

    private CreateGroupPresenter presenter;

    private MediaFilePicker photoPicker = null;
    @Nullable private File avatarFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setupRecyclerView();

        final ArrayList<UserModel> selectedUsers =
                getIntent().getParcelableArrayListExtra(EXTRA_GROUP_USERS);
        adapter.addAll(selectedUsers);

        Bundle bundle = null;
        if (savedInstanceState != null) {
            bundle = savedInstanceState.getBundle(ARG_MEDIA_PICKER);
            final Uri uri = savedInstanceState.getParcelable(ARG_UPLOAD_FILE_URI);
            if (uri != null) {
                setAvatarImage(new File(uri.getPath()));
            }
        }
        photoPicker = new MediaFilePicker(this, this, bundle);
        presenter = new CreateGroupPresenter(this);

        groupAvatarSize = getResources().getDimensionPixelSize(R.dimen.group_avatar_size);

        etGroupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    tilGroupTitle.setErrorEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        switchToMain(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(ARG_MEDIA_PICKER, photoPicker.saveState());
        if (avatarFile != null) {
            outState.putParcelable(ARG_UPLOAD_FILE_URI, Uri.fromFile(avatarFile));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFilePicked(File file) {
        if (file == null) {
            return;
        }
        presenter.compressAndRotate(this, file)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setAvatarImage);
    }

    private void setAvatarImage(File file) {
        this.avatarFile = file;
        final Uri uri = Uri.fromFile(file);
        Fresco.getImagePipeline().evictFromCache(uri);
        ivCamera.setVisibility(View.GONE);
        DraweeUtils.setResizedImage(sdvGroupAvatar, uri, groupAvatarSize, groupAvatarSize);
    }

    private void setupRecyclerView() {
        rvUsers.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new PeopleAdapter(this, this, Injection.provideColorGenerator(this));
        rvUsers.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (TextUtils.isEmpty(etGroupTitle.getText().toString().trim())) {
                    tilGroupTitle.setError(getString(R.string.create_group_fill_required));
                    tilGroupTitle.setErrorEnabled(true);
                    return true;
                } else {
                    tilGroupTitle.setErrorEnabled(false);
                }
                DisplayUtils.hideSoftKeyboard(this);
                switchToLoading(true);
                presenter.getSelfUserForCreateChat();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public int getMainContainerId() {
        return R.id.vgContainer;
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToMain(true);
    }

    @OnClick(R.id.sdvGroupAvatar)
    public void setPhoto(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        new AlertDialog.Builder(this)
                .setItems(R.array.photo_dialog_options, new DialogInterface.OnClickListener() {
                    static final int CAMERA = 0;
                    static final int GALLERY = 1;
                    static final int CANCEL = 2;

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case CAMERA:
                                photoPicker.requestCameraIntent();
                                break;
                            case GALLERY:
                                photoPicker.requestGalleryIntent();
                                break;
                            case CANCEL:
                                dialog.cancel();
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!photoPicker.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRemoveClick(int viewId, int position) {
        if (adapter.removeWithAnim(position)) {
            if (adapter.getItemCount() <= ONE_ON_ONE_CHAT_SIZE) {
                setResultUsers();
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResultUsers();
        super.onBackPressed();
    }

    private void setResultUsers() {
        final Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_GROUP_USERS,
                new ArrayList<>(adapter.getItems()));
        setResult(RESULT_CANCELED, data);
    }

    @Override
    public void onUserLoaded(UserModel user) {
        final String groupName = etGroupTitle.getText().toString().trim();
        presenter.createChat(adapter.getItems(), groupName,
                getString(R.string.create_group_user_created_group, user.getFullName(), groupName));
    }

    @Override
    public void onChatCreated(ChatModel chat) {
        if (avatarFile != null) {
            presenter.changeChatAvatar(avatarFile, chat);
        } else {
            onPrepareChatCompleted(chat);
        }
    }

    @Override
    public void onPrepareChatCompleted(ChatModel chat) {
        if (chat.getPhoto() != null) {
            presenter.sendUpdateChatEvent(chat);
        }
        startActivity(SingleChatActivity.openNewGroupChat(this, chat));
        setResult(RESULT_OK);
        finish();
    }

}
