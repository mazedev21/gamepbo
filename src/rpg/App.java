package rpg;

import java.awt.GraphicsEnvironment;
import java.util.Random;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import rpg.model.Character;
import rpg.model.Difficulty;
import rpg.model.Mage;
import rpg.model.Monster;
import rpg.model.Warrior;
import rpg.service.BattleService;
import rpg.service.GameSettings;
import rpg.service.SettingsService;
import rpg.ui.BattleDisplay;
import rpg.ui.SwingDisplay;

/**
 * Kelas {@code App} merupakan titik masuk utama (entry point) aplikasi
 * "Hero's Regret: Rescue Gone Wrong" sesuai arsitektur v3 (Sub-bab 11.2 & 11.3.4 Proposal).
 *
 * <p>Kelas ini bertugas melakukan perakitan objek (Dependency Injection) antara
 * lapisan model, service ({@link GameSettings}, {@link SettingsService},
 * {@link BattleService}), dan antarmuka pengguna ({@link SwingDisplay}).</p>
 */
public class App {

    public static void main(String[] args) {
        if (args.length > 0 && "--headless-demo".equalsIgnoreCase(args[0])) {
            runConsoleDemo();
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Lingkungan headless terdeteksi. Menjalankan simulasi demo konsol:");
            runConsoleDemo();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Gunakan look and feel bawaan jika gagal
            }

            // Perakitan objek (Dependency Injection) sesuai Sub-bab 11.3.4
            GameSettings gameSettings = new GameSettings();
            SettingsService settingsService = new SettingsService(gameSettings);
            SwingDisplay swingDisplay = new SwingDisplay(settingsService);
            BattleService battleService = new BattleService(swingDisplay);
            swingDisplay.setBattleService(battleService);

            swingDisplay.launchGui();
        });
    }

    /**
     * Menjalankan demonstrasi alur fitur Pengaturan Tingkat Kesulitan dan
     * Pertarungan RPG Turn-Based melalui implementasi polimorfik {@link BattleDisplay}
     * tanpa perlu membuka jendela GUI.
     */
    public static void runConsoleDemo() {
        System.out.println("======================================================================");
        System.out.println("  DEMO SIMULASI: HERO'S REGRET: RESCUE GONE WRONG (IMPLEMENTASI v3)  ");
        System.out.println("======================================================================");

        GameSettings settings = new GameSettings();
        SettingsService settingsService = new SettingsService(settings);

        System.out.println("[1] Kesulitan awal (Default) : " + settingsService.getCurrentDifficulty().getDescription());
        settingsService.changeDifficulty(Difficulty.SULIT);
        System.out.println("[2] Diubah oleh Pengelola    : " + settingsService.getCurrentDifficulty().getDescription());
        settingsService.resetDifficulty();
        System.out.println("[3] Reset ke Bawaan (KF-12)  : " + settingsService.getCurrentDifficulty().getDescription());
        System.out.println("----------------------------------------------------------------------");

        BattleDisplay consoleDisplay = new BattleDisplay() {
            @Override
            public void showMessage(String message) {
                System.out.println(message);
            }

            @Override
            public void showHpBar(Character character) {
                int filled = (int) Math.round(((double) character.getHp() / character.getMaxHp()) * 20);
                String bar = "#".repeat(Math.max(0, filled)) + "-".repeat(Math.max(0, 20 - filled));
                System.out.printf(
                    "    Status HP %-22s : [%s] %d/%d HP%n",
                    character.getName() + " (" + character.getRole() + ")",
                    bar,
                    character.getHp(),
                    character.getMaxHp()
                );
            }

            @Override
            public void showSummary(int turns, int totalDamage, boolean playerWon) {
                System.out.println("----------------------------------------------------------------------");
                System.out.println("HASIL AKHIR PERTARUNGAN:");
                System.out.println("• Pemenang            : " + (playerWon ? "PAHLAWAN (Putri Memblokir Sosmedmu!)" : "MONSTER"));
                System.out.println("• Total Giliran       : " + turns);
                System.out.println("• Total Damage Pemain : " + totalDamage);
                System.out.println("======================================================================");
            }
        };

        BattleService battleService = new BattleService(consoleDisplay, new Random(42L));
        Character hero = new Warrior("Henji sang Ksatria");
        Character magePreview = new Mage("Flora sang Penyihir");
        Character monster = new Monster("Goblin Penjaga Kastil", Difficulty.NORMAL);

        System.out.println("Kandidat Karakter 1 : " + hero.getName() + " | Role: " + hero.getRole()
            + " | HP: " + hero.getMaxHp() + " | Attack: " + hero.getAttack() + " | Skill: " + hero.getSkillName());
        System.out.println("Kandidat Karakter 2 : " + magePreview.getName() + " | Role: " + magePreview.getRole()
            + " | HP: " + magePreview.getMaxHp() + " | Attack: " + magePreview.getAttack() + " | Skill: " + magePreview.getSkillName());
        System.out.println("Lawan Monster       : " + monster.getName() + " | Role: " + monster.getRole()
            + " | HP: " + monster.getMaxHp() + " | Attack: " + monster.getAttack() + " | Skill: " + monster.getSkillName());
        System.out.println("----------------------------------------------------------------------");

        int[] actions = {1, 2, 2, 1};
        int idx = 0;
        while (!battleService.isBattleOver(hero, monster)) {
            int choice = actions[idx % actions.length];
            idx++;
            battleService.executePlayerAction(hero, monster, choice);
            if (battleService.isBattleOver(hero, monster)) {
                break;
            }
            battleService.executeEnemyTurn(monster, hero);
        }
        battleService.concludeBattle(hero, monster);
    }
}
