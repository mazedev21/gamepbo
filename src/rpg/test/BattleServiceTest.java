package rpg.test;

import rpg.model.Character;
import rpg.model.Difficulty;
import rpg.model.Mage;
import rpg.model.Monster;
import rpg.model.Warrior;
import rpg.service.BattleService;
import rpg.skill.Skill;
import rpg.ui.BattleDisplay;

/**
 * Kelas uji {@code BattleServiceTest} memverifikasi fitur Pertarungan RPG
 * beserta penerapan keempat pilar PBO dan kebutuhan nonfungsional (Sub-bab 11.4 Proposal).
 */
public class BattleServiceTest {

    public static void main(String[] args) {
        BattleDisplay silentDisplay = new BattleDisplay() {
            @Override
            public void showMessage(String m) {}

            @Override
            public void showHpBar(rpg.model.Character c) {}

            @Override
            public void showSummary(int t, int d, boolean w) {}
        };

        var svc = new BattleService(silentDisplay);
        var hero = new Warrior("Andi");
        var monster = new Monster("Goblin", Difficulty.NORMAL);

        // =====================================================================
        // Skenario 1 (Berhasil - Sesuai Sub-bab 11.4 Proposal):
        // Skill Power Strike memberi 40 damage, HP Goblin dari 110 tersisa 70
        // =====================================================================
        int dmg = svc.executePlayerAction(hero, monster, 2);
        cek(dmg == 40, "Power Strike harus memberi 40 damage");
        cek(monster.getHp() == 70, "HP Goblin harus tersisa 70");
        cek(svc.getTurns() == 1, "Jumlah giliran harus bertambah menjadi 1");
        cek(svc.getTotalPlayerDamage() == 40, "Total damage pemain harus tercatat 40");

        // =====================================================================
        // Skenario 2 (Gagal - Sesuai Sub-bab 11.4 Proposal):
        // Pilihan aksi di luar 1/2 melempar IllegalArgumentException
        // =====================================================================
        boolean gagalSesuaiAturan = false;
        try {
            svc.executePlayerAction(hero, monster, 9);
        } catch (IllegalArgumentException e) {
            gagalSesuaiAturan = true;
        }
        cek(gagalSesuaiAturan, "Pilihan aksi tidak valid harus ditolak");

        // =====================================================================
        // Skenario 3 (Uji Enkapsulasi & Batas HP >= 0 - KF-06 & Bab 8.4):
        // Damage berlebih tidak boleh membuat HP bernilai negatif
        // =====================================================================
        monster.takeDamage(999);
        cek(monster.getHp() == 0, "HP tidak boleh kurang dari 0 (enkapsulasi takeDamage)");
        cek(!monster.isAlive(), "Monster dengan HP 0 harus berstatus tidak hidup (!isAlive)");

        boolean tolakNamaKosong = false;
        try {
            new Warrior("   ");
        } catch (IllegalArgumentException e) {
            tolakNamaKosong = true;
        }
        cek(tolakNamaKosong, "Konstruktor Character wajib menolak nama kosong/blank");

        boolean tolakDamageNegatif = false;
        try {
            hero.takeDamage(-15);
        } catch (IllegalArgumentException e) {
            tolakDamageNegatif = true;
        }
        cek(tolakDamageNegatif, "Method takeDamage wajib menolak nilai damage negatif");

        // =====================================================================
        // Skenario 4 (Uji Polimorfisme Mage & KNF-04 Maintainability):
        // Menambah skill/karakter uji baru tanpa mengubah BattleService
        // =====================================================================
        var mage = new Mage("Flora");
        var targetMonster = new Monster("Orc", Difficulty.NORMAL);
        int fireballDmg = svc.executePlayerAction(mage, targetMonster, 2);
        cek(fireballDmg == 50, "Fireball Mage harus memberi 50 damage");
        cek(targetMonster.getHp() == 60, "HP Orc harus tersisa 60 setelah terkena Fireball");

        // Uji KNF-04: Tambah satu kelas Skill baru & subkelas Character baru tanpa mengubah BattleService
        Skill thunderBoltSkill = new Skill() {
            @Override
            public String getName() {
                return "Thunder Bolt";
            }

            @Override
            public int use(Character user, Character target) {
                int customDmg = user.getAttack() + 15;
                target.takeDamage(customDmg);
                return customDmg;
            }
        };

        Character customHero = new Character("Paladin Uji", 120, 25, thunderBoltSkill) {
            @Override
            public String getRole() {
                return "Paladin";
            }
        };

        int customSkillDmg = svc.executePlayerAction(customHero, targetMonster, 2);
        cek(customSkillDmg == 40, "Skill baru Thunder Bolt harus berjalan polimorfik di BattleService");

        System.out.println("Semua uji BattleService lulus.");
    }

    static void cek(boolean kondisi, String pesan) {
        if (!kondisi) {
            throw new AssertionError(pesan);
        }
    }
}
