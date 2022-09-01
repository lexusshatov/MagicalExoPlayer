package com.potyvideo.andexoplayer

import android.app.Application
import com.potyvideo.library.newplayer.player.MagicalExoPlayerProvider

class MagicalExoPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MagicalExoPlayerProvider.init(this)
    }
}