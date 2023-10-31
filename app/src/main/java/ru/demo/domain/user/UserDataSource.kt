package ru.demo.domain.user

import ru.demo.messenger.data.user.UserModel
import rx.Completable
import rx.Single

interface UserDataSource {

    fun getSmsInviteMessage(): Single<String>

    fun getChatInviteMessage(): Single<String>

    fun markUserAsInvited(userId: Long): Completable

    fun getUser(id: Long): Single<UserModel>

    fun updateMyUser(fullName: String,
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
    ): Single<UserModel>

    fun uploadMyUserAvatar(filePath: String) : Completable

}