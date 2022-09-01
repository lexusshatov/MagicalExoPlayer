package com.potyvideo.library.newplayer.player

import android.content.Context
import com.potyvideo.library.utils.MB
import kotlin.properties.Delegates

object MagicalExoPlayerProvider {
    internal lateinit var appContext: Context
    internal var cacheSize by Delegates.notNull<Long>()
    internal lateinit var headers: List<Header>
    internal lateinit var playerProvider: PlayerProvider


    fun init(
        appContext: Context,
        cacheSize: Long = DEFAULT_CACHE_SIZE,
        headers: List<Header> = emptyList(),
    ) {
        this.appContext = appContext
        this.cacheSize = cacheSize
        this.headers = headers
        val cacheDataSourceFactory = CacheDataSourceFactory(appContext, headers)
        playerProvider = PlayerProvider.Base(appContext, cacheDataSourceFactory)
    }

    private val DEFAULT_CACHE_SIZE = 200.MB.toLong()
}