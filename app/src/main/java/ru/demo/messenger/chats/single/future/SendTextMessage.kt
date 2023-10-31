package ru.demo.messenger.chats.single.future

import ru.demo.domain.message.MessageDataSource
import ru.demo.messenger.chats.single.future.message.TextFutureMessage
import ru.demo.messenger.data.message.MessageModel
import rx.Single

class SendTextMessage(
        private val messageDataSource: MessageDataSource,
        private val sendMessageHelper: SendMessageHelper
) : SendMessageStrategy<TextFutureMessage> {

    override fun execute(futureMessage: TextFutureMessage): Single<MessageModel> {
        val withoutAttachments = futureMessage.filePaths.isEmpty()
        return if (withoutAttachments) {
            sendMessage(futureMessage)
        } else {
            sendMessageHelper.uploadFiles(futureMessage)
                    .flatMap { sendMessage(futureMessage, it) }
        }
    }

    private fun sendMessage(
            futureMessage: TextFutureMessage,
            fileIds: List<String> = emptyList()
    ): Single<MessageModel> = with(futureMessage) {
        val sendMessage = messageDataSource.sendMessage(chatId, text, fileIds, payload)
        return sendMessageHelper.validateMessage(futureMessage, fileIds)
                .andThen(sendMessage)
    }

}