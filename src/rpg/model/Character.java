package rpg.model;

import rpg.skill.Skill;

/**
 * Kelas abstrak {@code Character} merupakan induk dari seluruh petarung di dalam game
 * ({@link Warrior}, {@link Mage}, dan {@link Monster}).
 *
 * <p><b>Penerapan Empat Pilar PBO (Sub-bab 11.3 Proposal):</b></p>
 * <ul>
 *   <li><b>Abstraction:</b> Dideklarasikan {@code abstract} karena tidak ada entitas
 *       "karakter umum" yang bertarung secara langsung; memiliki method abstrak
 *       {@link #getRole()} yang wajib diimplementasikan oleh subkelas.</li>
 *   <li><b>Encapsulation:</b> Seluruh atribut ({@code name}, {@code maxHp}, {@code attack},
 *       {@code skill}, dan {@code hp}) dideklarasikan {@code private}. Konstruktor dan
 *       method menegakkan 3 aturan bisnis:
 *       <ol>
 *         <li>Nama karakter wajib diisi (tidak boleh {@code null} atau blank).</li>
 *         <li>{@code maxHp} dan {@code attack} harus bernilai positif ({@code > 0}), serta
 *             {@code skill} tidak boleh {@code null}.</li>
 *         <li>Damage yang diterima tidak boleh negatif dan nilai {@code hp} dijaga agar
 *             tidak pernah turun di bawah {@code 0} (KF-06).</li>
 *       </ol>
 *   </li>
 *   <li><b>Inheritance:</b> Menyediakan konstruktor {@code protected} dan perilaku bersama
 *       ({@link #takeDamage(int)}, {@link #basicAttack(Character)},
 *       {@link #specialAttack(Character)}, {@link #isAlive()}) yang diwarisi oleh semua
 *       subkelas tanpa duplikasi kode.</li>
 *   <li><b>Polymorphism:</b> Method {@link #specialAttack(Character)} mendelegasikan
 *       eksekusi jurus ke objek {@link Skill} secara polimorfik.</li>
 * </ul>
 */
public abstract class Character {
    public static final int MAX_NAME_LENGTH = 24;

    private final String name;
    private final int maxHp;
    private final int attack;
    private final Skill skill;
    private int hp;

    protected Character(String name, int maxHp, int attack, Skill skill) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama karakter wajib diisi");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Nama karakter maksimal " + MAX_NAME_LENGTH + " karakter"
            );
        }
        if (maxHp <= 0 || attack <= 0) {
            throw new IllegalArgumentException("HP maksimum dan attack harus > 0");
        }
        if (skill == null) {
            throw new IllegalArgumentException("Skill tidak boleh null");
        }
        this.name = trimmedName;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.skill = skill;
    }

    /**
     * Mengurangi HP karakter sebesar damage yang diterima dan menjaga agar
     * HP tidak pernah bernilai negatif (KF-06).
     *
     * @param dmg jumlah damage yang diterima (harus >= 0)
     */
    public void takeDamage(int dmg) {
        if (dmg < 0) {
            throw new IllegalArgumentException("Damage tidak boleh negatif");
        }
        this.hp = Math.max(0, this.hp - dmg);
    }

    /**
     * Melakukan serangan biasa ke karakter target (KF-05).
     *
     * @param target karakter lawan
     * @return jumlah damage yang diberikan
     */
    public int basicAttack(Character target) {
        if (target == null) {
            throw new IllegalArgumentException("Target serangan tidak boleh null");
        }
        target.takeDamage(this.attack);
        return this.attack;
    }

    /**
     * Melancarkan skill spesial karakter secara polimorfik melalui interface {@link Skill} (KF-05).
     *
     * @param target karakter lawan
     * @return jumlah damage yang dihasilkan oleh skill spesial
     */
    public int specialAttack(Character target) {
        if (target == null) {
            throw new IllegalArgumentException("Target skill tidak boleh null");
        }
        return skill.use(this, target);
    }

    /**
     * Memeriksa apakah karakter masih hidup ({@code hp > 0}).
     *
     * @return {@code true} jika HP masih di atas 0
     */
    public boolean isAlive() {
        return hp > 0;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public String getSkillName() {
        return skill.getName();
    }

    public Skill getSkill() {
        return skill;
    }

    /**
     * Mengembalikan nama peran/kelas karakter (contoh: "Warrior", "Mage", "Monster").
     *
     * @return nama peran karakter
     */
    public abstract String getRole();
}
