package ru.demo.messenger.network;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.demo.messenger.BuildConfig;
import ru.demo.messenger.data.message.MessageDeserializer;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.user.UserDeserializer;
import ru.demo.messenger.data.user.UserModel;

public class RequestManager {
    private static Retrofit retrofit;
    private static Retrofit.Builder retrofitBuilder;
    private static Gson gson;

    public static void init(
            Context appContext,
            Interceptor policyAcceptedInterceptor,
            Interceptor requestTokenInterceptor,
            Interceptor obsoleteTokenInterceptor
    ) {
        retrofitBuilder = new Retrofit.Builder()
                .baseUrl(NetworkConst.getBaseUrl())
                .client(getHttpClient(
                        appContext,
                        policyAcceptedInterceptor,
                        requestTokenInterceptor,
                        obsoleteTokenInterceptor
                ))
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .validateEagerly(BuildConfig.DEBUG); // Fail early: check Retrofit configuration at creation time in Debug

        retrofit = retrofitBuilder.build();
    }

    @NonNull
    public static OkHttpClient getImageHttpClient(
            Interceptor requestTokenInterceptor,
            Interceptor obsoleteTokenInterceptor
    ) {
        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(requestTokenInterceptor)
                .addNetworkInterceptor(obsoleteTokenInterceptor);
        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpBuilder.addInterceptor(loggingInterceptor);
        }
        return okHttpBuilder.build();
    }

    @NonNull
    private static OkHttpClient getHttpClient(
            Context appContext,
            Interceptor policyAcceptedInterceptor,
            Interceptor requestTokenInterceptor,
            Interceptor obsoleteTokenInterceptor
    ) {
        return new OkHttpClientProvider().getHttpClient(
                appContext,
                policyAcceptedInterceptor,
                requestTokenInterceptor,
                obsoleteTokenInterceptor
        );
    }

    @NonNull
    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
//                    .registerTypeAdapter(ChatModel.class, new ChatDeserializer())
                    .registerTypeAdapter(MessageModel.class, new MessageDeserializer())
                    .registerTypeAdapter(UserModel.class, new UserDeserializer())
                    .create();
        }
        return gson;
    }

    public static void updateBaseUrl(@NonNull String newBaseUrl) {
        if (!newBaseUrl.endsWith("/")) {
            newBaseUrl = newBaseUrl.concat("/");
        }
        retrofitBuilder = retrofitBuilder.baseUrl(newBaseUrl);
        retrofit = retrofitBuilder.build();
        NetworkConst.setBaseUrl(newBaseUrl);
    }

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
