package ru.demo.messenger.chats.single.future.message

import ru.demo.messenger.data.message.MessageModel


class ReplyFutureMessage(
        chatId: Long,
        payload: String,
        text: String,
        filePaths: MutableList<String>,
        val replyMessage: MessageModel
) : TextFutureMessage(chatId, payload, text, filePaths)