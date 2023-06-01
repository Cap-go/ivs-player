package ee.forgr.ivsplayer

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin


@CapacitorPlugin(name = "CapacitorIvsPlayer")
class CapacitorIvsPlayerPlugin : Plugin() {
    private val implementation = CapacitorIvsPlayer()
    @PluginMethod
    fun echo(call: PluginCall) {
//        implementation
//        implementation.init(this.context)
        val ret = JSObject()
        call.resolve(ret)
        val intent = Intent(context, CapacitorIvsPlayer::class.java)

        startActivityForResult(call, intent, "verifyResult")
    }

    @PluginMethod
    fun togglePip(call: PluginCall){
        implementation.togglePip()
        call.resolve()
    }

    @PluginMethod
    fun start(call: PluginCall) {
        implementation.play()
    }

    @PluginMethod
    fun stop(call: PluginCall) {
        implementation.pause()
    }

    @PluginMethod
    fun destroy(call: PluginCall) {
        implementation.release()
    }
}