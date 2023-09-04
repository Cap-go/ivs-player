package ee.forgr.ivsplayer;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.app.PictureInPictureParams;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
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
import androidx.mediarouter.app.MediaRouteButton;
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
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.common.images.WebImage;
import org.json.JSONArray;

@CapacitorPlugin(name = "CapacitorIvsPlayer")
public class CapacitorIvsPlayerPlugin extends Plugin {

  public final String PLUGIN_VERSION = "0.13.34";

  private final int mainPiPFrameLayoutId = 257;
  private PlayerView playerView;
  private int marginButton = 40;
  private MediaRouteButton mediaRouteButton;
  private Boolean currentStateDisplayButton = true;

  private Point size = new Point();
  private Rational aspectRatio = new Rational(16, 9);
  private FrameLayout.LayoutParams playerViewParams;
  public String lastUrl = "";
  private Boolean isPip = false;
  private Boolean isCast = false;
  private Boolean autoPlay = false;
  private Boolean toBack = false;
  ImageView expandButton;
  ImageView closeButton;
  ImageView playPauseButton;
  View shadowView;
  CastContext castContext;
  CastSession castSession;

  private Animation expandAnimation;
  private Animation collapseAnimation;

  private GestureDetector gestureDetector;
  private ScaleGestureDetector scaleGestureDetector;
  private boolean isFullScreen = false;
  private SessionManagerListener<CastSession> mSessionManagerListener;
  private CastSession mCastSession;
  private SessionManager mSessionManager;
  public String title = "";
  public String description = "";
  public String cover = "";

  @Override
  public void handleOnStop() {
    Log.i("CapacitorIvsPlayer", "onActivityStopped");
    // pause the player
    if (playerView != null) {
      playerView.getPlayer().pause();
    }
  }

  @Override
  public void handleOnPause() {
    Log.i("CapacitorIvsPlayer", "onActivityPaused");
    // check if player is playing if so do nothing
    if (
      playerView == null ||
      playerView.getPlayer().getState() != Player.State.PLAYING
    ) {
      return;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      PictureInPictureParams params = new PictureInPictureParams.Builder()
        .setAspectRatio(aspectRatio)
        .build();
      Boolean didWorked = getBridge()
        .getActivity()
        .enterPictureInPictureMode(params);
      if (didWorked) {
        _setPip(true, false);
      }
    }
  }

  private void togglePip(Boolean pip) {
    if (pip) {
      playerView.setClipToOutline(false);
      getBridge().getWebView().setVisibility(View.VISIBLE);
      isPip = false;
      Log.i("CapacitorIvsPlayer", "togglePip false");
      final JSObject ret = new JSObject();
      notifyListeners("expandPip", ret);
      notifyListeners("stopPip", ret);
    } else {
      getBridge().getWebView().setVisibility(View.INVISIBLE);
      isPip = true;
      Log.i("CapacitorIvsPlayer", "togglePip true");
      final JSObject ret = new JSObject();
      notifyListeners("startPip", ret);
    }
  }

  private void closePip() {
    final JSObject ret = new JSObject();
    getBridge().getWebView().setVisibility(View.VISIBLE);
    notifyListeners("closePip", ret);
    notifyListeners("stopPip", ret);
    Log.i("CapacitorIvsPlayer", "closePip");
    isPip = false;
  }

  // function to calc 16:9 ratio height
  private int calcHeight(int width) {
    return (int) (width * 9.0 / 16.0);
  }

  private void addPipListener() {
    getBridge()
      .getActivity()
      .addOnPictureInPictureModeChangedListener(
        new Consumer<PictureInPictureModeChangedInfo>() {
          @Override
          public void accept(
            PictureInPictureModeChangedInfo pictureInPictureModeChangedInfo
          ) {
            getBridge().getActivity().getLifecycle().getCurrentState();
            final JSObject ret = new JSObject();
            if (
              getBridge().getActivity().getLifecycle().getCurrentState() ==
              Lifecycle.State.CREATED
            ) {
              closePip();
            } else if (
              getBridge().getActivity().getLifecycle().getCurrentState() ==
              Lifecycle.State.STARTED
            ) {
              togglePip(
                !pictureInPictureModeChangedInfo.isInPictureInPictureMode()
              );
            }
          }
        }
      );
  }

