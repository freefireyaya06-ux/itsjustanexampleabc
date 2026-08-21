package com.strikerrun.game;

/**
 * Striker 7 - the player character.
 * Lives on one of 3 lanes, can jump over obstacles or slide under them.
 */
public class Player {

    public enum State { RUNNING, JUMPING, SLIDING, CRASHED }

    private static final float JUMP_DURATION = 0.6f;
    private static final float SLIDE_DURATION = 0.5f;
    private static final float JUMP_HEIGHT = 220f;
    private static final float LANE_SWITCH_SPEED = 12f; // higher = snappier

    private int currentLane;   // 0 = left, 1 = middle, 2 = right
    private int targetLane;
    private float x;           // world x, interpolates toward the target lane
    private float y;           // ground-relative height (for jump arc)
    private float width = 80f;
    private float height = 140f;

    private State state = State.RUNNING;
    private float stateTimer = 0f;

    public Player(int startLane) {
        this.currentLane = startLane;
        this.targetLane = startLane;
    }

    public void update(float delta, float[] lanePositions) {
        // Smoothly slide toward target lane x-position
        float targetX = lanePositions[targetLane];
        x += (targetX - x) * Math.min(1f, LANE_SWITCH_SPEED * delta);

        if (state == State.JUMPING) {
            stateTimer += delta;
            float t = stateTimer / JUMP_DURATION;
            if (t >= 1f) {
                state = State.RUNNING;
                y = 0;
            } else {
                // Parabolic arc: peaks at t = 0.5
                y = JUMP_HEIGHT * (1f - (2f * t - 1f) * (2f * t - 1f));
            }
        } else if (state == State.SLIDING) {
            stateTimer += delta;
            if (stateTimer >= SLIDE_DURATION) {
                state = State.RUNNING;
            }
        }
    }

    public void moveLeft() {
        if (state == State.CRASHED) return;
        if (targetLane > 0) targetLane--;
    }

    public void moveRight() {
        if (state == State.CRASHED) return;
        if (targetLane < 2) targetLane++;
    }

    public void jump() {
        if (state == State.RUNNING) {
            state = State.JUMPING;
            stateTimer = 0f;
        }
    }

    public void slide() {
        if (state == State.RUNNING) {
            state = State.SLIDING;
            stateTimer = 0f;
        }
    }

    public void crash() {
        state = State.CRASHED;
    }

    /** Resets the player back to running state in the middle lane, ready for a new run. */
    public void reset(int startLane) {
        this.currentLane = startLane;
        this.targetLane = startLane;
        this.state = State.RUNNING;
        this.stateTimer = 0f;
        this.y = 0f;
    }

    public boolean isCrashed() {
        return state == State.CRASHED;
    }

    public State getState() {
        return state;
    }

    public int getCurrentLane() {
        return targetLane;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        // Sliding lowers the hitbox (ducking under obstacles)
        return state == State.SLIDING ? height * 0.5f : height;
    }
}
