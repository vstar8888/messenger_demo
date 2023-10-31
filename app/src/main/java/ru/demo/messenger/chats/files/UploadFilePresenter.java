package ru.demo.messenger.chats.files;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.FileResponse;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.FileService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class UploadFilePresenter extends BasePresenter<UploadFilePresenter.View> {
    private static MediaType MEDIA_TYPE = MediaType.parse("multipart/form-data");

    public interface View extends BaseLoadingView {
        void onFilesUploaded(List<String> filesIds);
        void onFilesUploadError(String message);
    }

    private final FileService fileService;

    public UploadFilePresenter(@NonNull View view) {
        super(view);
        fileService = RequestManager.createService(FileService.class);
    }

    public void uploadFiles(List<String> filePaths) {
        Observable.from(filePaths)
                .map(File::new)
                .map(file -> MultipartBody.Part.createFormData("attach",
                        file.getName(),
                        RequestBody.create(MEDIA_TYPE, file)))
                .concatMap(fileService::uploadFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<FileResponse>() {
                    private final List<String> files = new ArrayList<>(filePaths.size());

                    @Override
                    public void onCompleted() {
                        getView().onFilesUploaded(files);
                    }

                    @Override
                    public void onNext(FileResponse result) {
                        files.add(result.file_info.id);
                    }

                    @Override
                    public void onError(ServerError error) {
                        unsubscribe();
                        getView().onFilesUploadError(error.getMessage());
                    }
                });
    }

}
