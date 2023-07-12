package ee.forgr.ivsplayer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.PictureInPictureModeChangedInfo;
import androidx.core.util.Consumer;
import androidx.lifecycle.Lifecycle;

import com.amazonaws.ivs.player.Player;
import com.amazonaws.ivs.player.PlayerView;
import com.amazonaws.ivs.player.Quality;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

import java.util.List;

@CapacitorPlugin(name = "CapacitorIvsPlayer")
public class CapacitorIvsPlayerPlugin extends Plugin implements Application.ActivityLifecycleCallbacks {
    private final int mainPiPFrameLayoutId = 257;
    private PlayerView playerView;
    private FrameLayout.LayoutParams playerViewParams;
    private String lastUrl = "";
    private Boolean isPreviousMainActivity = true;
    private Boolean isPip = false;

    @Override
    public void onActivityStarted(@NonNull final Activity activity) {
        Log.i("CapacitorIvsPlayer", "onActivityStarted");
//        if (isPreviousMainActivity) {
//            _setPip(false, false);
//        }
//        isPreviousMainActivity = true;
    }

    private boolean isMainActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        Context mContext = this.getContext();
        ActivityManager activityManager =
                (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> runningTasks = activityManager.getAppTasks();
        ActivityManager.RecentTaskInfo runningTask = runningTasks
                .get(0)
                .getTaskInfo();
        String className = runningTask.baseIntent.getComponent().getClassName();
        String runningActivity = runningTask.topActivity.getClassName();
        boolean isThisAppActivity = className.equals(runningActivity);
        return isThisAppActivity;
    }

    @Override
    public void onActivityStopped(@NonNull final Activity activity) {
        Log.i("CapacitorIvsPlayer", "onActivityStopped");
    }

    @Override
    public void onActivityResumed(@NonNull final Activity activity) {
        Log.i("CapacitorIvsPlayer", "onActivityResumed");

    }

    @Override
    public void onActivityPaused(@NonNull final Activity activity) {
        Log.i("CapacitorIvsPlayer", "onActivityPaused");
        _setPip(true, false);
    }

    @Override
    public void onActivityCreated(
            @NonNull final Activity activity,
            @Nullable final Bundle savedInstanceState
    ) {
        Log.i("CapacitorIvsPlayer", "onActivityCreated");
    }

    @Override
    public void onActivitySaveInstanceState(
            @NonNull final Activity activity,
            @NonNull final Bundle outState
    ) {
        Log.i("CapacitorIvsPlayer", "onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(@NonNull final Activity activity) {
        Log.i("CapacitorIvsPlayer", "onActivityDestroyed");
    }

    private void tooglePip(Boolean pip) {
        final JSObject ret = new JSObject();
        if (pip) {
            getBridge().getWebView().setVisibility(View.VISIBLE);
            ret.put("pip", false);
            isPip = false;
            Log.i("CapacitorIvsPlayer", "tooglePip false");
        } else {
            getBridge().getWebView().setVisibility(View.INVISIBLE);
            ret.put("pip", true);
            isPip = true;
            Log.i("CapacitorIvsPlayer", "tooglePip true");
        }
        notifyListeners("tooglePip", ret);
    }
    private void closePip() {
        final JSObject ret = new JSObject();
        getBridge().getWebView().setVisibility(View.VISIBLE);
        notifyListeners("closePip", ret);
        Log.i("CapacitorIvsPlayer", "closePip");
        isPip = false;
    }

    @PluginMethod
    public void create(PluginCall call) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        // Calculate the corresponding height for a 16:9 ratio
        var x = call.getInt("x", 0);
        var y = call.getInt("y", 0);
        var width = call.getInt("width", size.x);
        var height = call.getInt("height", (int) (size.x * 9.0 / 16.0));
        Log.i("CapacitorIvsPlayer", "create");
        String url = call.getString("url");
        if (url == null) {
            call.reject("url is required");
        }
        lastUrl = url;
        Boolean autoPlay = call.getBoolean("autoPlay", false);
        Boolean toBack = call.getBoolean("toBack", false);
        FrameLayout mainPiPFrameLayout = getBridge().getActivity().findViewById(mainPiPFrameLayoutId);
        if (mainPiPFrameLayout != null) {
            Log.i("CapacitorIvsPlayer", "FrameLayout for VideoPicker already exists");
        } else {
            // Initialize a new FrameLayout as container for fragment
            mainPiPFrameLayout = new FrameLayout(getActivity().getApplicationContext());
            mainPiPFrameLayout.setId(mainPiPFrameLayoutId);
            // Apply the Layout Parameters to frameLayout
            mainPiPFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            final FrameLayout finalMainPiPFrameLayout = mainPiPFrameLayout;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) getBridge().getWebView().getParent()).addView(finalMainPiPFrameLayout);
                    // Initialize the Player view
                    playerView = new PlayerView(getContext());
                    FrameLayout.LayoutParams playerViewParams = new FrameLayout.LayoutParams(width, height);
                    playerViewParams.setMargins(x, y, 0, 0);
                    playerView.setLayoutParams(playerViewParams);
                    playerView.requestFocus();
                    playerView.setControlsEnabled(false);

