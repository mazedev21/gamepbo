package rpg.skill;

import rpg.model.Character;

/**
 * Implementasi {@link Skill} khusus untuk karakter {@link rpg.model.Warrior}.
 *
 * <p><b>Rumus Damage:</b> {@code 2 x attack} pengguna (contoh: Warrior dengan
 * attack 20 menghasilkan 40 damage sesuai Bab 8.4 dan Bab 11.3.3 proposal).</p>
 */
public class PowerStrike implements Skill {

    @Override
    public String getName() {
        return "Power Strike";
    }

    @Override
    public int use(Character user, Character target) {
        if (user == null || target == null) {
            throw new IllegalArgumentException("User dan target skill tidak boleh null");
        }
        int dmg = user.getAttack() * 2;
        target.takeDamage(dmg);
        return dmg;
    }
}
