package com.belajar.vigilanter.thumbnail

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.AsyncTask

class ThumbnailExtractor {

    interface ThumbnailCallback {
        fun onThumbnailReady(thumbnail: Bitmap?)
    }

    companion object {
        fun getThumbnail(videoUrl: String, callback: ThumbnailCallback) {
            object : AsyncTask<String, Void, Bitmap?>() {
                override fun doInBackground(vararg urls: String): Bitmap? {
                    val url = urls[0]
                    val retriever = MediaMetadataRetriever()
                    return try {
                        retriever.setDataSource(url, mutableMapOf())
                        retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    } finally {
                        retriever.release()
                    }
                }

                override fun onPostExecute(bitmap: Bitmap?) {
                    callback.onThumbnailReady(bitmap)
                }
            }.execute(videoUrl)
        }
    }
}