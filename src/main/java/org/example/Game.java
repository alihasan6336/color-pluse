package org.example;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Game supports singleplayer and split-screen multiplayer.
 * In multiplayer mode: left = player1 (SPACE), right = player2 (UP ARROW).
 * Each side has independent world state (rings, color changers, score).
 */
public class Game implements GLEventListener {
    private static final float GRAVITY = -0.01f;
    private static final float JUMP_VELOCITY = 0.2f;
    private static final float BASE_RING_SPACING = 20.0f;

    // Support 1 or 2 players
    public final boolean isMultiplayer;
    private final PlayerBall[] players; // index 0 = left (player1), index 1 = right (player2 if multiplayer)
    private final List<Ring>[] rings;
    private final List<ColorChanger>[] colorChangers;
    private final BackgroundStars backgroundStars; // not strictly per-player, but drawn in each viewport

    private final Random random = new Random();

    // state per player
    private final int[] scores;
    private final boolean[] isGameOver;
    private final boolean[] hasPressedSpace;

    private GameStateListener gameStateListener; // used for end-game UI callback for singleplayer; multiplayer not using it

    // world bounds (same for both worlds, but kept per-player for flexibility)
    private float worldMinX = -10f;
    private float worldMaxX = 10f;
    private float worldMinY = -20f;
    private float worldMaxY = 20f;

    public Game(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
        int playersCount = isMultiplayer ? 2 : 1;
        players = new PlayerBall[playersCount];
        rings = new List[playersCount];
        colorChangers = new List[playersCount];
        scores = new int[playersCount];
        isGameOver = new boolean[playersCount];
        hasPressedSpace = new boolean[playersCount];
        // initialize arrays
        for (int i = 0; i < playersCount; i++) {
            rings[i] = new ArrayList<>();
            colorChangers[i] = new ArrayList<>();
        }
        // create a single background stars manager; we will update its bounds in init/reshape
        backgroundStars = new BackgroundStars(worldMinX, worldMaxX, worldMinY, worldMaxY);
    }
    public boolean isMultiplayer() {
        return isMultiplayer;
    }


    public void setGameStateListener(GameStateListener listener) {
        this.gameStateListener = listener;
    }
    private void updateWorld(int idx) {
        if (isGameOver[idx]) return; // player cannot move if lost

        PlayerBall p = players[idx];

        // Apply gravity only after first press
        if (hasPressedSpace[idx]) {
            p.applyGravity(GRAVITY);
        }
        p.update();

        // Camera-style movement: move world down if ball y > 0
        if (hasPressedSpace[idx] && p.getY() > 0) {
            float dy = p.getY();
            p.setY(0);
            for (Ring r : rings[idx]) r.setY(r.getY() - dy);
            for (ColorChanger c : colorChangers[idx]) c.setY(c.getY() - dy);
        }

        // Only check collisions if player started
        if (hasPressedSpace[idx]) {
            checkCollisionsIdx(idx);
        }

        // Update ring rotations
        for (Ring r : rings[idx]) r.update();

        // Remove offscreen objects
        rings[idx].removeIf(r -> r.getY() < -20);
        colorChangers[idx].removeIf(c -> c.getY() < -20);

        // Spawn new rings if needed
        if (!rings[idx].isEmpty() && rings[idx].get(rings[idx].size() - 1).getY() < 15) {
            float lastRingY = rings[idx].get(rings[idx].size() - 1).getY();
            float spacing = BASE_RING_SPACING + random.nextFloat() * 4.0f;
            spawnRing(idx, lastRingY + spacing);
        }

        // Out-of-bounds check
        if (p.getY() < -12) {
            isGameOver[idx] = true;
            checkBothPlayersLost();
        }
    }

    /**
     * Maps key presses:
     * - Player 1 (left): SPACE
     * - Player 2 (right): UP arrow
     */
    public void handleKeyPress(int keyCode) {
        // Player 1
        if (keyCode == KeyEvent.VK_SPACE && !isGameOver[0]) {
            hasPressedSpace[0] = true;
            players[0].jump(JUMP_VELOCITY);
            return;
        }

        // Player 2
        if (isMultiplayer && keyCode == KeyEvent.VK_UP && !isGameOver[1]) {
            hasPressedSpace[1] = true;
            players[1].jump(JUMP_VELOCITY);
        }

        // ESC exit
        if (keyCode == KeyEvent.VK_ESCAPE && gameStateListener != null) {
            gameStateListener.onGameExit();
        }
    }


    public int getScore(int playerIdx) {
        return scores[playerIdx];
    }


    public boolean isGameOver(int playerIndex) {
        return isGameOver[playerIndex];
    }

    public void resetGamePublic() {
        resetAll();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        // set clear color if needed by caller; using same as before
        // initialize players and worlds
        resetAll();
    }

    private void resetAll() {
        int playersCount = players.length;
        for (int i = 0; i < playersCount; i++) {
            // place player X position: left side -8, right side +8 (but playerBall.x is fixed at 0 in PlayerBall class)
            // We'll keep PlayerBall centered horizontally within each viewport by leaving x at 0 and adjusting camera/view.
            players[i] = new PlayerBall(0.5f, 0.6f); // radius slightly larger for visibility
            rings[i].clear();
            colorChangers[i].clear();
            scores[i] = 0;
            isGameOver[i] = false;
            hasPressedSpace[i] = false;
            spawnInitialRings(i);
        }
    }

