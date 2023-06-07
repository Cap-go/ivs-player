package ee.forgr.ivsplayer;

import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Rational;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


public class CapacitorIvsPlayer extends AppCompatActivity {
    private FloatingWindowDialog floatingWindowDialog;

    boolean autoUnpip = false;

    private final BroadcastReceiver playerControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if (action != null) {
                switch (action) {
                    case "create":
                        create();
                        break;
                    case "play":
                        play();
                        break;
                    case "autoUnpip":
                        autoUnpip = true;
                        break;
                    case "pause":
                        pause();
                        break;
                    case "delete":
                        delete();
                        break;
                    case "loadUrl":
                        loadUrl(intent.getStringExtra("url"));
                        break;
                    case "togglePip":
                        togglePip();
                        break;
                }
            }
        }
    };

    public void create() {
        floatingWindowDialog = new FloatingWindowDialog(this);
        floatingWindowDialog.show();
    }

    public void play() {
        Log.i("CapacitorIvsPlayerX", "play");
        floatingWindowDialog.playerView.getPlayer().play();
    }

    public void pause() {
        Log.i("CapacitorIvsPlayerX", "pause");
        floatingWindowDialog.playerView.getPlayer().pause();
    }

    public void delete() {
        Log.i("CapacitorIvsPlayerX", "delete");
        floatingWindowDialog.playerView.getPlayer().release();
    }

    public void loadUrl(String url) {
        Log.i("CapacitorIvsPlayerX", "loadUrl: " + url);
        floatingWindowDialog.playerView.getPlayer().load(Uri.parse(url));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.i("CapacitorIvsPlayerX", "onUserLeaveHint");
        if (autoUnpip) {
            togglePip();
        }
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(playerControlReceiver, new IntentFilter("playerControl"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            // Hide other views and adjust player view layout when entering PiP mode
            // ...
        } else {
            // Restore the original layout when exiting PiP mode
            // ...
        }
    }

    public void togglePip() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        Log.d("CapacitorIvsPlayerX", "togglePip: " + isInPictureInPictureMode());
        if (isInPictureInPictureMode()) {
            // Exit PiP mode
            Intent intent = new Intent(this, CapacitorIvsPlayer.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            // Enter PiP mode
            Rational aspectRatio = new Rational(floatingWindowDialog.playerView.getWidth(), floatingWindowDialog.playerView.getHeight());
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build();
            enterPictureInPictureMode(params);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(playerControlReceiver);
        floatingWindowDialog.playerView.getPlayer().release();
    }
}
