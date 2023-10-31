package ru.demo.domain.user

import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.PermissionChecker
import ru.demo.data.notifications.NotificationsRepository
import ru.demo.domain.base.CompletableUseCase
import ru.demo.domain.notifications.NotificationsDataSource
import ru.demo.messenger.Consts
import ru.demo.messenger.chats.single.future.FutureMessageStorage
import ru.demo.messenger.network.ConnectionService
import ru.demo.messenger.utils.FileUtils
import ru.demo.messenger.utils.Prefs
import rx.Completable
import java.io.File

class Logout(
        private val notificationsDataSource: NotificationsDataSource
) : CompletableUseCase<Logout.Params>() {

    override fun execute(params: Logout.Params): Completable {
        return notificationsDataSource.unsubscribeFromPush()
                .andThen(Completable.fromCallable {
                    clearPrefs()
                    FutureMessageStorage.getInstance().dropAllData()
                    ConnectionService.getInstance().disconnect()
                    cleanDirectory(params.writeStoragePermissionCode)
                })
    }

    class Params(
            @PermissionChecker.PermissionResult
            val writeStoragePermissionCode: Int
    )

    private fun cleanDirectory(@PermissionChecker.PermissionResult granted: Int) {
        if (granted == PackageManager.PERMISSION_GRANTED) {
            val appDirectory = Environment.getExternalStorageDirectory()
            // TODO: DI 29.12.16 fix all names like that to appName
            val dataDirectory = File(appDirectory, "demoMessenger")
            FileUtils.cleanDirectory(dataDirectory)
        }
    }

    private fun clearPrefs() {
        Prefs.edit()
                .putString(Consts.Prefs.BASE_URL, null)
                .putString(Consts.Prefs.LIMITED_TOKEN, null)
                .putString(Consts.Prefs.AUTH_TOKEN, null)
                .putString(Consts.Prefs.USER_ID, null)
                .putString(NotificationsRepository.SUBSCRIBED_TO_NOTIFICATIONS, null)
                .putString(Consts.Prefs.USER_DATA, null)
                .putString(Consts.Prefs.IS_USER_LOGGED, null)
                .apply()
    }

}