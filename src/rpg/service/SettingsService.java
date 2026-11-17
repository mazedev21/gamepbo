package rpg.service;

import rpg.model.Difficulty;

/**
 * Kelas {@code SettingsService} menangani logika bisnis untuk fitur Pengaturan
 * Tingkat Kesulitan oleh aktor Pengelola Permainan (KF-11 & KF-12).
 */
public class SettingsService {
    private final GameSettings settings;

    public SettingsService(GameSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("GameSettings wajib ada");
        }
        this.settings = settings;
    }

    /**
     * Mengubah tingkat kesulitan aktif (KF-11).
     *
     * @param newDifficulty tingkat kesulitan baru (MUDAH, NORMAL, atau SULIT)
     */
    public void changeDifficulty(Difficulty newDifficulty) {
        if (newDifficulty == null) {
            throw new IllegalArgumentException("Pilihan tingkat kesulitan tidak boleh null");
        }
        settings.setDifficulty(newDifficulty);
    }

    /**
     * Mengembalikan tingkat kesulitan ke bawaan yaitu {@link Difficulty#NORMAL} (KF-12).
     */
    public void resetDifficulty() {
        settings.resetToDefault();
    }

    public Difficulty getCurrentDifficulty() {
        return settings.getDifficulty();
    }

    public double getCurrentMultiplier() {
        return settings.getMultiplier();
    }

    public GameSettings getSettings() {
        return settings;
    }
}
