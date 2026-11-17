package rpg.skill;

import rpg.model.Character;

/**
 * Implementasi {@link Skill} khusus untuk karakter {@link rpg.model.Mage}.
 *
 * <p><b>Rumus Damage:</b> {@code (attack * 2) - 6} (contoh: Mage dengan
 * attack 28 menghasilkan 50 damage sihir api berdaya ledak tinggi).</p>
 */
public class Fireball implements Skill {

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public int use(Character user, Character target) {
        if (user == null || target == null) {
            throw new IllegalArgumentException("User dan target skill tidak boleh null");
        }
        int dmg = (user.getAttack() * 2) - 6;
        target.takeDamage(dmg);
        return dmg;
    }
}
