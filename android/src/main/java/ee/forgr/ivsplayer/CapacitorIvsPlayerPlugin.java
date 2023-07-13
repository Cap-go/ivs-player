package ee.forgr.ivsplayer;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.PictureInPictureModeChangedInfo;
import androidx.core.util.Consumer;
import androidx.lifecycle.Lifecycle;

import com.amazonaws.ivs.player.Cue;
import com.amazonaws.ivs.player.Player;
import com.amazonaws.ivs.player.PlayerException;
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
    private int marginButton = 40;
    private Boolean currentStateDisplayButton = false;

    private Point size = new Point();
    private FrameLayout.LayoutParams playerViewParams;
    private String lastUrl = "";
    private Boolean isPip = false;
    ImageView expandButton;
    ImageView closeButton;
    ImageView playPauseButton;
    View shadowView;

    private Animation expandAnimation;
    private Animation collapseAnimation;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean isFullScreen = false;

    @Override
    public void onActivityStarted(@NonNull final Activity activity) {
        Log.i("CapacitorIvsPlayer", "onActivityStarted");
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

    private void togglePip(Boolean pip) {
        final JSObject ret = new JSObject();
        if (pip) {
            playerView.setClipToOutline(false);
            getBridge().getWebView().setVisibility(View.VISIBLE);
            ret.put("pip", false);
            isPip = false;
            Log.i("CapacitorIvsPlayer", "togglePip false");
        } else {
            getBridge().getWebView().setVisibility(View.INVISIBLE);
            ret.put("pip", true);
            isPip = true;
            Log.i("CapacitorIvsPlayer", "togglePip true");
        }
        notifyListeners("togglePip", ret);
    }
    private void closePip() {
        final JSObject ret = new JSObject();
        getBridge().getWebView().setVisibility(View.VISIBLE);
        notifyListeners("closePip", ret);
        Log.i("CapacitorIvsPlayer", "closePip");
        isPip = false;
    }

    // function to calc 16:9 ratio height
    private int calcHeight(int width) {
        return (int) (width * 9.0 / 16.0);
    }

    private void addPipListener() {
        getBridge().getActivity().addOnPictureInPictureModeChangedListener(new Consumer<PictureInPictureModeChangedInfo>() {

            @Override
            public void accept(PictureInPictureModeChangedInfo pictureInPictureModeChangedInfo) {
                getBridge().getActivity().getLifecycle().getCurrentState();
                final JSObject ret = new JSObject();
                if (getBridge().getActivity().getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
                    closePip();
                }
                else if (getBridge().getActivity().getLifecycle().getCurrentState() == Lifecycle.State.STARTED){
                    togglePip(!pictureInPictureModeChangedInfo.isInPictureInPictureMode());
                }
            }
        });
    }

    private void addPlayerListener() {
        // listen on player event
        playerView.getPlayer().addListener(new Player.Listener() {
            @Override
            public void onStateChanged(Player.State state) {
                final JSObject ret = new JSObject();
                ret.put("state", state);
                notifyListeners("onState", ret);
            }
            @Override
            public void onCue(Cue cue) {
                final JSObject ret = new JSObject();
                ret.put("cue", cue);
                notifyListeners("onCue", ret);
            }
            @Override
            public void onDurationChanged(long duration) {
                final JSObject ret = new JSObject();
                ret.put("duration", duration);
                notifyListeners("onDuration", ret);
            }
            @Override
            public void onError(PlayerException error) {
                final JSObject ret = new JSObject();
                ret.put("error", error);
                notifyListeners("onError", ret);
            }
            @Override
            public void onRebuffering() {
                final JSObject ret = new JSObject();
                notifyListeners("onRebuffering", ret);
            }
            @Override
            public void onSeekCompleted(long var1) {
                final JSObject ret = new JSObject();
                ret.put("position", var1);
                notifyListeners("onSeekCompleted", ret);
            }
            @Override
            public void onVideoSizeChanged(int var1, int var2) {
                final JSObject ret = new JSObject();
                ret.put("width", var1);
                ret.put("height", var2);
                notifyListeners("onVideoSize", ret);
            }
            @Override
            public void onQualityChanged(@NonNull Quality var1) {
                final JSObject ret = new JSObject();
                ret.put("quality", var1);
                notifyListeners("onQuality", ret);
            }
        });
    }

    @PluginMethod
    public void create(PluginCall call) {
        // Calculate the corresponding height for a 16:9 ratio
        getDisplaySize();
        var x = call.getInt("x", 0);
        var y = call.getInt("y", 0);
        var width = call.getInt("width", size.x);
        var height = call.getInt("height", calcHeight(size.x));
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
            addPipListener();
            addPlayerListener();
            final FrameLayout finalMainPiPFrameLayout = mainPiPFrameLayout;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) getBridge().getWebView().getParent()).addView(finalMainPiPFrameLayout);
                    finalMainPiPFrameLayout.addView(playerView);
                }
            });
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _setFrame(x, y, width, height);
                // Load the URL into the player
                Uri uri = Uri.parse(url);
                playerView.getPlayer().load(uri);
                playerView.setClipToOutline(false);
                if (autoPlay == null || !autoPlay) {
                    playerView.getPlayer().pause();
                }
                setPlayerPosition(toBack);
            }
        });
        call.resolve();
    }

    private void getDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);
    }

    @Override
    public void load() {
        super.load();
        getDisplaySize();
        expandAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.expand_animation);
        collapseAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.collapse_animation);


        // Initialize the Player view
        playerView = new PlayerView(getContext());
        playerView.requestFocus();
        playerView.setControlsEnabled(false);
        // Create the expand button
        expandButton = new ImageView(getContext());
        shadowView = new View(getContext());
        shadowView.setBackgroundColor(Color.BLACK);
        shadowView.setAlpha(0.5f);
        expandButton.setImageResource(R.drawable.baseline_zoom_out_map_24);
        // Create the close button
        closeButton = new ImageView(getContext());
        closeButton.setImageResource(R.drawable.baseline_close_24);

        // Create the play pause button
        playPauseButton = new ImageView(getContext());
        playPauseButton.setImageResource(R.drawable.baseline_pause_24);

        // Set the button click listeners
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePip(true);
                setDisplayPipButton(false);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDisplayPipButton(false);
                playerView.getPlayer().pause();
                playerView.setClipToOutline(false);
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

    private void setAutoHideDisplayButton () {
        setDisplayPipButton(true);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setDisplayPipButton(false);
            }
        }, 3000);
    }

    private void setDisplayPipButton (boolean displayPipButton) {
        if (currentStateDisplayButton == displayPipButton) {
            return;
        }
        if (displayPipButton) {
            fadeAnimation(shadowView, View.VISIBLE);
            fadeAnimation(expandButton, View.VISIBLE);
            fadeAnimation(closeButton, View.VISIBLE);
            fadeAnimation(playPauseButton, View.VISIBLE);
            currentStateDisplayButton = true;
        } else {
            fadeAnimation(shadowView, View.GONE);
            fadeAnimation(expandButton, View.GONE);
            fadeAnimation(closeButton, View.GONE);
            fadeAnimation(playPauseButton, View.GONE);
            currentStateDisplayButton = false;
        }
    }

    private void fadeAnimation(View view, int visibility) {
        AlphaAnimation animation;
        if (visibility == View.VISIBLE) {
            animation = new AlphaAnimation(0f, 1f);
        } else {
            animation = new AlphaAnimation(1f, 0f);
        }

        animation.setDuration(500);
        view.setVisibility(visibility);
        view.startAnimation(animation);
    }

    private void toggleFullScreen() {
        int x = playerView.getLeft();
        int y = playerView.getTop();

        if (isFullScreen) {
            animateResize(playerView.getWidth(), playerView.getHeight(), size.x / 2, calcHeight(size.x / 2));
        } else {
            // Maximize the player view width with animation and calculate the new height
            int newPlayerSizeX = calcHeight(size.x);
            animateResize(playerView.getWidth(), playerView.getHeight(), size.x, newPlayerSizeX);
        }

        // Toggle the full screen flag
        isFullScreen = !isFullScreen;
    }

    private void animateResize(int startWidth, int startHeight, int endWidth, int endHeight) {
        ValueAnimator widthAnimator = ValueAnimator.ofFloat(startWidth, endWidth);
        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                playerView.getLayoutParams().width = (int) animatedValue;
                int maxMarginX = size.x - (int) animatedValue;
                int newMarginX = Math.max(0, Math.min(playerView.getLeft(), maxMarginX));
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) playerView.getLayoutParams();
                layoutParams.leftMargin = newMarginX;
                playerView.setLayoutParams(layoutParams);
                playerView.requestLayout();
            }
        });

        ValueAnimator heightAnimator = ValueAnimator.ofFloat(startHeight, endHeight);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                playerView.getLayoutParams().height = (int) animatedValue;
                int maxMarginY = size.y - (int) animatedValue;
                int newMarginY = Math.max(0, Math.min(playerView.getTop(), maxMarginY));
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) playerView.getLayoutParams();
                layoutParams.topMargin = newMarginY;
                playerView.setLayoutParams(layoutParams);
                playerView.requestLayout();
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.playTogether(widthAnimator, heightAnimator);
        animatorSet.start();
    }

    public void makeFloating() {
        // Set the corner radius
        playerView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 64);
            }
        });
        playerView.setClipToOutline(true);

        FrameLayout.LayoutParams expandButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        expandButtonParams.gravity = Gravity.START | Gravity.TOP;
        expandButtonParams.topMargin = marginButton;
        expandButtonParams.leftMargin = marginButton;
        expandButton.setLayoutParams(expandButtonParams);

        FrameLayout.LayoutParams closeButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        closeButtonParams.gravity = Gravity.END | Gravity.TOP;
        closeButtonParams.topMargin = marginButton;
        closeButtonParams.rightMargin = marginButton;
        closeButton.setLayoutParams(closeButtonParams);

        FrameLayout.LayoutParams playPauseButtonParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        playPauseButtonParams.gravity = Gravity.CENTER;
        playPauseButton.setLayoutParams(playPauseButtonParams);

        // Add the buttons to the player view layout
        // check if already in view
        if (shadowView.getParent() == null) {
            playerView.addView(shadowView);
        }
        if (expandButton.getParent() == null) {
            playerView.addView(expandButton);
        }
        if (closeButton.getParent() == null) {
            playerView.addView(closeButton);
        }
        if (playPauseButton.getParent() == null) {
            playerView.addView(playPauseButton);
        }
        // Show the buttons for 3 seconds
        setAutoHideDisplayButton();

        // get middile of screen x y
        getDisplaySize();
        int width = size.x / 2;
        int height = calcHeight(width);
        // position the player view at the bottom right corner with a margin of 1/4 of screen
        int x = size.x - width - 30;
        int y = size.y - height - 30;
        // get half of width and calculate height
        _setFrame(x, y, width, height);

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleFullScreen();
                return true;
            }
        });

        // Initialize the scale gesture detector
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // Handle scale gestures if needed
                return true;
            }
        });

        playerView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private int maxMarginX;
            private int maxMarginY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Calculate the maximum margin values for X and Y
                gestureDetector.onTouchEvent(event);
                scaleGestureDetector.onTouchEvent(event);
                maxMarginX = size.x - playerViewParams.width;
                maxMarginY = size.y - playerViewParams.height;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = playerViewParams.leftMargin;
                        initialY = playerViewParams.topMargin;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        // Show the buttons for 3 seconds
                        setAutoHideDisplayButton();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        // Clamp the new margin values within the screen limits
                        int newMarginX = initialX + deltaX;
                        int newMarginY = initialY + deltaY;
                        newMarginX = Math.max(0, Math.min(newMarginX, maxMarginX));
                        newMarginY = Math.max(0, Math.min(newMarginY, maxMarginY));
                        playerViewParams.leftMargin = newMarginX;
                        playerViewParams.topMargin = newMarginY;
                        playerView.setLayoutParams(playerViewParams);
                        break;
                }
                return true;
            }
        });
    }
    // function to send webview to front
    private void setPlayerPosition(Boolean toBack) {
        if (toBack) {
            getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
            getBridge().getWebView().setBackgroundColor(0x00000000);
        } else {
            FrameLayout mainPiPFrameLayout = getBridge().getActivity().findViewById(mainPiPFrameLayoutId);
            getBridge().getWebView().getParent().bringChildToFront(mainPiPFrameLayout);
            getBridge().getWebView().setBackgroundColor(0x000000ff);
        }
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
                    setPlayerPosition(!pip);
                    if (pip) {
                        makeFloating();
                    }
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i("CapacitorIvsPlayer", "backgroundApp: " + pip);
                    isPip = pip;
                    if (pip) {
                        setDisplayPipButton(false);
                        PictureInPictureParams params = new PictureInPictureParams.Builder()
                                .setAspectRatio(aspectRatio)
                                .build();
                        getBridge().getActivity().enterPictureInPictureMode(params);
                        getDisplaySize();
                        // get PIP frame layout width and height
                        int width = size.x / 2;
                        int height = calcHeight(width);
                        _setFrame(0, 0, width, height);
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

    private void _setFrame(int x, int y, int width, int height) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerViewParams = new FrameLayout.LayoutParams(width, height);
                playerViewParams.setMargins(x, y, 0, 0);
                playerView.setLayoutParams(playerViewParams);
            }
        });
    }
    // function to get default height and width of the screen

    @PluginMethod
    public void setFrame(PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getDisplaySize();
                var x = call.getInt("x", 0);
                var y = call.getInt("y", 0);
                var width = call.getInt("width", size.x);
                var height = call.getInt("height", calcHeight(size.x));
                _setFrame(x, y, width, height);
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
