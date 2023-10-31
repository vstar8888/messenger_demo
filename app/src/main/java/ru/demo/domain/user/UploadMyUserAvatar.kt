package ru.demo.domain.user

import ru.demo.domain.base.CompletableUseCase
import rx.Completable

class UploadMyUserAvatar(
        private val usersDataSource: UserDataSource
) : CompletableUseCase<UploadMyUserAvatar.Params>() {

    override fun execute(params: UploadMyUserAvatar.Params): Completable {
        return usersDataSource.uploadMyUserAvatar(params.filePath)
    }

    class Params(val filePath: String)

}