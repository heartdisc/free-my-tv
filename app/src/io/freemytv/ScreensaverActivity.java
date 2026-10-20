package io.freemytv;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

/**
 * Fullscreen screensaver activity triggered when Back is pressed on the home screen (tvOS style).
 * Any key press immediately dismisses the screensaver and returns to the home screen.
 */
public class ScreensaverActivity extends Activity {
    private DreamView view;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui.init(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = new DreamView(this);
        setContentView(view);
    }

    @Override protected void onResume() {
        super.onResume();
        if (view != null) view.start();
    }

    @Override protected void onPause() {
        if (view != null) view.stop();
        super.onPause();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            finish();
            return true;
        }
        return true;
    }
}
