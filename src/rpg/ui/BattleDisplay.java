package rpg.ui;

import rpg.model.Character;

/**
 * Interface {@code BattleDisplay} merupakan kontrak untuk menampilkan informasi
 * jalannya pertarungan kepada pengguna.
 *
 * <p><b>Penerapan Konsep PBO (Sub-bab 11.1 & 11.3.3 Proposal):</b></p>
 * <ul>
 *   <li><b>Abstraction & Dependency Inversion Principle (DIP):</b> Lapisan logika
 *       bisnis ({@link rpg.service.BattleService}) hanya bergantung pada interface
 *       {@code BattleDisplay}, bukan langsung pada kelas {@code SwingDisplay}.</li>
 *   <li><b>Polymorphism & Testability:</b> Memungkinkan pergantian tampilan antara
 *       GUI sesungguhnya ({@link SwingDisplay}) dan tampilan tiruan/stub pada saat
 *       menjalankan pengujian otomatis ({@code BattleServiceTest}) tanpa membuka
 *       jendela GUI.</li>
 * </ul>
 */
public interface BattleDisplay {

    /**
     * Menampilkan pesan aksi atau narasi giliran pertarungan (KF-07).
     *
     * @param message pesan teks yang akan ditampilkan
     */
    void showMessage(String message);

    /**
     * Memperbarui tampilan bar HP karakter setelah menerima damage (KF-07).
     *
     * @param character karakter yang bar HP-nya diperbarui
     */
    void showHpBar(Character character);

    /**
     * Menampilkan hasil akhir pertarungan beserta ringkasan jumlah giliran
     * dan total damage yang diberikan pemain (KF-09, KF-10).
     *
     * @param turns       total giliran yang berlangsung
     * @param totalDamage total damage yang diberikan pemain
     * @param playerWon   {@code true} jika pemain menang, {@code false} jika kalah
     */
    void showSummary(int turns, int totalDamage, boolean playerWon);
}
