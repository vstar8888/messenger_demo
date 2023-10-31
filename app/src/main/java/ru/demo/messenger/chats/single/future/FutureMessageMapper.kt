package ru.demo.messenger.chats.single.future

import android.os.SystemClock
import org.threeten.bp.ZonedDateTime
import ru.demo.messenger.chats.single.future.message.*
import ru.demo.messenger.data.message.AttachedFiles
import ru.demo.messenger.data.message.Attachment
import ru.demo.messenger.data.message.MessageModel

class FutureMessageMapper(private val selfUserId: Long) {

    fun messagesOf(futureMessages: List<FutureMessage>?): List<MessageModel> {
        return futureMessages?.mapNotNull {
            when (it) {
                is ReplyFutureMessage -> messageOf(it)
                is ForwardFutureMessage -> messageOf(it)
                is TextFutureMessage -> messageOf(it)
                is StickerFutureMessage -> messageOf(it)
                else -> null
            }
        } ?: emptyList()
    }

    fun messageOf(futureMessage: TextFutureMessage): MessageModel = with(futureMessage) {
        return MessageModel(
                -SystemClock.elapsedRealtime(),
                chatId,
                ZonedDateTime.now().toString(),
                MessageModel.Direction.OUTGOING,
                MessageModel.ReadStatus.WAIT,
                selfUserId,
                text,
                getAttachedFiles(),
                "authorFullName",
                "authorAvatarUrl",
                "authorAlias",
                payload,
                MessageModel.MessageType.MESSAGE,
                null,
                null
        )
    }

    fun messageOf(futureMessage: ReplyFutureMessage): MessageModel = with(futureMessage) {
        return MessageModel(
                -SystemClock.elapsedRealtime(),
                chatId,
                ZonedDateTime.now().toString(),
                MessageModel.Direction.OUTGOING,
                MessageModel.ReadStatus.WAIT,
                selfUserId,
                text,
                getAttachedFiles(),
                "authorFullName",
                "authorAvatarUrl",
                "authorAlias",
                payload,
                MessageModel.MessageType.QUOTE,
                replyMessage,
                null
        )
    }

    private fun TextFutureMessage.getAttachedFiles(): AttachedFiles {
        return filePaths.map { Attachment(it) }
                .fold(AttachedFiles(arrayListOf()), { attachedFiles, attachment ->
                    attachedFiles.apply { attachments.add(attachment) }
                })
    }

    fun messageOf(futureMessage: ForwardFutureMessage): MessageModel = with(futureMessage) {
        val forwardedMessage = MessageModel(
                -SystemClock.elapsedRealtime(),
                chatId,
                ZonedDateTime.now().toString(),
                MessageModel.Direction.OUTGOING,
                MessageModel.ReadStatus.WAIT,
                authorId,
                text,
                attachedFiles,
                authorFullName,
                "authorAvatarUrl",
                "authorAlias",
                null,
                MessageModel.MessageType.MESSAGE,
                null,
                null
        )
        return MessageModel(
                -SystemClock.elapsedRealtime(),
                chatId,
                ZonedDateTime.now().toString(),
                MessageModel.Direction.OUTGOING,
                MessageModel.ReadStatus.WAIT,
                selfUserId,
                "",
                AttachedFiles(emptyList()),
                "authorFullName",
                "authorAvatarUrl",
                "authorAlias",
                payload,
                MessageModel.MessageType.FORWARD,
                forwardedMessage,
                null
        )
    }

    fun messageOf(futureMessage: StickerFutureMessage): MessageModel = with(futureMessage) {
        return MessageModel(
                -SystemClock.elapsedRealtime(),
                chatId,
                ZonedDateTime.now().toString(),
                MessageModel.Direction.OUTGOING,
                MessageModel.ReadStatus.WAIT,
                selfUserId,
                "",
                AttachedFiles(emptyList()),
                "authorFullName",
                "authorAvatarUrl",
                "authorAlias",
                payload,
                MessageModel.MessageType.STICKER,
                null,
                sticker
        )
    }

}