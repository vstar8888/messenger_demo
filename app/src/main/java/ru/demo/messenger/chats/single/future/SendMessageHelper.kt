package ru.demo.messenger.chats.single.future

import android.text.TextUtils
import ru.demo.domain.files.UploadFiles
import ru.demo.messenger.chats.single.future.message.InvalidFutureMessage
import ru.demo.messenger.chats.single.future.message.TextFutureMessage
import rx.Completable
import rx.Single
import java.io.File

class SendMessageHelper(private val uploadFiles: UploadFiles) {

    fun validateMessage(
            futureMessage: TextFutureMessage,
            fileIds: List<String> = emptyList()
    ): Completable = with(futureMessage) {
        return Completable.fromAction {
            if (TextUtils.isEmpty(text) && fileIds.isEmpty()) {
                throw InvalidFutureMessage()
            }
        }
    }

    fun uploadFiles(message: TextFutureMessage): Single<List<String>> {
        return uploadFiles.execute(UploadFiles.Params(message.filePaths))
                .map { it.map { fileInfo -> fileInfo.id } }
                .onErrorResumeNext {
                    removeIncorrectPaths(message)
                    Single.error(it)
                }
    }

    private fun removeIncorrectPaths(message: TextFutureMessage) {
        val paths = message.filePaths as ArrayList
        paths.forEach {
            val file = File(it)
            if (file.exists().not() || file.length() == 0L) {
                paths.remove(it)
            }
        }
    }

}