package ru.demo.messenger.chats.single.future.message

import ru.demo.messenger.data.message.AttachedFiles

class ForwardFutureMessage(
        chatId: Long,
        payload: String,
        val messageId: Long,
        val authorId: Long,
        val authorFullName: String,
        val text: String,
        val attachedFiles: AttachedFiles
) : FutureMessage(chatId, payload)