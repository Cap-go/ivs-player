package ee.forgr.ivsplayer

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.ivs.player.PlayerView

// extends with AppCompatActivity
class CapacitorIvsPlayer: AppCompatActivity() {
    private lateinit var playerView : PlayerView

    // on create method
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        this.playerView = PlayerView(this)
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
    fun play(){
        playerView.player.play()
    }
    fun pause(){
        playerView.player.pause()
    }
    fun release(){
        playerView.player.release()
    }
}