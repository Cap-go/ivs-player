package ee.forgr.ivsplayer

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin


@CapacitorPlugin(name = "CapacitorIvsPlayer")
class CapacitorIvsPlayerPlugin : Plugin() {
    @PluginMethod
    fun echo(call: PluginCall) {
        val ret = JSObject()
        call.resolve(ret)
        val intent = Intent(context, CapacitorIvsPlayer::class.java)

        startActivity(this.context, intent, null)
    }

    private val lifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            Log.d("CapacitorIvsPlayerPlugin", "App returned from background")
            sendPlayerControlBroadcast("togglePip")
        }
    }


    override fun load() {
        super.load()
        bridge.activity.lifecycle.addObserver(lifecycleObserver)
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
       call.resolve()
   }

   @PluginMethod
   fun stop(call: PluginCall) {
       sendPlayerControlBroadcast("pause")
       call.resolve()
   }
}