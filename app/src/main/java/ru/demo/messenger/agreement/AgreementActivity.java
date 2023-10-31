package ru.demo.messenger.agreement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import biz.growapp.base.loading.BaseAppLoadingActivity;
import butterknife.BindView;
import butterknife.OnClick;
import ru.demo.messenger.R;
import ru.demo.messenger.login.signup.SignUpActivity;
import ru.demo.messenger.utils.VectorUtils;

public class AgreementActivity extends BaseAppLoadingActivity
        implements AgreementPresenter.View {

    private static final String EXTRA_PHONE_NUMBER = "extra.PHONE_NUMBER";
    private static final String EXTRA_NEXT_STEP_URL = "extra.NEXT_STEP_URL";
    private static final String EXTRA_CAN_GO_BACK = "extra.CAN_GO_BACK";

    @BindView(R.id.vgMain) ViewGroup rootView;
    @BindView(R.id.svAgreement) ScrollView svAgreement;
    @BindView(R.id.btnAccept) Button btnAccept;
    @BindView(R.id.tvAgreementTitle) TextView tvAgreementTitle;
    @BindView(R.id.tvAgreement) TextView tvAgreement;

    private AgreementPresenter presenter;
    private boolean canGoBack;

    public static Intent forRegister(Context context, String phoneNumber, String url, boolean canGoBack) {
        return new Intent(context, AgreementActivity.class)
                .putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                .putExtra(EXTRA_NEXT_STEP_URL, url)
                .putExtra(EXTRA_CAN_GO_BACK, canGoBack);
    }

    public static Intent forUpdatedAgreement(Context context, boolean canGoBack) {
        return new Intent(context, AgreementActivity.class)
                .putExtra(EXTRA_CAN_GO_BACK, canGoBack);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        setOnScrollListener();
        canGoBack = getIntent().getBooleanExtra(EXTRA_CAN_GO_BACK, false);
        prepareNavigationIcon();
        presenter = new AgreementPresenter(this,
                getIntent().getStringExtra(EXTRA_NEXT_STEP_URL),
                getIntent().getStringExtra(EXTRA_PHONE_NUMBER)
        );
        presenter.getAgreementText();
    }

    private void prepareNavigationIcon() {
        if (canGoBack) {
            if (toolbar != null) {
                toolbar.setNavigationIcon(VectorUtils.getVectorDrawable(this, R.drawable.ic_close));
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void showAgreementText(String title, String content) {
        tvAgreementTitle.setText(title);
        tvAgreement.setText(content);
        // small delay to measure view's height
        tvAgreement.postDelayed(this::setAcceptButtonVisibility, 50);
    }

    private void setOnScrollListener() {
        svAgreement.getViewTreeObserver().addOnScrollChangedListener(() -> {
            // show button when ScrollView hits bottom
            if (svAgreement.getScrollY() + svAgreement.getHeight() >= tvAgreement.getHeight()) {
                btnAccept.setVisibility(View.VISIBLE);
            }
        });
    }

    // show button if TextView's content doesn't fit full screen
    private void setAcceptButtonVisibility() {
        if (tvAgreement.getHeight() <= svAgreement.getHeight()) {
            btnAccept.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void closeAgreement() {
        finish();
    }

    @Override
    public void signUp(String phoneNumber, String signUpUrl) {
        startActivity(SignUpActivity.getIntent(this, phoneNumber, signUpUrl, true));
        switchToMain(true);
    }

    @OnClick(R.id.btnAccept)
    protected void onAcceptClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        presenter.acceptAgreement();
    }

    @Override
    public int getMainContainerId() {
        return R.id.vgMain;
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToLoading(true);
        presenter.getAgreementText();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroyView();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (canGoBack) {
            super.onBackPressed();
        } else {
            moveTaskToBack(true);
        }
    }

}
