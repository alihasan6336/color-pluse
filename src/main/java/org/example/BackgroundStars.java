package org.example;

import com.jogamp.opengl.GL2;
import java.util.Random;

/**
 * Manages a procedural infinite starfield background.
 * Stars slowly fall down the screen and wrap around to create an infinite effect.
 */
public class BackgroundStars {
    private static final int STAR_COUNT = 250; // Number of stars
    private static final float MIN_SPEED = 0.01f; // Minimum fall speed
    private static final float MAX_SPEED = 0.05f; // Maximum fall speed

    private final Star[] stars;
    private final Random random;

    // Screen bounds (in world coordinates)
    private float minX, maxX, minY, maxY;

    /**
     * Individual star data structure
     */
    private static class Star {
        float x;
        float y;
        float speed;
        float brightness; // 0.5 to 1.0 for variety

        Star(float x, float y, float speed, float brightness) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.brightness = brightness;
        }
    }

    /**
     * Constructor initializes the starfield with random positions and speeds
     * @param minX Left bound of the screen in world coordinates
     * @param maxX Right bound of the screen in world coordinates
     * @param minY Bottom bound of the screen in world coordinates
     * @param maxY Top bound of the screen in world coordinates
     */
    public BackgroundStars(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        this.random = new Random();
        this.stars = new Star[STAR_COUNT];

        // Initialize stars with random positions and speeds
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = randomX();
            float y = randomY();
            float speed = MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED);
            float brightness = 0.5f + random.nextFloat() * 0.5f; // 0.5 to 1.0
            stars[i] = new Star(x, y, speed, brightness);
        }
    }

    /**
     * Updates the star positions, moving them downward and wrapping them when needed
     * @param cameraOffset The camera's Y offset to move stars relative to the camera
     */
    public void update(float cameraOffset) {
        for (Star star : stars) {
            // Move star downward based on its speed
            star.y -= star.speed;

            // Apply camera offset (so stars move with the world)
            star.y -= cameraOffset;

            // Wrap star to top if it goes below the screen
            if (star.y < minY) {
                star.y = maxY;
                star.x = randomX(); // Randomize X position when wrapping
            }
        }
    }

    /**
     * Renders all stars as small white points
     * @param gl OpenGL context
     */
    public void draw(GL2 gl) {
        // Set up for drawing points
        gl.glPointSize(2.0f); // Star size

        gl.glBegin(GL2.GL_POINTS);
        for (Star star : stars) {
            // Set color with varying brightness
            gl.glColor3f(star.brightness, star.brightness, star.brightness);
            gl.glVertex2f(star.x, star.y);
        }
        gl.glEnd();

        // Reset point size
        gl.glPointSize(1.0f);
    }

    /**
     * Alternative rendering method using small circles instead of points
     * for better visual appearance (optional, use if points are too small)
     */
    public void drawAsCircles(GL2 gl) {
        for (Star star : stars) {
            gl.glColor3f(star.brightness, star.brightness, star.brightness);
            drawTinyCircle(gl, star.x, star.y, 0.08f, 6); // Very small circles
        }
    }

    /**
     * Draws a small circle for a star
     */
    private void drawTinyCircle(GL2 gl, float cx, float cy, float radius, int segments) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex2f(cx, cy);
        for (int i = 0; i <= segments; i++) {
            double angle = i * 2.0 * Math.PI / segments;
            float x = cx + (float)(radius * Math.cos(angle));
            float y = cy + (float)(radius * Math.sin(angle));
            gl.glVertex2f(x, y);
        }
        gl.glEnd();
    }

    /**
     * Updates screen bounds (call this if window is resized)
     */
    public void updateBounds(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * Generates a random X coordinate within screen bounds
     */
    private float randomX() {
        return minX + random.nextFloat() * (maxX - minX);
    }

    /**
     * Generates a random Y coordinate within screen bounds
     */
    private float randomY() {
        return minY + random.nextFloat() * (maxY - minY);
    }
}

