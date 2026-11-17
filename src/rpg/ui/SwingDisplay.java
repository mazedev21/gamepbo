package rpg.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import rpg.model.Character;
import rpg.model.Difficulty;
import rpg.model.Mage;
import rpg.model.Monster;
import rpg.model.Warrior;
import rpg.service.BattleService;
import rpg.service.SettingsService;

/**
 * Kelas {@code SwingDisplay} mengimplementasikan interface {@link BattleDisplay}
 * menggunakan komponen standar Java Swing ({@code javax.swing}) tanpa pustaka eksternal.
 *
 * <p><b>Pemenuhan Kebutuhan Fungsional & Nonfungsional:</b></p>
 * <ul>
 *   <li><b>KF-01:</b> Menampilkan judul game, narasi pembukaan, dan menu utama.</li>
 *   <li><b>KF-02:</b> Memungkinkan pemain memilih karakter Warrior atau Mage beserta statistiknya.</li>
 *   <li><b>KF-03:</b> Membangkitkan monster dengan statistik acak sesuai tingkat kesulitan.</li>
 *   <li><b>KF-04 & KF-05:</b> Mengatur giliran dan menyediakan tombol Serangan Biasa & Skill Spesial.</li>
 *   <li><b>KF-07:</b> Menampilkan bar HP grafis ({@link JProgressBar}) dan pesan aksi tiap giliran.</li>
 *   <li><b>KF-09 & KF-10:</b> Menampilkan narasi akhir cerita komedi (menang/kalah) dan ringkasan pertarungan.</li>
 *   <li><b>KF-11 & KF-12:</b> Menyediakan panel Pengaturan Tingkat Kesulitan dan tombol Reset ke Bawaan.</li>
 *   <li><b>KF-13:</b> Menyediakan tombol Main Ulang atau Keluar setelah pertarungan selesai.</li>
 *   <li><b>KNF-01 s.d. KNF-05:</b> Responsif, intuitif, menangani input tidak valid dengan aman,
 *       dan berjalan murni di atas Java Swing JDK standar pada Linux Ubuntu.</li>
 * </ul>
 */
public class SwingDisplay implements BattleDisplay {

    private static final String CARD_MENU = "MENU";
    private static final String CARD_SETTINGS = "SETTINGS";
    private static final String CARD_CHARACTER_SELECT = "CHARACTER_SELECT";
    private static final String CARD_BATTLE = "BATTLE";

    private static final Color BG_DARK = new Color(24, 28, 38);
    private static final Color BG_PANEL = new Color(36, 42, 56);
    private static final Color BG_CARD = new Color(46, 54, 72);
    private static final Color ACCENT_GOLD = new Color(245, 194, 66);
    private static final Color ACCENT_BLUE = new Color(66, 153, 225);
    private static final Color ACCENT_PURPLE = new Color(159, 122, 234);
    private static final Color ACCENT_GREEN = new Color(72, 187, 120);
    private static final Color ACCENT_RED = new Color(229, 62, 62);
    private static final Color TEXT_LIGHT = new Color(237, 242, 247);
    private static final Color TEXT_MUTED = new Color(160, 174, 192);

    private final SettingsService settingsService;
    private BattleService battleService;
    private final Random random = new Random();

    // Komponen utama JFrame & CardLayout
    private JFrame frame;
    private JPanel mainContainer;
    private CardLayout cardLayout;

    // Komponen Menu Utama
    private JLabel menuDifficultyBadge;

    // Komponen Pengaturan
    private JRadioButton rbEasy;
    private JRadioButton rbNormal;
    private JRadioButton rbHard;
    private JLabel settingsStatusLabel;

    // Komponen Pemilihan Karakter
    private JTextField heroNameField;
    private JRadioButton rbWarrior;
    private JRadioButton rbMage;

    // Komponen Pertarungan
    private Character currentPlayer;
    private Character currentEnemy;

    private JLabel playerNameLabel;
    private JLabel playerStatsLabel;
    private JProgressBar playerHpBar;

    private JLabel enemyNameLabel;
    private JLabel enemyStatsLabel;
    private JProgressBar enemyHpBar;

