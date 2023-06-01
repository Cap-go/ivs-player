package ee.forgr.ivsplayer

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.amazonaws.ivs.player.*
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin


@CapacitorPlugin(name = "CapacitorIvsPlayer")
class CapacitorIvsPlayerPlugin : Plugin() {
    private val implementation = CapacitorIvsPlayer()
    private lateinit var playerView : PlayerView
    @PluginMethod
    fun echo(call: PluginCall) {
        val value = call.getString("value")
        val ret = JSObject()
        ret.put("value", implementation.echo(value!!))
        call.resolve(ret)

        val playerView = PlayerView(this.activity)
        playerView.player.load(Uri.parse("https://fcc3ddae59ed.us-west-2.playback.live-video.net/api/video/v1/us-west-2.893648527354.channel.DmumNckWFTqz.m3u8"))

    }

    @PluginMethod
    fun togglePip(call: PluginCall){
        Log.i("PIP", "PIP")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && context.packageManager
                        .hasSystemFeature(
                                PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            val activity: Activity? = this.activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                activity?.enterPictureInPictureMode(params.build());
            } else {
                activity?.enterPictureInPictureMode();
            }

        }
    }

    @PluginMethod
    fun start(call: PluginCall) {
        playerView.player.play()
    }

    @PluginMethod
    fun stop(call: PluginCall) {
        playerView.player.pause()
    }

    @PluginMethod
    fun destroy(call: PluginCall) {
        playerView.player.release()
    }
}