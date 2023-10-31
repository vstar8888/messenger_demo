package ru.demo.messenger.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

import biz.growapp.base.BaseAppActivity;
import butterknife.BindView;
import butterknife.OnClick;
import ru.demo.messenger.BuildConfig;
import ru.demo.messenger.R;
import ru.demo.messenger.utils.ActionUtils;

public class AboutActivity extends BaseAppActivity {

    @NonNull
    public static Intent getIntent(@NonNull Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @BindView(R.id.tvVersion) TextView tvVersion;
    @BindView(R.id.tvCopyright) TextView tvCopyright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        tvVersion.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
        final Calendar instance = Calendar.getInstance();
        tvCopyright.setText(getString(R.string.about_copyright, instance.get(Calendar.YEAR)));
    }

    @OnClick(R.id.tvSupport)
    public void reportProblem(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        ActionUtils.makeEmail(this, getString(R.string.about_report_email), null, null);
    }

    @OnClick(R.id.btnTermsOfUse)
    protected void onTermsOfUseClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        ActionUtils.showBrowserLink(this, getString(R.string.terms_of_use_url));
    }

}
