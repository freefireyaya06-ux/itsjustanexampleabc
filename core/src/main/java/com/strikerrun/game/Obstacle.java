package com.strikerrun.game;

/**
 * A football-themed obstacle on the track.
 * GOALPOST/DEFENDER: must be jumped over.
 * BARRICADE_LOW: must be slid under.
 */
public class Obstacle {

    public enum Type {
        CONE(60, 60, false),
        GOALPOST(90, 160, false),
        DEFENDER(70, 150, false),
        BARRICADE_LOW(100, 90, true); // slide under this one

        public final float width;
        public final float height;
        public final boolean slideUnder;

        Type(float width, float height, boolean slideUnder) {
            this.width = width;
            this.height = height;
            this.slideUnder = slideUnder;
        }
    }

    private final Type type;
    private final int lane;
    private float worldY; // distance ahead of player, decreases as it approaches

    public Obstacle(Type type, int lane, float worldY) {
        this.type = type;
        this.lane = lane;
        this.worldY = worldY;
    }

    public void update(float delta, float speed) {
        worldY -= speed * delta;
    }

    public boolean isOffscreen() {
        return worldY < -200;
    }

    public Type getType() {
        return type;
    }

    public int getLane() {
        return lane;
    }

    public float getWorldY() {
        return worldY;
    }
}
