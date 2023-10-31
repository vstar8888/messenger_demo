package ru.demo.messenger.chats.group.select;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import biz.growapp.base.BaseSearchListener;
import biz.growapp.base.loading.BaseAppLoadingActivity;
import biz.growapp.base.pagination.PaginationAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import ru.demo.messenger.MainApp;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.group.create.CreateGroupActivity;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.helpers.ItemDividerDecorator;
import ru.demo.messenger.helpers.ViewStateSwitcher;
import ru.demo.messenger.internal.di.Injection;
import ru.demo.messenger.people.OnlineUsersHolder;
import ru.demo.messenger.people.PeopleAdapter;
import ru.demo.messenger.people.list.SelectPeopleDelegate;
import ru.demo.messenger.utils.DimensionUtils;
import ru.demo.messenger.utils.DisplayUtils;
import rx.android.schedulers.AndroidSchedulers;

public class SelectGroupPeopleActivity extends BaseAppLoadingActivity
        implements SelectGroupPeoplePresenter.View, PaginationAdapter.Loader,
        SelectPeopleDelegate.Callback, BaseSearchListener.SearchEventListener,
        SelectedPeopleAdapter.ItemClickListener {

    private static final int REQUEST_CREATE_GROUP = 100;
    private static final int EMPTY_CHAT_USERS_QUANTITY = 0;

    private static final String EXTRA_IN_USERS_TO_EXCLUDE = "extra.IN_USERS_TO_EXCLUDE";
    private static final String EXTRA_IN_GROUP_NAME = "extra.IN_GROUP_NAME";
    private static final String EXTRA_OUT_USERS = "extra.OUT_USERS";

    @BindView(R.id.rvPeople) RecyclerView rvPeople;
    @BindView(R.id.rvSelectedUsers) RecyclerView rvSelectedUsers;

    @BindView(R.id.fabNext) FloatingActionButton fabNext;

    private PeopleAdapter adapter;

    private SelectedPeopleAdapter selectedPeopleAdapter;

    private SelectGroupPeoplePresenter presenter;
    private boolean isForChatSettings;

    public static Intent getIntent(@NonNull Context context) {
        return new Intent(context, SelectGroupPeopleActivity.class);
    }

    public static Intent forChatSettings(@NonNull Context context, List<UserModel> users, String groupName) {
        ArrayList<UserModel> usersList = new ArrayList<>();
        usersList.addAll(users);
        return new Intent(context, SelectGroupPeopleActivity.class)
                .putParcelableArrayListExtra(EXTRA_IN_USERS_TO_EXCLUDE, usersList)
                .putExtra(EXTRA_IN_GROUP_NAME, groupName);
    }

    @Nullable
    public static ArrayList<UserModel> unpackSelectedUsers(@NonNull Intent data) {
        return data.getParcelableArrayListExtra(EXTRA_OUT_USERS);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_people);
        setupRecyclerView();
        setupFab();
        final ArrayList<UserModel> usersToExclude = getIntent().getParcelableArrayListExtra(EXTRA_IN_USERS_TO_EXCLUDE);
        isForChatSettings = usersToExclude != null;
        if (isForChatSettings) {
            setTitle(getString(R.string.select_people_title_for_chat_settings));
        }
        presenter = new SelectGroupPeoplePresenter(this, usersToExclude);

        addTextState(ViewStateSwitcher.State.STATE_EMPTY,
                getString(R.string.select_group_people_empty_list));

        presenter.getUsers();
    }

    private void setupRecyclerView() {
        rvPeople.setLayoutManager(new LinearLayoutManager(this));
        rvPeople.addItemDecoration(createItemDivider());
        adapter = new PeopleAdapter(this);
        adapter.getManager()
                .addDelegate(new SelectPeopleDelegate(this, this,
                        Injection.provideColorGenerator(this)
                ));
        rvPeople.setAdapter(adapter);

        final LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSelectedUsers.setLayoutManager(layoutManager);
        selectedPeopleAdapter = new SelectedPeopleAdapter(this, this,
                Injection.provideColorGenerator(this)
        );
        rvSelectedUsers.setAdapter(selectedPeopleAdapter);
    }

    @NonNull
    private ItemDividerDecorator createItemDivider() {
        final int lineHeight = DimensionUtils.dp(1);
        final int leftMargin = DimensionUtils.dp(66);
        final int rightMargin = DimensionUtils.dp(16);
        final int dividerColor = ContextCompat.getColor(this, R.color.gray_divider);
        final Rect marginRectangle = new Rect(leftMargin, 0, rightMargin, 0);
        return new ItemDividerDecorator(dividerColor, lineHeight, marginRectangle, 1);
    }

    private void setupFab() {
        fabNext.hide();
    }

    @Override
    public void onLoadMore(int offset) {
        presenter.getUsers();
    }

    @Override
    public void onUsersLoaded(List<UserModel> users) {
        adapter.addAll(users);
        if (adapter.isEmpty()) {
            switchToEmpty(true);
        } else {
            switchToMain(true);
        }
    }

    @Override
    public void onItemClick(UserModel user, int position) {
        if (selectedPeopleAdapter.isExist(user)) {
            return;
        }
        selectedPeopleAdapter.addFirstWithAnim(user);
        rvSelectedUsers.scrollToPosition(0);

        if (selectedPeopleAdapter.getItemCount() > EMPTY_CHAT_USERS_QUANTITY) {
            fabNext.show();
        }
    }

    @Override
    public void onPhotoClick(UserModel user, int position) {
        onItemClick(user, position);
    }

    @Override
    public void onRemoveClick(int viewId, int position) {
        if (selectedPeopleAdapter.removeWithAnim(position)) {
            if (selectedPeopleAdapter.isEmpty() ||
                    selectedPeopleAdapter.getItemCount() == EMPTY_CHAT_USERS_QUANTITY) {
                fabNext.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        if (selectedPeopleAdapter.getItems().size() > EMPTY_CHAT_USERS_QUANTITY) {
                            fabNext.show();
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_people, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.searchtext));
        searchView.setOnQueryTextListener(new BaseSearchListener(this));
        return true;
    }

    @Override
    public void doSearch(String query, boolean isSubmit) {
        if (isSubmit) {
            DisplayUtils.hideSoftKeyboard(this);
        }
        adapter.clear();
        switchToLoading(true);
        presenter.searchUsers(query);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume(MainApp.globalBus
                .observeEvents(OnlineUsersHolder.UserStatusUpdate.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.id == OnlineUsersHolder.UserStatusUpdate.ALL_USERS) {
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.userOnlineStatusChanged(event.id);
                    }
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    private void showAddUsersToGroupDialog() {
        ArrayList<UserModel> users = new ArrayList<>();
        users.addAll(selectedPeopleAdapter.getItems());
        StringBuilder userNames = new StringBuilder(users.get(0).getFullName());
        for (int i = 1; i < users.size(); i++) {
            userNames.append(", ");
            userNames.append(users.get(i).getFullName());
        }
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.select_people_add_participant_question,
                        userNames,
                        getIntent().getStringExtra(EXTRA_IN_GROUP_NAME).trim())
                )
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    final Intent data = new Intent()
                            .putParcelableArrayListExtra(EXTRA_OUT_USERS, users);
                    setResult(RESULT_OK, data);
                    finish();
                })
                .show();
    }

    @OnClick(R.id.fabNext)
    protected void onNextClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        if (isForChatSettings) {
            showAddUsersToGroupDialog();
        } else {
            final Intent intent = CreateGroupActivity.getIntent(this, selectedPeopleAdapter.getItems());
            startActivityForResult(intent, REQUEST_CREATE_GROUP);
        }
    }

    @Override
    public int getMainContainerId() {
        return R.id.rvPeople;
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToLoading(true);
        presenter.getUsers();
    }

    @Override
    public void switchToError(boolean animate) {
        selectedPeopleAdapter.clear();
        super.switchToError(animate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CREATE_GROUP == requestCode) {
            if (RESULT_OK == resultCode) {
                setResult(RESULT_OK);
                finish();
            } else if (RESULT_CANCELED == resultCode) {
                if (data == null) {
                    return;
                }
                final ArrayList<UserModel> selectedUsers =
                        data.getParcelableArrayListExtra(CreateGroupActivity.EXTRA_GROUP_USERS);
                selectedPeopleAdapter.replaceAll(selectedUsers);
                if (selectedUsers.size() <= EMPTY_CHAT_USERS_QUANTITY) {
                    fabNext.hide();
                }
            }
        }
    }
}
