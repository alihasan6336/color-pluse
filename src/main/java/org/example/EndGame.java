package org.example;

import javax.swing.*;
import java.awt.*;

public class EndGame {
    private JPanel panel;
    private JLabel scoreValueLabel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Game gameInstance;

    public EndGame(CardLayout cardLayout, JPanel cardPanel, Game gameInstance) {
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.gameInstance = gameInstance;
        this.panel = createEndGameScreen();
    }

    public JPanel getPanel() {
        return panel;
    }

    private JPanel createEndGameScreen() {
        JPanel panel = new BackgroundPanel();
        panel.setLayout(new BorderLayout(0, 0));
        panel.setFocusable(true);

        // Top spacer
        JPanel topSpacer = new JPanel();
        topSpacer.setOpaque(false);
        topSpacer.setPreferredSize(new Dimension(0, 60));
        panel.add(topSpacer, BorderLayout.NORTH);

        // Center panel with score display
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Game Over title
        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 48));
        gameOverLabel.setForeground(new Color(0xFF3333));
        gameOverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gameOverLabel.setOpaque(false);
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Score display
        JLabel scoreLabel = new JLabel("YOUR SCORE");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(new Color(0x00FF00));
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setOpaque(false);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreValueLabel = new JLabel("0");
        scoreValueLabel.setFont(new Font("Arial", Font.BOLD, 72));
        scoreValueLabel.setForeground(new Color(0xFFFF00));
        scoreValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreValueLabel.setOpaque(false);
        scoreValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(gameOverLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(scoreLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(scoreValueLabel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));

        JButton exitButton = createStyledButton("EXIT", new Color(0xFF3333), Color.WHITE);
        exitButton.setMaximumSize(new Dimension(140, 50));
        exitButton.addActionListener(e -> System.exit(0));

        JButton playAgainButton = createStyledButton("PLAY AGAIN", new Color(0x00FF00), Color.BLACK);
        playAgainButton.setMaximumSize(new Dimension(140, 50));
        playAgainButton.addActionListener(e -> {
            // Reset game and go back to how to play
            gameInstance.resetGamePublic();
            cardLayout.show(cardPanel, "HOW_TO_PLAY");
        });

        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(playAgainButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add keyboard listener for ESC and ENTER keys
        panel.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    // ESC exits
                    System.exit(0);
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    // ENTER plays again
                    gameInstance.resetGamePublic();
                    cardLayout.show(cardPanel, "HOW_TO_PLAY");
                }
            }
        });

        // Request focus when panel is shown
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Update score display
                scoreValueLabel.setText(String.valueOf(gameInstance.getScore()));
                panel.requestFocusInWindow();
            }
        });

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    /**
     * Custom panel that renders the starfield background
     */
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Draw dark background
            g.setColor(new Color(0x1E1E1E));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw stars using Graphics2D for better rendering
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Simple star rendering
            java.util.Random random = new java.util.Random(42); // Fixed seed for consistency
            int starCount = 150;
            for (int i = 0; i < starCount; i++) {
                int x = random.nextInt(getWidth());
                int y = random.nextInt(getHeight());
                float brightness = 0.5f + random.nextFloat() * 0.5f;

                g2d.setColor(new Color(brightness, brightness, brightness));
                g2d.fillOval(x, y, 2, 2);
            }
        }
    }
}
