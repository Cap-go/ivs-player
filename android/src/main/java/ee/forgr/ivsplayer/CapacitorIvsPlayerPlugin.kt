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

    var ignoreNextPause = false
    @PluginMethod
    fun create(call: PluginCall) {
        val intent = Intent(context, CapacitorIvsPlayer::class.java)

        startActivity(this.context, intent, null)
        call.resolve()
    }

    private val lifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            Log.d("CapacitorIvsPlayerX", "App returned from background")
            if (ignoreNextPause) {
                ignoreNextPause = false
                return
            }
            sendPlayerControlBroadcast("togglePip")
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            Log.d("CapacitorIvsPlayerX", "App went to background")
            ignoreNextPause = false
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
       ignoreNextPause = true
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