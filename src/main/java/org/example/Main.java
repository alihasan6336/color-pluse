package org.example;

import  com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.opengl.*;

import com.jogamp.opengl.util.FPSAnimator;

public class  Main {
    public static void main(String[] args) {

        GLProfile.initSingleton();
        GLProfile glp = GLProfile.get(GLProfile.GL2);

        GLCapabilities caps = new GLCapabilities(glp);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        GLWindow window = GLWindow.create(caps);
        window.setTitle("Color Switch Clone");
        window.setSize(400, 800);
        window.setVisible(true);

        boolean multiplayer = false;
        Game game = new Game(multiplayer);
        window.addGLEventListener(game);

// Keyboard input
        window.addKeyListener(new com.jogamp.newt.event.KeyAdapter() {
            @Override
            public void keyPressed(com.jogamp.newt.event.KeyEvent e) {
                game.handleKeyPress(e.getKeyCode());
            }
        });

        FPSAnimator animator = new FPSAnimator(window, 60);
        animator.start();

        window.addWindowListener(new com.jogamp.newt.event.WindowAdapter() {
            @Override
            public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent e) {
                System.exit(0);
            }
        });

    }
}
