package com.chystialex.paddlebeamarcade;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public class MainActivity extends Activity {
    private GameView gameView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xff030712);
        getWindow().setNavigationBarColor(0xff030712);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        gameView = new GameView(this);
        setContentView(gameView);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) c.hide(WindowInsets.Type.statusBars());
        }
    }

    @Override protected void onPause() {
        super.onPause();
        gameView.pauseGame();
    }

    @Override protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.reloadSettings();
    }
}
