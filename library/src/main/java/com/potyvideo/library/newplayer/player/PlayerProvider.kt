package com.potyvideo.library.newplayer.player

import android.content.Context
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory

interface PlayerProvider {
    fun provide(): Player

    class Base(
        private val appContext: Context,
        private val cacheDataSourceFactory: CacheDataSourceFactory,
    ) : PlayerProvider {
        override fun provide() = SimpleExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build()
    }
}