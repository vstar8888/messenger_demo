package ru.demo.messenger.chats.single.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import biz.growapp.base.adapter.DelegationAdapter;
import biz.growapp.base.helpers.MediaFilePicker;
import biz.growapp.base.loading.BaseAppLoadingActivity;
import butterknife.BindView;
import butterknife.OnClick;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.fullscreen.PictureViewActivity;
import ru.demo.messenger.chats.group.select.SelectGroupPeopleActivity;
import ru.demo.messenger.chats.single.SingleChatActivity;
import ru.demo.messenger.chats.single.settings.rename.RenameChatActivity;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.helpers.CompositeImageHelper;
import ru.demo.messenger.helpers.CompositeImageView;
import ru.demo.messenger.internal.di.Injection;
import ru.demo.messenger.network.ConnectionService;
import ru.demo.messenger.people.profile.ProfileActivity;
import ru.demo.messenger.utils.VectorUtils;
import rx.android.schedulers.AndroidSchedulers;


public class ChatSettingsActivity extends BaseAppLoadingActivity implements
        ChatParticipantDelegate.Callback,
        AddParticipantDelegate.Callback,
        LeaveGroupDelegate.Callback,
        RemoveGroupDelegate.Callback,
        ChatSettingsPresenter.View,
        MediaFilePicker.OnFilePickerListener {

    private static final String EXTRA_IN_CHAT = "extra.IN_CHAT";
    private static final String EXTRA_OUT_UPDATED_CHAT = "extra.OUT_UPDATED_CHAT";

    private static final String ARG_MEDIA_PICKER = "ARG_MEDIA_PICKER";
    private static final String ARG_UPLOAD_FILE_URI = "ARG_UPLOAD_FILE_URI";

    private static final int REQUEST_ADD_USER = 100;
    private static final int REQUEST_RENAME_CHAT = 200;

    @NonNull
    public static Intent getIntent(@NonNull Context context, @NonNull ChatModel chat) {
        return new Intent(context, ChatSettingsActivity.class)
                .putExtra(EXTRA_IN_CHAT, chat);
    }

    @Nullable
    public static ChatModel unpackUpdatedChat(@NonNull Intent data) {
        return data.getParcelableExtra(EXTRA_OUT_UPDATED_CHAT);
    }

    @BindView(R.id.ablSettings) AppBarLayout ablSettings;
    @BindView(R.id.civAvatar) CompositeImageView civAvatar;
    @BindView(R.id.fabAvatar) FloatingActionButton fabAvatar;
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindView(R.id.tvStatus) TextView tvStatus;
    @BindView(R.id.rvUsers) RecyclerView rvUsers;

    private final DelegationAdapter<Object> adapter = new DelegationAdapter<>();

    private ChatSettingsPresenter presenter;

    private CompositeImageHelper compositeImageHelper;
    private MediaFilePicker photoPicker = null;
    @Nullable
    private File avatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        ChatModel chat = getIntent().getParcelableExtra(EXTRA_IN_CHAT);
        presenter = new ChatSettingsPresenter(this, chat,
                Injection.provideChatUpdateManager()
        );

        Bundle bundle = null;
        if (savedInstanceState != null) {
            bundle = savedInstanceState.getBundle(ARG_MEDIA_PICKER);
            final Uri uri = savedInstanceState.getParcelable(ARG_UPLOAD_FILE_URI);
            if (uri != null) {
                onFilePicked(new File(uri.getPath()));
            }
        }
        photoPicker = new MediaFilePicker(this, this, bundle);

        setupRecyclerView();

        setTitle(chat.getTitle().trim());
        updateUserCountTitle();
        updateAdminPossibilities();

        compositeImageHelper = Injection.provideCompositeImageHelper(this);
        updateToolbarParticipantsAvatar(chat);

        switchToMain(false);
    }

    private void updateTitleEditPencilVisibility() {
        if (presenter.isMeAdmin()) {
            final Drawable pencilIcon = VectorUtils.getVectorDrawable(this, R.drawable.ic_white_pencil);
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, pencilIcon, null);
        } else {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    @Override
    public void updateToolbarParticipantsAvatar(ChatModel chat) {
        compositeImageHelper.showChatImage(civAvatar, chat, presenter.getSelfUserId());
    }

    private void updateAdminPossibilities() {
        updateFabAvatarVisibility();
        updateTitleEditPencilVisibility();
    }

    @Override
    public void removeMe(long selfUserId) {
        Object firstItem = adapter.getItem(0);
        if (firstItem instanceof AddParticipantDelegate.Item) {
            adapter.remove(0);
        }
        Object lastItem = adapter.getItem(adapter.getItemCount() - 1);
        if (lastItem instanceof LeaveGroupDelegate.Item) {
            adapter.remove(adapter.getItemCount() - 1);
        }
        addYouNoLongerParticipant();
        int myPos = findPosByUserId(selfUserId);
        adapter.notifyItemChanged(myPos);
        updateAdminPossibilities();
    }

    @Override
    public void addMe() {
        Object lastItem = adapter.getItem(adapter.getItemCount() - 1);
        removeYouNoLongerParticipant();
        if (!(lastItem instanceof LeaveGroupDelegate.Item)) {
            adapter.add(new LeaveGroupDelegate.Item());
        }
    }

    private void setTitle(String chatTitle) {
        tvTitle.setText(chatTitle);
    }

    private void setupRecyclerView() {
        adapter.getManager()
                .addDelegate(new ChatParticipantDelegate(this, this, Injection.provideColorGenerator(this)))
                .addDelegate(new AddParticipantDelegate(this, this))
                .addDelegate(new LeaveGroupDelegate(this, this))
                .addDelegate(new YouNoLongerParticipantDelegate(this))
                .addDelegate(new RemoveGroupDelegate(this, this));
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        if (presenter.isMeAdmin()) {
            adapter.add(new AddParticipantDelegate.Item());
        }
        List<Object> list = new ArrayList<>();
        list.addAll(presenter.getChat().getUsers());
        adapter.addAll(list);
        if (presenter.getChat().isInChain()) {
            adapter.add(new LeaveGroupDelegate.Item());
        } else {
            addYouNoLongerParticipant();
        }
    }

    private void updateUserCountTitle() {
        int usersCount = presenter.getChat().getUsers().size();
        String userCountTitle = getResources().getQuantityString(R.plurals.group_chat_count, usersCount, usersCount);
        tvStatus.setText(userCountTitle);
    }

    @Override
    public void onRemoveGroupClick() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat_participants_remove_group_question))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> presenter.removeChat())
                .show();
    }

    @Override
    public void onUserClick(int position) {
        final UserModel user = (UserModel) adapter.getItem(position);
        final long userId = user.getId();
        if (userId == presenter.getSelfUserId()) {
            return;
        }
        final ArrayList<String> items = new ArrayList<>();
        items.add(getString(R.string.chat_participants_alert_write_to_user));
        items.add(getString(R.string.chat_participants_alert_open_user_profile));
        if (presenter.isMeAdmin()) {
            if (presenter.isUserAdmin(userId)) {
                items.add(getString(R.string.chat_participants_alert_withdraw_admin_rules));
            } else {
                items.add(getString(R.string.chat_participants_alert_give_admin_rules));
            }
            items.add(getString(R.string.chat_participants_alert_remove_from_group));
        }
        new AlertDialog.Builder(this)
                .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                    static final int WRITE_TO = 0;
                    static final int OPEN_PROFILE = 1;
                    static final int GIVE_OR_WITHDRAW_ADMIN_RULES = 2;
                    static final int REMOVE_FROM_GROUP = 3;

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case WRITE_TO:
                                startActivity(SingleChatActivity.openSingleChat(ChatSettingsActivity.this, user));
                                break;
                            case OPEN_PROFILE:
                                startActivity(ProfileActivity.ofUser(ChatSettingsActivity.this, userId));
                                break;
                            case GIVE_OR_WITHDRAW_ADMIN_RULES:
                                if (presenter.isUserAdmin(userId)) {
                                    presenter.removeFromChainAdmins(userId, position);
                                } else {
                                    presenter.addToChainAdmins(userId, position);
                                }
                                break;
                            case REMOVE_FROM_GROUP:
                                showRemoveUserAlert(userId, position);
                                break;
                        }
                    }
                })
                .show();
    }

    private void showRemoveUserAlert(long userId, int position) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat_participants_alert_remove_from_group_question))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        presenter.removeUserFromGroup(userId, position))
                .show();
    }

    @Override
    public List<Long> getAdminsList() {
        return presenter.getChat().getAdminsList();
    }

    @Override
    public void onAddUserClick(int position) {
        startActivityForResult(
                SelectGroupPeopleActivity.forChatSettings(this,
                        presenter.getChat().getUsers(),
                        presenter.getChat().getTitle()
                ),
                REQUEST_ADD_USER
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!photoPicker.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_RENAME_CHAT:
                final String newChatName = RenameChatActivity.unpackNewChatName(data);
                presenter.getChat().setTitle(newChatName);
                setTitle(newChatName);
                break;
            case REQUEST_ADD_USER: {
                final ArrayList<UserModel> users = SelectGroupPeopleActivity.unpackSelectedUsers(data);
                presenter.addUsersToGroup(users);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResultAndFinish();
    }

    private void setResultAndFinish() {
        final Intent data = new Intent()
                .putExtra(EXTRA_OUT_UPDATED_CHAT, presenter.getChat());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroyView();
        super.onDestroy();
    }

    @Override
    public void onUsersAdded(ArrayList<UserModel> users) {
        for (int i = 0; i < users.size(); i++) {
            // add before LeaveGroupDelegate.Item
            adapter.add(users.get(i), adapter.getItems().size() - 1);
        }
        updateUserCountTitle();
    }

    @Override
    public void onUserRemoved(int position) {
        adapter.remove(position);
        updateUserCountTitle();
    }

    @Override
    public void onUserRemovedFromAdmins(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void setUserAsAdmin(int position) {
        if (position >= 0) {
            adapter.notifyItemChanged(position);
        }
    }

    @Override
    public void setMeAsAdmin() {
        int position = findPosByUserId(presenter.getSelfUserId());
        if (position >= 0) {
            adapter.notifyItemChanged(position);
            if (!(adapter.getItem(0) instanceof AddParticipantDelegate.Item)) {
                adapter.add(new AddParticipantDelegate.Item(), 0);
            }
            updateAdminPossibilities();
        }
    }

    private int findPosByUserId(long userId) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Object item = adapter.getItem(i);
            if (item instanceof UserModel && ((UserModel) item).getId() == userId) {
                return i;
            }
        }
        return -1;
    }

    private void addYouNoLongerParticipant() {
        adapter.add(new YouNoLongerParticipantDelegate.Item(), 0);
        adapter.add(new RemoveGroupDelegate.Item());
    }

    private void removeYouNoLongerParticipant() {
        Object lastItem = adapter.getItem(adapter.getItemCount() - 1);
        if (lastItem instanceof RemoveGroupDelegate.Item) {
            adapter.remove(adapter.getItemCount() - 1);
            Object firstItem = adapter.getItem(0);
            if (firstItem instanceof YouNoLongerParticipantDelegate.Item) {
                adapter.remove(0);
            }
        }
    }

    @Override
    public void onGroupLeaved() {
        addYouNoLongerParticipant();
        setResultAndFinish();
    }

    @Override
    public void setGroupAvatar(String chatAvatar) {
        civAvatar.setImageUrl(chatAvatar);
    }

    @Override
    public void showViewLoadingDialog() {
        showLoadingDialog(null, false);
    }

    @Override
    public void dismissViewLoadingDialog() {
        dismissLoadingDialog();
    }

    @Override
    public void updateUsers(List<Long> prevAdminsList, List<Long> newAdminsList, List<UserModel> users) {
        for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
            Object item = adapter.getItem(i);
            if (!(item instanceof UserModel)) {
                continue;
            }
            UserModel user = (UserModel) item;

            if (!users.contains(user)) {
                adapter.remove(i);
                continue;
            }

            boolean isInPrevAdmins = prevAdminsList.contains(user.getId());
            boolean isInNewAdmins = newAdminsList.contains(user.getId());
            if (isInPrevAdmins && isInNewAdmins) {
                continue;
            }
            if (isInPrevAdmins || isInNewAdmins) {
                adapter.notifyItemChanged(i);
            }
        }
        for (int i = 0; i < users.size(); i++) {
            UserModel user = users.get(i);
            if (!adapter.getItems().contains(user) && adapter.getItemCount() > 0) {
                if (adapter.getItem(adapter.getItemCount() - 1) instanceof LeaveGroupDelegate.Item) {
                    adapter.add(user, adapter.getItemCount() - 1);
                } else {
                    adapter.add(user);
                }
            }
        }
        updateAdminPossibilities();
        updateUserCountTitle();
    }

    @Override
    public int getMainContainerId() {
        return R.id.rvUsers;
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToMain(false);
    }

    @Override
    public void onLeaveGroupClick() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.chat_participants_leave_group_question))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> presenter.leaveGroup())
                .show();
    }

    private void updateFabAvatarVisibility() {
        if (presenter.isMeAdmin()) {
            fabAvatar.setVisibility(View.VISIBLE);
        } else {
            fabAvatar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.fabAvatar)
    public void onFabAvatarClick(View view) {
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
    public void onChatRemoved() {
        setResultAndFinish();
    }

    @OnClick(R.id.tvTitle)
    public void onTitleClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId()) || !presenter.isMeAdmin()) {
            return;
        }
        startActivityForResult(RenameChatActivity.getIntent(this, presenter.getChat()), REQUEST_RENAME_CHAT);
    }

    @OnClick(R.id.civAvatar)
    public void onGroupAvatarClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        String photo = presenter.getChat().getPhoto();
        if (photo != null) {
            startActivity(PictureViewActivity.navigateToFullscreenImage(this, photo));
        }
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
        presenter.changeChatAvatar(file);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionService.tryToConnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(ARG_MEDIA_PICKER, photoPicker.saveState());
        if (avatarFile != null) {
            outState.putParcelable(ARG_UPLOAD_FILE_URI, Uri.fromFile(avatarFile));
        }
        super.onSaveInstanceState(outState);
    }

}
