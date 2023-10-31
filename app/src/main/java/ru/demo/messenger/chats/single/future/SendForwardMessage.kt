package ru.demo.messenger.chats.single.future

import ru.demo.domain.message.MessageDataSource
import ru.demo.messenger.chats.single.future.message.ForwardFutureMessage
import ru.demo.messenger.data.message.MessageModel
import rx.Single

class SendForwardMessage(
        private val messageDataSource: MessageDataSource
) : SendMessageStrategy<ForwardFutureMessage> {

    override fun execute(
            futureMessage: ForwardFutureMessage
    ): Single<MessageModel> = with(futureMessage) {
        return messageDataSource.forwardMessage(messageId, chatId, payload)
    }

}