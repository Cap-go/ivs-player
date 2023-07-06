package ee.forgr.ivsplayer;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.media.MediaSession2;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.util.Log;
import android.util.Rational;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.app.PictureInPictureModeChangedInfo;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CapacitorIvsPlayer")
public class CapacitorIvsPlayerPlugin extends Plugin {

    private boolean ignoreNextPause = false;
    private boolean autoPip = false;

    private final int frameLayoutViewId = 256;
    private final int mainPiPFrameLayoutId = 257;

    @PluginMethod
    public void create(PluginCall call) {
//        var x = call.getInt("x", 0);
//        var y = call.getInt("y", 0);
//        var width = call.getInt("width", 0);
//        var height = call.getInt("height", 0);

//        Log.i("CapacitorIvsPlayerX", "create");
//        Intent intent = new Intent(getContext(), CapacitorIvsPlayer.class);
//
//        ContextCompat.startActivity(getContext(), intent, null);
        // wait for the activity to be created
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        sendPlayerControlBroadcast("create");
//        String url = call.getString("url");
//        if (url != null) {
//            setUrl(url);
//        }
//        Boolean autoPlay = call.getBoolean("autoPlay");
//        if (autoPlay != null && autoPlay) {
//            sendPlayerControlBroadcast("start");
//        }
//        Boolean toBack = call.getBoolean("toBack");
//        if (toBack != null && toBack) {
//        getBridge().getWebView().setBackgroundColor(0x00000000);
//        getActivity().runOnUiThread(() -> getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView()));
//        }
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
            mainPiPFrameLayout.setBackgroundColor(getBridge().getActivity().getResources().getColor(R.color.colorAccent));

            SurfaceView surfaceView = new SurfaceView(getActivity().getApplicationContext());
            FrameLayout.LayoutParams surfaceViewParams = new FrameLayout.LayoutParams(
                    1000,
                    500
            );
            surfaceViewParams.setMargins(50, 200, 0, 0);
            surfaceView.setLayoutParams(surfaceViewParams);
            surfaceView.setBackgroundColor(getBridge().getActivity().getResources().getColor(R.color.colorPrimary));

            mainPiPFrameLayout.addView(surfaceView);

            final FrameLayout  finalMainPiPFrameLayout = mainPiPFrameLayout;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) getBridge().getWebView().getParent()).addView(finalMainPiPFrameLayout);
                    getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
                    getBridge().getWebView().setBackgroundColor(0x00000000);
                    Rational aspectRatio = new Rational(1, 1);
                    
                    PictureInPictureParams params = new PictureInPictureParams.Builder()
                            .setAspectRatio(aspectRatio)
                            .build();
                    getBridge().getActivity().enterPictureInPictureMode(params);
                    getBridge().getActivity().addOnPictureInPictureModeChangedListener(new Consumer<PictureInPictureModeChangedInfo>() {

                        @Override
                        public void accept(PictureInPictureModeChangedInfo pictureInPictureModeChangedInfo) {
                            if (!pictureInPictureModeChangedInfo.isInPictureInPictureMode()) {
                                getBridge().getWebView().setVisibility(View.VISIBLE);
                            } else {
                                getBridge().getWebView().setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            });
//            fragmentUtils.loadFragment(pkFragment, mainPiPFrameLayoutId);
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

    @PluginMethod
    public void getUrl(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void getState(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void setAutoQuality(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void getAutoQuality(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void setPip(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void setFrame(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void getFrame(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void setMute(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void getMute(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void setQuality(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void getQuality(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void getQualities(PluginCall call) {
        // TODO
        call.resolve();
    }
    //   addListener(
    //     eventName: "tooglePip",
    //     listenerFunc: (data: {
    //       pip: boolean;
    //     }) => void
    //   ): Promise<PluginListenerHandle> & PluginListenerHandle;
    //   addListener(
    //     eventName: "closePip",
    //     listenerFunc: () => void
    //   ): Promise<PluginListenerHandle> & PluginListenerHandle;
}
