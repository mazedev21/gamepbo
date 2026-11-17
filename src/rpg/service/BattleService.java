package rpg.service;

import java.util.Random;
import rpg.model.Character;
import rpg.ui.BattleDisplay;

/**
 * Kelas {@code BattleService} mengelola logika alur pertarungan bergiliran
 * satu lawan satu antara pahlawan pemain dan monster (KF-04 s.d. KF-10).
 *
 * <p><b>Penerapan Konsep PBO (Sub-bab 11.3.4 Proposal):</b></p>
 * <ul>
 *   <li><b>Dependency Injection & DIP:</b> Menerima objek {@link BattleDisplay}
 *       melalui konstruktor sehingga logika pertarungan tidak terikat pada
 *       komponen GUI tertentu.</li>
 *   <li><b>Polymorphism:</b> Method {@link #executePlayerAction(Character, Character, int)}
 *       dan {@link #executeEnemyTurn(Character, Character)} menerima parameter
 *       bertipe abstrak {@link Character}, sehingga dapat bekerja untuk
 *       {@code Warrior}, {@code Mage}, maupun subkelas {@code Character} baru
 *       di masa depan tanpa mengubah {@code BattleService} (memenuhi KNF-04).</li>
 *   <li><b>Encapsulation:</b> Penghitung {@code turns} dan {@code totalPlayerDamage}
 *       dijaga {@code private} dan hanya dimodifikasi melalui eksekusi giliran yang sah.</li>
 * </ul>
 */
public class BattleService {
    private static final double MONSTER_SKILL_CHANCE = 0.20; // KF-08: peluang 20% skill spesial

    private final BattleDisplay display;
    private final Random random;
    private int turns = 0;
    private int totalPlayerDamage = 0;

    public BattleService(BattleDisplay display) {
        this(display, new Random());
    }

    public BattleService(BattleDisplay display, Random random) {
        if (display == null) {
            throw new IllegalArgumentException("Display wajib ada");
        }
        if (random == null) {
            throw new IllegalArgumentException("Random generator tidak boleh null");
        }
        this.display = display;
        this.random = random;
    }

    /**
     * Mengeksekusi aksi pemain pada gilirannya (KF-05, KF-06, KF-07).
     *
     * @param player karakter pahlawan pemain
     * @param enemy  karakter monster lawan
     * @param choice pilihan aksi: {@code 1} untuk Serangan Biasa, {@code 2} untuk Skill Spesial
     * @return jumlah damage yang diberikan pemain kepada musuh
     */
    public int executePlayerAction(Character player, Character enemy, int choice) {
        validateCombatants(player, enemy);
        if (choice != 1 && choice != 2) {
            throw new IllegalArgumentException("Pilihan aksi harus 1 atau 2");
        }

        turns++;
        int dmg;
        String actionName;
        if (choice == 1) {
            dmg = player.basicAttack(enemy);
            actionName = "Serangan Biasa";
        } else {
            dmg = player.specialAttack(enemy);
            actionName = "Skill Spesial [" + player.getSkillName() + "]";
        }

        totalPlayerDamage += dmg;
        display.showMessage(
            "[Giliran " + turns + " - Pemain] " + player.getName() + " (" + player.getRole() + ") "
            + "menggunakan " + actionName + " -> memberi " + dmg + " damage ke " + enemy.getName()
            + "! (HP " + enemy.getName() + ": " + enemy.getHp() + "/" + enemy.getMaxHp() + ")"
        );
        display.showHpBar(enemy);
        return dmg;
    }

    /**
     * Mengeksekusi giliran monster secara otomatis dengan peluang 20% memakai
     * skill spesial dan 80% memakai serangan biasa (KF-08).
     *
     * @param enemy  karakter monster
     * @param player karakter pahlawan pemain
     * @return jumlah damage yang diberikan monster kepada pemain
     */
    public int executeEnemyTurn(Character enemy, Character player) {
        validateCombatants(player, enemy);
        if (!enemy.isAlive()) {
            return 0;
        }

        boolean useSpecial = random.nextDouble() < MONSTER_SKILL_CHANCE;
        int dmg;
        String actionDesc;
        if (useSpecial) {
            dmg = enemy.specialAttack(player);
            actionDesc = "Skill Spesial [" + enemy.getSkillName() + "] (Peluang 20% aktif!)";
        } else {
            dmg = enemy.basicAttack(player);
            actionDesc = "Serangan Biasa";
        }

        display.showMessage(
            "[Giliran " + turns + " - Musuh] " + enemy.getName() + " membalas dengan "
            + actionDesc + " -> memberi " + dmg + " damage ke " + player.getName()
            + "! (HP " + player.getName() + ": " + player.getHp() + "/" + player.getMaxHp() + ")"
        );
        display.showHpBar(player);
        return dmg;
    }

    /**
     * Memeriksa apakah pertarungan sudah berakhir (salah satu karakter HP-nya 0) (KF-04, KF-09).
     *
     * @param player karakter pemain
     * @param enemy  karakter musuh
     * @return {@code true} jika pertarungan telah selesai
     */
    public boolean isBattleOver(Character player, Character enemy) {
        validateCombatants(player, enemy);
        return !player.isAlive() || !enemy.isAlive();
    }

    /**
     * Menampilkan narasi akhir dan ringkasan pertarungan pada {@link BattleDisplay} (KF-09, KF-10).
     *
     * @param player karakter pemain
     * @param enemy  karakter musuh
     */
    public void concludeBattle(Character player, Character enemy) {
        validateCombatants(player, enemy);
        boolean playerWon = player.isAlive() && !enemy.isAlive();
        display.showSummary(turns, totalPlayerDamage, playerWon);
    }

    /**
     * Mereset statistik giliran dan total damage sebelum memulai pertarungan baru (KF-13).
     */
    public void resetBattleStats() {
        this.turns = 0;
        this.totalPlayerDamage = 0;
    }

    private void validateCombatants(Character player, Character enemy) {
        if (player == null || enemy == null) {
            throw new IllegalArgumentException("Karakter pemain dan musuh tidak boleh null");
        }
    }

    public int getTurns() {
        return turns;
    }

    public int getTotalPlayerDamage() {
        return totalPlayerDamage;
    }
}
