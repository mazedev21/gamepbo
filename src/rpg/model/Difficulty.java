package rpg.model;

/**
 * Enum Difficulty merepresentasikan tingkat kesulitan permainan yang dapat
 * diatur oleh aktor Pengelola Permainan (KF-11, KF-12) dan memengaruhi
 * statistik Monster (KF-03).
 *
 * <p><b>Penerapan Konsep PBO (v2 -> v3):</b> Menggantikan teks/konstanta bebas
 * dengan tipe {@code enum} yang <i>type-safe</i> serta mengenkapsulasi nilai
 * pengali ({@code multiplier}) dan label tampilan.</p>
 */
public enum Difficulty {
    MUDAH(0.8, "Mudah (Statistik Monster x0.8)"),
    NORMAL(1.0, "Normal (Statistik Monster x1.0 - Bawaan)"),
    SULIT(1.3, "Sulit (Statistik Monster x1.3)");

    private final double multiplier;
    private final String description;

    Difficulty(double multiplier, String description) {
        this.multiplier = multiplier;
        this.description = description;
    }

    /**
     * Mengembalikan faktor pengali statistik monster untuk tingkat kesulitan ini.
     *
     * @return pengali statistik (0.8 untuk Mudah, 1.0 untuk Normal, 1.3 untuk Sulit)
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Mengembalikan deskripsi tingkat kesulitan untuk ditampilkan di GUI.
     *
     * @return teks deskripsi
     */
    public String getDescription() {
        return description;
    }
}
