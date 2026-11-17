package rpg.model;

import rpg.skill.PowerStrike;

/**
 * Kelas {@code Warrior} merupakan subkelas dari {@link Character} dengan
 * karakteristik HP tinggi (140), serangan sedang (20), dan skill spesial
 * {@link PowerStrike} (KF-02, Sub-bab 11.3.2).
 */
public class Warrior extends Character {

    public Warrior(String name) {
        super(name, 140, 20, new PowerStrike());
    }

    @Override
    public String getRole() {
        return "Warrior";
    }
}
