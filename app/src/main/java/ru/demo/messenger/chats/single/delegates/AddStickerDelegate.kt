package ru.demo.messenger.chats.single.delegates

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.growapp.base.adapter.AbstractAdapterDelegate
import ru.demo.messenger.R

class AddStickerItem

class AddStickerDelegate(
        context: Context,
        private val callback: (viewId: Int, position: Int) -> Unit
) : AbstractAdapterDelegate<AddStickerItem, Any, AddStickerDelegate.Holder>() {

    private val inflater = LayoutInflater.from(context)

    override fun isForViewType(item: Any, items: MutableList<Any>, position: Int): Boolean =
            item is AddStickerItem

    override fun onCreateViewHolder(parent: ViewGroup) =
            Holder(inflater.inflate(R.layout.item_sticker_add, parent, false)).apply {
                itemView.setOnClickListener { callback.invoke(it.id, adapterPosition) }

            }

    override fun onBindViewHolder(holder: Holder, item: AddStickerItem, payloads: List<Any>) = Unit

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
