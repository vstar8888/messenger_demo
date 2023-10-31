package ru.demo.messenger.chats.single.future

import ru.demo.messenger.chats.single.future.message.FutureMessage
import ru.demo.messenger.data.message.MessageModel
import rx.Single

interface SendMessageStrategy<in T : FutureMessage> {

    fun execute(futureMessage: T): Single<MessageModel>

}