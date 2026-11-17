package rpg.skill;

import rpg.model.Character;

/**
 * Implementasi {@link Skill} khusus untuk karakter {@link rpg.model.Monster}.
 *
 * <p><b>Rumus Damage:</b> {@code 1.5 x attack} monster ditambah bonus hantaman.</p>
 */
public class MonsterSmash implements Skill {

    @Override
    public String getName() {
        return "Monster Smash";
    }

    @Override
    public int use(Character user, Character target) {
        if (user == null || target == null) {
            throw new IllegalArgumentException("User dan target skill tidak boleh null");
        }
        int dmg = (int) Math.round(user.getAttack() * 1.5) + 6;
        target.takeDamage(dmg);
        return dmg;
    }
}
