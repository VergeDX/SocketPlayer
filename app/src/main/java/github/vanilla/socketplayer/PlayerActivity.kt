package github.vanilla.socketplayer

import android.app.Activity
import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import github.vanilla.socketplayer.databinding.ActivityPlayerBinding

class PlayerActivity : Activity() {
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private val resId by lazy { intent.getIntExtra("resId", 0) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        UiUtils.setScreenOn(this)
        UiUtils.hideNavBar(this)

        // https://developer.android.com/training/basics/firstapp/starting-activity#DisplayMessage
        if (!ResUtils.checkResIdValid(resId, this)) finish()
    }

    private var player: SimpleExoPlayer? = null
    override fun onStart() {
        super.onStart()

        // initializePlayer()
        player = SimpleExoPlayer.Builder(this).build()
            .also { viewBinding.videoView.player = it }

            // https://stackoverflow.com/questions/27351784/how-to-implement-oncompletionlistener-to-detect-end-of-media-file-in-exoplayer
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if (playbackState == ExoPlayer.STATE_ENDED) this@PlayerActivity.finish()
                    }
                })
            }
            .apply {
                val outerThis = this@PlayerActivity
                setMediaItem(MediaItem.fromUri(ResUtils.getUriInRaw(resId, outerThis)))
            }
            .apply {
                playWhenReady = true
                seekTo(currentWindow, playbackPosition)
                prepare()
            }
    }

    override fun onPause() {
        super.onPause()

        this.onStop()
        this.finish()
    }

    private var currentWindow = 0
    private var playbackPosition = 0L
    override fun onStop() {
        super.onStop()

        player?.release()
        player = null
    }
}