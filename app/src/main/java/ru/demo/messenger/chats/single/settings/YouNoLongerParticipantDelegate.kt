package ru.demo.messenger.chats.single.settings

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.growapp.base.adapter.AbstractAdapterDelegate
import ru.demo.messenger.R

internal class YouNoLongerParticipantDelegate(context: Context
) : AbstractAdapterDelegate<YouNoLongerParticipantDelegate.Item, Any, YouNoLongerParticipantDelegate.Holder>() {

    class Item

    private val inflater = LayoutInflater.from(context)

    override fun isForViewType(item: Any, items: MutableList<Any>, position: Int): Boolean {
        return item is Item
    }

    override fun onCreateViewHolder(parent: ViewGroup): Holder =
            Holder(inflater.inflate(R.layout.item_no_longer_participant, parent, false))

    override fun onBindViewHolder(holder: Holder, item: Item, payloads: List<Any>) = Unit

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
