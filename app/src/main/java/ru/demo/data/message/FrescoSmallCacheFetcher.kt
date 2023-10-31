package ru.demo.data.message

import android.net.Uri
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder

class FrescoSmallCacheFetcher {

    fun requestFromUrl(imageUrl: String?): ImageRequest {
        val imageUri = imageUrl?.let { Uri.parse(it) } ?: Uri.EMPTY
        return ImageRequestBuilder.newBuilderWithSource(imageUri)
                .setCacheChoice(ImageRequest.CacheChoice.SMALL)
                .build()
    }

}