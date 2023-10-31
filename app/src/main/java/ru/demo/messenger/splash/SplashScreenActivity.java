package ru.demo.messenger.splash;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import biz.growapp.base.BaseAppActivity;
import ru.demo.messenger.R;
import ru.demo.messenger.login.AuthStorage;
import ru.demo.messenger.login.email.EmailLoginActivity;
import ru.demo.messenger.login.otherapp.OtherAppLoginActivity;
import ru.demo.messenger.main.MainActivity;

public class SplashScreenActivity extends BaseAppActivity
        implements SplashScreenPresenter.SplashScreenView {

    public static Intent getClearTaskIntent(Context context) {
        final Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private SplashScreenPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        presenter = new SplashScreenPresenter(this, new AuthStorage(this));
        presenter.checkLoginAfterDelay();
    }

    @Override
    public void alreadyLoggedIn() {
        startActivity(MainActivity.getIntent(this));
        finish();
    }

    @Override
    public void onOtherAppLoginAvailable(String baseUrl, String token, long userId) {
        startActivity(OtherAppLoginActivity.getIntent(this, baseUrl, token, userId));
        finish();
    }

    @Override
    public void showSelectLogin() {
        startActivity(EmailLoginActivity.getIntent(this));
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroyView();
        super.onDestroy();
    }

}
