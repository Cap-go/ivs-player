package ee.forgr.ivsplayer;

import android.app.PictureInPictureParams;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.app.PictureInPictureModeChangedInfo;
import androidx.core.util.Consumer;
import androidx.lifecycle.Lifecycle;
import com.amazonaws.ivs.player.PlayerView;
import com.amazonaws.ivs.player.Quality;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

@CapacitorPlugin(name = "CapacitorIvsPlayer")
public class CapacitorIvsPlayerPlugin extends Plugin {
    private final int mainPiPFrameLayoutId = 257;
    private PlayerView playerView;
    private String lastUrl = "";

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
        Log.i("CapacitorIvsPlayerX", "create");
        String url = call.getString("url");
        if (url == null) {
            call.reject("url is required");
        }
        lastUrl = url;
        Boolean autoPlay = call.getBoolean("autoPlay", false);
        Boolean toBack = call.getBoolean("toBack", false);
        FrameLayout mainPiPFrameLayout = getBridge().getActivity().findViewById(mainPiPFrameLayoutId);
        if (mainPiPFrameLayout != null) {
            Log.i("CapacitorIvsPlayerX", "FrameLayout for VideoPicker already exists");
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
                                playerView.getPlayer().pause();
                                self.notifyListeners("closePip", ret);
                            }
                            else if (getBridge().getActivity().getLifecycle().getCurrentState() == Lifecycle.State.STARTED){
                                if (!pictureInPictureModeChangedInfo.isInPictureInPictureMode()) {
                                    getBridge().getWebView().setVisibility(View.VISIBLE);
                                    ret.put("pip", false);
                                } else {
                                    getBridge().getWebView().setVisibility(View.INVISIBLE);
                                    ret.put("pip", true);
                                }
                                self.notifyListeners("tooglePip", ret);
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
    public void setPip(PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Rational aspectRatio = new Rational(16, 9);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PictureInPictureParams params = new PictureInPictureParams.Builder()
                            .setAspectRatio(aspectRatio)
                            .build();
                    getBridge().getActivity().enterPictureInPictureMode(params);
                }
            }
        });
        call.resolve();
    }

    @PluginMethod
    public void setFrame(PluginCall call) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        var x = call.getInt("x", 0);
        var y = call.getInt("y", 0);
        var width = call.getInt("width", size.x);
        var height = call.getInt("height", (int) (size.x * 9.0 / 16.0));
        FrameLayout.LayoutParams playerViewParams = new FrameLayout.LayoutParams(width, height);
        playerViewParams.setMargins(x, y, 0, 0);
        playerView.setLayoutParams(playerViewParams);
        call.resolve();
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
