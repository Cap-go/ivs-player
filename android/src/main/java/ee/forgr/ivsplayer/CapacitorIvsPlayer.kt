package ee.forgr.ivsplayer

import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Rational
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.amazonaws.ivs.player.PlayerView

import android.app.Dialog
import android.content.res.Configuration

class FloatingWindowDialog(context: Context) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {

    public val playerView: PlayerView

    init {
        val constraintLayout = ConstraintLayout(context)
        constraintLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        playerView = PlayerView(context)
        playerView.id = View.generateViewId()
        playerView.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        }

        constraintLayout.addView(playerView)
        setContentView(constraintLayout)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window!!.attributes)
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 200
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.windowAnimations = android.R.style.Animation_Dialog

        window!!.attributes = layoutParams
    }
}

// extends with AppCompatActivity
class CapacitorIvsPlayer: AppCompatActivity() {
    private lateinit var floatingWindowDialog: FloatingWindowDialog

    var autoUnpip = false

    private val playerControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra("action")) {
                "create" -> create()
                "play" -> play()
                "autoUnpip" -> autoUnpip = true
                "pause" -> pause()
                "delete" -> delete()
                "loadUrl" -> loadUrl(intent.getStringExtra("url")!!)
                "togglePip" -> togglePip()
            }
        }
    }

    fun create() {
        floatingWindowDialog = FloatingWindowDialog(this)
        floatingWindowDialog.show()
    }

    fun play() {
        Log.i("CapacitorIvsPlayerX", "play")
        floatingWindowDialog.playerView.player.play()
    }

    fun pause() {
        Log.i("CapacitorIvsPlayerX", "pause")
        floatingWindowDialog.playerView.player.pause()
    }
    fun delete() {
        Log.i("CapacitorIvsPlayerX", "delete")
        floatingWindowDialog.playerView.player.release()
    }
    fun loadUrl(url: String) {
        Log.i("CapacitorIvsPlayerX", "loadUrl: " + url)
//        playerView.player.load(Uri.parse(url))
        floatingWindowDialog.playerView.player.load(Uri.parse(url))
    }
    @RequiresApi(Build.VERSION_CODES.O)
   override fun onUserLeaveHint() {
       super.onUserLeaveHint()
       Log.i("CapacitorIvsPlayerX", "onUserLeaveHint")
        if (autoUnpip) {
            togglePip()
        }
   }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(playerControlReceiver, IntentFilter("playerControl"))
   }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            // Hide other views and adjust player view layout when entering PiP mode
            // ...
        } else {
            // Restore the original layout when exiting PiP mode
            // ...
        }
    }


    fun togglePip() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        Log.d("CapacitorIvsPlayerX", "togglePip: " + isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            // Exit PiP mode
            val intent = Intent(this, CapacitorIvsPlayer::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        } else {
            // Enter PiP mode
            val aspectRatio = Rational(floatingWindowDialog.playerView.width, floatingWindowDialog.playerView.height)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(params)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(playerControlReceiver)
        floatingWindowDialog.playerView.player.release()
    }
}