    private JTextArea battleLogArea;
    private JLabel turnStatusBanner;
    private JButton btnBasicAttack;
    private JButton btnSpecialSkill;
    private JButton btnPlayAgain;
    private JButton btnBackToMenu;
    private JButton btnExitAfterBattle;
    private JPanel postBattlePanel;

    public SwingDisplay(SettingsService settingsService) {
        if (settingsService == null) {
            throw new IllegalArgumentException("SettingsService tidak boleh null");
        }
        this.settingsService = settingsService;
    }

    /**
     * Menghubungkan {@link BattleService} ke tampilan GUI ini.
     *
     * @param battleService service pertarungan
     */
    public void setBattleService(BattleService battleService) {
        if (battleService == null) {
            throw new IllegalArgumentException("BattleService tidak boleh null");
        }
        this.battleService = battleService;
    }

    /**
     * Membangun dan menampilkan jendela utama aplikasi GUI Java Swing.
     */
    public void launchGui() {
        frame = new JFrame("Hero's Regret: Rescue Gone Wrong — RPG Turn-Based PBO (v3)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 680);
        frame.setMinimumSize(new Dimension(820, 620));
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(BG_DARK);

        mainContainer.add(buildMainMenuPanel(), CARD_MENU);
        mainContainer.add(buildSettingsPanel(), CARD_SETTINGS);
        mainContainer.add(buildCharacterSelectPanel(), CARD_CHARACTER_SELECT);
        mainContainer.add(buildBattlePanel(), CARD_BATTLE);

        frame.setContentPane(mainContainer);
        refreshDifficultyIndicators();
        cardLayout.show(mainContainer, CARD_MENU);
        frame.setVisible(true);
    }

    // =========================================================================
    // 1. PANEL MENU UTAMA (KF-01)
    // =========================================================================

    private JPanel buildMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(30, 45, 30, 45));

        // Header Judul
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(BG_DARK);

        JLabel subtitleTop = new JLabel("TEAM BASED PROJECT PBO — TIM EXPERT IZIN LEWAT", SwingConstants.CENTER);
        subtitleTop.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleTop.setFont(new Font("SansSerif", Font.BOLD, 12));
        subtitleTop.setForeground(ACCENT_BLUE);

        JLabel titleLabel = new JLabel("HERO'S REGRET: RESCUE GONE WRONG", SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_GOLD);

        menuDifficultyBadge = new JLabel("Tingkat Kesulitan Aktif: Normal", SwingConstants.CENTER);
        menuDifficultyBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuDifficultyBadge.setFont(new Font("SansSerif", Font.ITALIC, 14));
        menuDifficultyBadge.setForeground(ACCENT_GREEN);

        headerPanel.add(subtitleTop);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(menuDifficultyBadge);