  private void addPlayerListener() {
    // listen on player event
    playerView
      .getPlayer()
      .addListener(
        new Player.Listener() {
          @Override
          public void onStateChanged(Player.State state) {
            if (isCast) {
              return;
            }
            final JSObject ret = new JSObject();
            ret.put("state", state);
            Log.i("CapacitorIvsPlayer", "onStateChanged: " + state);
            if (state == Player.State.READY && autoPlay) {
              playerView.getPlayer().play();
            }
            if (
              state == Player.State.PLAYING && playerView.getParent() == null
            ) {
              FrameLayout mainPiPFrameLayout = getBridge()
                .getActivity()
                .findViewById(mainPiPFrameLayoutId);
              mainPiPFrameLayout.addView(playerView);
            }
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
        }
      );
  }

  public void loadUrl(String url) {
    Log.i("CapacitorIvsPlayer", "loadUrl: " + url);
    playerView.getPlayer().load(Uri.parse(url));
  }

  public void cyclePlayer(String prevUrl, String nextUrl) {
    FrameLayout mainPiPFrameLayout = getBridge()
      .getActivity()
      .findViewById(mainPiPFrameLayoutId);
    //            && prevUrl != nextUrl
    if (mainPiPFrameLayout != null) {
      Log.i("CapacitorIvsPlayer", "FrameLayout for VideoPicker already exists");
      // check if playerView is already in mainPiPFrameLayout
      if (playerView.getParent() != null) {
        Log.i(
          "CapacitorIvsPlayer",
          "playerView is already in mainPiPFrameLayout"
        );
        if (prevUrl.equals(nextUrl)) {
          loadUrl(nextUrl);
          return;
        }
        mainPiPFrameLayout.removeView(playerView);
        loadUrl(nextUrl);
      } else {
        Log.i("CapacitorIvsPlayer", "playerView is not in mainPiPFrameLayout");
        // add playerView to mainPiPFrameLayout
        mainPiPFrameLayout.addView(playerView);
        loadUrl(nextUrl);
      }
    } else {
      // Initialize a new FrameLayout as container for fragment
      mainPiPFrameLayout =
        new FrameLayout(getActivity().getApplicationContext());
      mainPiPFrameLayout.setId(mainPiPFrameLayoutId);
      // Apply the Layout Parameters to frameLayout
      mainPiPFrameLayout.setLayoutParams(
        new FrameLayout.LayoutParams(
          FrameLayout.LayoutParams.MATCH_PARENT,
          FrameLayout.LayoutParams.MATCH_PARENT
        )
      );
      final FrameLayout finalMainPiPFrameLayout = mainPiPFrameLayout;

      getActivity()
        .runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              ((ViewGroup) getBridge().getWebView().getParent()).addView(
                  finalMainPiPFrameLayout
                );
              finalMainPiPFrameLayout.addView(playerView);
              loadUrl(nextUrl);
            }
          }
        );
    }
  }

  private void setupCastListener() {
    mSessionManager =
      CastContext.getSharedInstance(getContext()).getSessionManager();
    mSessionManagerListener =
      new SessionManagerListener<CastSession>() {
        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
          castSession = session;
          Log.i("CapacitorIvsPlayer", "onSessionStarted");
          // Create a new MediaMetadata object.
          MediaMetadata metadata = new MediaMetadata(
            MediaMetadata.MEDIA_TYPE_MOVIE
          );
          metadata.putString(
            MediaMetadata.KEY_TITLE,
            CapacitorIvsPlayerPlugin.this.title
          );
          metadata.putString(
            MediaMetadata.KEY_SUBTITLE,
            CapacitorIvsPlayerPlugin.this.description
          );
          metadata.addImage(
            new WebImage(Uri.parse(CapacitorIvsPlayerPlugin.this.cover))
          );

          MediaLoadOptions mediaLoadOptions = new MediaLoadOptions.Builder()
            .setAutoplay(true)
            .setPlayPosition(playerView.getPlayer().getPosition())
            .build();

          // Create a new MediaInfo object.
          MediaInfo mediaInfo = new MediaInfo.Builder(
            CapacitorIvsPlayerPlugin.this.lastUrl
          )
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setContentType("application/x-mpegURL")
            .setMetadata(metadata)
            .build();

          // Load the media.
          session.getRemoteMediaClient().load(mediaInfo, mediaLoadOptions);
          playerView.getPlayer().pause();
          isCast = true;
          final JSObject ret = new JSObject();
          ret.put("isActive", isCast);
          notifyListeners("onCastStatus", ret);
        }

        @Override
        public void onSessionStarting(@NonNull CastSession castSession) {
          Log.i("CapacitorIvsPlayer", "onSessionStarting");
        }

        @Override
        public void onSessionSuspended(
          @NonNull CastSession castSession,
          int i
        ) {
          Log.i("CapacitorIvsPlayer", "onSessionSuspended");
        }

        @Override
        public void onSessionResumed(
          CastSession session,
          boolean wasSuspended
        ) {
          castSession = session;
          Log.i("CapacitorIvsPlayer", "onSessionResumed");
        }

        @Override
        public void onSessionResuming(
          @NonNull CastSession castSession,
          @NonNull String s
        ) {
          Log.i("CapacitorIvsPlayer", "onSessionResuming");
        }

        @Override
        public void onSessionStartFailed(
          @NonNull CastSession castSession,
          int i
        ) {
          Log.i("CapacitorIvsPlayer", "onSessionStartFailed");
        }

        @Override
        public void onSessionEnded(CastSession session, int error) {
          Log.i("CapacitorIvsPlayer", "onSessionEnded");
          isCast = false;
          mSessionManager.removeSessionManagerListener(
            mSessionManagerListener,
            CastSession.class
          );
          final JSObject ret = new JSObject();
          ret.put("isActive", isCast);
          notifyListeners("onCastStatus", ret);
          playerView.getPlayer().pause();
        }

        @Override
        public void onSessionEnding(@NonNull CastSession castSession) {
          Log.i("CapacitorIvsPlayer", "onSessionEnding");
        }

        @Override
        public void onSessionResumeFailed(
          @NonNull CastSession castSession,
          int i
        ) {
          Log.i("CapacitorIvsPlayer", "onSessionResumeFailed");
        }
      };
  }

  @PluginMethod
  public void getPluginVersion(final PluginCall call) {
    try {
      final JSObject ret = new JSObject();
      ret.put("version", this.PLUGIN_VERSION);
      call.resolve(ret);
    } catch (final Exception e) {
      call.reject("Could not get plugin version", e);
    }
  }

  @PluginMethod
  public void cast(PluginCall call) {
    Log.i("CapacitorIvsPlayer", "cast");
    //    app:actionProviderClass="androidx.mediarouter.app.MediaRouteActionProvider"
    //    open MediaRouteActionProvider

    var lastUrl = this.lastUrl;
    getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            Log.i("CapacitorIvsPlayer", "CreateCast");
            // Check if the CastContext is null
            if (castContext == null) {
              Log.i("CapacitorIvsPlayer", "CastContext is null");
            } else {
              // Check if there are any available devices
              if (
                castContext.getCastState() == CastState.NO_DEVICES_AVAILABLE
              ) {
                Log.i("CapacitorIvsPlayer", "No devices available for casting");
              } else {
                Log.i("CapacitorIvsPlayer", "Devices available for casting");
              }
            }
            mSessionManager.addSessionManagerListener(
              mSessionManagerListener,
              CastSession.class
            );
            if (mSessionManager.getCurrentCastSession() != null) {
              mCastSession = mSessionManager.getCurrentCastSession();
            }
            // Programmatically click the MediaRouteButton to show the device selection dialog.
            mediaRouteButton.performClick();
            Log.i("CapacitorIvsPlayer", "CreateCast performClick");
            // Check if a session is activ
            call.resolve();
          }
        }
      );
  }

  @PluginMethod
  public void getCastStatus(PluginCall call) {
    Log.i("CapacitorIvsPlayer", "getCastStatus");
    final JSObject ret = new JSObject();
    ret.put("isActive", isCast);
    call.resolve(ret);
  }

  @PluginMethod
  public void create(PluginCall call) {
    // Calculate the corresponding height for a 16:9 ratio
    getDisplaySize();
    var x = (int) convertDpToPixel(call.getFloat("x", 0.0f));
    var y = (int) convertDpToPixel(call.getFloat("y", 0.0f));
    var width = (int) convertDpToPixel(
      call.getFloat("width", convertPixelsToDp(size.x))
    );
    var height = (int) convertDpToPixel(
      call.getFloat("height", convertPixelsToDp(calcHeight(size.x)))
    );
    Log.i("CapacitorIvsPlayer", "create");
    String url = call.getString("url", "");
    if (url == null) {
      call.reject("url is required");
    }
    var prevUrl = lastUrl;
    lastUrl = url;
    autoPlay = call.getBoolean("autoPlay", false);
    Boolean toBack = call.getBoolean("toBack", false);
    this.title = call.getString("title", "");
    this.description = call.getString("description", "");
    this.cover = call.getString("cover", "");
    getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            cyclePlayer(prevUrl, url);
            _setFrame(x, y, width, height);
            playerView.setClipToOutline(false);
            _setPlayerPosition(toBack);
            setPip(call);
          }
        }
      );
  }

  private void getDisplaySize() {
    Display display = getActivity().getWindowManager().getDefaultDisplay();
    display.getSize(size);
    Log.i("CapacitorIvsPlayer", "getDisplaySize: " + size.x + "x" + size.y);
  }

  private void prepareButtonInternalPip() {
    expandAnimation =
      AnimationUtils.loadAnimation(getContext(), R.anim.expand_animation);
    collapseAnimation =
      AnimationUtils.loadAnimation(getContext(), R.anim.collapse_animation);

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

    FrameLayout.LayoutParams playPauseButtonParams =
      new FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
      );
    playPauseButtonParams.gravity = Gravity.CENTER;
    playPauseButton.setLayoutParams(playPauseButtonParams);

    // Set the button click listeners
    expandButton.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          togglePip(true);
          setDisplayPipButton(false);
        }
      }
    );

    closeButton.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          setDisplayPipButton(false);
          playerView.getPlayer().pause();
          playerView.setClipToOutline(false);
          _setPip(false, true);
        }
      }
    );

    playPauseButton.setOnClickListener(
      new View.OnClickListener() {
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
      }
    );
    // Add the buttons to the player view layout
    playerView.addView(shadowView);
    playerView.addView(expandButton);
    playerView.addView(closeButton);
    playerView.addView(playPauseButton);
    setDisplayPipButton(false);
  }

  @Override
  public void load() {
    super.load();
    getDisplaySize();
    setupCastListener();
    mediaRouteButton = new MediaRouteButton(bridge.getActivity());
    CastButtonFactory.setUpMediaRouteButton(
      bridge.getActivity().getApplicationContext(),
      mediaRouteButton
    );
    mediaRouteButton.setVisibility(View.GONE);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
      FrameLayout.LayoutParams.WRAP_CONTENT,
      FrameLayout.LayoutParams.WRAP_CONTENT
    );
    params.gravity = Gravity.TOP | Gravity.END;
    bridge.getActivity().addContentView(mediaRouteButton, params);

    castContext = CastContext.getSharedInstance(getActivity());
    // Initialize the Player view
    playerView = new PlayerView(getContext());
    playerView.requestFocus();
    playerView.setControlsEnabled(false);
    prepareButtonInternalPip();
    addPipListener();
    addPlayerListener();

    // Set the corner radius
    playerView.setOutlineProvider(
      new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
          outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 64);
        }
      }
    );

    gestureDetector =
      new GestureDetector(
        getContext(),
        new GestureDetector.SimpleOnGestureListener() {
          @Override
          public boolean onDoubleTap(MotionEvent e) {
            toggleFullScreen();
            return true;
          }
        }
      );

    // Initialize the scale gesture detector
    scaleGestureDetector =
      new ScaleGestureDetector(
        getContext(),
        new ScaleGestureDetector.SimpleOnScaleGestureListener() {
          @Override
          public boolean onScale(ScaleGestureDetector detector) {
            // TODO: Handle scale gestures if needed
            return true;
          }
        }
      );

    playerView.setOnTouchListener(
      new View.OnTouchListener() {
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
      }
    );
  }

  @PluginMethod
  public void start(PluginCall call) {
    playPauseButton.setImageResource(R.drawable.baseline_pause_24);
    playerView.getPlayer().play();
    call.resolve();
  }

  @PluginMethod
  public void pause(PluginCall call) {
    playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24);
    playerView.getPlayer().pause();
    call.resolve();
  }

  @PluginMethod
  public void delete(PluginCall call) {
    FrameLayout mainPiPFrameLayout = getBridge()
      .getActivity()
      .findViewById(mainPiPFrameLayoutId);
    playerView.getPlayer().pause();
    if (mainPiPFrameLayout != null) {
      // remove playerView from mainPiPFrameLayout
      getActivity()
        .runOnUiThread(
          new Runnable() {
            @Override
            public void run() {
              mainPiPFrameLayout.removeView(playerView);
            }
          }
        );
    }
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
    ret.put("state", playerView.getPlayer().getState());
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

  private void setAutoHideDisplayButton() {
    setDisplayPipButton(true);
    final Handler handler = new Handler();
    handler.postDelayed(
      new Runnable() {
        @Override
        public void run() {
          setDisplayPipButton(false);
        }
      },
      3000
    );
  }

  private void setDisplayPipButton(boolean displayPipButton) {
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
    Log.i("CapacitorIvsPlayer", "toggleFullScreen: " + isFullScreen);
    int x = playerView.getLeft();
    int y = playerView.getTop();

    if (isFullScreen) {
      int halfScreenSizeX = size.x / 2;
      animateResize(
        playerView.getWidth(),
        playerView.getHeight(),
        halfScreenSizeX,
        calcHeight(halfScreenSizeX),
        x,
        y,
        x,
        y
      );
    } else {
      // Maximize the player view width with animation and calculate the new height
      int newPlayerSizeX = calcHeight(size.x);
      animateResize(
        playerView.getWidth(),
        playerView.getHeight(),
        size.x,
        newPlayerSizeX,
        x,
        y,
        x,
        y
      );
    }

    // Toggle the full screen flag
    isFullScreen = !isFullScreen;
  }

  private void animateResize(
    int startWidth,
    int startHeight,
    int endWidth,
    int endHeight,
    int startX,
    int startY,
    int endX,
    int endY
  ) {
    // ValueAnimator XAnimator = ValueAnimator.ofFloat(startX, endX);
    // XAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    //     @Override
    //     public void onAnimationUpdate(ValueAnimator animation) {
    //         float animatedValue = (float) animation.getAnimatedValue();
    //         int maxMarginX = size.x - (int) animatedValue;
    //         int newMarginX = Math.max(0, Math.min(playerView.getLeft(), maxMarginX));
    //         FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) playerView.getLayoutParams();
    //         layoutParams.leftMargin = newMarginX;
    //         playerView.setLayoutParams(layoutParams);
    //         playerView.requestLayout();
    //     }
    // });

    // ValueAnimator YAnimator = ValueAnimator.ofFloat(startY, endY);
    // YAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    //     @Override
    //     public void onAnimationUpdate(ValueAnimator animation) {
    //         float animatedValue = (float) animation.getAnimatedValue();
    //         int maxMarginY = size.y - (int) animatedValue;
    //         int newMarginY = Math.max(0, Math.min(playerView.getTop(), maxMarginY));
    //         FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) playerView.getLayoutParams();
    //         layoutParams.topMargin = newMarginY;
    //         playerView.setLayoutParams(layoutParams);
    //         playerView.requestLayout();
    //     }
    // });

    ValueAnimator widthAnimator = ValueAnimator.ofFloat(startWidth, endWidth);
    widthAnimator.addUpdateListener(
      new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          float animatedValue = (float) animation.getAnimatedValue();
          playerView.getLayoutParams().width = (int) animatedValue;
          int maxMarginX = size.x - (int) animatedValue;
          int newMarginX = Math.max(
            0,
            Math.min(playerView.getLeft(), maxMarginX)
          );
          FrameLayout.LayoutParams layoutParams =
            (FrameLayout.LayoutParams) playerView.getLayoutParams();
          layoutParams.leftMargin = newMarginX;
          playerView.setLayoutParams(layoutParams);
          playerView.requestLayout();
        }
      }
    );

    ValueAnimator heightAnimator = ValueAnimator.ofFloat(
      startHeight,
      endHeight
    );
    heightAnimator.addUpdateListener(
      new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          float animatedValue = (float) animation.getAnimatedValue();
          playerView.getLayoutParams().height = (int) animatedValue;
          int maxMarginY = size.y - (int) animatedValue;
          int newMarginY = Math.max(
            0,
            Math.min(playerView.getTop(), maxMarginY)
          );
          FrameLayout.LayoutParams layoutParams =
            (FrameLayout.LayoutParams) playerView.getLayoutParams();
          layoutParams.topMargin = newMarginY;
          playerView.setLayoutParams(layoutParams);
          playerView.requestLayout();
        }
      }
    );

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.setDuration(300);

    animatorSet.playTogether(widthAnimator, heightAnimator);
    // animatorSet.playTogether(widthAnimator, heightAnimator, XAnimator, YAnimator);
    animatorSet.start();
  }

  public void makeFloating() {
    Log.i("CapacitorIvsPlayer", "makeFloating");
    playerView.setClipToOutline(true);

    // Show the buttons for 3 seconds
    setAutoHideDisplayButton();

    // get middile of screen x y
    getDisplaySize();
    int halfScreenSizeX = size.x / 2;
    int height = calcHeight(halfScreenSizeX);
    // position the player view at the bottom right corner with a margin of 1/4 of screen
    int x = size.x - halfScreenSizeX - 30;
    int y = size.y - height - 30;
    // get half of width and calculate height
    _setFrame(x, y, halfScreenSizeX, height);
  }

  // function to send webview to front
  private void _setPlayerPosition(Boolean toBack) {
    if (toBack) {
      getBridge()
        .getActivity()
        .findViewById(mainPiPFrameLayoutId)
        .setBackgroundColor(Color.parseColor("#000000"));
      getBridge()
        .getWebView()
        .getParent()
        .bringChildToFront(getBridge().getWebView());
      getBridge().getWebView().setBackgroundColor(0x00000000);
    } else {
      getBridge()
        .getActivity()
        .findViewById(mainPiPFrameLayoutId)
        .setBackgroundColor(Color.parseColor("#00000000"));
      FrameLayout mainPiPFrameLayout = getBridge()
        .getActivity()
        .findViewById(mainPiPFrameLayoutId);
      getBridge()
        .getWebView()
        .getParent()
        .bringChildToFront(mainPiPFrameLayout);
      getBridge().getWebView().setBackgroundColor(0x000000);
    }
  }

  @PluginMethod
  public void setPlayerPosition(PluginCall call) {
    this.toBack = call.getBoolean("toBack", false);
    _setPlayerPosition(toBack);
    call.resolve();
  }

  @PluginMethod
  public void getPlayerPosition(PluginCall call) {
    final JSObject ret = new JSObject();
    ret.put("toBack", toBack);
    call.resolve(ret);
  }

  public void _setPip(Boolean pip, Boolean foregroundApp) {
    Log.i("CapacitorIvsPlayer", "_setPip pip: " + pip);
    getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            Log.i(
              "CapacitorIvsPlayer",
              "foregroundApp: " + foregroundApp + " pip: " + pip
            );
            isPip = pip;
            if (foregroundApp) {
              _setPlayerPosition(!pip);
              if (pip) {
                makeFloating();
              }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              if (pip) {
                setDisplayPipButton(false);
                // Set player width to 100% of parent (Native PiP window)
                _setFrameMatchParent();
              }
            }
          }
        }
      );
  }

  @PluginMethod
  public void setPip(PluginCall call) {
    Boolean pip = call.getBoolean("pip", false);
    _setPip(pip, true);
    call.resolve();
  }

  private void _setFrame(int x, int y, int width, int height) {
    getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            playerViewParams = new FrameLayout.LayoutParams(width, height);
            playerViewParams.setMargins(x, y, 0, 0);
            playerView.setLayoutParams(playerViewParams);
          }
        }
      );
  }

  private void _setFrameMatchParent() {
    getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            playerViewParams =
              new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
              );
            playerViewParams.setMargins(0, 0, 0, 0);
            playerView.setLayoutParams(playerViewParams);
          }
        }
      );
  }

  // function to get default height and width of the screen

  /**
   * This method converts dp unit to equivalent pixels, depending on device density.
   *
   * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
   * @return A float value to represent px equivalent to dp depending on device density
   */
  public float convertDpToPixel(float dp) {
    return (
      dp *
      (
        (float) getContext().getResources().getDisplayMetrics().densityDpi /
        DisplayMetrics.DENSITY_DEFAULT
      )
    );
  }

  /**
   * This method converts device specific pixels to density independent pixels.
   *
   * @param px A value in px (pixels) unit. Which we need to convert into db
   * @return A float value to represent dp equivalent to px value
   */
  public float convertPixelsToDp(float px) {
    return (
      px /
      (
        (float) getContext().getResources().getDisplayMetrics().densityDpi /
        DisplayMetrics.DENSITY_DEFAULT
      )
    );
  }

  @PluginMethod
  public void setFrame(PluginCall call) {
    Log.i("CapacitorIvsPlayer", "setFrame");
    getActivity()
      .runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            getDisplaySize();
            var x = (int) convertDpToPixel(call.getFloat("x", 0.0f));
            var y = (int) convertDpToPixel(call.getFloat("y", 0.0f));
            var width = (int) convertDpToPixel(
              call.getFloat("width", convertPixelsToDp(size.x))
            );
            var height = (int) convertDpToPixel(
              call.getFloat("height", convertPixelsToDp(calcHeight(size.x)))
            );
            _setFrame(x, y, width, height);
            call.resolve();
          }
        }
      );
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
