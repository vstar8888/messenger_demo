package ru.demo.data

import rx.Scheduler

interface RxSchedulers {
    val io: Scheduler
    val computation: Scheduler
}