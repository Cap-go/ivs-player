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
    var autoPip = false
    @PluginMethod
    fun create(call: PluginCall) {
        val intent = Intent(context, CapacitorIvsPlayer::class.java)

        startActivity(this.context, intent, null)
        // wait for the activity to be created
        Thread.sleep(100)
        sendPlayerControlBroadcast("create")
        val url = call.getString("url")
        if (url != null) {
            setUrl(url)
        }
        val autoPlay = call.getBoolean("autoPlay")
        if (autoPlay != null && autoPlay) {
            sendPlayerControlBroadcast("start")
        }
        val toBack = call.getBoolean("toBack")
        if (toBack != null && toBack) {
            bridge.webView.setBackgroundColor(0x00000000)
            activity.runOnUiThread {
                bridge.webView.parent.bringChildToFront(bridge.webView)
            }
        }
        autoPip = call.getBoolean("autoPip", false)!!
        if (autoPip) {
            sendPlayerControlBroadcast("autoUnpip")
        }
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
            if (autoPip)
                sendPlayerControlBroadcast("togglePip")
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            Log.d("CapacitorIvsPlayerX", "App went to background")
            ignoreNextPause = false
            if (autoPip) {
                sendPlayerControlBroadcast("togglePip")
                sendPlayerControlBroadcast("play")
            }

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

    private fun setUrl(url: String) {
        val intent = Intent("playerControl")
        intent.putExtra("action", "loadUrl")
        intent.putExtra("url", url)
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
   fun pause(call: PluginCall) {
       sendPlayerControlBroadcast("pause")
       call.resolve()
   }

   @PluginMethod
   fun delete(call: PluginCall) {
       sendPlayerControlBroadcast("delete")
       call.resolve()
   }
}