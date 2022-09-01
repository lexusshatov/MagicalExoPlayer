package com.potyvideo.library.newplayer.player

import android.content.Context
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource

class CacheDataSourceFactory(
    appContext: Context,
    headers: List<Header> = emptyList(),
) : DataSource.Factory {

    private val httpDataSourceFactory = DataSource.Factory {
        val dataSource = DefaultHttpDataSource.Factory().createDataSource()
        headers.forEach {
            dataSource.setRequestProperty(it.key, it.value)
        }
        return@Factory dataSource
    }

    private val defaultDataSourceFactory =
        DefaultDataSourceFactory(appContext, httpDataSourceFactory)

    private val cacheDataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
        .setUpstreamDataSourceFactory(defaultDataSourceFactory)
        .setCache(CacheProvider.Base.provide())

    override fun createDataSource() = cacheDataSourceFactory.createDataSource()
}