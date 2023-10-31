package ru.demo.data.auth

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import ru.demo.data.RxSchedulers
import ru.demo.domain.auth.AuthDataSource
import ru.demo.messenger.Consts
import ru.demo.messenger.data.user.UserModel
import rx.Single

class AuthRepository(
        context: Context,
        private val gson: Gson,
        private val rxSchedulers: RxSchedulers
) : AuthDataSource {

    private companion object {
        // TODO: NR 18.12.2018  remove Consts class
        const val USER = Consts.Prefs.USER_DATA
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    override fun isLogged(): Single<Boolean> {
        return myUser().map { it != null }
    }

    override fun saveUser(me: UserModel?) {
        prefs.edit().putString(USER, gson.toJson(me)).apply()
    }

    override fun myUser(): Single<UserModel> {
        return Single.fromCallable {
            gson.fromJson(prefs.getString(USER, null), UserModel::class.java)
        }.subscribeOn(rxSchedulers.computation)
    }

}