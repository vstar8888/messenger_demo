package ru.demo.messenger.helpers;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.util.LongSparseArray;

import java.util.Random;

import ru.demo.messenger.R;
import ru.demo.messenger.data.user.UserModel;

public class ColorGenerator {

    private final Context context;
    private LongSparseArray<Integer> colors;

    private final Random rand = new Random();

    public ColorGenerator(@NonNull Context context) {
        this.context = context;
        this.colors = new LongSparseArray<>();
    }

    @ColorInt
    public int from(UserModel user) {
        Integer color = colors.get(user.getId());
        if (color == null) {
            color = getColor();
            colors.put(user.getId(), color);
        }
        return color;
    }

    private int getColor() {
        final int color;
        switch (getColorType()) {
            case 1:
                color = R.color.dark_blue;
                break;
            case 2:
                color = R.color.peoplered;
                break;
            case 3:
                color = R.color.peoplegreen;
                break;
            default:
                color = R.color.main_blue_light;
        }
        return ContextCompat.getColor(context, color);
    }

    private int getColorType() {
        return rand.nextInt(3) + 1;
    }

}
