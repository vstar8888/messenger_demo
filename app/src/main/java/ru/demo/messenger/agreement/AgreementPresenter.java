package ru.demo.messenger.agreement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import retrofit2.Response;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.LoginBySmsResponse;
import ru.demo.messenger.network.response.UsageRulesResponse;
import ru.demo.messenger.network.response.base.BaseResponse;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.AuthService;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static ru.demo.messenger.network.response.base.BaseLoginResponse.NextStepType.SIGNUP_USER;

class AgreementPresenter extends BasePresenter<AgreementPresenter.View> {

    interface View extends BaseLoadingView {
        void showAgreementText(String title, String content);
        void closeAgreement();
        void signUp(String phoneNumber, String signUpUrl);
    }

    private AuthService authService;
    private String acceptRegistrationUrl;
    @Nullable private String nextStepUrl;
    @Nullable private String phoneNumber;

    AgreementPresenter(@NonNull View view, @Nullable String nextStepUrl, @Nullable String phoneNumber) {
        super(view);
        this.authService = RequestManager.createService(AuthService.class);
        this.nextStepUrl = nextStepUrl;
        this.phoneNumber = phoneNumber;
    }

    void getAgreementText() {
        Single<Response<UsageRulesResponse>> getRules;
        if (nextStepUrl != null) {
            getRules = authService.getUsageRules(nextStepUrl);
        } else {
            getRules = authService.getUpdatedUsageRules();
        }
        subscriptions.add(
                getRules
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<UsageRulesResponse>() {
                            @Override
                            public void onNext(UsageRulesResponse result) {
                                acceptRegistrationUrl = result.acceptUrl;
                                getView().switchToMain(true);
                                getView().showAgreementText(result.agreementTitle, result.agreementText);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

    void acceptAgreement() {
        getView().switchToLoading(true);
        if (nextStepUrl != null && phoneNumber != null) {
            acceptRegistrationAgreement(phoneNumber);
        } else {
            acceptUpdatedAgreement();
        }
    }

    private void acceptUpdatedAgreement() {
        subscriptions.add(
                authService.acceptUsageRules()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                getView().closeAgreement();
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

    private void acceptRegistrationAgreement(String phoneNumber) {
        subscriptions.add(
                authService.acceptRegistrationUsageRules(acceptRegistrationUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<LoginBySmsResponse>() {
                            @Override
                            public void onNext(LoginBySmsResponse result) {
                                if (SIGNUP_USER.equals(result.getNextStep())) {
                                    getView().switchToMain(true);
                                    getView().signUp(phoneNumber, result.url);
                                }
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

}
