package ru.demo.messenger.clearer

import android.app.ActivityManager
import android.content.Context
import android.os.Build

class DataClearPresenter(private val view: DataClearPresenter.View) {

    interface View {
        fun showPopup()
    }

    fun start() {
        view.showPopup()
    }

    fun clear(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .clearApplicationUserData()
        } else {
            //go to login activity in MainApp
        }
    }

}