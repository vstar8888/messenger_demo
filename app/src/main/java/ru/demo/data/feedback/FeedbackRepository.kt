package ru.demo.data.feedback

import android.content.Context
import android.os.Build
import ru.demo.data.RxSchedulers
import ru.demo.data.network.RestApi
import ru.demo.domain.feedback.FeedbackDataSource
import ru.demo.messenger.BuildConfig
import ru.demo.messenger.R
import ru.demo.messenger.network.RequestManager
import rx.Completable

class FeedbackRepository(
        private val context: Context,
        private val rxSchedulers: RxSchedulers
) : FeedbackDataSource {

    private val feedbackService = RequestManager.createService(FeedbackService::class.java)

    override fun sendFeedback(
            appName: String,
            companyTitle: String?,
            email: String?,
            phoneNumber: String?,
            text: String
    ): Completable {
        val technicalInfo = "\n\n" +
                "${context.getString(R.string.app_name)} для Android" +
                "\n" +
                "Версия приложения: ${BuildConfig.VERSION_NAME}" +
                "\n" +
                "Код версии: ${BuildConfig.VERSION_CODE}" +
                "\n" +
                "Устройство: ${Build.MANUFACTURER} ${Build.MODEL}" +
                "\n" +
                "Версия Android: ${Build.VERSION.RELEASE}"
        return RestApi.prepareRequest(feedbackService.sendFeedback(
                appName,
                companyTitle,
                email,
                phoneNumber,
                text + technicalInfo
        ))//-----|
                .toCompletable()
                .subscribeOn(rxSchedulers.io)
    }

}