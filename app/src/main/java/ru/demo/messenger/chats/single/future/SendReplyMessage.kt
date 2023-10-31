package ru.demo.messenger.chats.single.future

import ru.demo.domain.message.MessageDataSource
import ru.demo.messenger.chats.single.future.message.ReplyFutureMessage
import ru.demo.messenger.data.message.MessageModel
import rx.Single

class SendReplyMessage(
        private val messageDataSource: MessageDataSource,
        private val sendMessageHelper: SendMessageHelper
) : SendMessageStrategy<ReplyFutureMessage> {

    override fun execute(
            futureMessage: ReplyFutureMessage
    ): Single<MessageModel> = with(futureMessage) {
        val withoutAttachments = futureMessage.filePaths.isEmpty()
        return if (withoutAttachments) {
            sendMessage(futureMessage)
        } else {
            sendMessageHelper.uploadFiles(futureMessage)
                    .flatMap { sendMessage(futureMessage, it) }
        }
    }

    private fun sendMessage(
            futureMessage: ReplyFutureMessage,
            fileIds: List<String> = emptyList()
    ): Single<MessageModel> = with(futureMessage) {
        val sendMessage = messageDataSource.replyMessage(replyMessage.id, chatId, text, fileIds, payload)
        return sendMessageHelper.validateMessage(futureMessage, fileIds)
                .andThen(sendMessage)
    }

}