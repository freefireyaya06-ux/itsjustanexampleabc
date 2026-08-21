# Striker Run

Endless runner (Temple Run 2 / Subway Surfers style) featuring **Striker 7**,
a football-themed runner. Built with libGDX + Android.

## Gameplay
- Auto-runs forward on a 3-lane track
- **Swipe left/right** — change lane
- **Swipe up** — jump (clear cones, goalposts, defenders)
- **Swipe down** — slide (duck under low barricades)
- Collect football-shaped coins for bonus score
- Speed increases the longer you survive
- Tap screen to restart after crashing

## Project structure
```
striker-run/
├── core/          # Shared game logic (platform-independent)
│   └── src/main/java/com/strikerrun/game/
│       ├── StrikerRunGame.java   # entry point / render loop
│       ├── GameWorld.java        # spawning, collisions, scoring
│       ├── Player.java           # Striker 7 state machine
│       ├── Obstacle.java
│       ├── Coin.java
│       └── InputController.java  # swipe gesture detection
├── android/       # Android-specific wrapper (produces the APK)
├── gradle/wrapper/
├── gradlew
└── .github/workflows/build-apk.yml
```

## Building the APK

### Option A — Let GitHub Actions do everything (recommended for your workflow)
1. Push this whole folder to a GitHub repo.
2. GitHub Actions will automatically:
   - Generate the Gradle wrapper jar (first run only, since it's a binary
     file that can't be hand-written)
   - Run `./gradlew :android:assembleDebug`
   - Upload the resulting APK as a build artifact
3. Go to the **Actions** tab → latest run → download `striker-run-debug-apk`.

### Option B — Build inside Codespaces manually
```bash
# One-time: generate the wrapper jar (needs internet, which Codespaces has)
gradle wrapper --gradle-version 8.7

chmod +x gradlew
./gradlew :android:assembleDebug
```
APK will be at `android/build/outputs/apk/debug/android-debug.apk`.

## Current state (placeholder art)
Rendering currently uses flat-colored rectangles/circles via `ShapeRenderer`
so the game is fully playable immediately:
- Red block = Striker 7
- Orange = cone, White = goalpost, Red = defender, Brown = low barricade
- Gold circles = coins

Swap in real sprites/3D models later by replacing the `ShapeRenderer` calls
in `GameWorld.render()` with `SpriteBatch` texture draws — the game logic
won't need to change.

## Known next steps
- [ ] Add real sprite art (Striker 7 running/jump/slide animations)
- [ ] Add sound effects (coin pickup, crash, footsteps)
- [ ] Power-ups: magnet, shield, score multiplier
- [ ] High score persistence (Android `SharedPreferences`)
- [ ] Parallax stadium/crowd background
