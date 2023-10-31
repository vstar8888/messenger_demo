package ru.demo.messenger.chats.single.delegates

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.growapp.base.adapter.AbstractAdapterDelegate
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import ru.demo.data.message.FrescoSmallCacheFetcher
import ru.demo.data.message.Sticker
import ru.demo.messenger.R

class StickerDelegate(
        context: Context,
        private val frescoSmallCacheFetcher: FrescoSmallCacheFetcher,
        private val clickListener: (position: Int) -> Unit
) : AbstractAdapterDelegate<Sticker, Any, StickerDelegate.Holder>() {

    private val inflater = LayoutInflater.from(context)

    override fun isForViewType(item: Any, items: MutableList<Any>, position: Int): Boolean =
            item is Sticker

    override fun onCreateViewHolder(parent: ViewGroup) =
            Holder(inflater.inflate(R.layout.item_sticker, parent, false))

    override fun onBindViewHolder(
            holder: Holder,
            item: Sticker,
            payloads: MutableList<Any>
    ) = with(holder) {
        sdvImage.run {
            val request = frescoSmallCacheFetcher.requestFromUrl(item.imageUrl)
            controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(sdvImage.controller)
                    .setImageRequest(request)
                    .build()
        }
        holder.itemView.setOnClickListener {
            clickListener(holder.adapterPosition)
        }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sdvImage = itemView.findViewById(R.id.sdvImage) as SimpleDraweeView
    }

}
