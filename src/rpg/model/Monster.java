package rpg.model;

import java.util.Random;
import rpg.skill.MonsterSmash;

/**
 * Kelas {@code Monster} merupakan subkelas dari {@link Character} yang menjadi
 * lawan pahlawan di kastil.
 *
 * <p>Statistik monster disesuaikan dengan tingkat kesulitan ({@link Difficulty})
 * dan mendukung pembangkitan statistik acak dalam rentang sesuai tingkat
 * kesulitan (KF-03).</p>
 */
public class Monster extends Character {

    private static final String[] MONSTER_NAMES = {
        "Goblin Penjaga Kastil",
        "Orc Pengawal Putri",
        "Troll Gerbang Menara",
        "Gargoyle Sayap Besi"
    };

    /**
     * Konstruktor utama sesuai spesifikasi Sub-bab 11.3.2 proposal.
     * Menghasilkan HP dasar {@code 110 * multiplier} dan Attack {@code 16 * multiplier}.
     *
     * @param name nama monster
     * @param diff tingkat kesulitan permainan
     */
    public Monster(String name, Difficulty diff) {
        super(
            name,
            (int) (110 * requireDifficulty(diff).getMultiplier()),
            (int) (16 * requireDifficulty(diff).getMultiplier()),
            new MonsterSmash()
        );
    }

    /**
     * Konstruktor dengan variasi statistik dasar (untuk pembuatan monster dengan
     * rentang acak sesuai KF-03).
     *
     * @param name       nama monster
     * @param baseHp     HP dasar sebelum dikalikan multiplier kesulitan
     * @param baseAttack attack dasar sebelum dikalikan multiplier kesulitan
     * @param diff       tingkat kesulitan permainan
     */
    public Monster(String name, int baseHp, int baseAttack, Difficulty diff) {
        super(
            name,
            Math.max(1, (int) Math.round(baseHp * requireDifficulty(diff).getMultiplier())),
            Math.max(1, (int) Math.round(baseAttack * requireDifficulty(diff).getMultiplier())),
            new MonsterSmash()
        );
    }

    private static Difficulty requireDifficulty(Difficulty diff) {
        if (diff == null) {
            throw new IllegalArgumentException("Tingkat kesulitan (Difficulty) tidak boleh null");
        }
        return diff;
    }

    /**
     * Membuat objek {@code Monster} dengan nama dan statistik acak dalam rentang
     * yang disesuaikan dengan tingkat kesulitan (memenuhi KF-03).
     *
     * <ul>
     *   <li>Rentang HP dasar: 100 s.d. 125 (dikalikan multiplier kesulitan)</li>
     *   <li>Rentang Attack dasar: 14 s.d. 19 (dikalikan multiplier kesulitan)</li>
     * </ul>
     *
     * @param diff   tingkat kesulitan aktif
     * @param random generator angka acak
     * @return objek Monster baru
     */
    public static Monster createRandomMonster(Difficulty diff, Random random) {
        requireDifficulty(diff);
        Random rng = (random != null) ? random : new Random();
        String chosenName = MONSTER_NAMES[rng.nextInt(MONSTER_NAMES.length)];
        int randomBaseHp = 100 + rng.nextInt(26);     // 100..125
        int randomBaseAttack = 14 + rng.nextInt(6);   // 14..19
        return new Monster(chosenName, randomBaseHp, randomBaseAttack, diff);
    }

    @Override
    public String getRole() {
        return "Monster";
    }
}
