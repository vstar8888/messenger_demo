package ru.demo.messenger.chats.group.select;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import retrofit2.Response;
import ru.demo.messenger.Consts;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.UserList;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.UserService;
import ru.demo.messenger.utils.Prefs;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class SelectGroupPeoplePresenter extends BasePresenter<SelectGroupPeoplePresenter.View> {

    private Subscription changeAdapterSubscription;

    private boolean isReadOnly;

    public interface View extends BaseLoadingView {
        void onUsersLoaded(List<UserModel> users);
    }

    private final UserService userService;
    private boolean isNoMoreData = false;
    private String loadMoreUrl;
    @Nullable private final ArrayList<UserModel> usersToExclude;

    private final long selfUserId;

    SelectGroupPeoplePresenter(@NonNull View view, @Nullable ArrayList<UserModel> usersToExclude) {
        super(view);
        userService = RequestManager.createService(UserService.class);
        selfUserId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0L);
        this.usersToExclude = usersToExclude;
    }

    private void refresh() {
        resetData();
        getUsers();
    }

    void searchUsers(String query) {
        if (TextUtils.isEmpty(query)) {
            refresh();
        } else {
            resetData();
            subscribe(userService.search(query));
        }
    }

    // TODO: DI 12.09.16 load from db!11
//        loadFromDb();
    public void getUsers(long[] userIds) {
        isReadOnly = userIds != null;
        if (isReadOnly) {
            Observable.range(0, userIds.length)
                    .map(i -> userIds[i])
                    .buffer(50)
                    .subscribeOn(Schedulers.io())
                    .subscribe(ids -> subscribe(userService.getUsers(ids)));
        } else {
            getUsers();
        }
    }

    public void getUsers() {
        if (isReadOnly || isNoMoreData) {
            getView().onUsersLoaded(Collections.emptyList());
            return;
        }
        if (loadMoreUrl == null) {
            subscribe(userService.getUsers(1));
        } else {
            subscribe(userService.getUsers(loadMoreUrl));
        }
    }

    private void resetData() {
        isNoMoreData = false;
        loadMoreUrl = null;
    }

    private void subscribe(Observable<Response<UserList>> users) {
        users.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<UserList>() {
                    @Override
                    public void onNext(UserList result) {
                        loadMoreUrl = result.load_more_url;
                        if (loadMoreUrl == null) {
                            isNoMoreData = true;
                        }
                        getView().onUsersLoaded(excludeMe(excludeUsers(result.users)));
                    }

                    @Override
                    public void onError(ServerError error) {
                        getView().switchToError(true, error.getMessage());
                    }
                });
    }

    private List<UserModel> excludeMe(@NonNull List<UserModel> users) {
        int myPosition = -1;
        for (int index = 0; index < users.size(); index++) {
            final UserModel user = users.get(index);
            if (user.getId() == selfUserId) {
                myPosition = index;
                break;
            }
        }
        if (myPosition != -1) {
            users.remove(myPosition);
        }
        return users;
    }

    private List<UserModel> excludeUsers(@NonNull List<UserModel> users) {
        if (usersToExclude == null || usersToExclude.isEmpty()) {
            return users;
        }
        nextUser: for (int i = users.size() - 1; i >= 0; i--) {
            final UserModel user = users.get(i);

            for (int y = 0; y < usersToExclude.size(); y++) {
                if (user.getId() == usersToExclude.get(y).getId()) {
                    users.remove(i);
                    continue nextUser;
                }
            }
        }
        return users;
    }

    void onPause() {
        if (changeAdapterSubscription != null) {
            changeAdapterSubscription.unsubscribe();
        }
    }

    void onResume(Subscription subscription) {
        changeAdapterSubscription = subscription;
    }

}
