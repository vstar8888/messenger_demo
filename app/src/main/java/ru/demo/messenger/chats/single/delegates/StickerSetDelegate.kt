package ru.demo.messenger.chats.single.delegates

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.growapp.base.adapter.AbstractAdapterDelegate
import com.facebook.drawee.view.SimpleDraweeView
import ru.demo.data.message.StickerSet
import ru.demo.messenger.R

class StickerSetDelegate(
        context: Context,
        private val callback: (viewId: Int, position: Int) -> Unit
) : AbstractAdapterDelegate<StickerSet, Any, StickerSetDelegate.Holder>() {

    companion object {
        const val SELECT_PAYLOAD = "SELECT_PAYLOAD"
        const val UNSELECT_PAYLOAD = "UNSELECT_PAYLOAD"
    }

    private val inflater = LayoutInflater.from(context)

    override fun isForViewType(item: Any, items: MutableList<Any>, position: Int): Boolean =
            item is StickerSet

    override fun onCreateViewHolder(parent: ViewGroup) =
            Holder(inflater.inflate(R.layout.item_sticker_set, parent, false)).apply {
                itemView.setOnClickListener { callback.invoke(it.id, adapterPosition) }
            }

    override fun onBindViewHolder(holder: Holder, item: StickerSet, payloads: MutableList<Any>
    ) = with(holder) {
        if (item.stickers.isEmpty()) {
            @Suppress("CAST_NEVER_SUCCEEDS")
            sdvSticker.setImageURI(null as? String)
        } else {
            sdvSticker.setImageURI(item.stickers[0].imageUrl)
        }

        when {
            payloads.contains(SELECT_PAYLOAD) -> {
                val color = ContextCompat.getColor(itemView.context, R.color.dark_divider)
                itemView.setBackgroundColor(color)
            }
            payloads.contains(UNSELECT_PAYLOAD) -> {
                itemView.background = null
            }
            else -> itemView.background = null
        }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sdvSticker = itemView.findViewById(R.id.sdvSticker) as SimpleDraweeView
    }

}
