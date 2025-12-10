package org.example;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

/**
 * Handles rendering of the transparent score UI overlay.
 * Displays current score and high score with clean, modern styling.
 */
public class ScoreUI {
    private TextRenderer scoreRenderer;
    private TextRenderer highScoreRenderer;
    private int windowWidth;
    private int windowHeight;
    private float uiAlpha = 0f; // For fade-in animation
    private static final float FADE_SPEED = 0.02f;

    public ScoreUI() {
        // Create text renderers with different sizes
        Font scoreFont = new Font("SansSerif", Font.BOLD, 48);
        Font highScoreFont = new Font("SansSerif", Font.PLAIN, 24);

        scoreRenderer = new TextRenderer(scoreFont, true, true);
        highScoreRenderer = new TextRenderer(highScoreFont, true, true);
    }

    /**
     * Update window dimensions for proper text positioning.
     */
    public void updateDimensions(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    /**
     * Reset the fade-in animation.
     */
    public void resetFade() {
        uiAlpha = 0f;
    }

    /**
     * Update the fade-in animation.
     */
    public void update() {
        if (uiAlpha < 1f) {
            uiAlpha = Math.min(1f, uiAlpha + FADE_SPEED);
        }
    }

    /**
     * Draw the score UI overlay with transparency and shadow effects.
     */
    public void draw(GL2 gl, int currentScore, int highScore) {
        if (windowWidth == 0 || windowHeight == 0) return;

        // Enable blending for transparency
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // Prepare for 2D rendering
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, windowWidth, 0, windowHeight, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        // Draw high score first (on top)
        drawHighScore(highScore);

        // Draw current score (below high score)
        drawScore(currentScore);

        // Restore matrices
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glDisable(GL2.GL_BLEND);
    }

    /**
     * Draw the current score with shadow effect.
     */
    private void drawScore(int score) {
        String scoreText = String.valueOf(score);

        // Calculate position (centered horizontally, 12% from top)
        Rectangle2D bounds = scoreRenderer.getBounds(scoreText);
        int x = (int) ((windowWidth - bounds.getWidth()) / 2);
        int y = windowHeight - (int) (windowHeight * 0.12);

        scoreRenderer.beginRendering(windowWidth, windowHeight);

        // Draw shadow (offset by 3 pixels, darker)
        scoreRenderer.setColor(0f, 0f, 0f, 0.5f * uiAlpha);
        scoreRenderer.draw(scoreText, x + 3, y - 3);

        // Draw main score text (white with transparency)
        scoreRenderer.setColor(1f, 1f, 1f, 0.85f * uiAlpha);
        scoreRenderer.draw(scoreText, x, y);

        scoreRenderer.endRendering();
    }

    /**
     * Draw the high score text (smaller, above current score).
     */
    private void drawHighScore(int highScore) {
        String highScoreText = "BEST: " + highScore;

        // Calculate position (centered horizontally, 6% from top)
        Rectangle2D bounds = highScoreRenderer.getBounds(highScoreText);
        int x = (int) ((windowWidth - bounds.getWidth()) / 2);
        int y = windowHeight - (int) (windowHeight * 0.06);

        highScoreRenderer.beginRendering(windowWidth, windowHeight);

        // Draw shadow
        highScoreRenderer.setColor(0f, 0f, 0f, 0.4f * uiAlpha);
        highScoreRenderer.draw(highScoreText, x + 2, y - 2);

        // Draw main high score text (lighter and more transparent)
        highScoreRenderer.setColor(1f, 1f, 1f, 0.65f * uiAlpha);
        highScoreRenderer.draw(highScoreText, x, y);

        highScoreRenderer.endRendering();
    }

    /**
     * Draw game over text.
     */
    public void drawGameOver(GL2 gl) {
        if (windowWidth == 0 || windowHeight == 0) return;

        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, windowWidth, 0, windowHeight, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        String gameOverText = "GAME OVER";
        String restartText = "Press SPACE to restart";

        // Game Over text (center of screen)
        Rectangle2D bounds = scoreRenderer.getBounds(gameOverText);
        int x = (int) ((windowWidth - bounds.getWidth()) / 2);
        int y = windowHeight / 2 + 30;

        scoreRenderer.beginRendering(windowWidth, windowHeight);

        // Shadow
        scoreRenderer.setColor(0f, 0f, 0f, 0.6f);
        scoreRenderer.draw(gameOverText, x + 3, y - 3);

        // Main text (red tint)
        scoreRenderer.setColor(1f, 0.3f, 0.3f, 0.9f);
        scoreRenderer.draw(gameOverText, x, y);

        scoreRenderer.endRendering();

        // Restart instruction
        Rectangle2D restartBounds = highScoreRenderer.getBounds(restartText);
        int restartX = (int) ((windowWidth - restartBounds.getWidth()) / 2);
        int restartY = windowHeight / 2 - 40;

        highScoreRenderer.beginRendering(windowWidth, windowHeight);

        highScoreRenderer.setColor(0f, 0f, 0f, 0.5f);
        highScoreRenderer.draw(restartText, restartX + 2, restartY - 2);

        highScoreRenderer.setColor(1f, 1f, 1f, 0.7f);
        highScoreRenderer.draw(restartText, restartX, restartY);

        highScoreRenderer.endRendering();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glDisable(GL2.GL_BLEND);
    }

    /**
     * Clean up resources.
     */
    public void dispose() {
        if (scoreRenderer != null) {
            scoreRenderer.dispose();
        }
        if (highScoreRenderer != null) {
            highScoreRenderer.dispose();
        }
    }
}

