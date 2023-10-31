package ru.demo.data.user

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.demo.data.network.RestApi
import ru.demo.domain.user.UserDataSource
import ru.demo.messenger.BuildConfig
import ru.demo.messenger.R
import ru.demo.messenger.data.user.UserModel
import ru.demo.messenger.network.RequestManager
import rx.Completable
import rx.Single
import rx.schedulers.Schedulers
import java.io.File

class UserRepository(private val context: Context) : UserDataSource {

    companion object {
        private val MEDIA_TYPE_IMAGE = "image/jpeg".toMediaType()
        private const val MULTIPART_BODY_NAME = "file"
    }

    private val userService = RequestManager.createService(UserService::class.java)

    override fun getSmsInviteMessage(): Single<String> {
        return Single.fromCallable {
            context.getString(R.string.send_invite_sms)
        }
    }

    override fun getChatInviteMessage(): Single<String> {
        return Single.fromCallable {
            val appName = context.getString(R.string.app_name)
            val iosAppLink = context.getString(R.string.ios_app_link)
            context.getString(
                    R.string.send_invite_private_message,
                    appName,
                    iosAppLink,
                    BuildConfig.APPLICATION_ID
            )
        }
    }

    override fun markUserAsInvited(userId: Long): Completable {
        return RestApi.prepareRequest(userService.inviteUser(userId))
                .toCompletable()
    }

    override fun getUser(id: Long): Single<UserModel> {
        return RestApi.prepareRequest(userService.getUser(id))
                .map { it.user }
                .subscribeOn(Schedulers.io())
    }

    override fun updateMyUser(fullName: String,
                              lastName: String?,
                              subdivision: String,
                              position: String,
                              birthday: String?,
                              phoneMobile: String,
                              phoneWork: String,
                              skype: String,
                              googleTalk: String,
                              twitter: String,
                              vkUrl: String?,
                              facebookUrl: String?,
                              icq: String,
                              roomLocation: String,
                              about: String,
                              hobby: String,
                              education: String,
                              workExperience: String,
                              region: String
    ): Single<UserModel> {
        val updateMyUser = userService.updateMyUser(fullName,
                lastName,
                subdivision,
                position,
                birthday,
                phoneMobile,
                phoneWork,
                skype,
                googleTalk,
                twitter,
                vkUrl,
                facebookUrl,
                icq,
                roomLocation,
                about,
                hobby,
                education,
                workExperience,
                region)
        return RestApi.prepareRequest(updateMyUser)
                .map { it.user }
                .subscribeOn(Schedulers.io())
    }

    override fun uploadMyUserAvatar(filePath: String): Completable {
        val createMultiPartBody = Single.fromCallable {
            File(filePath).let {
                val requestBody = RequestBody.create(MEDIA_TYPE_IMAGE, it)
                MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(MULTIPART_BODY_NAME, it.name, requestBody)
                        .build()
            }
        }
        return RestApi.prepareRequest(
                createMultiPartBody.flatMap { userService.updateMyUserAvatar(it) }
        )//-----|
                .toCompletable()
                .subscribeOn(Schedulers.io())

    }

}