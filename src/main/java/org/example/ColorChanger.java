package org.example;

import com.jogamp.opengl.GL2;
import java.util.Random;

public class ColorChanger {
    private float x, y, radius;
    private int colorIndex;
    private Random random = new Random();

    private static final float[][] COLORS = {
            {0.0f, 1.0f, 1.0f}, // Cyan
            {1.0f, 1.0f, 0.0f}, // Yellow
            {1.0f, 0.0f, 1.0f}, // Magenta
            {0.5f, 0.0f, 1.0f}  // Purple
    };

    public ColorChanger(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.colorIndex = random.nextInt(COLORS.length);
    }

    public void draw(GL2 gl) {
        gl.glColor3fv(COLORS[colorIndex], 0);
        drawCircle(gl, x, y, radius, 16);
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

    public boolean isColliding(PlayerBall ball) {
        float dist = (float) Math.sqrt(Math.pow(ball.getX() - x, 2) + Math.pow(ball.getY() - y, 2));
        return dist < ball.getRadius() + radius;
    }

    // Getters and Setters
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public int getColorIndex() { return colorIndex; }
}

