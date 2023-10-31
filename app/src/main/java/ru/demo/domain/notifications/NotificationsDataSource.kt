package ru.demo.domain.notifications

import rx.Completable
import rx.Single

interface NotificationsDataSource {

    fun subscribeToPush(): Completable
    fun unsubscribeFromPush(): Completable
    fun isSubscribedToPush(): Single<Boolean>

}