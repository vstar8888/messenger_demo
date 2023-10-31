package ru.demo.messenger.splash;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import biz.growapp.base.loading.BasePresenter;
import biz.growapp.base.loading.BaseView;
import ru.demo.messenger.Consts;
import ru.demo.messenger.login.AuthStorage;
import ru.demo.messenger.utils.Prefs;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static ru.demo.messenger.login.AuthStorage.INDEX_AUTH_TOKEN;
import static ru.demo.messenger.login.AuthStorage.INDEX_BASE_URL;
import static ru.demo.messenger.login.AuthStorage.INDEX_USER_ID;

class SplashScreenPresenter extends BasePresenter<SplashScreenPresenter.SplashScreenView> {
    private static final long SPLASH_DELAY = 1;

    interface SplashScreenView extends BaseView {
        void onOtherAppLoginAvailable(String baseUrl, String token, long userId);

        void showSelectLogin();

        void alreadyLoggedIn();
    }

    private final AuthStorage authStorage;

    SplashScreenPresenter(@NonNull SplashScreenView view, AuthStorage authStorage) {
        super(view);
        this.authStorage = authStorage;
    }

    void checkLoginAfterDelay() {
        subscriptions.add(Observable.timer(SPLASH_DELAY, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(delay -> {
                    if (userLoggedIn()) {
                        getView().alreadyLoggedIn();
                    } else {
                        getView().showSelectLogin();
//                        tryToLoginViaOtherApps();
                    }
                })
        );
    }

    private boolean userLoggedIn() {
        final SharedPreferences prefs = Prefs.get();
        final String token = prefs.getString(Consts.Prefs.AUTH_TOKEN, null);
        final boolean isLoggedIn = prefs.getBoolean(Consts.Prefs.IS_USER_LOGGED, false);
        return !TextUtils.isEmpty(token) && isLoggedIn;
    }

    private void tryToLoginViaOtherApps() {
        final String[] creds = authStorage.searchCredentialsInAnotherApp();
        if (creds != null) {
            final String baseUrl = creds[INDEX_BASE_URL];
            final String authToken = creds[INDEX_AUTH_TOKEN];
            final Long userId = Long.parseLong(creds[INDEX_USER_ID]);
            getView().onOtherAppLoginAvailable(baseUrl, authToken, userId);
        } else {
            getView().showSelectLogin();
        }
    }

}
