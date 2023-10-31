package ru.demo.messenger.chats.list

import biz.growapp.base.pagination.PaginationAdapter
import ru.demo.messenger.data.chat.ChatModel

class ChatsAdapter(
        private val callback: Callback,
        loader: PaginationAdapter.Loader,
        pageSize: Int
) : PaginationAdapter<Any>(loader, PaginationAdapter.Direction.TO_END, pageSize) {

    interface Callback {
        fun onChatClick(position: Int)
        fun onChatLongClick(position: Int)
        fun onDisabledActionMode()
    }

    val isActionMode: Boolean
        get() = selectedChat != null
    var selectedChat: ChatModel? = null
        private set

    private fun enableActionMode(chatModel: ChatModel) {
        selectedChat = chatModel
        val indexSelectedChat = items.indexOf(selectedChat)
        notifyItemChanged(indexSelectedChat)
    }

    fun disableActionMode() {
        val indexSelectedChat = items.indexOf(selectedChat)
        selectedChat = null
        notifyItemChanged(indexSelectedChat)
    }


    fun proceedClick(position: Int) {
        callback.onChatClick(position)
    }

    fun proceedLongClick(position: Int) {
        if (!isActionMode) {
            proceedActionMode(position)
        }
    }

    fun proceedActionMode(position: Int) {
        if (isActionMode) {
            disableActionMode()
            callback.onDisabledActionMode()
        } else {
            val chat = items[position] as ChatModel
            enableActionMode(chat)
            callback.onChatLongClick(position)

        }
    }
}
