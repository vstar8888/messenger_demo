package ru.demo.domain.auth

import ru.demo.messenger.data.user.UserModel
import rx.Single


interface AuthDataSource {

    fun isLogged(): Single<Boolean>

    fun saveUser(me: UserModel?)

    fun myUser() : Single<UserModel>

}