package com.potyvideo.library

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.potyvideo.library.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoBinding

    private val videoPath: String by lazy {
        intent.getStringExtra(EXTRA_URL)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.close.setOnClickListener { finish() }
        binding.player.setSource(videoPath)
    }

    companion object {
        private const val EXTRA_URL = "url"

        fun start(context: Context, url: String) {
            val intent = Intent(context, VideoActivity::class.java)
                .putExtra(EXTRA_URL, url)
            context.startActivity(intent)
        }
    }
}