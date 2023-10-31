package ru.demo.messenger.chats.single.delegates

import biz.growapp.base.adapter.DelegationAdapter

class SelectItemManager(
        private val adapter: DelegationAdapter<*>,
        private val selectPayload: String,
        private val unselectPayload: String
) {

    private companion object {
        private const val NO_ACTION_CLICKED = -1
    }

    private var lastActionClickedPosition = NO_ACTION_CLICKED

    fun onItemClick(position: Int) {
        if (position == lastActionClickedPosition) {
            return
        }
        updateItemSelection(lastActionClickedPosition, unselectPayload)
        updateItemSelection(position, selectPayload)
        lastActionClickedPosition = position
    }

    private fun updateItemSelection(position: Int, payload: String) {
        if (NO_ACTION_CLICKED == position) {
            return
        }
        adapter.notifyItemChanged(position, payload)
    }

    fun reset() {
        lastActionClickedPosition = NO_ACTION_CLICKED
    }

}
