package rpg.test;

import rpg.model.Difficulty;
import rpg.model.Monster;
import rpg.service.GameSettings;
import rpg.service.SettingsService;

/**
 * Kelas uji {@code SettingsServiceTest} memverifikasi fitur Pengaturan Tingkat
 * Kesulitan oleh aktor Pengelola Permainan (KF-11, KF-12, dan Sub-bab 11.4 Proposal).
 */
public class SettingsServiceTest {

    public static void main(String[] args) {
        var settings = new GameSettings();
        var service = new SettingsService(settings);

        // =====================================================================
        // Skenario 1 (Berhasil):
        // Nilai bawaan adalah NORMAL (1.0x), dapat diubah ke MUDAH (0.8x) & SULIT (1.3x),
        // memengaruhi statistik Monster, dan dapat di-reset kembali ke NORMAL.
        // =====================================================================
        cek(service.getCurrentDifficulty() == Difficulty.NORMAL, "Kesulitan bawaan harus NORMAL");
        cek(service.getCurrentMultiplier() == 1.0, "Multiplier NORMAL harus 1.0");

        // Ubah ke MUDAH (KF-11)
        service.changeDifficulty(Difficulty.MUDAH);
        cek(service.getCurrentDifficulty() == Difficulty.MUDAH, "Kesulitan harus berubah menjadi MUDAH");
        Monster easyMonster = new Monster("Goblin Mudah", service.getCurrentDifficulty());
        cek(easyMonster.getMaxHp() == 88, "HP Monster pada kesulitan MUDAH harus 88 (110 * 0.8)");
        cek(easyMonster.getAttack() == 12, "Attack Monster pada kesulitan MUDAH harus 12 (int)(16 * 0.8)");

        // Ubah ke SULIT (KF-11)
        service.changeDifficulty(Difficulty.SULIT);
        cek(service.getCurrentDifficulty() == Difficulty.SULIT, "Kesulitan harus berubah menjadi SULIT");
        Monster hardMonster = new Monster("Goblin Sulit", service.getCurrentDifficulty());
        cek(hardMonster.getMaxHp() == 143, "HP Monster pada kesulitan SULIT harus 143 (110 * 1.3)");
        cek(hardMonster.getAttack() == 20, "Attack Monster pada kesulitan SULIT harus 20 (int)(16 * 1.3)");

        // Reset ke bawaan NORMAL (KF-12)
        service.resetDifficulty();
        cek(service.getCurrentDifficulty() == Difficulty.NORMAL, "Reset harus mengembalikan kesulitan ke NORMAL");

        // =====================================================================
        // Skenario 2 (Gagal):
        // Mengubah tingkat kesulitan dengan nilai null harus ditolak
        // =====================================================================
        boolean tolakNull = false;
        try {
            service.changeDifficulty(null);
        } catch (IllegalArgumentException e) {
            tolakNull = true;
        }
        cek(tolakNull, "Input tingkat kesulitan null harus ditolak dengan IllegalArgumentException");

        System.out.println("Semua uji SettingsService lulus.");
    }

    static void cek(boolean kondisi, String pesan) {
        if (!kondisi) {
            throw new AssertionError(pesan);
        }
    }
}
