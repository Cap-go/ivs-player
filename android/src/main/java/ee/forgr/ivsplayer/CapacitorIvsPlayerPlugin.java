package ee.forgr.ivsplayer;

import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CapacitorIvsPlayer")
public class CapacitorIvsPlayerPlugin extends Plugin {

    private boolean ignoreNextPause = false;
    private boolean autoPip = false;

    @PluginMethod
    public void create(PluginCall call) {
        Log.i("CapacitorIvsPlayerX", "create");
        Intent intent = new Intent(getContext(), CapacitorIvsPlayer.class);

        ContextCompat.startActivity(getContext(), intent, null);
        // wait for the activity to be created
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendPlayerControlBroadcast("create");
        String url = call.getString("url");
        if (url != null) {
            setUrl(url);
        }
        Boolean autoPlay = call.getBoolean("autoPlay");
        if (autoPlay != null && autoPlay) {
            sendPlayerControlBroadcast("start");
        }
        Boolean toBack = call.getBoolean("toBack");
        if (toBack != null && toBack) {
            getBridge().getWebView().setBackgroundColor(0x00000000);
            getActivity().runOnUiThread(() -> getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView()));
        }
        autoPip = call.getBoolean("autoPip", false);
        if (autoPip) {
            sendPlayerControlBroadcast("autoUnpip");
        }
        call.resolve();
    }

    @Override
    public void load() {
        super.load();
    }

    private void sendPlayerControlBroadcast(String action) {
        Intent intent = new Intent("playerControl");
        intent.putExtra("action", action);
        getContext().sendBroadcast(intent);
    }

    private void setUrl(String url) {
        Intent intent = new Intent("playerControl");
        intent.putExtra("action", "loadUrl");
        intent.putExtra("url", url);
        getContext().sendBroadcast(intent);
    }

    @PluginMethod
    public void togglePip(PluginCall call) {
        ignoreNextPause = true;
        sendPlayerControlBroadcast("togglePip");
        call.resolve();
    }

    @PluginMethod
    public void start(PluginCall call) {
        sendPlayerControlBroadcast("start");
        call.resolve();
    }

    @PluginMethod
    public void pause(PluginCall call) {
        sendPlayerControlBroadcast("pause");
        call.resolve();
    }

    @PluginMethod
    public void delete(PluginCall call) {
        sendPlayerControlBroadcast("delete");
        call.resolve();
    }
}