        // Narasi Pembukaan (KF-01)
        JTextArea openingNarration = new JTextArea(
            "NARASI PEMBUKAAN:\n\n"
            + "Putri kerajaan telah diculik dan dikurung di menara Kastil Kelam yang dijaga oleh monster ganas!\n"
            + "Sebagai pahlawan yang sedang butuh konten demi menaikkan jumlah followers, kamu bergegas "
            + "menuju kastil untuk menyelamatkan sang Putri.\n\n"
            + "Namun hati-hati... kabarnya sang Putri sangat pemilih soal estetika penyelamatan.\n"
            + "Pilih kelas pahlawanmu (Warrior atau Mage), kalahkan monster penjaga dalam pertarungan "
            + "bergiliran (turn-based), dan buktikan apakah penyelamatan ini akan berakhir manis atau "
            + "justru menjadi penyesalan terbesar dalam hidupmu!"
        );
        openingNarration.setEditable(false);
        openingNarration.setLineWrap(true);
        openingNarration.setWrapStyleWord(true);
        openingNarration.setFont(new Font("SansSerif", Font.PLAIN, 15));
        openingNarration.setBackground(BG_PANEL);
        openingNarration.setForeground(TEXT_LIGHT);
        openingNarration.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(74, 85, 104), 1),
            new EmptyBorder(20, 24, 20, 24)
        ));

        // Tombol Menu Utama
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        buttonPanel.setBackground(BG_DARK);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnStart = createStyledButton("Mulai Permainan", ACCENT_GREEN, Color.WHITE);
        btnStart.addActionListener(e -> {
            cardLayout.show(mainContainer, CARD_CHARACTER_SELECT);
        });

        JButton btnSettings = createStyledButton("Pengaturan Kesulitan", ACCENT_BLUE, Color.WHITE);
        btnSettings.addActionListener(e -> {
            syncSettingsRadioButtons();
            settingsStatusLabel.setText("Silakan pilih tingkat kesulitan permainan.");
            cardLayout.show(mainContainer, CARD_SETTINGS);
        });

        JButton btnExit = createStyledButton("Keluar", ACCENT_RED, Color.WHITE);
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Apakah Anda yakin ingin keluar dari permainan?",
                "Konfirmasi Keluar",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                frame.dispose();
                System.exit(0);
            }
        });

        buttonPanel.add(btnStart);
        buttonPanel.add(btnSettings);
        buttonPanel.add(btnExit);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(openingNarration, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // 2. PANEL PENGATURAN TINGKAT KESULITAN (KF-11, KF-12)
    // =========================================================================

    private JPanel buildSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("PENGATURAN TINGKAT KESULITAN (AKTOR: PENGELOLA PERMAINAN)", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(ACCENT_GOLD);

        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setBackground(BG_PANEL);
        centerBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(74, 85, 104), 1),
            new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel infoLabel = new JLabel(
            "<html>Pilih tingkat kesulitan yang memengaruhi pengali HP dan Attack Monster.<br>"
            + "Pengaturan ini berlaku selama program berjalan dan kembali ke Normal saat ditutup.</html>"
        );
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoLabel.setForeground(TEXT_LIGHT);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rbEasy = createDifficultyRadio(
            "MUDAH (x0.8) — Monster memiliki HP & Attack 20% lebih rendah (Cocok untuk pemula)"
        );
        rbNormal = createDifficultyRadio(
            "NORMAL (x1.0 - Bawaan) — Statistik Monster seimbang sesuai rancangan standar"
        );
        rbHard = createDifficultyRadio(
            "SULIT (x1.3) — Monster memiliki HP & Attack 30% lebih tinggi (Tantangan ekstra!)"
        );

        ButtonGroup group = new ButtonGroup();
        group.add(rbEasy);
        group.add(rbNormal);
        group.add(rbHard);

        settingsStatusLabel = new JLabel("Tingkat kesulitan saat ini: " + settingsService.getCurrentDifficulty());
        settingsStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        settingsStatusLabel.setForeground(ACCENT_GREEN);
        settingsStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerBox.add(infoLabel);
        centerBox.add(Box.createVerticalStrut(20));
        centerBox.add(rbEasy);
        centerBox.add(Box.createVerticalStrut(12));
        centerBox.add(rbNormal);
        centerBox.add(Box.createVerticalStrut(12));
        centerBox.add(rbHard);
        centerBox.add(Box.createVerticalStrut(25));
        centerBox.add(settingsStatusLabel);

        JPanel bottomButtons = new JPanel(new GridLayout(1, 3, 15, 0));
        bottomButtons.setBackground(BG_DARK);

        JButton btnSave = createStyledButton("Terapkan Kesulitan (KF-11)", ACCENT_GREEN, Color.WHITE);
        btnSave.addActionListener(e -> {
            Difficulty selected = Difficulty.NORMAL;
            if (rbEasy.isSelected()) {
                selected = Difficulty.MUDAH;
            } else if (rbHard.isSelected()) {
                selected = Difficulty.SULIT;
            }
            settingsService.changeDifficulty(selected);
            refreshDifficultyIndicators();
            settingsStatusLabel.setText(
                "Berhasil disimpan! Kesulitan aktif: " + selected.getDescription()
            );
        });

        JButton btnReset = createStyledButton("Reset ke Bawaan / Normal (KF-12)", ACCENT_GOLD, Color.BLACK);
        btnReset.addActionListener(e -> {
            settingsService.resetDifficulty();
            syncSettingsRadioButtons();
            refreshDifficultyIndicators();
            settingsStatusLabel.setText(
                "Pengaturan dikembalikan ke bawaan: " + settingsService.getCurrentDifficulty().getDescription()
            );
        });

        JButton btnBack = createStyledButton("Kembali ke Menu Utama", ACCENT_BLUE, Color.WHITE);
        btnBack.addActionListener(e -> {
            refreshDifficultyIndicators();
            cardLayout.show(mainContainer, CARD_MENU);
        });

        bottomButtons.add(btnSave);
        bottomButtons.add(btnReset);
        bottomButtons.add(btnBack);

        panel.add(title, BorderLayout.NORTH);
        panel.add(centerBox, BorderLayout.CENTER);
        panel.add(bottomButtons, BorderLayout.SOUTH);

        return panel;
    }

    private JRadioButton createDifficultyRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("SansSerif", Font.BOLD, 14));
        rb.setBackground(BG_PANEL);
        rb.setForeground(TEXT_LIGHT);
        rb.setFocusPainted(false);
        rb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return rb;
    }

    private void syncSettingsRadioButtons() {
        Difficulty current = settingsService.getCurrentDifficulty();
        rbEasy.setSelected(current == Difficulty.MUDAH);
        rbNormal.setSelected(current == Difficulty.NORMAL);
        rbHard.setSelected(current == Difficulty.SULIT);
    }

    private void refreshDifficultyIndicators() {
        Difficulty current = settingsService.getCurrentDifficulty();
        if (menuDifficultyBadge != null) {
            menuDifficultyBadge.setText("Tingkat Kesulitan Aktif: " + current.getDescription());
        }
    }

    // =========================================================================
    // 3. PANEL PEMILIHAN KARAKTER (KF-02)
    // =========================================================================

    private JPanel buildCharacterSelectPanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("PILIH KARAKTER PAHLAWANMU (KF-02)", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(ACCENT_GOLD);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(BG_DARK);

        // Input nama pahlawan
        JPanel nameInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        nameInputPanel.setBackground(BG_PANEL);
        nameInputPanel.setBorder(BorderFactory.createLineBorder(new Color(74, 85, 104), 1));

        JLabel namePrompt = new JLabel("Nama Pahlawan:");
        namePrompt.setFont(new Font("SansSerif", Font.BOLD, 14));
        namePrompt.setForeground(TEXT_LIGHT);

        heroNameField = new JTextField("Sir Arthur", 22);
        heroNameField.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JLabel nameHint = new JLabel("(Wajib diisi — divalidasi oleh konstruktor Character)");
        nameHint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        nameHint.setForeground(TEXT_MUTED);

        nameInputPanel.add(namePrompt);
        nameInputPanel.add(heroNameField);
        nameInputPanel.add(nameHint);

        // Kartu pilihan Warrior vs Mage
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsGrid.setBackground(BG_DARK);

        rbWarrior = new JRadioButton("Pilih Kelas WARRIOR", true);
        rbMage = new JRadioButton("Pilih Kelas MAGE", false);
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(rbWarrior);
        roleGroup.add(rbMage);

        JPanel warriorCard = buildRoleCard(
            "WARRIOR (Ksatria Pedang)",
            ACCENT_BLUE,
            "• HP Maksimum : 140 HP (Ketahanan Tinggi)\n"
            + "• Kekuatan Serang : 20 Attack\n"
            + "• Skill Spesial : Power Strike\n"
            + "• Efek Skill : 2 x Attack (40 Damage)\n\n"
            + "Penerapan PBO:\n"
            + "Subkelas Character yang memasang objek PowerStrike (implements Skill).",
            rbWarrior
        );

        JPanel mageCard = buildRoleCard(
            "MAGE (Penyihir Elemen)",
            ACCENT_PURPLE,
            "• HP Maksimum : 100 HP (Ketahanan Rendah)\n"
            + "• Kekuatan Serang : 28 Attack (Serangan Kuat)\n"
            + "• Skill Spesial : Fireball\n"
            + "• Efek Skill : (2 x Attack) - 6 (50 Damage)\n\n"
            + "Penerapan PBO:\n"
            + "Subkelas Character yang memasang objek Fireball (implements Skill).",
            rbMage
        );

        cardsGrid.add(warriorCard);
        cardsGrid.add(mageCard);

        centerPanel.add(nameInputPanel, BorderLayout.NORTH);
        centerPanel.add(cardsGrid, BorderLayout.CENTER);

        // Tombol Bawah
        JPanel footerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        footerPanel.setBackground(BG_DARK);

        JButton btnBack = createStyledButton("Kembali ke Menu Utama", new Color(113, 128, 150), Color.WHITE);
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, CARD_MENU));

        JButton btnStartBattle = createStyledButton("Masuki Kastil & Mulai Pertarungan!", ACCENT_GREEN, Color.WHITE);
        btnStartBattle.addActionListener(e -> initiateBattleFromSelection());

        footerPanel.add(btnBack);
        footerPanel.add(btnStartBattle);

        panel.add(title, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildRoleCard(String header, Color badgeColor, String details, JRadioButton radioBtn) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(badgeColor, 2),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel title = new JLabel(header);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(badgeColor);

        JTextArea desc = new JTextArea(details);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("Monospaced", Font.PLAIN, 13));
        desc.setBackground(BG_CARD);
        desc.setForeground(TEXT_LIGHT);

        radioBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        radioBtn.setBackground(BG_PANEL);
        radioBtn.setForeground(ACCENT_GOLD);
        radioBtn.setFocusPainted(false);
        radioBtn.setBorder(new EmptyBorder(8, 12, 8, 12));

        card.add(title, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(radioBtn, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 4. PANEL ARENA PERTARUNGAN & RINGKASAN (KF-03 s.d. KF-10, KF-13)
    // =========================================================================

    private JPanel buildBattlePanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Bagian Atas: Kartu Status Pemain & Monster dengan JProgressBar HP (KF-07)
        JPanel combatantsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        combatantsPanel.setBackground(BG_DARK);

        // Panel Status Pemain
        JPanel playerCard = new JPanel(new GridLayout(3, 1, 4, 4));
        playerCard.setBackground(BG_PANEL);
        playerCard.setBorder( createTitledBorder("PAHLAWAN PEMAIN", ACCENT_BLUE) );

        playerNameLabel = new JLabel("Nama Pahlawan (Role)");
        playerNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        playerNameLabel.setForeground(TEXT_LIGHT);

        playerStatsLabel = new JLabel("Attack: - | Skill: -");
        playerStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        playerStatsLabel.setForeground(TEXT_MUTED);

        playerHpBar = new JProgressBar(0, 100);
        playerHpBar.setStringPainted(true);
        playerHpBar.setFont(new Font("SansSerif", Font.BOLD, 13));
        playerHpBar.setForeground(ACCENT_GREEN);

        playerCard.add(playerNameLabel);
        playerCard.add(playerStatsLabel);
        playerCard.add(playerHpBar);

        // Panel Status Monster
        JPanel enemyCard = new JPanel(new GridLayout(3, 1, 4, 4));
        enemyCard.setBackground(BG_PANEL);
        enemyCard.setBorder( createTitledBorder("MONSTER PENJAGA KASTIL", ACCENT_RED) );

        enemyNameLabel = new JLabel("Nama Monster (Monster)");
        enemyNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        enemyNameLabel.setForeground(TEXT_LIGHT);

        enemyStatsLabel = new JLabel("Attack: - | Skill: -");
        enemyStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        enemyStatsLabel.setForeground(TEXT_MUTED);

        enemyHpBar = new JProgressBar(0, 100);
        enemyHpBar.setStringPainted(true);
        enemyHpBar.setFont(new Font("SansSerif", Font.BOLD, 13));
        enemyHpBar.setForeground(ACCENT_GREEN);

        enemyCard.add(enemyNameLabel);
        enemyCard.add(enemyStatsLabel);
        enemyCard.add(enemyHpBar);

        combatantsPanel.add(playerCard);
        combatantsPanel.add(enemyCard);

        // Bagian Tengah: Log Pertarungan & Narasi Akhir
        JPanel logContainer = new JPanel(new BorderLayout(6, 6));
        logContainer.setBackground(BG_DARK);

        turnStatusBanner = new JLabel("Giliranmu! Pilih aksi serangan di bawah.", SwingConstants.CENTER);
        turnStatusBanner.setFont(new Font("SansSerif", Font.BOLD, 15));
        turnStatusBanner.setForeground(ACCENT_GOLD);
        turnStatusBanner.setOpaque(true);
        turnStatusBanner.setBackground(BG_CARD);
        turnStatusBanner.setBorder(new EmptyBorder(8, 12, 8, 12));

        battleLogArea = new JTextArea();
        battleLogArea.setEditable(false);
        battleLogArea.setLineWrap(true);
        battleLogArea.setWrapStyleWord(true);
        battleLogArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        battleLogArea.setBackground(new Color(18, 21, 28));
        battleLogArea.setForeground(TEXT_LIGHT);
        battleLogArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scrollPane = new JScrollPane(battleLogArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(74, 85, 104), 1));

        logContainer.add(turnStatusBanner, BorderLayout.NORTH);
        logContainer.add(scrollPane, BorderLayout.CENTER);

        // Bagian Bawah: Tombol Aksi Giliran & Tombol Main Ulang / Keluar (KF-05, KF-13)
        JPanel southContainer = new JPanel(new GridLayout(2, 1, 8, 8));
        southContainer.setBackground(BG_DARK);

        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        actionButtonsPanel.setBackground(BG_DARK);

        btnBasicAttack = createStyledButton("1. Serangan Biasa", ACCENT_BLUE, Color.WHITE);
        btnBasicAttack.addActionListener(e -> handlePlayerTurnAction(1));

        btnSpecialSkill = createStyledButton("2. Gunakan Skill Spesial", ACCENT_PURPLE, Color.WHITE);
        btnSpecialSkill.addActionListener(e -> handlePlayerTurnAction(2));

        actionButtonsPanel.add(btnBasicAttack);
        actionButtonsPanel.add(btnSpecialSkill);

        postBattlePanel = new JPanel(new GridLayout(1, 3, 14, 0));
        postBattlePanel.setBackground(BG_DARK);

        btnPlayAgain = createStyledButton("Main Ulang [KF-13]", ACCENT_GREEN, Color.WHITE);
        btnPlayAgain.addActionListener(e -> {
            cardLayout.show(mainContainer, CARD_CHARACTER_SELECT);
        });

        btnBackToMenu = createStyledButton("Menu Utama", ACCENT_GOLD, Color.BLACK);
        btnBackToMenu.addActionListener(e -> {
            refreshDifficultyIndicators();
            cardLayout.show(mainContainer, CARD_MENU);
        });

        btnExitAfterBattle = createStyledButton("Keluar [KF-13]", ACCENT_RED, Color.WHITE);
        btnExitAfterBattle.addActionListener(e -> {
            frame.dispose();
            System.exit(0);
        });

        postBattlePanel.add(btnPlayAgain);
        postBattlePanel.add(btnBackToMenu);
        postBattlePanel.add(btnExitAfterBattle);
        postBattlePanel.setVisible(false);

        southContainer.add(actionButtonsPanel);
        southContainer.add(postBattlePanel);

        panel.add(combatantsPanel, BorderLayout.NORTH);
        panel.add(logContainer, BorderLayout.CENTER);
        panel.add(southContainer, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // LOGIKA INTERAKSI GUI <-> SERVICE
    // =========================================================================

    private void initiateBattleFromSelection() {
        if (battleService == null) {
            JOptionPane.showMessageDialog(
                frame,
                "BattleService belum diinisialisasi!",
                "Kesalahan Sistem",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String heroName = heroNameField.getText();
        try {
            if (rbWarrior.isSelected()) {
                currentPlayer = new Warrior(heroName);
            } else {
                currentPlayer = new Mage(heroName);
            }
        } catch (IllegalArgumentException ex) {
            // KNF-03: Menangani masukan tidak valid dengan dialog tanpa menghentikan aplikasi
            JOptionPane.showMessageDialog(
                frame,
                ex.getMessage() + "\nSilakan masukkan nama pahlawan terlebih dahulu.",
                "Input Tidak Valid (Enkapsulasi Character)",
                JOptionPane.WARNING_MESSAGE
            );
            heroNameField.requestFocusInWindow();
            return;
        }

        Difficulty diff = settingsService.getCurrentDifficulty();
        currentEnemy = Monster.createRandomMonster(diff, random);
        battleService.resetBattleStats();

        // Setup label dan bar HP awal
        playerNameLabel.setText(currentPlayer.getName() + " [" + currentPlayer.getRole() + "]");
        playerStatsLabel.setText(
            "Attack: " + currentPlayer.getAttack() + " | Skill: " + currentPlayer.getSkillName()
        );
        enemyNameLabel.setText(currentEnemy.getName() + " [" + currentEnemy.getRole() + "]");
        enemyStatsLabel.setText(
            "Attack: " + currentEnemy.getAttack() + " | Skill: " + currentEnemy.getSkillName()
            + " | Kesulitan: " + diff.name()
        );

        showHpBar(currentPlayer);
        showHpBar(currentEnemy);

        btnBasicAttack.setText(
            "1. Serangan Biasa (" + currentPlayer.getAttack() + " Damage)"
        );
        btnSpecialSkill.setText(
            "2. Skill Spesial: " + currentPlayer.getSkillName()
        );
        btnBasicAttack.setEnabled(true);
        btnSpecialSkill.setEnabled(true);
        postBattlePanel.setVisible(false);

        battleLogArea.setText("");
        showMessage("======================================================================");
        showMessage("PERTARUNGAN DIMULAI! (Tingkat Kesulitan: " + diff.getDescription() + ")");
        showMessage(
            currentPlayer.getName() + " (" + currentPlayer.getRole() + ", HP: "
            + currentPlayer.getHp() + ") berhadapan dengan "
            + currentEnemy.getName() + " (HP: " + currentEnemy.getHp() + ")!"
        );
        showMessage("======================================================================\n");

        turnStatusBanner.setText(
            "Giliran " + currentPlayer.getName() + "! Pilih Serangan Biasa atau Skill Spesial."
        );
        turnStatusBanner.setForeground(ACCENT_GOLD);

        cardLayout.show(mainContainer, CARD_BATTLE);
    }

    private void handlePlayerTurnAction(int choice) {
        if (currentPlayer == null || currentEnemy == null || battleService.isBattleOver(currentPlayer, currentEnemy)) {
            return;
        }

        // Nonaktifkan tombol sementara aksi diproses (KNF-03)
        btnBasicAttack.setEnabled(false);
        btnSpecialSkill.setEnabled(false);

        // Giliran Pemain (KF-04, KF-05, KF-06, KF-07)
        battleService.executePlayerAction(currentPlayer, currentEnemy, choice);

        // Cek apakah monster sudah kalah (KF-09)
        if (battleService.isBattleOver(currentPlayer, currentEnemy)) {
            battleService.concludeBattle(currentPlayer, currentEnemy);
            return;
        }

        // Giliran Monster otomatis dengan peluang 20% skill spesial (KF-08)
        battleService.executeEnemyTurn(currentEnemy, currentPlayer);
        showMessage("----------------------------------------------------------------------");

        // Cek apakah pemain kalah setelah serangan monster (KF-09)
        if (battleService.isBattleOver(currentPlayer, currentEnemy)) {
            battleService.concludeBattle(currentPlayer, currentEnemy);
            return;
        }

        // Lanjut ke giliran berikutnya
        btnBasicAttack.setEnabled(true);
        btnSpecialSkill.setEnabled(true);
        turnStatusBanner.setText(
            "Giliran ke-" + (battleService.getTurns() + 1) + ": Pilih aksi untuk " + currentPlayer.getName() + "!"
        );
    }

    // =========================================================================
    // IMPLEMENTASI INTERFACE BattleDisplay (Sub-bab 11.3.3)
    // =========================================================================

    @Override
    public void showMessage(String message) {
        if (battleLogArea != null) {
            battleLogArea.append(message + "\n");
            battleLogArea.setCaretPosition(battleLogArea.getDocument().getLength());
        }
    }

    @Override
    public void showHpBar(Character character) {
        if (character == null) {
            return;
        }
        JProgressBar targetBar = (character == currentPlayer) ? playerHpBar : enemyHpBar;
        if (targetBar == null) {
            return;
        }

        targetBar.setMaximum(character.getMaxHp());
        targetBar.setValue(character.getHp());
        targetBar.setString("HP: " + character.getHp() + " / " + character.getMaxHp());

        double ratio = (double) character.getHp() / character.getMaxHp();
        if (ratio > 0.5) {
            targetBar.setForeground(ACCENT_GREEN);
        } else if (ratio > 0.25) {
            targetBar.setForeground(ACCENT_GOLD);
        } else {
            targetBar.setForeground(ACCENT_RED);
        }
    }

    @Override
    public void showSummary(int turns, int totalDamage, boolean playerWon) {
        btnBasicAttack.setEnabled(false);
        btnSpecialSkill.setEnabled(false);
        postBattlePanel.setVisible(true);

        String endingStory;
        if (playerWon) {
            turnStatusBanner.setText("KEMENANGAN! Monster berhasil ditumbangkan... tapi tunggu dulu!");
            turnStatusBanner.setForeground(ACCENT_GREEN);

            endingStory =
                "\n======================================================================\n"
                + "AKHIR CERITA (MENANG): \"HERO'S REGRET: RESCUE GONE WRONG\"\n"
                + "======================================================================\n"
                + "Monster penjaga akhirnya tumbang! Dengan napas tersengal-sengal dan jubah\n"
                + "yang gosong sebelah, " + currentPlayer.getName() + " mendobrak pintu kamar menara\n"
                + "sambil berteriak bangga, \"Tenang Tuan Putri, aku datang menyelamatkanmu!\"\n\n"
                + "Sayangnya, dobrakan pintu itu membuat ember pel penjaga tumpah tepat ke gaun\n"
                + "mahal sang Putri, sementara kamu terpeleset kulit pisang di depan mukanya.\n\n"
                + "Sang Putri menatapmu dingin: \"Jijik banget! Penyelamatan macam apa ini?!\n"
                + "Merusak feeds estetikku saja!\" Detik itu juga sang Putri memblokir akun\n"
                + "Instagram, TikTok, X, hingga nomor WhatsApp milik " + currentPlayer.getName() + ".\n"
                + "Selamat, misi penyelamatan berhasil... tapi reputasi sosialmu hancur lebur!\n";
        } else {
            turnStatusBanner.setText("KEKALAHAN! Pahlawan tumbang di hadapan monster kastil.");
            turnStatusBanner.setForeground(ACCENT_RED);

            endingStory =
                "\n======================================================================\n"
                + "AKHIR CERITA (KALAH): \"TERKAPAR DI DEPAN GERBANG MENARA\"\n"
                + "======================================================================\n"
                + currentPlayer.getName() + " kehabisan seluruh HP dan terkapar tak berdaya.\n"
                + "Dari jendela atas menara, sang Putri mengintip sambil menggelengkan kepala:\n"
                + "\"Sudahlah lemah, gayanya sok keren pula. Untung belum sempat aku follback!\"\n"
                + "Sang Monster pun menyeretmu keluar gerbang kastil. Coba lagi dengan strategi\n"
                + "atau tingkat kesulitan yang berbeda!\n";
        }

        String battleSummary =
            "----------------------------------------------------------------------\n"
            + "RINGKASAN PERTARUNGAN (KF-10):\n"
            + "• Status Akhir               : " + (playerWon ? "MENANG (Putri Memblokir Sosmedmu)" : "KALAH") + "\n"
            + "• Pahlawan                   : " + currentPlayer.getName() + " (" + currentPlayer.getRole() + ")\n"
            + "• Lawan                      : " + currentEnemy.getName() + "\n"
            + "• Tingkat Kesulitan          : " + settingsService.getCurrentDifficulty().getDescription() + "\n"
            + "• Total Giliran Berlangsung  : " + turns + " giliran\n"
            + "• Total Damage Diberikan     : " + totalDamage + " damage\n"
            + "======================================================================";

        showMessage(endingStory);
        showMessage(battleSummary);
    }

    // =========================================================================
    // HELPER UI
    // =========================================================================

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            new EmptyBorder(12, 16, 12, 16)
        ));
        return btn;
    }

    private TitledBorder createTitledBorder(String title, Color color) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(color, 2),
            title
        );
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        border.setTitleColor(color);
        return border;
    }
}
