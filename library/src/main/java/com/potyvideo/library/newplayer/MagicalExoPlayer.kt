package com.potyvideo.library.newplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.toRectF
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.util.MimeTypes
import com.potyvideo.library.R
import com.potyvideo.library.VideoActivity
import com.potyvideo.library.databinding.LayoutPlayerBaseKotlinBinding
import com.potyvideo.library.globalEnums.*
import com.potyvideo.library.globalInterfaces.AndExoPlayerListener
import com.potyvideo.library.newplayer.player.MagicalExoPlayerProvider
import com.potyvideo.library.newplayer.player.Shape
import com.potyvideo.library.utils.*
import kotlin.math.min

/**
 * Copied and modified from
 * https://github.com/HamidrezaAmz/MagicalExoPlayer
 */
class MagicalExoPlayer @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attributeSet, defStyle), Player.Listener {

    private var binding: LayoutPlayerBaseKotlinBinding = LayoutPlayerBaseKotlinBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var backwardView: AppCompatButton =
        binding.playerView.findViewById(R.id.exo_backward)
    private var forwardView: AppCompatButton =
        binding.playerView.findViewById(R.id.exo_forward)
    private var mute: AppCompatImageButton =
        binding.playerView.findViewById(R.id.exo_mute)
    private var unMute: AppCompatImageButton =
        binding.playerView.findViewById(R.id.exo_unmute)
    private var settingContainer: FrameLayout =
        binding.playerView.findViewById(R.id.container_setting)
    private var fullScreenContainer: FrameLayout =
        binding.playerView.findViewById(R.id.container_fullscreen)
    private var enterFullScreen: AppCompatImageButton =
        binding.playerView.findViewById(R.id.exo_enter_fullscreen)
    private var exitFullScreen: AppCompatImageButton =
        binding.playerView.findViewById(R.id.exo_exit_fullscreen)

