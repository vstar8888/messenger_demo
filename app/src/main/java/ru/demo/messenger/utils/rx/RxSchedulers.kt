package ru.demo.messenger.utils.rx

import ru.demo.data.RxSchedulers
import rx.Scheduler
import rx.schedulers.Schedulers

class RxSchedulersProvider : RxSchedulers {

    override val io: Scheduler
        get() = Schedulers.io()

    override val computation: Scheduler
        get() = Schedulers.computation()
}