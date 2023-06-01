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

   @PluginMethod
   fun togglePip(call: PluginCall){
       implementation.togglePip()
       call.resolve()
   }
//
//    @PluginMethod
//    fun start(call: PluginCall) {
//        implementation.play()
//    }
//
//    @PluginMethod
//    fun stop(call: PluginCall) {
//        implementation.pause()
//    }
//
//    @PluginMethod
//    fun destroy(call: PluginCall) {
//        implementation.release()
//    }
}