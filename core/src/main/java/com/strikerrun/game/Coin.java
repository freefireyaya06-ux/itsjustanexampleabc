package com.strikerrun.game;

/** A collectible football-shaped coin. */
public class Coin {

    private final int lane;
    private float worldY;
    private boolean collected = false;

    public Coin(int lane, float worldY) {
        this.lane = lane;
        this.worldY = worldY;
    }

    public void update(float delta, float speed) {
        worldY -= speed * delta;
    }

    public boolean isOffscreen() {
        return worldY < -200;
    }

    public int getLane() {
        return lane;
    }

    public float getWorldY() {
        return worldY;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