    private void resetPlayer(int idx) {
        rings[idx].clear();
        colorChangers[idx].clear();
        scores[idx] = 0;
        isGameOver[idx] = false;
        hasPressedSpace[idx] = false;
        players[idx] = new PlayerBall(0.5f, 0.6f);
        spawnInitialRings(idx); // will place player under first ring
    }


    private void spawnInitialRings(int worldIdx) {
        float firstRingY = 5.0f;
        spawnRing(worldIdx, firstRingY);
        for (int i = 1; i < 3; i++) {
            float lastRingY = rings[worldIdx].get(rings[worldIdx].size() - 1).getY();
            float spacing = BASE_RING_SPACING + random.nextFloat() * 4.0f;
            spawnRing(worldIdx, lastRingY + spacing);
        }

        // Set ball starting position just below the first ring
        players[worldIdx].setY(firstRingY - 2.0f); // adjust offset so it's under the ring
    }



    private void spawnRing(int worldIdx, float y) {
        float outerRadius = 3.5f + random.nextFloat() * 3.0f;
        float thickness = 0.8f + random.nextFloat() * 0.7f;
        float innerRadius = outerRadius - thickness;
        float baseSpeed = 1.5f;
        float rotationSpeed = baseSpeed * (4.5f / outerRadius) * (random.nextBoolean() ? 1 : -1);

        rings[worldIdx].add(new Ring(y, innerRadius, outerRadius, rotationSpeed));

        if (random.nextFloat() > 0.6f) {
            float colorChangerY = y + (BASE_RING_SPACING / 2);
            colorChangers[worldIdx].add(new ColorChanger(0, colorChangerY, 0.35f));
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}


    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        int surfaceWidth = drawable.getSurfaceWidth();
        int surfaceHeight = drawable.getSurfaceHeight();

        if (isMultiplayer) {
            gl.glViewport(0, 0, surfaceWidth / 2, surfaceHeight);
            renderViewport(gl, surfaceWidth / 2, surfaceHeight, 0);

            gl.glViewport(surfaceWidth / 2, 0, surfaceWidth / 2, surfaceHeight);
            renderViewport(gl, surfaceWidth / 2, surfaceHeight, 1);

            // Check if both players lost
            if (isGameOver[0] && isGameOver[1]) {
                announceWinner();
            }
        } else {
            gl.glViewport(0, 0, surfaceWidth, surfaceHeight);
            renderViewport(gl, surfaceWidth, surfaceHeight, 0);

            if (isGameOver[0] && gameStateListener != null) {
                gameStateListener.onGameExit();
            }
        }
    }


    /**
     * Renders one player's viewport. Each world is independent.
     *
     * @param gl OpenGL context
     * @param vpWidth viewport width in pixels
     * @param vpHeight viewport height in pixels
     * @param playerIdx 0 or 1
     */
    private void renderViewport(GL2 gl, int vpWidth, int vpHeight, int playerIdx) {
        // Set up projection scaled to viewport aspect ratio
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        float aspect = (float) vpHeight / (float) vpWidth; // note swapped to match previous ortho style
        // keep same world width (-10 to 10) but adjust vertical bounds using aspect
        gl.glOrtho(-10, 10, -10 * aspect, 10 * aspect, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        // Update world and objects
        updateWorld(playerIdx);
        // Render background (shared but drawn per-viewport)
        backgroundStars.draw(gl);

        // Draw rings, player, color changers
        players[playerIdx].draw(gl);
        for (Ring r : rings[playerIdx]) r.draw(gl);
        for (ColorChanger c : colorChangers[playerIdx]) c.draw(gl);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // update world bounds based on aspect ratio to match earlier behavior
        float aspect = (float) height / width;
        worldMaxY = 10 * aspect;
        worldMinY = -worldMaxY;
        if (backgroundStars != null) backgroundStars.updateBounds(worldMinX, worldMaxX, worldMinY, worldMaxY);
    }
    private void announceWinner() {
        int score1 = scores[0];
        int score2 = scores[1];

        String message;
        if (score1 > score2) {
            message = "Player 1 Wins! " + score1 + " - " + score2;
        } else if (score2 > score1) {
            message = "Player 2 Wins! " + score2 + " - " + score1;
        } else {
            message = "It's a Tie! " + score1 + " - " + score2;
        }

        // Show in a Swing popup
        javax.swing.SwingUtilities.invokeLater(() ->
                javax.swing.JOptionPane.showMessageDialog(null, message, "Game Over", javax.swing.JOptionPane.INFORMATION_MESSAGE)
        );

        // Reset both players for next game
        resetAll();
    }

    /**
     * Updates the world for one player (gravity, collisions, spawning).
     */
    private void checkBothPlayersLost() {
        if (isMultiplayer && isGameOver[0] && isGameOver[1] && gameStateListener != null) {
            gameStateListener.onGameExit(); // trigger EndGame screen
        }
    }


    private void checkCollisionsIdx(int idx) {
        PlayerBall p = players[idx];

        // Ring collision
        for (Ring ring : rings[idx]) {
            if (p.isCollidingWithRing(ring)) {
                int segment = ring.getSegmentAtAngle(p.getX(), p.getY());
                if (segment != p.getColorIndex()) {
                    isGameOver[idx] = true;
                    return;
                } else if (!ring.isPassed()) {
                    scores[idx]++;
                    ring.setPassed(true);
                    System.out.println("Player " + (idx+1) + " Score: " + scores[idx]);
                }
            }
        }

        // Color changer collision
        for (ColorChanger changer : colorChangers[idx]) {
            if (changer.isColliding(p)) {
                p.changeColor();
                changer.setY(-20);
            }
        }

    }
}
