package com.strikerrun.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Owns the whole simulation: player, lanes, obstacles, coins, score, speed ramp.
 * Rendering here is placeholder shapes (ShapeRenderer) so the game is playable
 * immediately; swap in sprites/models later without touching this logic.
 */
public class GameWorld {

    private static final int LANE_COUNT = 3;
    private static final float PLAYER_GROUND_Y = 260f;
    private static final float SPAWN_INTERVAL_START = 1.4f;
    private static final float SPAWN_INTERVAL_MIN = 0.7f;
    private static final float BASE_SPEED = 420f;   // px/sec
    private static final float MAX_SPEED = 1000f;
    private static final float SPEED_RAMP_PER_SEC = 4f; // how fast difficulty increases

    private final float worldWidth;
    private final float worldHeight;
    private final float[] lanePositions;

    private final Player player;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();

    private float speed = BASE_SPEED;
    private float spawnTimer = 0f;
    private float spawnInterval = SPAWN_INTERVAL_START;
    private float elapsedTime = 0f;
    private int score = 0;
    private boolean gameOver = false;

    public GameWorld(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        lanePositions = new float[LANE_COUNT];
        float laneWidth = worldWidth / LANE_COUNT;
        for (int i = 0; i < LANE_COUNT; i++) {
            lanePositions[i] = laneWidth * i + laneWidth / 2f;
        }

        player = new Player(1); // start in middle lane
        player.setX(lanePositions[1]);
    }

    public void update(float delta) {
        if (gameOver) return;

        elapsedTime += delta;
        speed = Math.min(MAX_SPEED, BASE_SPEED + elapsedTime * SPEED_RAMP_PER_SEC);
        spawnInterval = Math.max(SPAWN_INTERVAL_MIN, SPAWN_INTERVAL_START - elapsedTime * 0.01f);

        player.update(delta, lanePositions);

        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            spawnWave();
        }

        updateObstacles(delta);
        updateCoins(delta);
        checkCollisions();

        // Distance-based score, always increasing while alive
        score += Math.round(speed * delta * 0.1f);
    }

    private void spawnWave() {
        int lane = MathUtils.random(0, LANE_COUNT - 1);
        Obstacle.Type[] types = Obstacle.Type.values();
        Obstacle.Type type = types[MathUtils.random(0, types.length - 1)];
        obstacles.add(new Obstacle(type, lane, worldHeight + 100));

        // Occasionally drop a coin in a different (safe-ish) lane
        if (MathUtils.randomBoolean(0.6f)) {
            int coinLane = MathUtils.random(0, LANE_COUNT - 1);
            coins.add(new Coin(coinLane, worldHeight + 250));
        }
    }

    private void updateObstacles(float delta) {
        Iterator<Obstacle> it = obstacles.iterator();
        while (it.hasNext()) {
            Obstacle o = it.next();
            o.update(delta, speed);
            if (o.isOffscreen()) it.remove();
        }
    }

    private void updateCoins(float delta) {
        Iterator<Coin> it = coins.iterator();
        while (it.hasNext()) {
            Coin c = it.next();
            c.update(delta, speed);
            if (c.isOffscreen() || c.isCollected()) it.remove();
        }
    }

    private void checkCollisions() {
        float playerScreenY = PLAYER_GROUND_Y + player.getY();

        for (Obstacle o : obstacles) {
            if (o.getLane() != player.getCurrentLane()) continue;

            boolean overlapsY = Math.abs(o.getWorldY() - PLAYER_GROUND_Y) < (o.getType().height / 2f + 40f);
            if (!overlapsY) continue;

            boolean avoided;
            if (o.getType().slideUnder) {
                avoided = player.getState() == Player.State.SLIDING;
            } else {
                // Must be jumping AND high enough to clear it
                avoided = player.getState() == Player.State.JUMPING && player.getY() > o.getType().height * 0.5f;
            }

            if (!avoided) {
                player.crash();
                gameOver = true;
            }
        }

        for (Coin c : coins) {
            if (c.isCollected()) continue;
            if (c.getLane() != player.getCurrentLane()) continue;
            if (Math.abs(c.getWorldY() - playerScreenY) < 60f) {
                c.collect();
                score += 50;
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Track: 3 lane strips
        float laneWidth = worldWidth / LANE_COUNT;
        for (int i = 0; i < LANE_COUNT; i++) {
            shapeRenderer.setColor(i % 2 == 0 ? new Color(0.2f, 0.6f, 0.2f, 1f) : new Color(0.25f, 0.65f, 0.25f, 1f));
            shapeRenderer.rect(i * laneWidth, 0, laneWidth, worldHeight);
        }

        // Obstacles
        for (Obstacle o : obstacles) {
            Obstacle.Type t = o.getType();
            switch (t) {
                case CONE: shapeRenderer.setColor(Color.ORANGE); break;
                case GOALPOST: shapeRenderer.setColor(Color.WHITE); break;
                case DEFENDER: shapeRenderer.setColor(Color.RED); break;
                case BARRICADE_LOW: shapeRenderer.setColor(Color.BROWN); break;
            }
            float cx = lanePositions[o.getLane()];
            shapeRenderer.rect(cx - t.width / 2f, o.getWorldY() - t.height / 2f, t.width, t.height);
        }

        // Coins
        shapeRenderer.setColor(Color.GOLD);
        for (Coin c : coins) {
            if (c.isCollected()) continue;
            float cx = lanePositions[c.getLane()];
            shapeRenderer.circle(cx, c.getWorldY(), 20f);
        }

        // Player (Striker 7) - jersey-colored block, ducks when sliding
        shapeRenderer.setColor(player.isCrashed() ? Color.DARK_GRAY : new Color(0.9f, 0.1f, 0.1f, 1f));
        float py = PLAYER_GROUND_Y + player.getY();
        shapeRenderer.rect(player.getX() - player.getWidth() / 2f, py - player.getHeight() / 2f,
                player.getWidth(), player.getHeight());

        shapeRenderer.end();

        batch.begin();
        font.draw(batch, "Score: " + score, 30, worldHeight - 30);
        if (gameOver) {
            font.draw(batch, "GAME OVER - Tap to Restart", worldWidth / 2f - 260, worldHeight / 2f);
        }
        batch.end();
    }

    // --- Input hooks ---
    public void onSwipeLeft() { player.moveLeft(); }
    public void onSwipeRight() { player.moveRight(); }
    public void onSwipeUp() { player.jump(); }
    public void onSwipeDown() { player.slide(); }

    public void onTap() {
        if (gameOver) restart();
    }

    public void restart() {
        obstacles.clear();
        coins.clear();
        speed = BASE_SPEED;
        spawnTimer = 0f;
        spawnInterval = SPAWN_INTERVAL_START;
        elapsedTime = 0f;
        score = 0;
        gameOver = false;
        player.reset(1);
        player.setX(lanePositions[1]);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getScore() {
        return score;
    }
}
