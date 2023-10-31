package ru.demo.data.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import ru.demo.data.network.RestApi
import ru.demo.domain.notifications.NotificationService
import ru.demo.domain.notifications.NotificationsDataSource
import ru.demo.messenger.network.RequestManager
import ru.demo.messenger.network.request.NotificationToken
import ru.demo.messenger.utils.ExecutorsProvider
import rx.Completable
import rx.Single

class NotificationsRepository(context: Context) : NotificationsDataSource {

    companion object {
        const val SUBSCRIBED_TO_NOTIFICATIONS = "subscribed_to_notifications"
    }

    private val notificationService = RequestManager.createService(NotificationService::class.java)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun subscribeToPush(): Completable {
        return getFirebaseToken()
                .flatMapCompletable { token ->
                    RestApi.prepareRequest(notificationService.registerDevice(NotificationToken(token)))
                            .toCompletable()
                            .onErrorResumeNext {
                                setSubscriptionState(false)
                                        .andThen(Completable.error(it))
                            }
                            .andThen(setSubscriptionState(true))
                }
    }

    private fun getFirebaseToken(): Single<String> {
        return Single.create {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(
                    ExecutorsProvider.provideFirebaseTokenExecutor(),
                    OnCompleteListener { instanceIdResult ->
                        if (instanceIdResult.isSuccessful) {
                            val token = instanceIdResult.result?.token ?: ""
                            it.onSuccess(token)
                        } else {
                            it.onError(instanceIdResult.exception)
                        }
                    })
        }
    }

    override fun unsubscribeFromPush(): Completable {
        return getFirebaseToken()
                .flatMapCompletable { token ->
                    RestApi.prepareRequest(notificationService.unregisterDevice(NotificationToken(token)))
                            .toCompletable()
                            .onErrorResumeNext {
                                Completable.fromAction {
                                    FirebaseInstanceId.getInstance().deleteInstanceId()
                                }
                            }
                }
                .andThen(setSubscriptionState(false))
    }

    override fun isSubscribedToPush(): Single<Boolean> {
        return Single.fromCallable {
            prefs.getBoolean(SUBSCRIBED_TO_NOTIFICATIONS, false)
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun setSubscriptionState(subscribed: Boolean): Completable {
        return Completable.fromAction {
            prefs.edit()
                    .putBoolean(SUBSCRIBED_TO_NOTIFICATIONS, subscribed)
                    .commit()
        }
    }

}