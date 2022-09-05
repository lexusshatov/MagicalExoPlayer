package com.potyvideo.library.globalInterfaces

import android.util.Log

interface MagicalExoPlayerListener {

    fun onExoPlayerStart() {}

    fun onExoPlayerFinished() {}

    fun onExoPlayerLoading() {}

    fun onExoPlayerError(errorMessage: String?) {
        Log.e(TAG_PLAYER, errorMessage.toString())
    }

    fun onExoBuffering() {}

    fun onExoEnded() {}

    fun onExoIdle() {}

    fun onExoReady() {}

    private companion object {
        const val TAG_PLAYER = "MagicalExoPlayer"
    }

    object Default : MagicalExoPlayerListener
}