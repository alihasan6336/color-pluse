package org.example;

import javax.swing.*;
import java.awt.*;

public class EndGame {
    private JPanel panel;
    private JLabel scoreLabel;
    private JLabel winnerLabel;
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
        panel.setLayout(new BorderLayout());
        panel.setFocusable(true);

        // Title
        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 48));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Winner info
        winnerLabel = new JLabel("");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        winnerLabel.setForeground(Color.CYAN);
        winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Score info
        scoreLabel = new JLabel("");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 48));
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(gameOverLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(winnerLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(scoreLabel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton exitButton = createButton("EXIT", Color.RED, Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));

        JButton playAgainButton = createButton("PLAY AGAIN", Color.GREEN, Color.BLACK);
        playAgainButton.addActionListener(e -> {
            gameInstance.resetGamePublic();
            cardLayout.show(cardPanel, "HOW_TO_PLAY");
        });

        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(playAgainButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Show results when panel is displayed
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                showResults();
            }
        });

        return panel;
    }

    private void showResults() {
        if (gameInstance.isMultiplayer()) {
            int score1 = gameInstance.getScore(0);
            int score2 = gameInstance.getScore(1);
            String winnerText;

            if (score1 > score2) winnerText = "PLAYER 1 WINS!";
            else if (score2 > score1) winnerText = "PLAYER 2 WINS!";
            else winnerText = "IT'S A TIE!";

            winnerLabel.setText(winnerText);
            scoreLabel.setText(score1 + " - " + score2);
        } else {
            // Single player
            winnerLabel.setText("");
            scoreLabel.setText(String.valueOf(gameInstance.getScore(0)));
        }
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(0x1E1E1E));
            g.fillRect(0, 0, getWidth(), getHeight());

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            java.util.Random random = new java.util.Random(42);
            for (int i = 0; i < 150; i++) {
                int x = random.nextInt(getWidth());
                int y = random.nextInt(getHeight());
                float brightness = 0.5f + random.nextFloat() * 0.5f;
                g2d.setColor(new Color(brightness, brightness, brightness));
                g2d.fillOval(x, y, 2, 2);
            }
        }
    }
}
