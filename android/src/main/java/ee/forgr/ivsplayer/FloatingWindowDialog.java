package ee.forgr.ivsplayer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.amazonaws.ivs.player.PlayerView;

public class FloatingWindowDialog extends Dialog {

    public final PlayerView playerView;

    public FloatingWindowDialog(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        playerView = new PlayerView(context);
        playerView.setControlsEnabled(false);
        playerView.setId(View.generateViewId());
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        playerView.setLayoutParams(layoutParams);

        constraintLayout.addView(playerView);
        setContentView(constraintLayout);

        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams();
        windowLayoutParams.copyFrom(getWindow().getAttributes());
        windowLayoutParams.gravity = Gravity.TOP | Gravity.START;
        windowLayoutParams.x = 0;
        windowLayoutParams.y = 200;
        windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowLayoutParams.format = PixelFormat.TRANSLUCENT;
        windowLayoutParams.windowAnimations = android.R.style.Animation_Dialog;

        getWindow().setAttributes(windowLayoutParams);
    }
}
