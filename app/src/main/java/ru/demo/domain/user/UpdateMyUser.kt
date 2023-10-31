package ru.demo.domain.user

import org.threeten.bp.LocalDate
import ru.demo.domain.auth.AuthDataSource
import ru.demo.domain.base.SingleUseCase
import ru.demo.messenger.data.user.UserModel
import rx.Single

class UpdateMyUser(
        private val usersDataSource: UserDataSource,
        private val authDataSource: AuthDataSource
) : SingleUseCase<UserModel, UpdateMyUser.Params>() {

    override fun execute(params: Params): Single<UserModel> {
        val indexOfSpace = params.fullName.indexOf(' ')
        val firstName = params.fullName.substring(0, indexOfSpace)
        val lastName = params.fullName.substring(indexOfSpace + 1, params.fullName.length)
        val birthday = params.birthday?.toString()
        return usersDataSource.updateMyUser(firstName,
                lastName,
                params.subdivision,
                params.position,
                birthday,
                params.phoneMobile,
                params.phoneWork,
                params.skype,
                params.googleTalk,
                params.twitter,
                params.vkUrl,
                params.facebookUrl,
                params.icq,
                params.roomLocation,
                params.about,
                params.hobby,
                params.education,
                params.workExperience,
                params.region
        )//-----|
                .doOnSuccess { authDataSource.saveUser(it) }
    }

    class Params(val fullName: String,
                 val subdivision: String,
                 val position: String,
                 val birthday: LocalDate?,
                 val phoneMobile: String,
                 val phoneWork: String,
                 val skype: String,
                 val googleTalk: String,
                 val twitter: String,
                 val vkUrl: String?,
                 val facebookUrl: String?,
                 val icq: String,
                 val roomLocation: String,
                 val about: String,
                 val hobby: String,
                 val education: String,
                 val workExperience: String,
                 val region: String
    )

}