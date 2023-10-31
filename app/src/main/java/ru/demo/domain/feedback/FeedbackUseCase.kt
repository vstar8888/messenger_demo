package ru.demo.domain.feedback

import ru.demo.domain.base.CompletableUseCase
import rx.Completable

class SendFeedbackUseCase(
        private val feedbackDataSource: FeedbackDataSource
) : CompletableUseCase<SendFeedbackUseCase.Params>() {

    override fun execute(params: Params): Completable {
        return feedbackDataSource.sendFeedback(params.appName, params.companyTitle, params.email, params.phoneNumber,
                params.text)
    }

    class Params(
            val appName: String,
            val companyTitle: String?,
            val email: String?,
            val phoneNumber: String?,
            val text: String
    )

}
