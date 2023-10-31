package ru.demo.messenger.network;

import android.util.Log;

import retrofit2.Response;
import ru.demo.messenger.MainApp;
import ru.demo.messenger.R;
import ru.demo.messenger.network.response.base.BaseResponse;
import ru.demo.messenger.network.response.base.ServerError;
import rx.Subscriber;

public abstract class BaseSubscriber<T extends BaseResponse> extends Subscriber<Response<T>> {

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        onError(createUnknownError());
    }

    private ServerError createUnknownError() {
        return new ServerError(NetworkConst.ErrorCodes.UNKNOWN_ERROR, MainApp.getInstance().getString(R.string.no_connection));
    }

    @Override
    public void onNext(Response<T> response) {
        final T body = response.body();
        if (response.isSuccessful() && body != null) {
            if (body.isSuccess()) {
                onNext(body);
            } else {
                onError(body.getError());
            }
        } else {
            onError(createUnknownError());
        }
    }

    public abstract void onNext(T result);

    public abstract void onError(ServerError error);

}
