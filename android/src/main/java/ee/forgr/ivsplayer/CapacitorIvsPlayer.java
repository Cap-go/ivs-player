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
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.ivs.player.Cue;
import com.amazonaws.ivs.player.Player;
import com.amazonaws.ivs.player.PlayerException;
import com.amazonaws.ivs.player.Quality;


public class CapacitorIvsPlayer extends AppCompatActivity implements SurfaceHolder.Callback {
    private FloatingWindowDialog floatingWindowDialog;

    private SurfaceView surfaceView;
    private Surface surface;

    private Player player;

    private final BroadcastReceiver playerControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            if (action != null) {
                switch (action) {
                    case "create":
//                        create();
                        break;
                    case "play":
//                        play();
                        break;
                    case "pause":
//                        pause();
                        break;
                    case "delete":
//                        delete();
                        break;
                    case "loadUrl":
//                        loadUrl(intent.getStringExtra("url"));
                        break;
                    case "togglePip":
//                        togglePip();
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
        togglePip();
    }
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capacitor_ivs_player_layout);
//        this.surfaceView = findViewById(R.id.surfaceView);
//        this.surfaceView.getHolder().addCallback(this);
//        player = Player.Factory.create(this);
//        player.addListener(new Player.Listener() {
//            @Override
//            public void onCue(@NonNull Cue cue) {
//                Log.i("CapacitorIvsPlayerX", "onCue");
//            }
//
//            @Override
//            public void onDurationChanged(long l) {
//                Log.i("CapacitorIvsPlayerX", "onDurationChanged" + l);
//            }
//
//            @Override
//            public void onStateChanged(@NonNull Player.State state) {
//                Log.i("CapacitorIvsPlayerX", "onDurationChanged" + state.name());
//            }
//
//            @Override
//            public void onError(@NonNull PlayerException e) {
//                Log.i("CapacitorIvsPlayerX", "onError" + e.toString());
//            }
//
//            @Override
//            public void onRebuffering() {
//                Log.i("CapacitorIvsPlayerX", "onRebuffering");
//            }
//
//            @Override
//            public void onSeekCompleted(long l) {
//                Log.i("CapacitorIvsPlayerX", "onSeekCompleted" + l);
//            }
//
//            @Override
//            public void onVideoSizeChanged(int i, int i1) {
//                Log.i("CapacitorIvsPlayerX", "onSeekCompleted" + i + ", " + i1);
//            }
//
//            @Override
//            public void onQualityChanged(@NonNull Quality quality) {
//                Log.i("CapacitorIvsPlayerX", "onQualityChanged" + quality.getName());
//            }
//        });
//        player.load(Uri.parse("https://d6hwdeiig07o4.cloudfront.net/ivs/956482054022/cTo5UpKS07do/2020-07-13T22-54-42.188Z/OgRXMLtq8M11/media/hls/master.m3u8"));
//        player.play();
//        registerReceiver(playerControlReceiver, new IntentFilter("playerControl"));
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surface = holder.getSurface();
        if (player != null) {
            player.setSurface(this.surface);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        this.surface = holder.getSurface();
        if (player != null) {
            player.setSurface(this.surface);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.surface = null;
        if (player != null) {
            player.setSurface(null);
        }
    }
}
