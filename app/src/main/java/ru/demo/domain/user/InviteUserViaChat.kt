package ru.demo.domain.user

import ru.demo.domain.base.CompletableUseCase
import ru.demo.domain.message.MessageDataSource
import rx.Completable

class InviteUserViaChat(
        private val userDataSource: UserDataSource,
        private val messageDataSource: MessageDataSource
) : CompletableUseCase<InviteUserViaChat.Params>() {

    class Params(val userId: Long)

    override fun execute(params: Params): Completable = with(params) {
        return sendInviteToChat(userId)
                .andThen(userDataSource.markUserAsInvited(userId))
    }

    private fun sendInviteToChat(userId: Long): Completable {
        return userDataSource.getChatInviteMessage()
                .flatMap { messageDataSource.createChat(listOf(userId), it, listOf()) }
                .toCompletable()
    }

}