                    // Load the URL into the player
                    Uri uri = Uri.parse(url);
                    playerView.getPlayer().load(uri);
                    if (autoPlay == null || !autoPlay) {
                        playerView.getPlayer().pause();
                    }
                    finalMainPiPFrameLayout.addView(playerView);
                    if (toBack != null && toBack) {
                        getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
                        getBridge().getWebView().setBackgroundColor(0x00000000);
                    }
                    var self = CapacitorIvsPlayerPlugin.this;
                    getBridge().getActivity().addOnPictureInPictureModeChangedListener(new Consumer<PictureInPictureModeChangedInfo>() {

                        @Override
                        public void accept(PictureInPictureModeChangedInfo pictureInPictureModeChangedInfo) {
                            getBridge().getActivity().getLifecycle().getCurrentState();
                            final JSObject ret = new JSObject();
                            if (getBridge().getActivity().getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
                                closePip();
                            }
                            else if (getBridge().getActivity().getLifecycle().getCurrentState() == Lifecycle.State.STARTED){
                                tooglePip(!pictureInPictureModeChangedInfo.isInPictureInPictureMode());
                            }
                        }
                    });
                }
            });
        }
        call.resolve();
    }

    @Override
    public void load() {
        super.load();
        final Application application = (Application) this.getContext()
                .getApplicationContext();
        application.registerActivityLifecycleCallbacks(this);
    }

    @PluginMethod
    public void start(PluginCall call) {
        playerView.getPlayer().play();
        call.resolve();
    }

    @PluginMethod
    public void pause(PluginCall call) {
        playerView.getPlayer().pause();
        call.resolve();
    }

    @PluginMethod
    public void delete(PluginCall call) {
        playerView.getPlayer().release();
        call.resolve();
    }

    @PluginMethod
    public void getUrl(PluginCall call) {
        final JSObject ret = new JSObject();
        ret.put("url", lastUrl);
        call.resolve(ret);
    }

    @PluginMethod
    public void getState(PluginCall call) {
        final JSObject ret = new JSObject();
        ret.put("isPlaying", playerView.getPlayer().getState());
        call.resolve(ret);
    }

    @PluginMethod
    public void setAutoQuality(PluginCall call) {
        Boolean autoQuality = call.getBoolean("autoQuality", false);
        playerView.getPlayer().setAutoQualityMode(autoQuality);
        call.resolve();
    }

    @PluginMethod
    public void getAutoQuality(PluginCall call) {
        final JSObject ret = new JSObject();
        ret.put("autoQuality", playerView.getPlayer().isAutoQualityMode());
        call.resolve(ret);
    }


    @PluginMethod
    public void getPip(PluginCall call) {
        final JSObject ret = new JSObject();
        ret.put("pip", isPip);
        call.resolve(ret);
    }

    public void makeFloating() {
        playerView.setControlsEnabled(false);

        // Create the expand button and set its position
        ImageButton expandButton = new ImageButton(getContext());
        expandButton.setImageResource(R.drawable.baseline_zoom_out_map_24);

        FrameLayout.LayoutParams expandButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        expandButtonParams.gravity = Gravity.START | Gravity.TOP;
        expandButton.setLayoutParams(expandButtonParams);

        // Create the close button and set its position
        ImageView closeButton = new ImageView(getContext());
        closeButton.setImageResource(R.drawable.baseline_close_24);
        FrameLayout.LayoutParams closeButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        closeButtonParams.gravity = Gravity.END | Gravity.TOP;
        closeButton.setLayoutParams(closeButtonParams);

        // Create the play/pause button and set its position
        ImageButton playPauseButton = new ImageButton(getContext());
        playPauseButton.setImageResource(R.drawable.baseline_pause_24);
        FrameLayout.LayoutParams playPauseButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        playPauseButtonParams.gravity = Gravity.CENTER;
        playPauseButton.setLayoutParams(playPauseButtonParams);

        // Add the buttons to the player view layout
        playerView.addView(expandButton);
        playerView.addView(closeButton);
        playerView.addView(playPauseButton);

        // Set the button click listeners
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tooglePip(true);
                expandButton.setVisibility(View.GONE);
                closeButton.setVisibility(View.GONE);
                playPauseButton.setVisibility(View.GONE);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandButton.setVisibility(View.GONE);
                closeButton.setVisibility(View.GONE);
                playPauseButton.setVisibility(View.GONE);
                playerView.getPlayer().pause();
                _setPip(false, true);
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerView.getPlayer().getState() == Player.State.PLAYING) {
                    playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24);
                    playerView.getPlayer().pause();
                } else {
                    playPauseButton.setImageResource(R.drawable.baseline_pause_24);
                    playerView.getPlayer().play();
                }
            }
        });

        // playerView.setControlsEnabled(true);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = playerViewParams.leftMargin;
                        initialY = playerViewParams.topMargin;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        playerViewParams.leftMargin = initialX + deltaX;
                        playerViewParams.topMargin = initialY + deltaY;
                        playerView.setLayoutParams(playerViewParams);
                        break;
                }
                return true;
            }
        });
    }

    public void _setPip(Boolean pip, Boolean foregroundApp) {
        Log.i("CapacitorIvsPlayer", "_setPip pip: " + pip);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Rational aspectRatio = new Rational(16, 9);
                if (foregroundApp) {
                    isPip = pip;
                    Log.i("CapacitorIvsPlayer", "foregroundApp pip: " + pip);
                    if (pip) {
                        FrameLayout mainPiPFrameLayout = getBridge().getActivity().findViewById(mainPiPFrameLayoutId);
                        getBridge().getWebView().getParent().bringChildToFront(mainPiPFrameLayout);
                        getBridge().getWebView().setBackgroundColor(0x000000ff);
                        //                    TODO: allow to drag and drop or find a way to have pip on top of the app
                        makeFloating();
                    }
                    else {
                        playerView.setControlsEnabled(false);
                        getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
                        getBridge().getWebView().setBackgroundColor(0x00000000);
                    }
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i("CapacitorIvsPlayer", "backgroundApp: " + pip);
                    isPip = pip;
                    if (pip) {
                        PictureInPictureParams params = new PictureInPictureParams.Builder()
                                .setAspectRatio(aspectRatio)
                                .build();
                        getBridge().getActivity().enterPictureInPictureMode(params);
                    }
                }
            }
        });
    }

    @PluginMethod
    public void setPip(PluginCall call) {
        Boolean pip = call.getBoolean("pip", false);
        _setPip(pip, true);
        call.resolve();
    }

    @PluginMethod
    public void setFrame(PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                var x = call.getInt("x", 0);
                var y = call.getInt("y", 0);
                var width = call.getInt("width", size.x);
                var height = call.getInt("height", (int) (size.x * 9.0 / 16.0));
                playerViewParams = new FrameLayout.LayoutParams(width, height);
                playerViewParams.setMargins(x, y, 0, 0);
                playerView.setLayoutParams(playerViewParams);
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void getFrame(PluginCall call) {
        var layoutParams = (FrameLayout.LayoutParams) playerView.getLayoutParams();
        final JSObject ret = new JSObject();
        ret.put("x", layoutParams.leftMargin);
        ret.put("y", layoutParams.topMargin);
        ret.put("width", layoutParams.width);
        ret.put("height", layoutParams.height);
        call.resolve(ret);
    }

    @PluginMethod
    public void setMute(PluginCall call) {
        Boolean muted = call.getBoolean("muted", false);
        playerView.getPlayer().setMuted(muted);
        call.resolve();
    }

    @PluginMethod
    public void getMute(PluginCall call) {
        final JSObject ret = new JSObject();
        ret.put("muted", playerView.getPlayer().isMuted());
        call.resolve(ret);
        call.resolve();
    }

    @PluginMethod
    public void setQuality(PluginCall call) {
        String qualityName = call.getString("quality");
//        loop through qualities and find the one with the name
        Quality quality = null;
        for (var q : playerView.getPlayer().getQualities()) {
            if (q.getName().equals(qualityName)) {
                quality = q;
            }
        }
        playerView.getPlayer().setQuality(quality);
        call.resolve();
    }

    @PluginMethod
    public void getQuality(PluginCall call) {
        final JSObject ret = new JSObject();
        ret.put("quality", playerView.getPlayer().getQuality().getName());
        call.resolve();
    }

    @PluginMethod
    public void getQualities(PluginCall call) {
        final JSObject ret = new JSObject();
        var qualities = playerView.getPlayer().getQualities();
        var qualitiesArray = new JSONArray();
        for (var quality : qualities) {
            qualitiesArray.put(quality.getName());
        }
        ret.put("qualities", qualitiesArray);
        call.resolve(ret);
    }
}
