package ru.demo.messenger.chats.list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import biz.growapp.base.loading.BaseAppLoadingFragment;
import biz.growapp.base.pagination.PaginationAdapter;
import butterknife.BindView;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.single.SingleChatActivity;
import ru.demo.messenger.chats.single.selection.ChatSelectionActivity;
import ru.demo.messenger.chats.single.selection.ChatSelectionActivity2;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.chat.LastChatMessage;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.helpers.ItemDividerDecorator;
import ru.demo.messenger.helpers.ViewStateSwitcher;
import ru.demo.messenger.internal.di.Injection;
import ru.demo.messenger.main.MainActivity;
import ru.demo.messenger.network.ConnectionService;
import ru.demo.messenger.people.OnlineUsersHolder;
import ru.demo.messenger.people.list.SelectPeopleActivity;
import ru.demo.messenger.utils.DimensionUtils;

public class ChatsFragment extends BaseAppLoadingFragment
        implements PaginationAdapter.Loader, ChatsPresenter.View, ChatsDelegate.Callback,
        ChatsAdapter.Callback {

    private static final String ARG_FOR_SELECTION = "ARG_FOR_SELECTION";

    private static final int SCROLL_DOWN_THRESHOLD = 2;
    private static final int REQUEST_CHAT = 100;
    private static final int PAGE_SIZE = 20;


    public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    public static ChatsFragment forSelection() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ARG_FOR_SELECTION, true);
        ChatsFragment fragment = new ChatsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R.id.srlRefresh) SwipeRefreshLayout srlRefresh;
    @BindView(R.id.rvChats) RecyclerView rvChats;
    @BindView(R.id.tvEmptyState) TextView tvEmptyState;

    private LinearLayoutManager chatsLayoutManager;
    private ChatsAdapter adapter;
    private boolean isRefreshing;

    private ChatsPresenter presenter;
    private ChatsDelegate chatsDelegate;

    @Override
    public void switchToState(@ViewStateSwitcher.State String state, boolean animate) {
        String newState;
        if (ViewStateSwitcher.State.STATE_EMPTY.equals(state)) {
            rvChats.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            newState = ViewStateSwitcher.State.STATE_MAIN;
        } else if (ViewStateSwitcher.State.STATE_MAIN.equals(state)) {
            rvChats.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            newState = ViewStateSwitcher.State.STATE_MAIN;
        } else {
            tvEmptyState.setVisibility(View.GONE);
            newState = state;
        }
        super.switchToState(newState, animate);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        addTextState(ViewStateSwitcher.State.STATE_EMPTY, getString(R.string.chats_empty_list));

        presenter = new ChatsPresenter(this);

        setupSwipeRefresh();

        presenter.getActiveChats();
    }

    private void setupSwipeRefresh() {
        srlRefresh.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.main_blue));
        srlRefresh.setOnRefreshListener(() -> {
            isRefreshing = true;
            presenter.resetChats();
            ActionMode actionMode = getActionMode();
            if (actionMode != null) {
                actionMode.finish();
            }
        });
    }

    private void setupRecyclerView() {
        chatsLayoutManager = new LinearLayoutManager(getContext());
        rvChats.setLayoutManager(chatsLayoutManager);
        rvChats.addItemDecoration(createItemDivider());

        adapter = new ChatsAdapter(this, this, PAGE_SIZE);
        chatsDelegate = new ChatsDelegate(adapter, getContext(),
                Injection.provideCompositeImageHelper(getContext()),
                isForSelection(),
                this
        );
        adapter.getManager()
                .addDelegate(chatsDelegate);
        rvChats.setAdapter(adapter);
    }

    @NonNull
    private ItemDividerDecorator createItemDivider() {
        final int lineHeight = DimensionUtils.dp(1);
        final int leftMargin = DimensionUtils.dp(85);
        final int rightMargin = DimensionUtils.dp(16);
        final int dividerColor = ContextCompat.getColor(getContext(), R.color.gray_divider);
        final Rect marginRectangle = new Rect(leftMargin, 0, rightMargin, 0);
        return new ItemDividerDecorator(dividerColor, lineHeight, marginRectangle);
    }

    @Override
    public void onLoadMore(int offset) {
        presenter.getActiveChats();
    }

    @Override
    public void onChatsLoaded(List<ChatModel> chats) {
        if (isRefreshing) {
            adapter.clear();
            isRefreshing = false;
            srlRefresh.setRefreshing(false);
        }
        if (isForSelection()) {
            final List<ChatModel> withoutLeavedChats = new ArrayList<>();
            for (int i = 0; i < chats.size(); i++) {
                final ChatModel chatModel = chats.get(i);
                if (chatModel.isInChain()) {
                    withoutLeavedChats.add(chatModel);
                }
            }
            adapter.addAll(withoutLeavedChats);
        } else {
            adapter.addAll(chats);
        }
        if (adapter.getItemCount() == 0) {
            switchToEmpty(true);
        } else {
            switchToMain(true);
        }
    }

    private boolean isForSelection() {
        return getArguments() != null && getArguments().getBoolean(ARG_FOR_SELECTION);
    }

    @Override
    public void addOrUpdateChat(ChatModel chat) {
        if (chatsDelegate.addOrUpdate(chat)) {
            scrollChatsToTop();
        }
    }

    private void scrollChatsToTop() {
        final int firstVisibleItemPosition = chatsLayoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItemPosition < SCROLL_DOWN_THRESHOLD) {
            rvChats.smoothScrollToPosition(0);
        }
    }

    @Nullable
    private ActionMode getActionMode() {
        if (!(getActivity() instanceof MainActivity)) {
            return null;
        }
        final MainActivity activity = (MainActivity) getActivity();
        if (activity == null) {
            return null;
        } else {
            return activity.getActionMode();
        }
    }

    @Override
    public void onChatClick(int position) {
        if (clickHelper.isDoubleClicked(position)) {
            return;
        }
        final ChatModel chat = (ChatModel) adapter.getItem(position);
        startActivityForResult(SingleChatActivity.openChat(getContext(), chat), REQUEST_CHAT);
    }

    @Override
    public void onChatLongClick(int position) {
        if (!(getActivity() instanceof MainActivity)) {
            return;
        }
        final MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.startActionMode(actionCallback);
        }
    }

    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_select_group_chat, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            final ChatModel selectedChat = adapter.getSelectedChat();
            if (selectedChat != null) {
                if (selectedChat.isOneToOne()) {
                    menu.findItem(R.id.action_exit)
                            .setVisible(false);
                    menu.findItem(R.id.action_remove_chat)
                            .setVisible(selectedChat.isInChain() && selectedChat.isOneToOne());
                } else {
                    menu.findItem(R.id.action_exit)
                            .setVisible(selectedChat.isInChain());
                    menu.findItem(R.id.action_remove_chat)
                            .setVisible(!selectedChat.isInChain());
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove_chat: {
                    final ChatModel chatModel = adapter.getSelectedChat();
                    if (chatModel != null) {
                        showRemoveChatAlert(chatModel);
                    }
                    return true;
                }
                case R.id.action_exit: {
                    final ChatModel chatModel = adapter.getSelectedChat();
                    if (chatModel != null) {
                        showLeaveChatAlert(chatModel);
                    }
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            onDisabledActionMode();
        }
    };

    public void onDisabledActionMode() {
        if (!(getActivity() instanceof MainActivity)) {
            return;
        }
        final MainActivity activity = (MainActivity) getActivity();
        adapter.disableActionMode();
        if (activity != null) {
            activity.disableActionMode();
        }
    }

    private void showLeaveChatAlert(ChatModel chatModel) {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.chat_participants_leave_group_question))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        (dialog, which) -> {
                            ActionMode actionMode = getActionMode();
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                            presenter.leaveGroup(chatModel);
                        })
                .show();
    }

    private void showRemoveChatAlert(ChatModel chatModel) {
        final String message = chatModel.isOneToOne()
                ? getString(R.string.delete_chat, chatModel.getTitle())
                : getString(R.string.delete_group, chatModel.getTitle());
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    ActionMode actionMode = getActionMode();
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    presenter.removeChat(chatModel);
                })
                .show();
    }

    @Override
    public void onChatRemoved(ChatModel chatModel) {
        final int removedChatIndex = adapter.getItems().indexOf(chatModel);
        if (removedChatIndex != RecyclerView.NO_POSITION) {
            adapter.remove(removedChatIndex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == REQUEST_CHAT) {
            final ChatModel updatedChat = SingleChatActivity.unpackUpdatedChat(data);
            final boolean isChatDeleted = SingleChatActivity.unpackIsChatDeleted(data);
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Object item = adapter.getItem(i);
                if (item instanceof ChatModel) {
                    ChatModel chat = (ChatModel) item;
                    if (chat.getId() == updatedChat.getId()) {
                        if (isChatDeleted) {
                            adapter.remove(i);
                        } else {
                            updatedChat.setLastMessage(chat.getLastMessage());
                            updatedChat.setUnreadMessagesCount(chat.getUnreadMessagesCount());
                            addOrUpdateChat(updatedChat);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onSelectedChatsUpdated(int position) {
        chatsDelegate.changeChatSelectState(position);
        if (getActivity() instanceof ChatSelectionActivity)
            ((ChatSelectionActivity) getActivity()).changeFabVisibility(chatsDelegate.getSelectedChatsSize());
        else if (getActivity() instanceof ChatSelectionActivity2)
            ((ChatSelectionActivity2) getActivity()).changeFabVisibility(chatsDelegate.getSelectedChatsSize());
    }

    @Override
    public void onAddChatClick(MainActivity.ActionClickEvent event) {
        startActivity(SelectPeopleActivity.getIntent(getContext()));
    }

    @Override
    public synchronized void onReceiveMessage(MessageModel message) {
        switchToMain(false);
        if (chatsDelegate.isChatExist(message.getChatId())) {
            chatsDelegate.updateLastMessageInChat(message);
            scrollChatsToTop();
        } else {
            presenter.getChatById(message.getChatId());
        }
    }

    @Override
    public void onMessageRead(long chatId, long messageId) {
        final List<Object> items = adapter.getItems();
        for (int i = 0; i < items.size(); i++) {
            final Object item = items.get(i);
            if (item instanceof ChatModel) {
                ChatModel chat = (ChatModel) item;
                if (chat.getId() == chatId) {
                    final LastChatMessage lastMessage = chat.getLastMessage();
                    if (lastMessage.getMessageId() == messageId) {
                        chat.setIsRead();
                        adapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }

    @Override
    public void onUserOnlineStatusChanged(OnlineUsersHolder.UserStatusUpdate status) {
        if (status.id == OnlineUsersHolder.UserStatusUpdate.ALL_USERS) {
            adapter.notifyDataSetChanged();
        } else {
            chatsDelegate.userOnlineStatusChanged(status.id);
        }
    }

    @Override
    public void onMessageDelivered(long chatId, @MessageModel.ReadStatus String status) {
        switchToMain(false);
        chatsDelegate.changeOutgoingMessageStatuses(chatId, status);
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionService.tryToConnect();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public int getMainContainerId() {
        return R.id.srlRefresh;
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToLoading(true);
        presenter.resetChats();
        ConnectionService.tryToConnect();
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroyView();
        super.onDestroyView();
    }

    public ArrayList<ChatModel> getSelectedChats() {
        return chatsDelegate.getSelectedChats();
    }

    public ChatModel getFirstSelectedChat() {
        return chatsDelegate.getFirstSelectedChat();
    }

    @Override
    public void updateChatPhoto(long chatId, String photo) {
        chatsDelegate.updateChatPhoto(chatId, photo);
    }

}
