package ru.demo.messenger.chats.single.delegates

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import biz.growapp.base.adapter.AbstractAdapterDelegate
import com.facebook.drawee.view.SimpleDraweeView
import ru.demo.messenger.R
import ru.demo.messenger.data.message.MessageModel
import ru.demo.messenger.helpers.UserNameHelper
import ru.demo.messenger.people.TextDrawable

open class StickerMessageDelegate(
        context: Context,
        private val selfUserId: Long,
        private val adapter: MessagesAdapter?
) : AbstractAdapterDelegate<MessageModel, Any, StickerMessageDelegate.Holder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val userPhotoSize: Int =
            context.resources.getDimensionPixelSize(R.dimen.message_user_photo_size)
    private val previewBackgroundColor: Int = ContextCompat.getColor(context, R.color.dark_blue)

    override fun isForViewType(item: Any, items: List<Any>, position: Int): Boolean =
            item is MessageModel && item.isStickerMessage

    override fun onCreateViewHolder(parent: ViewGroup) =
            Holder(inflater.inflate(R.layout.item_message_sticker, parent, false)).apply {
                adapter?.let { messageAdapter ->
                    itemView.setOnLongClickListener {
                        messageAdapter.proceedLongClick(this)
                        return@setOnLongClickListener true
                    }
                    itemView.setOnClickListener {
                        if (messageAdapter.isActionMode) {
                            messageAdapter.proceedActionMode(this)
                        }
                    }
                }
            }

    override fun onBindViewHolder(holder: Holder, item: MessageModel, payloads: MutableList<Any>
    ) = with(holder) {
        adapter?.let { MessageBindHelper.setSelectedBackground(itemView, it, item) }

        tvSendTime.text = MessageBindHelper.getFormattedTime(item.createdAt)

        if (isOnRightSide(item)) {
            setStickerGravity(Gravity.END)
            MessageBindHelper.bindReadStatus(tvSendTime, item)
        } else {
            setStickerGravity(Gravity.START)
            tvSendTime.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
        sdvSticker.setImageURI(item.sticker?.largeImageUrl)

        if (adapter?.isOneToOneChat == true) {
            sdvPhoto.visibility = View.GONE
        } else {
            if (adapter?.isNeedShowAvatar(item) == true) {
                sdvPhoto.visibility = View.VISIBLE
                sdvPhoto.hierarchy.setPlaceholderImage(
                        TextDrawable(userPhotoSize, userPhotoSize,
                                previewBackgroundColor,
                                UserNameHelper.getInitials(item.authorFullName), true)
                )
                sdvPhoto.setImageURI(item.authorAvatarUrl)
            } else {
                sdvPhoto.visibility = View.INVISIBLE
            }
        }
    }

    open fun isOnRightSide(message: MessageModel): Boolean {
        return message.authorId == selfUserId
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var sdvPhoto = itemView.findViewById(R.id.sdvPhoto) as SimpleDraweeView
        var vgContainer = itemView.findViewById(R.id.vgContainer) as ViewGroup

        var sdvSticker = itemView.findViewById(R.id.sdvSticker) as SimpleDraweeView
        var tvSendTime = itemView.findViewById(R.id.tvSendTime) as TextView

        fun setStickerGravity(gravity: Int) {
            (vgContainer.layoutParams as FrameLayout.LayoutParams).gravity = gravity
        }

    }

}