    var currAspectRatio: EnumAspectRatio = EnumAspectRatio.ASPECT_16_9
    var currRepeatMode: EnumRepeatMode = EnumRepeatMode.REPEAT_OFF
    var currPlayerSize: EnumPlayerSize = EnumPlayerSize.EXACTLY
    var currResizeMode: EnumResizeMode = EnumResizeMode.FILL
    var currMute: EnumMute = EnumMute.UNMUTE
    var currPlaybackSpeed: EnumPlaybackSpeed = EnumPlaybackSpeed.NORMAL
    var currScreenMode: EnumScreenMode = EnumScreenMode.MINIMISE

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        listOf(
            binding.retryView.buttonTryAgain,
            backwardView,
            forwardView,
            mute,
            unMute,
            fullScreenContainer,
            enterFullScreen,
            exitFullScreen
        ).forEach { it.setOnClickListener(customClickListener) }
    }

    private fun showRetryView() {
        showRetryView(null)
    }

    private fun showRetryView(retryTitle: String?) {
        binding.retryView.root.visibility = VISIBLE
        if (retryTitle != null)
            binding.retryView.textViewRetryTitle.text = retryTitle
    }

    private fun hideRetryView() {
        binding.retryView.root.visibility = GONE
    }

    private fun showLoading() {
        hideRetryView()
    }

    private fun hideLoading() {
        hideRetryView()
    }

    private fun setShowController(showController: Boolean = true) {
        if (showController)
            showController()
        else
            hideController()
    }

    private fun showController() {
        binding.playerView.showController()
    }

    private fun hideController() {
        binding.playerView.hideController()
    }

    private fun showUnMuteButton() {
        mute.visibility = GONE
        unMute.visibility = VISIBLE
    }

    private fun showMuteButton() {
        mute.visibility = VISIBLE
        unMute.visibility = GONE
    }

    private fun setShowSettingButton(showSetting: Boolean = false) {
        if (showSetting)
            settingContainer.visibility = VISIBLE
        else
            settingContainer.visibility = GONE
    }

    private fun setShowFullScreenButton(showFullscreenButton: Boolean = false) {
        if (showFullscreenButton)
            fullScreenContainer.visibility = VISIBLE
        else
            fullScreenContainer.visibility = GONE
    }

    private fun setShowScreenModeButton(screenMode: EnumScreenMode = EnumScreenMode.MINIMISE) {
        when (screenMode) {
            EnumScreenMode.FULLSCREEN -> {
                enterFullScreen.visibility = GONE
                exitFullScreen.visibility = VISIBLE
            }
            EnumScreenMode.MINIMISE -> {
                enterFullScreen.visibility = VISIBLE
                exitFullScreen.visibility = GONE
            }
            else -> {
                enterFullScreen.visibility = VISIBLE
                exitFullScreen.visibility = GONE
            }
        }
    }

    private fun showSystemUI() {
        binding.playerView.systemUiVisibility = (SYSTEM_UI_FLAG_LOW_PROFILE
                or SYSTEM_UI_FLAG_IMMERSIVE
                or SYSTEM_UI_FLAG_FULLSCREEN
                or SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun hideSystemUI() {
        binding.playerView.systemUiVisibility = (SYSTEM_UI_FLAG_LOW_PROFILE
                or SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    private var currSource: String? = null

    private val player: Player = MagicalExoPlayerProvider.playerProvider.provide()

    private var andExoPlayerListener: AndExoPlayerListener? = null
    private var currPlayWhenReady: Boolean = true
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var currVolume: Float = 0f

    var shape: Shape = Shape.RECTANGLE
        set(value) {
            field = value
            invalidate()
        }

    override fun dispatchDraw(canvas: Canvas) {
        val rect = canvas.clipBounds.toRectF()
        val path = Path()
        when (shape) {
            Shape.CIRCLE -> path.addCircle(
                rect.centerX(),
                rect.centerY(),
                min(rect.width(), rect.height()) / 2,
                Path.Direction.CW
            )
            Shape.RECTANGLE -> path.addRect(rect, Path.Direction.CW)
        }
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
    }

    private var customClickListener = DoubleClick(object : DoubleClickListener {
        override fun onSingleClickEvent(view: View) {
            when (view) {
                binding.retryView.buttonTryAgain -> {
                    hideRetryView()
                    restartPlayer()
                }
                mute -> setMute(EnumMute.UNMUTE)
                unMute -> setMute(EnumMute.MUTE)
                enterFullScreen -> setScreenMode(EnumScreenMode.FULLSCREEN)
                exitFullScreen -> setScreenMode(EnumScreenMode.MINIMISE)
            }
        }

        override fun onDoubleClickEvent(view: View) {
            when (view) {
                backwardView -> seekBackward()
                forwardView -> seekForward()
            }
        }
    })

    init {
        player.addListener(this)
        extractAttrs(attributeSet)
        listOf(
            binding.retryView.buttonTryAgain,
            mute,
            unMute,
            enterFullScreen,
            exitFullScreen,
            backwardView,
            forwardView
        ).forEach { it.setOnClickListener(customClickListener) }
        rootView.setOnClickListener {
            currSource?.let { VideoActivity.start(context, it) }
        }
    }

    private fun extractAttrs(attributeSet: AttributeSet?) {

        attributeSet.let {

            val typedArray: TypedArray =
                context.obtainStyledAttributes(it, R.styleable.MagicalExoPlayer)

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_andexo_aspect_ratio)) {
                val aspectRatio = typedArray.getInteger(
                    R.styleable.MagicalExoPlayer_andexo_aspect_ratio,
                    EnumAspectRatio.ASPECT_16_9.value
                )
                setAspectRatio(EnumAspectRatio[aspectRatio])
            }

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_andexo_resize_mode)) {
                val resizeMode: Int = typedArray.getInteger(
                    R.styleable.MagicalExoPlayer_andexo_resize_mode,
                    EnumResizeMode.FILL.value
                )
                setResizeMode(EnumResizeMode[resizeMode])
            }

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_andexo_play_when_ready)) {
                setPlayWhenReady(
                    typedArray.getBoolean(
                        R.styleable.MagicalExoPlayer_andexo_play_when_ready,
                        true
                    )
                )
            }

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_andexo_mute)) {
                val muteValue = typedArray.getInteger(
                    R.styleable.MagicalExoPlayer_andexo_mute,
                    EnumMute.UNMUTE.value
                )
                setMute(EnumMute[muteValue])
            }

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_andexo_show_controller)) {
                setShowControllers(
                    typedArray.getBoolean(
                        R.styleable.MagicalExoPlayer_andexo_show_controller,
                        true
                    )
                )
            }

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_andexo_show_full_screen)) {
                setShowFullScreenButton(
                    typedArray.getBoolean(
                        R.styleable.MagicalExoPlayer_andexo_show_full_screen,
                        true
                    )
                )
            }

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_andexo_loop)) {
                val loop = typedArray.getInt(
                    R.styleable.MagicalExoPlayer_andexo_loop,
                    EnumRepeatMode.REPEAT_OFF.value
                )
                setRepeatMode(EnumRepeatMode[loop])
            }

            if (typedArray.hasValue(R.styleable.MagicalExoPlayer_shape)) {
                val shapeInt = typedArray.getInt(
                    R.styleable.MagicalExoPlayer_shape,
                    Shape.RECTANGLE.ordinal
                )
                this.shape = Shape.values()[shapeInt]
            }

            typedArray.recycle()
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
    }

    override fun onPlayerError(error: PlaybackException) {
        showRetryView(error.message)
        andExoPlayerListener?.let {
            andExoPlayerListener!!.onExoPlayerError(errorMessage = error.message)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> andExoPlayerListener?.onExoBuffering()
            ExoPlayer.STATE_ENDED -> andExoPlayerListener?.onExoEnded()
            ExoPlayer.STATE_IDLE -> andExoPlayerListener?.onExoIdle()
            ExoPlayer.STATE_READY -> andExoPlayerListener?.onExoReady()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onLoadingChanged(isLoading: Boolean) {
    }

    @Deprecated("Deprecated in Java")
    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
    }

    private fun releasePlayer() {
        currPlayWhenReady = player.playWhenReady
        playbackPosition = player.currentPosition
        currentWindow = player.currentWindowIndex
        player.stop()
        player.release()
    }

    private fun restartPlayer() {
        player.seekTo(0)
        player.prepare()
    }

    private fun buildMediaItem(source: String, extraHeaders: Map<String, String>): MediaItem {

        return when (PublicFunctions.getMimeType(source)) {

            PublicValues.KEY_MP4 -> buildMediaItemMP4(source, extraHeaders)
            PublicValues.KEY_M3U8 -> buildMediaHLS(source, extraHeaders)
            PublicValues.KEY_MP3 -> buildMediaItemMP4(source, extraHeaders)
            PublicValues.KEY_MDP -> buildMediaDash(source, extraHeaders)
            else -> buildMediaGlobal(source, extraHeaders)
        }
    }

    private fun buildMediaItemMP4(
        source: String,
        extraHeaders: Map<String, String>,
    ): MediaItem {
        return MediaItem.Builder()
            .setUri(source)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .setDrmLicenseRequestHeaders(extraHeaders)
            .build()
    }

    private fun buildMediaHLS(source: String, extraHeaders: Map<String, String>): MediaItem {
        return MediaItem.Builder()
            .setUri(source)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .setDrmLicenseRequestHeaders(extraHeaders)
            .build()
    }

    private fun buildMediaDash(source: String, extraHeaders: Map<String, String>): MediaItem {
        return MediaItem.Builder()
            .setUri(source)
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .setDrmLicenseRequestHeaders(extraHeaders)
            .build()
    }

    private fun buildMediaGlobal(source: String, extraHeaders: Map<String, String>): MediaItem {
        return MediaItem.Builder()
            .setUri(source)
            .setDrmLicenseRequestHeaders(extraHeaders)
            .build()
    }

    fun setAndExoPlayerListener(andExoPlayerListener: AndExoPlayerListener) {
        this.andExoPlayerListener = andExoPlayerListener
    }

    fun setSource(
        source: String,
        extraHeaders: Map<String, String> = mapOf(),
    ) {
        val fixedSource = source.replace("http:", "https:")
        Log.d("ExoPlayer", "Load file: $fixedSource")
        currSource = fixedSource

        val mediaItem = buildMediaItem(fixedSource, extraHeaders)

        binding.playerView.player = player
        player.playWhenReady = currPlayWhenReady
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun seekBackward(backwardValue: Int = 10000) {
        var seekValue = player.currentPosition - backwardValue
        if (seekValue < 0) seekValue = 0
        player.seekTo(seekValue)
    }

    fun seekForward(ForwardValue: Int = 10000) {
        var seekValue = player.currentPosition + ForwardValue
        if (seekValue > player.duration) seekValue = player.duration
        player.seekTo(seekValue)
    }

    fun setShowControllers(showControllers: Boolean = true) {
        setShowTimeOut(if (showControllers) 4000 else 0)
    }

    fun setShowTimeOut(showTimeoutMs: Int) {
        binding.playerView.controllerShowTimeoutMs = showTimeoutMs
        if (showTimeoutMs == 0) {
            binding.playerView.useController = false
            binding.playerView.controllerHideOnTouch = false
        }
    }

    private fun setMute(mute: EnumMute) {
        when (mute) {
            EnumMute.MUTE -> mutePlayer()
            EnumMute.UNMUTE -> unMutePlayer()
            EnumMute.UNDEFINE -> unMutePlayer()
        }
    }

    fun mutePlayer() {
        currVolume = player.volume
        player.volume = 0f
        showMuteButton()
    }

    fun unMutePlayer() {
        player.volume = currVolume
        showUnMuteButton()
    }

    fun setRepeatMode(repeatMode: EnumRepeatMode) {
        this.currRepeatMode = repeatMode
        player.repeatMode = when (repeatMode) {
            EnumRepeatMode.REPEAT_OFF, EnumRepeatMode.UNDEFINE, EnumRepeatMode.Finite -> Player.REPEAT_MODE_OFF
            EnumRepeatMode.REPEAT_ONE, EnumRepeatMode.INFINITE -> Player.REPEAT_MODE_ONE
            EnumRepeatMode.REPEAT_ALWAYS -> Player.REPEAT_MODE_ALL
        }
    }

    fun setAspectRatio(aspectRatio: EnumAspectRatio) {
        this.currAspectRatio = aspectRatio
        val value = PublicFunctions.getScreenWidth()
        when (aspectRatio) {
            EnumAspectRatio.ASPECT_1_1 -> binding.playerView.layoutParams =
                FrameLayout.LayoutParams(value, value)
            EnumAspectRatio.ASPECT_4_3 -> binding.playerView.layoutParams =
                FrameLayout.LayoutParams(value, 3 * value / 4)
            EnumAspectRatio.ASPECT_16_9 -> binding.playerView.layoutParams =
                FrameLayout.LayoutParams(value, 9 * value / 16)
            EnumAspectRatio.ASPECT_MATCH -> binding.playerView.layoutParams =
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            EnumAspectRatio.ASPECT_MP3 -> {
                binding.playerView.controllerShowTimeoutMs = 0
                binding.playerView.controllerHideOnTouch = false
                val mp3Height =
                    context.resources.getDimensionPixelSize(R.dimen.player_controller_base_height)
                binding.playerView.layoutParams =
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mp3Height)
            }
            EnumAspectRatio.UNDEFINE -> {
                val baseHeight = resources.getDimension(R.dimen.player_base_height).toInt()
                binding.playerView.layoutParams =
                    FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, baseHeight)
            }
        }
    }

    fun setResizeMode(resizeMode: EnumResizeMode) {
        when (resizeMode) {
            EnumResizeMode.FIT -> binding.playerView.resizeMode =
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            EnumResizeMode.FILL -> binding.playerView.resizeMode =
                AspectRatioFrameLayout.RESIZE_MODE_FILL
            EnumResizeMode.ZOOM -> binding.playerView.resizeMode =
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            else -> binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    fun setPlayWhenReady(playWhenReady: Boolean = true) {
        this.currPlayWhenReady = playWhenReady
        player.playWhenReady = playWhenReady
    }

    fun pausePlayer() {
        player.playWhenReady = false
        player.playbackState
    }

    fun stopPlayer() {
        player.stop()
        player.playbackState
    }

    fun startPlayer() {
        player.playWhenReady = true
        player.playbackState
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun setScreenMode(screenMode: EnumScreenMode = EnumScreenMode.MINIMISE) {

        when (screenMode) {
            EnumScreenMode.FULLSCREEN -> {
                context.getActivity()?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            EnumScreenMode.MINIMISE -> {
                context.getActivity()?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            else -> {
                context.getActivity()?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        currScreenMode = screenMode
        setShowScreenModeButton(currScreenMode)
    }

    fun getCurrentPlayerPosition(): Long {
        var currentPosition = 0L
        if (player.isPlaying)
            currentPosition = player.currentPosition
        return currentPosition
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releasePlayer()
        listOf(
            binding.retryView.buttonTryAgain,
            backwardView,
            forwardView,
            mute,
            unMute,
            fullScreenContainer,
            enterFullScreen,
            exitFullScreen
        ).forEach { it.setOnClickListener(null) }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        // Checking the orientation of the screen
        // Checking the orientation of the screen
        if (newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // First Hide other objects (list-view or recyclerview), better hide them using Gone.
            hideSystemUI()
            val params = binding.playerView.layoutParams as FrameLayout.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.playerView.layoutParams = params
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // un hide your objects here.
            showSystemUI()
            setAspectRatio(currAspectRatio)
        }
    }

}