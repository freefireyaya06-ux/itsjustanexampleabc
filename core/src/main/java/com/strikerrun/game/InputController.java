package com.strikerrun.game;

import com.badlogic.gdx.InputAdapter;

/**
 * Translates raw touch input into swipe gestures:
 * swipe left/right -> lane change, swipe up -> jump, swipe down -> slide,
 * tap (no movement) -> restart when game over.
 */
public class InputController extends InputAdapter {

    private static final float SWIPE_THRESHOLD = 60f; // px, in screen coords

    private final GameWorld gameWorld;

    private float touchStartX, touchStartY;
    private boolean tracking = false;

    public InputController(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchStartX = screenX;
        touchStartY = screenY;
        tracking = true;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!tracking) return false;
        tracking = false;

        float dx = screenX - touchStartX;
        float dy = screenY - touchStartY; // screen Y grows downward

        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        if (absDx < SWIPE_THRESHOLD && absDy < SWIPE_THRESHOLD) {
            gameWorld.onTap();
            return true;
        }

        if (absDx > absDy) {
            if (dx > 0) gameWorld.onSwipeRight();
            else gameWorld.onSwipeLeft();
        } else {
            if (dy > 0) gameWorld.onSwipeDown(); // dragged downward on screen
            else gameWorld.onSwipeUp();          // dragged upward on screen
        }
        return true;
    }
}
