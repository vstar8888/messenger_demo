package ru.demo.messenger.internal.di

import android.content.Context
import ru.demo.domain.user.GetMyUser
import ru.demo.domain.user.UpdateMyUser
import ru.demo.domain.user.UploadMyUserAvatar

object ProfileModule {

    fun provideGetMyUser(context: Context): GetMyUser {
        val authDataSource = Injection.provideAuthDataSource(context)
        val usersDataSource = Injection.provideUserDataSource(context)
        return GetMyUser(authDataSource, usersDataSource)
    }

    fun provideUpdateMyUser(context: Context): UpdateMyUser {
        val usersDataSource = Injection.provideUserDataSource(context)
        val authDataSource = Injection.provideAuthDataSource(context)
        return UpdateMyUser(usersDataSource, authDataSource)
    }

    fun provideUpdateMyUserAvatar(context: Context): UploadMyUserAvatar {
        val usersDataSource = Injection.provideUserDataSource(context)
        return UploadMyUserAvatar(usersDataSource)
    }

}