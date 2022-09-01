package com.potyvideo.library.newplayer.player

import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

interface CacheProvider {
    fun provide(): Cache

    object Base : CacheProvider {
        private val file = File(MagicalExoPlayerProvider.appContext.cacheDir, "video")
        private val databaseProvider = ExoDatabaseProvider(MagicalExoPlayerProvider.appContext)
        private val cache = SimpleCache(
            file,
            LeastRecentlyUsedCacheEvictor(MagicalExoPlayerProvider.cacheSize),
            databaseProvider
        )

        override fun provide(): Cache = cache
    }
}