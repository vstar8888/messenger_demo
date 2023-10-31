package ru.demo.messenger.feedback

import biz.growapp.base.extensions.plusAssign
import biz.growapp.base.loading.BasePresenter
import biz.growapp.base.loading.BaseView
import ru.demo.domain.feedback.SendFeedbackUseCase
import ru.demo.messenger.Consts
import ru.demo.messenger.utils.Prefs
import rx.Scheduler

class FeedbackPresenter(
        private val view: View,
        private val feedback: SendFeedbackUseCase,
        private val uiScheduler: Scheduler
) : BasePresenter<FeedbackPresenter.View>(view) {

    interface View : BaseView {
        fun feedbackSent()
        fun onError(throwable: Throwable)
        fun onLoginDataLoaded(phone: String?)
    }

    fun loadLoginData() {
        val phone = Prefs.get().getString(Consts.Prefs.AUTH_PHONE, null)
        view.onLoginDataLoaded(phone)
    }

    fun sendFeedback(appName: String, companyTitle: String?, email: String?, phoneNumber: String?, text: String) {
        subscriptions += feedback.execute(SendFeedbackUseCase.Params(
                appName = appName,
                companyTitle = companyTitle,
                email = email,
                phoneNumber = phoneNumber,
                text = text
        ))//----|
                .observeOn(uiScheduler)
                .subscribe({
                    view.feedbackSent()
                }, {
                    view.onError(it)
                })
    }

}