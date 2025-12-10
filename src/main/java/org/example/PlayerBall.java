package org.example;

import com.jogamp.opengl.GL2;
import java.util.Random;

public class PlayerBall {
    private final float x = 0; // Ball is always centered horizontally
    private float y;
    private final float radius;
    private float velocityY;
    private int colorIndex;
    private final Random random = new Random();

    private static final float[][] COLORS = {
            {0.0f, 1.0f, 1.0f}, // Cyan
            {1.0f, 1.0f, 0.0f}, // Yellow
            {1.0f, 0.0f, 1.0f}, // Magenta
            {0.5f, 0.0f, 1.0f}  // Purple
    };

    public PlayerBall(float y, float radius) {
        this.y = y;
        this.radius = radius;
        this.velocityY = 0;
        this.colorIndex = random.nextInt(COLORS.length);
    }

    public void applyGravity(float gravity) {
        this.velocityY += gravity;
    }

    public void jump(float jumpVelocity) {
        this.velocityY = jumpVelocity;
    }

    public void update() {
        this.y += velocityY;
    }

    public void draw(GL2 gl) {
        gl.glColor3fv(COLORS[colorIndex], 0);
        drawCircle(gl, x, y, radius, 32);
    }

    public void changeColor() {
        int newColorIndex;
        do {
            newColorIndex = random.nextInt(COLORS.length);
        } while (newColorIndex == this.colorIndex);
        this.colorIndex = newColorIndex;
    }

    public void setColor(int index) {
        this.colorIndex = index;
    }

    private void drawCircle(GL2 gl, float cx, float cy, float r, int numSegments) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glVertex2f(cx, cy); // center of circle
        for (int i = 0; i <= numSegments; i++) {
            double angle = i * 2.0 * Math.PI / numSegments;
            gl.glVertex2f(cx + (float) (r * Math.cos(angle)), cy + (float) (r * Math.sin(angle)));
        }
        gl.glEnd();
    }

    // Getters and Setters
    public float getX() { return x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getRadius() { return radius; }
    public int getColorIndex() { return colorIndex; }

    public boolean isCollidingWithRing(Ring ring) {
        float dist = (float) Math.sqrt(Math.pow(x - 0, 2) + Math.pow(y - ring.getY(), 2));
        return dist > ring.getInnerRadius() - radius && dist < ring.getOuterRadius() + radius;
    }
}

