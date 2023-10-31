package ru.demo.messenger.chats.single.selection;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import biz.growapp.base.BaseAppActivity;
import butterknife.BindView;
import butterknife.OnClick;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.list.ChatsFragment;
import ru.demo.messenger.chats.single.future.FutureMessageStorage;
import ru.demo.messenger.chats.single.future.message.TextFutureMessage;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.main.MainActivity;
import ru.demo.messenger.utils.UriUtils;

public class ChatSelectionActivity2 extends BaseAppActivity {

    private static final String EXTRA_IS_ACTION_MODE_FORWARD = "extra.IS_ACTION_MODE_FORWARD";
    private static final String EXTRA_OUT_SELECTED_CHATS = "extra.OUT_SELECTED_CHATS";
    private static final String EXTRA_OUT_CHAT_FOR_OPEN = "extra.OUT_CHAT_FOR_OPEN";

    @NonNull
    public static Intent getIntent(@NonNull Context context, boolean isActionModeForward) {
        return new Intent(context, ChatSelectionActivity2.class).putExtra(EXTRA_IS_ACTION_MODE_FORWARD, isActionModeForward);
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
    private Msg msg;
    private Sender sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.vgContainer, ChatsFragment.forSelection())
                .commit();

        sender = new Sender();
        msg = new Msg();
        msg.switchHandler();
    }



    @OnClick(R.id.fabForward)
    protected void onActionClick(View view) {
        ChatsFragment fragment = (ChatsFragment) getSupportFragmentManager().findFragmentById(R.id.vgContainer);
        ArrayList<ChatModel> selectedChats = fragment.getSelectedChats();
        if (selectedChats.size() == 0) return;
        sender.sendMessage(msg.text, selectedChats, msg.getPaths());
        startActivity(new Intent(this, MainActivity.class));
        finish();
//        if (fragment.isAdded()) {
//            final Intent data = new Intent()
//                    .putParcelableArrayListExtra(EXTRA_OUT_SELECTED_CHATS, fragment.getSelectedChats())
//                    .putExtra(EXTRA_OUT_CHAT_FOR_OPEN, fragment.getFirstSelectedChat())
//                    .putExtra(EXTRA_IS_ACTION_MODE_FORWARD, getIntent().getBooleanExtra(EXTRA_IS_ACTION_MODE_FORWARD, false));
//            setResult(RESULT_OK, data);
//            finish();
//        }
    }
    public void changeFabVisibility(int chatsSize) {
        if (chatsSize > 0) {
            fabForward.show();
        } else {
            fabForward.hide();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {//vs
        super.onNewIntent(intent);
        setIntent(intent);
//        ShareIn msg = new ShareIn().switchHandler();
    }
    private final class Msg {
        String text;
        String path;
        List<String> getPaths() {
            List<String> paths = null;
            if (path != null) {
                paths = new ArrayList<>();
                paths.add(path);
            }
            return paths;
        }
        private void switchHandler() {
            text = "";
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            if (!Intent.ACTION_SEND.equals(action)) return;
            if (type == null) return;
            if ("text/plain".equals(type)) {
                handleText(intent);
                if (text.isEmpty())
                    handleSingleFile(intent);
            } else {
                handleSingleFile(intent);
            }

//            else if (type.startsWith("image/")) handleSingleImage(intent);
//        else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
//            if (type.startsWith("image/")) {
//                handleSendMultipleImages(intent); // Handle multiple images being sent
//            }
//        } else {
//            // Handle other intents, such as being started from the home screen
//        }
        }
        private void handleText(Intent intent) {
            text = intent.getStringExtra(Intent.EXTRA_TEXT);//            if (text == null) return;
            if (text == null) text = "";
        }
        private void handleSingleFile(Intent intent) {
            Uri uri = intent.getData();
            if (uri == null) {
                ClipData clipData = intent.getClipData();
                if (clipData == null) return;
                ClipData.Item item = clipData.getItemAt(0);
                uri = item.getUri();
            }
            if (uri != null) {
                path = UriUtils.getPath(ChatSelectionActivity2.this, uri);
            }
        }
    }
    private final class Sender {
        public void sendMessage(String message, List<ChatModel> targetChats, @Nullable List<String> attachmentPaths) {
            final List<Long> targetChatIds = new ArrayList<>();
            for (ChatModel targetChat : targetChats)
                targetChatIds.add(targetChat.getId());
            final String payload = generateMobilePayload();
            if (attachmentPaths == null || attachmentPaths.size() == 0)
                attachmentPaths = Collections.emptyList();
            for (Long chatId : targetChatIds) {
                final TextFutureMessage textFutureMessage;
                if (!(TextUtils.isEmpty(message) && attachmentPaths.isEmpty())) {
                    textFutureMessage = new TextFutureMessage(chatId, payload, message, attachmentPaths);
                    FutureMessageStorage.getInstance().add(textFutureMessage);
                }
            }
        }
        private String generateMobilePayload() {
            return UUID.randomUUID().toString();
        }
    }



}
