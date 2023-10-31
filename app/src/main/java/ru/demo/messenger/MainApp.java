package ru.demo.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.google.firebase.FirebaseApp;
import com.jakewharton.threetenabp.AndroidThreeTen;

import io.fabric.sdk.android.Fabric;
import kotlin.Unit;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import okhttp3.Interceptor;
import okhttp3.Response;
import ru.demo.data.network.RestApi;
import ru.demo.messenger.agreement.AgreementActivity;
import ru.demo.messenger.clearer.DataClearActivity;
import ru.demo.messenger.login.email.EmailLoginActivity;
import ru.demo.messenger.network.ObsoleteTokenInterceptor;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.RequestTokenInterceptor;
import ru.demo.messenger.utils.ConnectionUtils;
import ru.demo.messenger.utils.Prefs;
import ru.demo.messenger.utils.RxBus;

public class MainApp extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    public static final String demo_AUTHORITY = "ru.demo.projectcontentprovider";
    public static final String demo_TOKEN_PATH = "token";
    public static final Uri demo_TOKEN_URI = Uri.parse("content://" + demo_AUTHORITY + "/" + demo_TOKEN_PATH);
    public static final String demo_ID_COLUMN = "_id";
    public static final String demo_TOKEN_COLUMN = "token";

    private static final String POLICY_ACCEPTED_HEADER = "Policy-Accepted";
    private static final String VALUE_FALSE = "false";

    public static boolean appInForeground;
    public static long lastForegroundTimeStamp;

    private static MainApp instance;

    public static MainApp getInstance() {
        return instance;
    }

    public static final RxBus<Object> globalBus = new RxBus<>();

    private boolean needShowAgreement = false;
    private boolean isActivityResumed = false;
    private Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Set up Crashlytics, disabled for debug builds
        Fabric.with(this, new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.CRASHLYTICS_DISABLED).build())
                .build()
        );

        FirebaseApp.initializeApp(this);
        AndroidThreeTen.init(this);
        Prefs.init(this);

        Interceptor requestTokenInterceptor = getRequestTokenInterceptor();
        Interceptor obsoleteTokenInterceptor = getObsoleteTokenInterceptor();

        Fresco.initialize(this, OkHttpImagePipelineConfigFactory
                .newBuilder(
                        this,
                        RequestManager.getImageHttpClient(
                                requestTokenInterceptor,
                                obsoleteTokenInterceptor
                        ))
                // TODO: NR 23.08.2017  need to figure out, how to get rid of lag differently
                .setDownsampleEnabled(true)
                .build());

        RestApi.INSTANCE.init(this);
        RequestManager.init(
                this,
                getPolicyAcceptedInterceptor(),
                requestTokenInterceptor,
                obsoleteTokenInterceptor
        );
        Platform.loadPlatformComponent(new AndroidPlatformComponent());
        registerActivityLifecycleCallbacks(this);

    }

    private Interceptor getPolicyAcceptedInterceptor() {
        return chain -> {
            Response response = chain.proceed(chain.request());
            if (VALUE_FALSE.equals(response.header(POLICY_ACCEPTED_HEADER))) {
                if (isActivityResumed) {
                    startAgreementActivity(currentActivity);
                } else {
                    needShowAgreement = true;
                }
            }
            return response;
        };
    }

    private Interceptor getRequestTokenInterceptor() {
        return new RequestTokenInterceptor();
    }

    private Interceptor getObsoleteTokenInterceptor() {
        return new ObsoleteTokenInterceptor(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent intent = DataClearActivity.Companion.createIntent(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Intent intent = EmailLoginActivity.getIntent(MainApp.this);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            return Unit.INSTANCE;
        });
    }

    public static void setForegroundInfo(boolean inForeground) {
        appInForeground = inForeground;
        lastForegroundTimeStamp = System.currentTimeMillis();
    }

    public static void setConnectionInfo(Context context) {
        new AsyncTask<Context, Void, Void>() {
            boolean connected;

            @Override
            protected Void doInBackground(Context... params) {
                connected = ConnectionUtils.isConnected(params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
//                if (currentActivity instanceof ChatsActivity) {
//                    ((ChatsActivity) currentActivity).configConnectionLabel(connected);
//                } else if (currentActivity instanceof SingleChatActivity) {
//                    ((SingleChatActivity) currentActivity).configConnectionLabel(connected);
//                }
            }
        }.execute(context);

    }

    private void startAgreementActivity(Activity activity) {
        activity.startActivity(AgreementActivity.forUpdatedAgreement(this, false));
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        isActivityResumed = true;
        if (needShowAgreement) {
            needShowAgreement = false;
            startAgreementActivity(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        currentActivity = null;
        isActivityResumed = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
