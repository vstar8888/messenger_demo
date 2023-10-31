package ru.demo.domain.user

import ru.demo.domain.auth.AuthDataSource
import ru.demo.domain.base.SingleUseCase
import ru.demo.messenger.data.user.UserModel
import rx.Single

class GetMyUser(
        private val authDataSource: AuthDataSource,
        private val usersDataSource: UserDataSource
) : SingleUseCase<UserModel, Void?>() {
    override fun execute(params: Void?): Single<UserModel> {
        return authDataSource.myUser()
                .flatMap { usersDataSource.getUser(it.id) }
                .doOnSuccess { authDataSource.saveUser(it) }
    }
}