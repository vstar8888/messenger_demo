package ru.demo.messenger;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import ru.demo.domain.notifications.SubscribeToPush;
import ru.demo.messenger.internal.di.Injection;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MessengerRegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    public MessengerRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SubscribeToPush subscribeToPush = Injection.provideSubscribeToPush(this);
        subscribeToPush.execute(null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d(TAG, "subscribe to push - success");
                }, throwable -> {
                    Log.d(TAG, "subscribe to push - failed", throwable);
                });
    }

}
