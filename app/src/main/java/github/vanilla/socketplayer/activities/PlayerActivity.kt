package github.vanilla.socketplayer.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import github.vanilla.socketplayer.databinding.ActivityPlayerBinding
import github.vanilla.socketplayer.utils.FileUtils.getFileOrThrow
import github.vanilla.socketplayer.utils.UiUtils

class PlayerActivity : Activity() {
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private val fileName by lazy { intent.getStringExtra("fileName") }
    override fun onCreate(savedInstanceState: Bundle?) {
        kotlin.runCatching { getFileOrThrow(fileName) }.onFailure {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            // https://developer.android.com/training/basics/firstapp/starting-activity#DisplayMessage
            this.finish()
        }

        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        UiUtils.setScreenOn(this)
        // UiUtils.hideNavBar(this)
    }

    private var player: SimpleExoPlayer? = null
    override fun onStart() {
        super.onStart()

        // initializePlayer()
        player = SimpleExoPlayer.Builder(this).build()
            .also { viewBinding.videoView.player = it }
            // https://stackoverflow.com/questions/27351784/how-to-implement-oncompletionlistener-to-detect-end-of-media-file-in-exoplayer
            .apply { // this@activity.finish() after finish playing.
                addListener(object : Player.Listener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if (playbackState == ExoPlayer.STATE_ENDED) this@PlayerActivity.finish()
                    }
                })
            }
            .apply { setMediaItem(MediaItem.fromUri(Uri.fromFile(getFileOrThrow(fileName)))) }
            .apply {
                playWhenReady = true
                seekTo(currentWindow, playbackPosition)
                prepare()
            }
    }

    private var currentWindow = 0
    private var playbackPosition = 0L
    override fun onStop() {
        super.onStop()

        player?.release(); player = null
        finish()
    }
}
