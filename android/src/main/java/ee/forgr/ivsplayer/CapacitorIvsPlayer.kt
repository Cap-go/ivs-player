package ee.forgr.ivsplayer

import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.amazonaws.ivs.player.PlayerView

// extends with AppCompatActivity
class CapacitorIvsPlayer: AppCompatActivity() {
    private lateinit var playerView : PlayerView

    private val playerControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra("action")) {
                "create" -> create()
                "play" -> play()
                "pause" -> pause()
                "delete" -> delete()
                "loadUrl" -> loadUrl(intent.getStringExtra("url")!!)
                "togglePip" -> togglePip()
            }
        }
    }

    fun create() {
        playerView.player.play()
    }

    fun play() {
        playerView.player.play()
    }

    fun pause() {
        playerView.player.pause()
    }
    fun delete() {
        playerView.player.release()
    }
    fun loadUrl(url: String) {
        Log.i("CapacitorIvsPlayerX", "loadUrl: " + url)
        playerView.player.load(Uri.parse(url))
    }
    @RequiresApi(Build.VERSION_CODES.O)
   override fun onUserLeaveHint() {
       super.onUserLeaveHint()
       Log.i("CapacitorIvsPlayerX", "onUserLeaveHint")
       togglePip()
   }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(playerControlReceiver, IntentFilter("playerControl"))
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)

        val constraintLayout = ConstraintLayout(this)
        constraintLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        playerView = PlayerView(this)
        playerView.id = View.generateViewId()
        playerView.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        }
        
        constraintLayout.addView(playerView)
        setContentView(constraintLayout)
        
//        this.playerView.player.play()
//        playerView.player.load(Uri.parse("https://fcc3ddae59ed.us-west-2.playback.live-video.net/api/video/v1/us-west-2.893648527354.channel.DmumNckWFTqz.m3u8"))
    }



    fun togglePip() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        Log.d("CapacitorIvsPlayerX", "togglePip: " + isInPictureInPictureMode)
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