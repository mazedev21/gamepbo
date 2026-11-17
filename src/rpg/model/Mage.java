package rpg.model;

import rpg.skill.Fireball;

/**
 * Kelas {@code Mage} merupakan subkelas dari {@link Character} dengan
 * karakteristik HP lebih rendah (100), serangan tinggi (28), dan skill spesial
 * {@link Fireball} (KF-02, Sub-bab 11.3.2).
 */
public class Mage extends Character {

    public Mage(String name) {
        super(name, 100, 28, new Fireball());
    }

    @Override
    public String getRole() {
        return "Mage";
    }
}
