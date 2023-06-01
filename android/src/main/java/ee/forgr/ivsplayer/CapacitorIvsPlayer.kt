package ee.forgr.ivsplayer

import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.WindowManager
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

    fun togglePip(){
        Log.i("PIP", "PIP")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && this.packageManager
                        .hasSystemFeature(
                                PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                this.enterPictureInPictureMode(params.build());
            } else {
                this.enterPictureInPictureMode();
            }

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(playerControlReceiver)
        playerView.player.release()
    }
}