package ru.demo.messenger.chats.single.selection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;

import java.util.ArrayList;

import biz.growapp.base.BaseAppActivity;
import butterknife.BindView;
import butterknife.OnClick;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.list.ChatsFragment;
import ru.demo.messenger.data.chat.ChatModel;

public class ChatSelectionActivity extends BaseAppActivity {

    private static final String EXTRA_IS_ACTION_MODE_FORWARD = "extra.IS_ACTION_MODE_FORWARD";
    private static final String EXTRA_OUT_SELECTED_CHATS = "extra.OUT_SELECTED_CHATS";
    private static final String EXTRA_OUT_CHAT_FOR_OPEN = "extra.OUT_CHAT_FOR_OPEN";

    @NonNull
    public static Intent getIntent(@NonNull Context context, boolean isActionModeForward) {
        return new Intent(context, ChatSelectionActivity.class).putExtra(EXTRA_IS_ACTION_MODE_FORWARD, isActionModeForward);
    }

    @Nullable
    public static ArrayList<ChatModel> unpackSelectedChats(@NonNull Intent data) {
        return data.getParcelableArrayListExtra(EXTRA_OUT_SELECTED_CHATS);
    }

    @Nullable
    public static ChatModel unpackChatForOpen(@NonNull Intent data) {
        return data.getParcelableExtra(EXTRA_OUT_CHAT_FOR_OPEN);
    }

    @Nullable
    public static boolean unpackIsActionModeForward(@NonNull Intent data) {
        return data.getBooleanExtra(EXTRA_IS_ACTION_MODE_FORWARD, false);
    }

    @BindView(R.id.fabForward) FloatingActionButton fabForward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.vgContainer, ChatsFragment.forSelection())
                .commit();
    }

    @OnClick(R.id.fabForward)
    protected void onActionClick(View view) {
        ChatsFragment fragment = (ChatsFragment) getSupportFragmentManager().findFragmentById(R.id.vgContainer);
        if (fragment.isAdded()) {
            final Intent data = new Intent()
                    .putParcelableArrayListExtra(EXTRA_OUT_SELECTED_CHATS, fragment.getSelectedChats())
                    .putExtra(EXTRA_OUT_CHAT_FOR_OPEN, fragment.getFirstSelectedChat())
                    .putExtra(EXTRA_IS_ACTION_MODE_FORWARD, getIntent().getBooleanExtra(EXTRA_IS_ACTION_MODE_FORWARD, false));
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public void changeFabVisibility(int chatsSize) {
        if (chatsSize > 0) {
            fabForward.show();
        } else {
            fabForward.hide();
        }
    }
}
