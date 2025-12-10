package org.example;

import java.util.prefs.Preferences;

/**
 * Manages high score persistence using Java Preferences API.
 * Stores and retrieves the high score across game sessions.
 */
public class HighScoreManager {
    private static final String HIGH_SCORE_KEY = "highScore";
    private final Preferences prefs;
    private int highScore;

    public HighScoreManager() {
        // Use Preferences to store high score persistently
        prefs = Preferences.userNodeForPackage(HighScoreManager.class);
        highScore = prefs.getInt(HIGH_SCORE_KEY, 0);
    }

    /**
     * Get the current high score.
     */
    public int getHighScore() {
        return highScore;
    }

    /**
     * Update the high score if the new score is higher.
     * Returns true if high score was updated.
     */
    public boolean updateHighScore(int newScore) {
        if (newScore > highScore) {
            highScore = newScore;
            prefs.putInt(HIGH_SCORE_KEY, highScore);
            return true;
        }
        return false;
    }

    /**
     * Reset the high score to 0.
     */
    public void resetHighScore() {
        highScore = 0;
        prefs.putInt(HIGH_SCORE_KEY, 0);
    }
}

