package ru.demo.messenger.chats.single.future.message

abstract class FutureMessage(
        val chatId: Long,
        val payload: String
)

class InvalidFutureMessage : RuntimeException()