package org.example;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class StartGame {
    private static JFrame mainFrame;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;
    private static FPSAnimator animator;
    private static GLJPanel glPanel;
    private static Game gameInstance;
    private static EndGame endGameScreen;
    private static HowToPlay howToPlayScreen;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartGame::initializeMainFrame);
    }

    private static void initializeMainFrame() {
        mainFrame = new JFrame("Color Switch Clone");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(true);

        // Card layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Screens
        cardPanel.add(createStartScreen(), "START");
        cardPanel.add(createGameScreen(), "GAME");

        mainFrame.add(cardPanel);
        mainFrame.setVisible(true);

        // Create HowToPlay (uses callbacks)
        howToPlayScreen = new HowToPlay(cardLayout, cardPanel, () -> launchGame(false));
        cardPanel.add(howToPlayScreen.getPanel(), "HOW_TO_PLAY");

        // EndGame will be created dynamically when launching game (so it shows correct score)
        // Show start screen first
        cardLayout.show(cardPanel, "START");

        // Background animation timer for nice UI effects
        javax.swing.Timer animationTimer = new javax.swing.Timer(50, e -> {
            cardPanel.repaint();
        });
        animationTimer.start();
    }

    private static JPanel createStartScreen() {
        JPanel panel = new BackgroundPanel();
        panel.setLayout(new BorderLayout(0, 0));

        JPanel mainContainer = new JPanel();
        mainContainer.setOpaque(false);
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

        JLabel titleLabel = new JLabel("COLOR SWITCH");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(0x00FF00));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setOpaque(false);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel subtitleLabel = new JLabel("Master the Colors");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitleLabel.setForeground(new Color(0xFFFF00));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setOpaque(false);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        mainContainer.add(titleLabel);
        mainContainer.add(Box.createVerticalStrut(8));
        mainContainer.add(subtitleLabel);
        mainContainer.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(250, 300));

        // SINGLEPLAYER
        JButton playButton = createEnhancedButton("PLAY (Singleplayer)", new Color(0x00FF00), Color.BLACK, 20);
        playButton.setMaximumSize(new Dimension(250, 50));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(e -> launchGame(false));

        // MULTIPLAYER
        JButton multiButton = createEnhancedButton("MULTIPLAYER (Split)", new Color(0x00CCFF), Color.BLACK, 16);
        multiButton.setMaximumSize(new Dimension(250, 50));
        multiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        multiButton.addActionListener(e -> launchGame(true));

        // HOW TO PLAY
        JButton howToPlayButton = createEnhancedButton("HOW TO PLAY", new Color(0x00CCFF), Color.BLACK, 16);
        howToPlayButton.setMaximumSize(new Dimension(250, 50));
        howToPlayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        howToPlayButton.addActionListener(e -> cardLayout.show(cardPanel, "HOW_TO_PLAY"));

        // EXIT
        JButton exitButton = createEnhancedButton("EXIT", new Color(0xFF3333), Color.WHITE, 16);
        exitButton.setMaximumSize(new Dimension(250, 50));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(playButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(multiButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(howToPlayButton);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(exitButton);

        mainContainer.add(buttonPanel);
        mainContainer.add(Box.createVerticalGlue());

        panel.add(mainContainer, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createGameScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x1E1E1E));
        panel.setFocusable(true);

        // Initialize OpenGL GLJPanel (we reuse this panel for single & multiplayer)
        GLProfile.initSingleton();
        GLProfile glProfile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glProfile);
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);

        glPanel = new GLJPanel(caps);
        glPanel.setPreferredSize(new Dimension(800, 800));

        // We will add the Game GLEventListener dynamically in launchGame(...)

        // Add keyboard listener that forwards keys to the active gameInstance
        glPanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (gameInstance != null) {
                    gameInstance.handleKeyPress(e.getKeyCode());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        // Request focus when panel is shown
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                glPanel.requestFocusInWindow();
            }
        });

        // Animator created here, but started/stopped in launchGame
        animator = new FPSAnimator(glPanel, 60);

        panel.add(glPanel, BorderLayout.CENTER);
        return panel;
    }

    private static JButton createEnhancedButton(String text, Color bgColor, Color fgColor, int fontSize) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        // Add hover and press effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.setFont(new Font("Arial", Font.BOLD, fontSize + 2));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
                button.setFont(new Font("Arial", Font.BOLD, fontSize));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
        });

        return button;
    }

    /**
     * Launches the game. If multiplayer == true -> split-screen.
     */
    private static void launchGame(boolean multiplayer) {
        // Remove any previous listeners from glPanel
        if (gameInstance != null) {
            glPanel.removeGLEventListener(gameInstance);
        }

        // Create new game instance with requested mode
        gameInstance = new Game(multiplayer);

        // Setup singleplayer end-game callback to show END_GAME card
        gameInstance.setGameStateListener(() -> {
            // create EndGame screen with current game instance and show it
            SwingUtilities.invokeLater(() -> {
                // If an EndGame exists, remove previous card (so we can create a fresh one)
                if (endGameScreen != null) {
                    cardPanel.remove(endGameScreen.getPanel());
                }
                endGameScreen = new EndGame(cardLayout, cardPanel, gameInstance);
                cardPanel.add(endGameScreen.getPanel(), "END_GAME");
                cardLayout.show(cardPanel, "END_GAME");
            });
        });

        // Add as GLEventListener and start animator
        glPanel.addGLEventListener(gameInstance);
        if (!animator.isAnimating()) animator.start();

        // Show game card and focus
        cardLayout.show(cardPanel, "GAME");
        glPanel.requestFocusInWindow();
    }

    /**
     * Custom panel that renders the starfield background with gradient for menus.
     */
    private static class BackgroundPanel extends JPanel {
        private long startTime = System.currentTimeMillis();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                    0, 0, new Color(0x0A0A1A),
                    0, getHeight(), new Color(0x1E1E2E)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            java.util.Random random = new java.util.Random(42);
            int starCount = 200;
            long elapsed = System.currentTimeMillis() - startTime;

            for (int i = 0; i < starCount; i++) {
                int x = random.nextInt(getWidth());
                int y = random.nextInt(getHeight());

                float twinkle = (float) Math.sin((elapsed + i * 100) / 500.0f);
                float brightness = 0.3f + (0.7f * (twinkle + 1) / 2);
                brightness = Math.min(1.0f, brightness);

                int size = brightness > 0.7f ? 3 : 2;
                g2d.setColor(new Color(brightness, brightness, brightness * 0.9f));
                g2d.fillOval(x, y, size, size);
            }

            g2d.setColor(new Color(0, 100, 200, 10));
            for (int i = 0; i < 3; i++) {
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        }
    }
}
