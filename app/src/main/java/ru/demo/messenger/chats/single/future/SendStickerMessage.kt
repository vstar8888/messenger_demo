package ru.demo.messenger.chats.single.future

import ru.demo.domain.message.MessageDataSource
import ru.demo.messenger.chats.single.future.message.StickerFutureMessage
import ru.demo.messenger.data.message.MessageModel
import rx.Single

class SendStickerMessage(
        private val messageDataSource: MessageDataSource
) : SendMessageStrategy<StickerFutureMessage> {

    override fun execute(
            futureMessage: StickerFutureMessage
    ): Single<MessageModel> = with(futureMessage) {
        return messageDataSource.sendSticker(sticker.id, chatId, payload)
    }

}