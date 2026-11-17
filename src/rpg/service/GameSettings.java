package rpg.service;

import rpg.model.Difficulty;

/**
 * Kelas {@code GameSettings} menyimpan state konfigurasi permainan yang aktif
 * selama aplikasi berjalan (Batasan 4.3 poin 5: kembali ke bawaan {@code NORMAL}
 * saat program ditutup atau di-reset).
 *
 * <p><b>Penerapan Encapsulation:</b> Atribut {@code difficulty} bersifat
 * {@code private} dan hanya dapat diubah melalui {@link #setDifficulty(Difficulty)}
 * yang memvalidasi agar nilai tidak {@code null} atau melalui
 * {@link #resetToDefault()}.</p>
 */
public class GameSettings {
    private Difficulty difficulty;

    public GameSettings() {
        this.difficulty = Difficulty.NORMAL;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("Tingkat kesulitan tidak boleh null");
        }
        this.difficulty = difficulty;
    }

    /**
     * Mengembalikan pengaturan tingkat kesulitan ke nilai bawaan (NORMAL) (KF-12).
     */
    public void resetToDefault() {
        this.difficulty = Difficulty.NORMAL;
    }

    /**
     * Mengembalikan pengali statistik monster sesuai tingkat kesulitan aktif.
     *
     * @return nilai pengali (0.8 / 1.0 / 1.3)
     */
    public double getMultiplier() {
        return difficulty.getMultiplier();
    }
}
