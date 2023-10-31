package ru.demo.messenger.chats.single.delegates;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import ru.demo.messenger.R;
import ru.demo.messenger.utils.DateUtil;

public class MessageDateDelegate extends AbstractAdapterDelegate<LocalDate, Object, MessageDateDelegate.Holder> {

    private final LayoutInflater inflater;

    private final DateTimeFormatter thisYearFormatter = DateTimeFormatter.ofPattern("d MMMM",
            DateUtil.getDefaultLocale()
    );
    private final DateTimeFormatter pastYearsFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy",
            DateUtil.getDefaultLocale()
    );

    private final DateTimeFormatter thisYearFormatterDe = DateTimeFormatter.ofPattern("d. MMMM",
            DateUtil.getDefaultLocale()
    );
    private final DateTimeFormatter pastYearsFormatterDe = DateTimeFormatter.ofPattern("d. MMMM yyyy",
            DateUtil.getDefaultLocale()
    );

    public MessageDateDelegate(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    private String getDate(@NonNull LocalDate item) {
        final LocalDate now = LocalDate.now();
        boolean isDeLocale = DateUtil.getDefaultLocale().getLanguage().equals(new Locale("de").getLanguage());
        if (isDeLocale) {
            return item.getYear() == now.getYear()
                    ? thisYearFormatterDe.format(item)
                    : pastYearsFormatterDe.format(item);
        } else {
            return item.getYear() == now.getYear()
                    ? thisYearFormatter.format(item)
                    : pastYearsFormatter.format(item);
        }
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List items, int position) {
        return item instanceof LocalDate;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(inflater.inflate(R.layout.item_message_header, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull LocalDate item,
                                    @NonNull List<Object> payloads) {
        holder.text.setText(getDate(item));
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView text;

        Holder(View itemView) {
            super(itemView);
            text = (TextView) itemView;
        }
    }

}