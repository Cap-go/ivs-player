package ee.forgr.ivsplayer;

import android.app.PictureInPictureParams;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.app.PictureInPictureModeChangedInfo;
import androidx.core.util.Consumer;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CapacitorIvsPlayer")
public class CapacitorIvsPlayerPlugin extends Plugin {
    private final int mainPiPFrameLayoutId = 257;

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
            mainPiPFrameLayout.setBackgroundColor(getBridge().getActivity().getResources().getColor(R.color.colorAccent));

            SurfaceView surfaceView = new SurfaceView(getActivity().getApplicationContext());
            FrameLayout.LayoutParams surfaceViewParams = new FrameLayout.LayoutParams(
                    width,
                    height
            );
            surfaceViewParams.setMargins(x, y, 0, 0);
            surfaceView.setLayoutParams(surfaceViewParams);
            surfaceView.setBackgroundColor(getBridge().getActivity().getResources().getColor(R.color.colorPrimary));

            mainPiPFrameLayout.addView(surfaceView);

            final FrameLayout  finalMainPiPFrameLayout = mainPiPFrameLayout;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) getBridge().getWebView().getParent()).addView(finalMainPiPFrameLayout);
                    if (toBack != null && toBack) {
                        getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
                        getBridge().getWebView().setBackgroundColor(0x00000000);
                    }
                    Rational aspectRatio = new Rational(1, 1);

                    PictureInPictureParams params = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        params = new PictureInPictureParams.Builder()
                                .setAspectRatio(aspectRatio)
                                .build();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getBridge().getActivity().enterPictureInPictureMode(params);
                    }
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
        }
        call.resolve();
    }

    @Override
    public void load() {
        super.load();
    }

    private void setUrl(String url) {
        // TODO
    }

    @PluginMethod
    public void togglePip(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void start(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void pause(PluginCall call) {
        // TODO
        call.resolve();
    }

    @PluginMethod
    public void delete(PluginCall call) {
        // TODO
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
