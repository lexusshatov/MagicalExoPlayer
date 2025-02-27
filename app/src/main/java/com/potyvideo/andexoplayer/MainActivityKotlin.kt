package com.potyvideo.andexoplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.potyvideo.library.globalEnums.EnumResizeMode
import com.potyvideo.library.globalInterfaces.MagicalExoPlayerListener
import com.potyvideo.library.newplayer.MagicalExoPlayer
import com.potyvideo.library.utils.PathUtil
import com.potyvideo.library.utils.PublicFunctions
import com.potyvideo.library.utils.PublicValues
import java.net.URISyntaxException

class MainActivityKotlin : AppCompatActivity(), MagicalExoPlayerListener, View.OnClickListener {

    private lateinit var magicalExoPlayer: MagicalExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_kotlin)

        magicalExoPlayer = findViewById(R.id.andExoPlayerView)

        magicalExoPlayer.setResizeMode(EnumResizeMode.ZOOM) // sync with attrs
        magicalExoPlayer.setAndExoPlayerListener(this)
        magicalExoPlayer.setPlayWhenReady(true)

        findViewById<AppCompatButton>(R.id.local).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.stream_mp4).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.stream_hls).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.stream_dash).setOnClickListener(this)
        findViewById<AppCompatButton>(R.id.stream_mkv).setOnClickListener(this)

        // starter stream
        loadMP4Stream(PublicValues.TEST_URL_MP4_V3)


        magicalExoPlayer.setSource("video.mp4")
    }

    override fun onExoPlayerError(errorMessage: String?) {
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.local -> {
                selectLocaleVideo()
            }
            R.id.stream_mp4 -> {
                loadMP4Stream(PublicValues.TEST_URL_MP4_V3)
            }
            R.id.stream_hls -> {
                loadHLSStream(PublicValues.TEST_URL_HLS)
            }
            R.id.stream_dash -> {
                loadHLSStream(PublicValues.TEST_URL_DASH)
            }
            R.id.stream_mkv -> {
                Toast.makeText(this, "Test Link Not Available", Toast.LENGTH_SHORT).show()
            }
            R.id.ogg -> {
                Toast.makeText(this, "Test Link Not Available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PublicValues.request_code_select_video && resultCode == RESULT_OK) {
            val finalVideoUri = data?.data
            var filePath: String? = null
            try {
                filePath = PathUtil.getPath(this, finalVideoUri)
                filePath?.let {
                    loadMP4Locale(filePath)
                }
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectLocaleVideo() {
        if (PublicFunctions.checkAccessStoragePermission(this)) {
            val intent = Intent()
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Video"),
                PublicValues.request_code_select_video
            )
        }
    }

    private fun loadMP4Locale(filePath: String) {
        magicalExoPlayer.setSource(filePath)
    }

    private fun loadMP4Stream(urlMP4: String) {
        magicalExoPlayer.setSource(urlMP4)
    }

    private fun loadHLSStream(urlHLS: String) {
        magicalExoPlayer.setSource(urlHLS)
    }

}