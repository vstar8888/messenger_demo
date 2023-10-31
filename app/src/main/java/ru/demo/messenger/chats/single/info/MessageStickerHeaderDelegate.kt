package ru.demo.messenger.chats.single.info

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.widget.FrameLayout
import ru.demo.messenger.R
import ru.demo.messenger.chats.single.delegates.StickerMessageDelegate
import ru.demo.messenger.data.message.MessageModel
import ru.demo.messenger.utils.DimensionUtils

class MessageStickerHeaderDelegate(
        context: Context
) : StickerMessageDelegate(context, 0, null) {

    override fun onCreateViewHolder(parent: ViewGroup): Holder {
        return super.onCreateViewHolder(parent).apply {
            val color = ContextCompat.getColor(itemView.context, R.color.chat_background_gray)
            itemView.setBackgroundColor(color)
            (this.vgContainer.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = DimensionUtils.dp(8f)
                bottomMargin = DimensionUtils.dp(8f)
            }
        }
    }

    override fun isOnRightSide(message: MessageModel): Boolean {
        return true
    }

}