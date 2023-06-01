package ee.forgr.ivsplayer

import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
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

        startActivity(this.context, intent, null)
    }

    private fun sendPlayerControlBroadcast(action: String) {
        val intent = Intent("playerControl")
        intent.putExtra("action", action)
        context.sendBroadcast(intent)
    }

   @PluginMethod
   fun togglePip(call: PluginCall){
       sendPlayerControlBroadcast("togglePip")
       call.resolve()
   }
   @PluginMethod
   fun start(call: PluginCall) {
       sendPlayerControlBroadcast("start")
       implementation.play()
   }

   @PluginMethod
   fun stop(call: PluginCall) {
       sendPlayerControlBroadcast("pause")
       implementation.pause()
   }
}