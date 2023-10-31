package ru.demo.domain.feedback

import rx.Completable

interface FeedbackDataSource {

    fun sendFeedback(appName: String, companyTitle: String?, email: String?, phoneNumber: String?,
                     text: String): Completable
}