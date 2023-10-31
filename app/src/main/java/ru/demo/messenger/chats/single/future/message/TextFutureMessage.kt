package ru.demo.messenger.chats.single.future.message

open class TextFutureMessage(
        chatId: Long,
        payload: String,
        val text: String,
        val filePaths: MutableList<String>
) : FutureMessage(chatId, payload)
