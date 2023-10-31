package ru.demo.messenger.chats.single.settings.rename;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import biz.growapp.base.loading.BaseAppLoadingActivity;
import butterknife.BindView;
import ru.demo.messenger.R;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.helpers.ViewStateSwitcher;
import ru.demo.messenger.utils.DisplayUtils;

public class RenameChatActivity extends BaseAppLoadingActivity implements RenameChatPresenter.View {

    private static final String EXTRA_CHAT = "extra.CHAT";
    private static final String EXTRA_OUT_NEW_CHAT_NAME = "extra.OUT_NEW_CHAT_NAME";

    @NonNull
    public static Intent getIntent(@NonNull Context context, @NonNull ChatModel chat) {
        return new Intent(context, RenameChatActivity.class)
                .putExtra(EXTRA_CHAT, chat);
    }

    @Nullable
    public static String unpackNewChatName(@NonNull Intent data) {
        return data.getStringExtra(EXTRA_OUT_NEW_CHAT_NAME);
    }

    @BindView(R.id.etGroupTitle) EditText etGroupTitle;
    @BindView(R.id.tilGroupTitle) TextInputLayout tilGroupTitle;

    @Nullable
    private MenuItem renameMenuItem;

    private RenameChatPresenter presenter;
    private ChatModel chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_chat);

        chat = getIntent().getParcelableExtra(EXTRA_CHAT);
        presenter = new RenameChatPresenter(this);

        etGroupTitle.setText(chat.getTitle().trim());
        switchToMain(false);

        etGroupTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && isGroupNameNotEmpty()) {
                renameChat();
            }
            return false;
        });
        etGroupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString().trim())) {
                    tilGroupTitle.setErrorEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rename_chat, menu);
        renameMenuItem = menu.findItem(R.id.action_rename);
        return true;
    }

    private boolean isGroupNameNotEmpty() {
        if (TextUtils.isEmpty(etGroupTitle.getText().toString().trim())) {
            tilGroupTitle.setError(getString(R.string.create_group_fill_required));
            tilGroupTitle.setErrorEnabled(true);
            return false;
        } else {
            tilGroupTitle.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rename:
                if (isGroupNameNotEmpty()) {
                    DisplayUtils.hideSoftKeyboard(this);
                    renameChat();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void renameChat() {
        final String newName = etGroupTitle.getText().toString().trim();
        if (chat.getTitle().trim().equals(newName)) {
            finish();
        } else {
            presenter.renameChat(chat, newName);
        }
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroyView();
        super.onDestroy();
    }

    @Override
    public void onChatRenamed(String name) {
        final Intent data = new Intent()
                .putExtra(EXTRA_OUT_NEW_CHAT_NAME, name);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public int getMainContainerId() {
        return R.id.tilGroupTitle;
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToMain(true);
    }

    @Override
    public void switchToState(@ViewStateSwitcher.State String state, boolean animate) {
        super.switchToState(state, animate);
        if (renameMenuItem != null) {
            if (ViewStateSwitcher.State.STATE_LOADING.equals(state)) {
                renameMenuItem.setVisible(false);
            } else if (ViewStateSwitcher.State.STATE_MAIN.equals(state)) {
                renameMenuItem.setVisible(true);
            }
        }
    }

}