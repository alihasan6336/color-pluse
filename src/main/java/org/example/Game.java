package org.example;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game implements GLEventListener {
    // Game constants
    private static final float GRAVITY = -0.015f;
    private static final float JUMP_VELOCITY = 0.3f;
    private static final float BASE_RING_SPACING = 20.0f; // Increased spacing between rings

    // Game objects
    private PlayerBall playerBall;
    private List<Ring> rings;
    private List<ColorChanger> colorChangers;
    private BackgroundStars backgroundStars;

    // Game state
    private int score;
    private boolean isGameOver;
    private final Random random = new Random();

    // UI and High Score
    private ScoreUI scoreUI;
    private HighScoreManager highScoreManager;

    // Rendering
    private final GLCanvas canvas;

    // Camera/World bounds for background
    private float worldMinX = -10f;
    private float worldMaxX = 10f;
    private float worldMinY = -20f;
    private float worldMaxY = 20f;
    private float cameraOffsetY = 0f;

    public Game(GLCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // Dark space background (near black with slight blue tint)
        gl.glClearColor(0.02f, 0.03f, 0.08f, 1.0f);

        // Initialize background stars
        backgroundStars = new BackgroundStars(worldMinX, worldMaxX, worldMinY, worldMaxY);

        // Initialize UI and high score manager
        scoreUI = new ScoreUI();
        highScoreManager = new HighScoreManager();

        resetGame();
        setupKeyListeners();
    }

    private void setupOrthographicProjection(GL2 gl, int width, int height) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-10, 10, -10 * (float) height / width, 10 * (float) height / width, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private void resetGame() {
        playerBall = new PlayerBall(-8, 0.5f); // Start ball much lower to avoid initial collision
        rings = new ArrayList<>();
        colorChangers = new ArrayList<>();
        score = 0;
        isGameOver = false;
        cameraOffsetY = 0f; // Reset camera offset

        // Reset UI fade animation
        if (scoreUI != null) {
            scoreUI.resetFade();
        }

        spawnInitialRings();
    }

    private void spawnInitialRings() {
        // Spawn first ring well above the player (at y=5.0f when player is at y=-8)
        spawnRing(5.0f);
        // Spawn subsequent rings with proper spacing
        for (int i = 1; i < 3; i++) {
            float lastRingY = rings.get(rings.size() - 1).getY();
            float spacing = BASE_RING_SPACING + random.nextFloat() * 4.0f; // 12-16 units spacing
            spawnRing(lastRingY + spacing);
        }
    }

    private void spawnRing(float y) {
        // Varied ring sizes - outer radius between 3.5 and 6.5
        float outerRadius = 3.5f + random.nextFloat() * 3.0f;
        float thickness = 0.8f + random.nextFloat() * 0.7f; // Thickness between 0.8 and 1.5
        float innerRadius = outerRadius - thickness;

        // Rotation speed inversely proportional to size
        // Larger rings rotate slower, smaller rings rotate faster
        float baseSpeed = 1.5f;
        float rotationSpeed = baseSpeed * (4.5f / outerRadius); // Speed factor based on radius
        rotationSpeed *= (random.nextBoolean() ? 1 : -1); // Random direction

        rings.add(new Ring(y, innerRadius, outerRadius, rotationSpeed));

        // Spawn a color changer occasionally between rings
        if (random.nextFloat() > 0.6f) {
            float colorChangerY = y + (BASE_RING_SPACING / 2);
            colorChangers.add(new ColorChanger(0, colorChangerY, 0.35f));
        }
    }

    private void setupKeyListeners() {
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (isGameOver) {
                        resetGame();
                    } else {
                        playerBall.jump(JUMP_VELOCITY);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
        // Request focus for the canvas to receive key events.
        canvas.requestFocusInWindow();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (scoreUI != null) {
            scoreUI.dispose();
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        update();
        render(gl);
    }

    private void update() {
        if (isGameOver) return;

        playerBall.applyGravity(GRAVITY);
        playerBall.update();

        // Update background stars
        if (backgroundStars != null) {
            backgroundStars.update(0); // Stars fall on their own, not affected by camera
        }

        // Update UI fade animation
        if (scoreUI != null) {
            scoreUI.update();
        }

        // Move camera (world) down to follow the ball
        if (playerBall.getY() > 0) {
            float dy = playerBall.getY();
            playerBall.setY(0);
            cameraOffsetY += dy; // Track camera offset for background
            for (Ring ring : rings) {
                ring.setY(ring.getY() - dy);
            }
            for (ColorChanger changer : colorChangers) {
                changer.setY(changer.getY() - dy);
            }
        }

        // Check for collisions
        checkCollisions();

        // Update rings
        rings.forEach(Ring::update);

        // Remove off-screen elements and spawn new ones
        rings.removeIf(ring -> ring.getY() < -20);
        colorChangers.removeIf(changer -> changer.getY() < -20);

        // Spawn new ring when the last one is getting close
        if (!rings.isEmpty() && rings.get(rings.size() - 1).getY() < 15) {
            float lastRingY = rings.get(rings.size() - 1).getY();
            float spacing = BASE_RING_SPACING + random.nextFloat() * 4.0f;
            spawnRing(lastRingY + spacing);
        }
    }

    private void checkCollisions() {
        // Ring collision
        for (Ring ring : rings) {
            if (playerBall.isCollidingWithRing(ring)) {
                int segment = ring.getSegmentAtAngle(playerBall.getX(), playerBall.getY());
                if (segment != playerBall.getColorIndex()) {
                    isGameOver = true;
                    // Update high score when game ends
                    if (highScoreManager != null) {
                        highScoreManager.updateHighScore(score);
                    }
                    return;
                } else if (!ring.isPassed()) {
                    score++;
                    ring.setPassed(true);
                    System.out.println("Score: " + score);
                }
            }
        }

        // Color changer collision
        colorChangers.forEach(changer -> {
            if (changer.isColliding(playerBall)) {
                playerBall.setColor(changer.getColorIndex());
                changer.setY(-20); // "Remove" it
            }
        });

        // Out of bounds
        if (playerBall.getY() < -12) {
            isGameOver = true;
            // Update high score when game ends
            if (highScoreManager != null) {
                highScoreManager.updateHighScore(score);
            }
        }
    }

    private void render(GL2 gl) {
        // Render background stars first (behind everything)
        if (backgroundStars != null) {
            backgroundStars.draw(gl);
        }

        // Render game objects
        playerBall.draw(gl);
        rings.forEach(ring -> ring.draw(gl));
        colorChangers.forEach(changer -> changer.draw(gl));

        // Render score UI overlay (always on top)
        if (scoreUI != null && highScoreManager != null) {
            scoreUI.draw(gl, score, highScoreManager.getHighScore());

            // Draw game over screen if game ended
            if (isGameOver) {
                scoreUI.drawGameOver(gl);
            }
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        setupOrthographicProjection(gl, width, height);

        // Update world bounds for background stars
        worldMaxY = 10 * (float) height / width;
        worldMinY = -worldMaxY;
        if (backgroundStars != null) {
            backgroundStars.updateBounds(worldMinX, worldMaxX, worldMinY, worldMaxY);
        }

        // Update UI dimensions
        if (scoreUI != null) {
            scoreUI.updateDimensions(width, height);
        }
    }
}

