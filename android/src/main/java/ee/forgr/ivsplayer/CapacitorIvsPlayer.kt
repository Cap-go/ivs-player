package ee.forgr.ivsplayer

import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Rational
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.ivs.player.PlayerView

// extends with AppCompatActivity
class CapacitorIvsPlayer: AppCompatActivity() {
    private lateinit var playerView : PlayerView

    private val playerControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra("action")) {
                "play" -> playerView.player.play()
                "pause" -> playerView.player.pause()
                "togglePip" -> togglePip()
            }
        }
    }
   @RequiresApi(Build.VERSION_CODES.O)
   override fun onUserLeaveHint() {
       super.onUserLeaveHint()
       togglePip()
   }

    // on create method
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(playerControlReceiver, IntentFilter("playerControl"))
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)

        this.playerView = PlayerView(this)
        this.playerView.player.play()

        playerView.player.load(Uri.parse("https://fcc3ddae59ed.us-west-2.playback.live-video.net/api/video/v1/us-west-2.893648527354.channel.DmumNckWFTqz.m3u8"))
        setContentView(playerView)

    }

    fun togglePip() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        if (isInPictureInPictureMode) {
            // Exit PiP mode and return to floating mode
            val intent = Intent(this, CapacitorIvsPlayer::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        } else {
            // Enter PiP mode
            val aspectRatio = Rational(playerView.width, playerView.height)
            val pipParams = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()

            enterPictureInPictureMode(pipParams)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(playerControlReceiver)
        playerView.player.release()
    }
}