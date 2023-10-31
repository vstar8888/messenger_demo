package ru.demo.messenger.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import android.view.View;

import biz.growapp.base.BaseAppActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.demo.messenger.MainApp;
import ru.demo.messenger.MessengerRegistrationIntentService;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.single.future.FutureMessagesService;
import ru.demo.messenger.helpers.ColorChangingAnimation;
import ru.demo.messenger.network.ConnectionService;

import static ru.demo.messenger.utils.VectorUtils.getVectorDrawable;

public class MainActivity extends BaseAppActivity {

    public static final int TAB_POSITION_MESSAGE = 0;
    public static final int TAB_POSITION_PEOPLE = 1;
    public static final int TAB_POSITION_PROFILE = 2;

    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.tlSections) TabLayout tlSections;
    @BindView(R.id.vpSections) ViewPager vpSections;
    @BindView(R.id.fabAction) FloatingActionButton fabAction;

    private boolean fabHidden;
    private ActionMode actionMode;

    @ColorInt private int mainToolbarColor;
    @ColorInt private int actionModeColor;

    public static Intent getIntent(Context context) {
        registerToken(context);
        return new Intent(context, MainActivity.class);
    }

    public static Intent getClearTaskIntent(Context context) {
        registerToken(context);
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static void registerToken(Context context) {
        final Intent registerToken = new Intent(context.getApplicationContext(), MessengerRegistrationIntentService.class);
        context.getApplicationContext().startService(registerToken);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        vpSections.setAdapter(providePagerAdapter());
        tlSections.setupWithViewPager(vpSections);

        mainToolbarColor = ContextCompat.getColor(this, R.color.main_blue);
        actionModeColor = ContextCompat.getColor(this, R.color.dark_blue);

        final ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(false);
        supportActionBar.setHomeButtonEnabled(false);

        setupTabs();
        setupFab();

        startFutureMessageService();

        startSignalRConnection();
        vpSections.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (actionMode != null) {
                    disableActionMode();
                }
            }
        });
    }

    public void startActionMode(@NonNull ActionMode.Callback callback) {
        this.actionMode = startSupportActionMode(callback);
    }

    public ActionMode getActionMode() {
        return actionMode;
    }

    public void disableActionMode() {
        actionMode.finish();
        actionMode = null;
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        if (toolbar != null) {
            toolbar.setBackgroundColor(actionModeColor);
        }
        tlSections.setBackgroundColor(actionModeColor);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        if (toolbar != null) {
            toolbar.setBackgroundColor(mainToolbarColor);
        }
        ColorChangingAnimation.change(tlSections, actionModeColor, mainToolbarColor);
    }

    private MainPagerAdapter providePagerAdapter() {
        return new MainPagerAdapter(getSupportFragmentManager());
    }

    private void setupTabs() {
        final StateListDrawable stateListMessage = getTabStateListDrawable(
                R.drawable.ic_message_tab_active,
                R.drawable.ic_message_tab_inactive);
        tlSections.getTabAt(TAB_POSITION_MESSAGE).setIcon(stateListMessage);
        final StateListDrawable stateListPeople = getTabStateListDrawable(
                R.drawable.ic_people_tab_active,
                R.drawable.ic_people_tab_inactive);
        tlSections.getTabAt(TAB_POSITION_PEOPLE).setIcon(stateListPeople);
        final StateListDrawable stateListProfile = getTabStateListDrawable(
                R.drawable.ic_profile_tab_active,
                R.drawable.ic_profile_tab_inactive);
        tlSections.getTabAt(TAB_POSITION_PROFILE).setIcon(stateListProfile);

        vpSections.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeToolbarTitleByPosition(position);
                appBarLayout.setExpanded(true, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (vpSections.getCurrentItem() != TAB_POSITION_PEOPLE) {
                        changeFabIconByPosition(vpSections.getCurrentItem());
                        fabAction.show();
                        fabHidden = false;
                    }
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    fabAction.hide();
                    fabHidden = true;
                }
            }
        });

        tlSections.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            private FloatingActionButton.OnVisibilityChangedListener visibilityChangedListener =
                    new FloatingActionButton.OnVisibilityChangedListener() {
                        @Override
                        public void onHidden(FloatingActionButton fab) {
                            if (vpSections.getCurrentItem() != TAB_POSITION_PEOPLE) {
                                changeFabIconByPosition(vpSections.getCurrentItem());
                                fab.show();
                            }
                        }
                    };

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpSections.setCurrentItem(tab.getPosition());
                if (fabHidden) {
                    return;
                }
                fabAction.hide(visibilityChangedListener);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        changeToolbarTitleByPosition(vpSections.getCurrentItem());
    }

    @NonNull
    private StateListDrawable getTabStateListDrawable(@DrawableRes int activeResId,
                                                      @DrawableRes int inactiveResId) {
        final StateListDrawable stateListMessage = new StateListDrawable();
        final Drawable active = getVectorDrawable(this, activeResId);
        stateListMessage.addState(new int[]{android.R.attr.state_selected}, active);
        final Drawable inactive = getVectorDrawable(this, inactiveResId);
        stateListMessage.addState(new int[]{-android.R.attr.state_selected}, inactive);
        return stateListMessage;
    }

    private void setupFab() {
        changeFabIconByPosition(vpSections.getCurrentItem());
    }

    private void changeFabIconByPosition(int position) {
        if (TAB_POSITION_MESSAGE == position) {
            fabAction.setImageResource(R.drawable.ic_write_message);
        } else if (TAB_POSITION_PEOPLE == position) {
            fabAction.setImageResource(R.drawable.ic_add_white_24dp);
        } else if (TAB_POSITION_PROFILE == position) {
            fabAction.setImageResource(R.drawable.ic_edit_profile);
        }
    }

    private void changeToolbarTitleByPosition(int position) {
        if (TAB_POSITION_MESSAGE == position) {
            getSupportActionBar().setTitle(R.string.main_messages);
        } else if (TAB_POSITION_PEOPLE == position) {
            getSupportActionBar().setTitle(R.string.main_contacts);
        } else if (TAB_POSITION_PROFILE == position) {
            getSupportActionBar().setTitle(R.string.main_profile);
        }
    }

    private void startFutureMessageService() {
        final Intent serviceIntent = FutureMessagesService.getIntent(this);
        startService(serviceIntent);
    }

    private void startSignalRConnection() {
        ConnectionService.tryToConnect();
    }

    @OnClick(R.id.fabAction)
    protected void onActionClick(View view) {
        MainApp.globalBus.send(new ActionClickEvent(view.getId(), vpSections.getCurrentItem()));
    }

    public static class ActionClickEvent {
        private int viewId;
        private int tabPosition;


        public ActionClickEvent(int viewId, int tabPosition) {
            this.viewId = viewId;
            this.tabPosition = tabPosition;
        }

        public int getViewId() {
            return viewId;
        }

        public int getTabPosition() {
            return tabPosition;
        }

    }

}
