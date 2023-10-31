package ru.demo.messenger.view;


import android.content.Context;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;


public class BlockedSelectionEditText extends AppCompatEditText {

    protected boolean blockSelection;


    public BlockedSelectionEditText(Context context) {
        super(context);
    }

    public BlockedSelectionEditText(Context context,
                                    AttributeSet attrs) {
        super(context, attrs);
    }

    public BlockedSelectionEditText(Context context,
                                    AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTextIsSelectable(false);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (blockSelection) {
            setSelection(length());
        }
    }

    public void setBlockSelection(boolean enable) {
        blockSelection = enable;
    }

    public boolean isBlockSelection() {
        return blockSelection;
    }

}