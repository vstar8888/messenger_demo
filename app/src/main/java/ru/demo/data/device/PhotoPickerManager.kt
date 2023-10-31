package ru.demo.data.device

import android.util.Log
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import java.io.File

object PhotoPickerManager {

    interface Callback {
        fun onImagesPicked(files: List<File>)
        fun onError(t: Throwable?)
        fun onCancel()
    }

    private val TAG = PhotoPickerManager::class.java.simpleName

    var callback: Callback? = null

    private var behaviourSubject: BehaviorSubject<List<File>> = BehaviorSubject.create()

    var subscription: Subscription? = null


    fun subscribe() {
        subscription = behaviourSubject
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<List<File>> {
                    override fun onError(e: Throwable) {
                        Log.e(TAG, e.message, e)
                        callback?.onError(e)
                    }

                    override fun onCompleted() {
                        Log.d(TAG, "onCompleted::")
                    }

                    override fun onNext(files: List<File>) {
                        Log.d(TAG, "onNext:: $files")
                        callback?.onImagesPicked(files)
                    }
                })
    }

    fun setImagePicked(files: List<File>) {
        behaviourSubject.onNext(files)
    }

    fun setError(exception: Exception) {
        Log.e(TAG, "setError:: exception = ${exception.message}")
        behaviourSubject.onError(exception)
        behaviourSubject = BehaviorSubject.create()
        subscribe()
    }

    fun cancel() {
        subscription?.unsubscribe()
        subscription = null
    }
}