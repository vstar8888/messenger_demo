package ru.demo.messenger.chats.single.info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import biz.growapp.base.adapter.DelegationAdapter;
import biz.growapp.base.loading.BaseAppLoadingActivity;
import butterknife.BindView;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.fullscreen.PictureViewActivity;
import ru.demo.messenger.chats.single.SingleChatActivity;
import ru.demo.messenger.chats.single.selection.ChatSelectionActivity;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.message.Attachment;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.people.profile.ProfileActivity;

public class MessageInfoActivity extends BaseAppLoadingActivity implements
        MessageInfoUserDelegate.Callback,
        MessageInfoHeaderDelegate.Callback,
        MessageInfoPresenter.View {

    private static final String EXTRA_IN_MESSAGE = "extra.IN_MESSAGE";
    private static final int REQUEST_SELECT_CHATS = 333;

    @NonNull
    public static Intent getIntent(@NonNull Context context, @NonNull MessageModel message) {
        return new Intent(context, MessageInfoActivity.class)
                .putExtra(EXTRA_IN_MESSAGE, message);
    }

    @BindView(R.id.rvMessageInfo) RecyclerView rvMessageInfo;

    private final DelegationAdapter<Object> adapter = new DelegationAdapter<>();
    private MessageInfoPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_info);
        MessageModel message = getIntent().getParcelableExtra(EXTRA_IN_MESSAGE);
        setupRecyclerView(message);
        presenter = new MessageInfoPresenter(this, this, message);
        presenter.getMessageInfo();
    }

    private void setupRecyclerView(MessageModel message) {
        if (message.isStickerMessage()) {
            adapter.getManager()
                    .addDelegate(new MessageStickerHeaderDelegate(this));
        } else {
            adapter.getManager()
                    .addDelegate(new MessageInfoHeaderDelegate(this, this));
        }
        adapter.getManager()
                .addDelegate(new MessageInfoUserDelegate(this, this))
                .addDelegate(new MessageInfoReadStatusDelegate(this));
        rvMessageInfo.setLayoutManager(new LinearLayoutManager(this));
        rvMessageInfo.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroyView();
        super.onDestroy();
    }

    @Override
    public void onMessageInfoLoaded(List<Object> usersDelivered,
                                    List<Object> usersIsRead,
                                    MessageModel message) {
        adapter.add(message);
        adapter.add(new MessageInfoReadStatusDelegate.Item(InfoReadStatus.ISREAD));
        adapter.addAll(usersIsRead);
        if (!usersDelivered.isEmpty()) {
            adapter.add(new MessageInfoReadStatusDelegate.Item(InfoReadStatus.UNREAD));
            adapter.addAll(usersDelivered);
        }
    }

    @Override
    public int getMainContainerId() {
        return R.id.rvMessageInfo;
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToLoading(true);
        adapter.clear();
        presenter.getMessageInfo();
    }

    @Override
    public void onAttachedFileClick(Attachment attachment) {
        String toastMessage = getString(R.string.file_download_added_queue, attachment.getName());
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        presenter.downloadFile(this, attachment.getUrl(), attachment.getName());
    }

    @Override
    public void onProfileClick(int viewId, long userId) {
        if (clickHelper.isDoubleClicked(viewId)) {
            return;
        }
        startActivity(ProfileActivity.ofUser(this, userId));
    }

    @Override
    public void onFileForwardClick(int position) {
        startActivityForResult(
                ChatSelectionActivity.getIntent(this, false),
                REQUEST_SELECT_CHATS
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        switch (requestCode) {
            case REQUEST_SELECT_CHATS:
                ArrayList<ChatModel> selectedChats = ChatSelectionActivity.unpackSelectedChats(data);
                ChatModel chatForOpen = ChatSelectionActivity.unpackChatForOpen(data);

                if (selectedChats != null && chatForOpen != null) {
                    final ArrayList<MessageModel> forwardMessages = new ArrayList<>();
                    forwardMessages.add(presenter.getMessage());
                    startActivity(SingleChatActivity.openChatForForward(this, selectedChats, chatForOpen, forwardMessages));
                    finish();
                }
                break;
        }
    }


    @Override
    public void onImageClick(Attachment attachment, int position) {
        startActivity(PictureViewActivity.navigateToFullscreenImage(this, attachment.getUrl()));
    }

}