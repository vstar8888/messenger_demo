package ru.demo.domain.notifications

import ru.demo.domain.base.CompletableUseCase
import rx.Completable

class SubscribeToPush(
        private val notificationsDataSource: NotificationsDataSource
) : CompletableUseCase<Void?>() {

    override fun execute(params: Void?): Completable {
        return notificationsDataSource.isSubscribedToPush()
                .flatMapCompletable { isSubscribed ->
                    if (isSubscribed) {
                        Completable.complete()
                    } else {
                        notificationsDataSource.subscribeToPush()
                    }
                }

    }

}