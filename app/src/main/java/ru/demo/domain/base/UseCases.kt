package ru.demo.domain.base

import rx.Completable
import rx.Observable
import rx.Single


interface UseCase<out OutputT, in InputT> {
    fun execute(params: InputT): OutputT
}

abstract class SingleUseCase<T, in InputT> : UseCase<Single<T>, InputT> {
    abstract override fun execute(params: InputT): Single<T>
}

abstract class CompletableUseCase<in T> : UseCase<Completable, T> {
    abstract override fun execute(params: T): Completable
}

abstract class PagedSingleUseCase<T, in P : PagedSingleUseCase.Params> : SingleUseCase<DataPage<T>, P>() {
    open class Params(val page: Int = 1)
}

abstract class ObservableUseCase<T, in InputT> : UseCase<Observable<T>, InputT> {
    abstract override fun execute(params: InputT): Observable<T>
}