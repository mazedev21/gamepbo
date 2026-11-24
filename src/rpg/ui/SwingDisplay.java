package rpg.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import rpg.model.Character;
import rpg.model.Difficulty;
import rpg.model.Mage;
import rpg.model.Monster;
import rpg.model.Warrior;
import rpg.service.BattleService;
import rpg.service.SettingsService;
import rpg.ui.AvatarPanel.ActionEffect;
import rpg.ui.AvatarPanel.AvatarType;
import rpg.ui.SkillIcon.IconType;

/**
 * Kelas {@code SwingDisplay} mengimplementasikan interface {@link BattleDisplay}
 * menggunakan komponen standar Java Swing ({@code javax.swing}) dan grafik
 * vektor {@code java.awt.Graphics2D} (melalui {@link AvatarPanel} dan {@link SkillIcon})
 * tanpa pustaka eksternal.
 */
public class SwingDisplay implements BattleDisplay {

    private static final String CARD_MENU = "MENU";
    private static final String CARD_SETTINGS = "SETTINGS";
    private static final String CARD_CHARACTER_SELECT = "CHARACTER_SELECT";
    private static final String CARD_BATTLE = "BATTLE";
    private static final String SOUTH_CARD_ACTIONS = "ACTIONS";
    private static final String SOUTH_CARD_POST_BATTLE = "POST_BATTLE";

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
    private JPanel warriorRoleCard;
    private JPanel mageRoleCard;

    // Komponen Pertarungan
    private Character currentPlayer;
    private Character currentEnemy;
    private Timer turnDelayTimer;
    private Timer effectClearTimer;

    private AvatarPanel playerAvatarPanel;
    private JLabel playerNameLabel;
    private JLabel playerStatsLabel;
    private JProgressBar playerHpBar;

    private AvatarPanel enemyAvatarPanel;
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
    private JPanel southButtonsContainer;
    private CardLayout southButtonsLayout;

    public SwingDisplay(SettingsService settingsService) {
        if (settingsService == null) {
            throw new IllegalArgumentException("SettingsService tidak boleh null");
        }
        this.settingsService = settingsService;
    }

    public void setBattleService(BattleService battleService) {
        if (battleService == null) {
            throw new IllegalArgumentException("BattleService tidak boleh null");
        }
        this.battleService = battleService;
    }

    public void launchGui() {
        frame = new JFrame("Hero's Regret: Rescue Gone Wrong — RPG Turn-Based PBO (v3)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(940, 740);
        frame.setMinimumSize(new Dimension(860, 680));
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
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(24, 40, 24, 40));

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
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(menuDifficultyBadge);

        // Panel Tengah: Showcase Grafik 3 Karakter + Narasi Pembukaan
        JPanel centerContent = new JPanel(new BorderLayout(16, 16));
        centerContent.setBackground(BG_DARK);

        JPanel previewHeroesRow = new JPanel(new GridLayout(1, 3, 16, 0));
        previewHeroesRow.setBackground(BG_DARK);

        AvatarPanel pWarrior = new AvatarPanel(AvatarType.WARRIOR, 180, 145);
        pWarrior.setCaptionText("WARRIOR — Power Strike");
        AvatarPanel pMage = new AvatarPanel(AvatarType.MAGE, 180, 145);
        pMage.setCaptionText("MAGE — Fireball");
        AvatarPanel pMonster = new AvatarPanel(AvatarType.MONSTER, 180, 145);
        pMonster.setCaptionText("MONSTER — Monster Smash");

        previewHeroesRow.add(pWarrior);
        previewHeroesRow.add(pMage);
        previewHeroesRow.add(pMonster);

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
        openingNarration.setFont(new Font("SansSerif", Font.PLAIN, 14));
        openingNarration.setBackground(BG_PANEL);
        openingNarration.setForeground(TEXT_LIGHT);
        openingNarration.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(74, 85, 104), 1),
            new EmptyBorder(16, 20, 16, 20)
        ));

        centerContent.add(previewHeroesRow, BorderLayout.NORTH);
        centerContent.add(openingNarration, BorderLayout.CENTER);

        // Tombol Menu Utama
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        buttonPanel.setBackground(BG_DARK);

        JButton btnStart = createStyledButton("Mulai Permainan", ACCENT_GREEN, Color.WHITE);
        btnStart.addActionListener(e -> cardLayout.show(mainContainer, CARD_CHARACTER_SELECT));

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
        panel.add(centerContent, BorderLayout.CENTER);
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
    // 3. PANEL PEMILIHAN KARAKTER DENGAN GRAFIK AVATAR & SKILL ICON (KF-02)
    // =========================================================================

    private JPanel buildCharacterSelectPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(22, 36, 22, 36));

        JLabel title = new JLabel("PILIH KARAKTER PAHLAWANMU (KF-02)", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(ACCENT_GOLD);

        JPanel centerPanel = new JPanel(new BorderLayout(14, 14));
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
        applyMaxLengthFilter(heroNameField, Character.MAX_NAME_LENGTH);
        heroNameField.addActionListener(e -> initiateBattleFromSelection());

        JLabel nameHint = new JLabel(
            "(Wajib diisi, maks. " + Character.MAX_NAME_LENGTH + " karakter — divalidasi oleh Character)"
        );
        nameHint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        nameHint.setForeground(TEXT_MUTED);

        nameInputPanel.add(namePrompt);
        nameInputPanel.add(heroNameField);
        nameInputPanel.add(nameHint);

        // Kartu pilihan Warrior vs Mage dilengkapi grafik AvatarPanel + SkillIcon
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsGrid.setBackground(BG_DARK);

        rbWarrior = new JRadioButton("Pilih Kelas WARRIOR", true);
        rbMage = new JRadioButton("Pilih Kelas MAGE", false);
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(rbWarrior);
        roleGroup.add(rbMage);

        warriorRoleCard = buildRoleCard(
            "WARRIOR (Ksatria Pedang)",
            ACCENT_BLUE,
            AvatarType.WARRIOR,
            IconType.POWER_STRIKE,
            "Skill Spesial: Power Strike (2 x Attack = 40 Damage)",
            "• HP Maksimum     : 140 HP (Ketahanan Tinggi)\n"
            + "• Kekuatan Serang : 20 Attack\n"
            + "• Gaya Bertarung  : Tebasan pedang & perisai baja\n"
            + "• Penerapan PBO   : Warrior extends Character + PowerStrike",
            rbWarrior
        );

        mageRoleCard = buildRoleCard(
            "MAGE (Penyihir Elemen)",
            ACCENT_PURPLE,
            AvatarType.MAGE,
            IconType.FIREBALL,
            "Skill Spesial: Fireball (2 x Attack - 6 = 50 Damage)",
            "• HP Maksimum     : 100 HP (Ketahanan Rendah)\n"
            + "• Kekuatan Serang : 28 Attack (Serangan Kuat)\n"
            + "• Gaya Bertarung  : Ledakan bola api & tongkat kristal\n"
            + "• Penerapan PBO   : Mage extends Character + Fireball",
            rbMage
        );

        rbWarrior.addActionListener(e -> updateRoleCardSelectionBorders());
        rbMage.addActionListener(e -> updateRoleCardSelectionBorders());
        updateRoleCardSelectionBorders();

        cardsGrid.add(warriorRoleCard);
        cardsGrid.add(mageRoleCard);

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

    private JPanel buildRoleCard(
        String header,
        Color badgeColor,
        AvatarType avatarType,
        IconType skillIconType,
        String skillBannerText,
        String details,
        JRadioButton radioBtn
    ) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(badgeColor, 2),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel(header, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(badgeColor);

        // Panel tengah berisi Avatar Grafik Vektor + Badge Skill Icon + Deskripsi Statistik
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setBackground(BG_CARD);

        AvatarPanel avatarPreview = new AvatarPanel(avatarType, 180, 145);

        JPanel infoBox = new JPanel(new BorderLayout(6, 6));
        infoBox.setBackground(BG_CARD);

        JLabel skillBadge = new JLabel(skillBannerText, new SkillIcon(skillIconType, 26), SwingConstants.LEFT);
        skillBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        skillBadge.setForeground(ACCENT_GOLD);
        skillBadge.setOpaque(true);
        skillBadge.setBackground(BG_PANEL);
        skillBadge.setBorder(new EmptyBorder(6, 10, 6, 10));

        JTextArea desc = new JTextArea(details);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("Monospaced", Font.PLAIN, 12));
        desc.setBackground(BG_CARD);
        desc.setForeground(TEXT_LIGHT);

        infoBox.add(skillBadge, BorderLayout.NORTH);
        infoBox.add(desc, BorderLayout.CENTER);

        body.add(avatarPreview, BorderLayout.NORTH);
        body.add(infoBox, BorderLayout.CENTER);

        radioBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        radioBtn.setBackground(BG_PANEL);
        radioBtn.setForeground(ACCENT_GOLD);
        radioBtn.setFocusPainted(false);
        radioBtn.setBorder(new EmptyBorder(8, 12, 8, 12));

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(radioBtn, BorderLayout.SOUTH);

        MouseAdapter selectCardListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!radioBtn.isSelected()) {
                    radioBtn.setSelected(true);
                }
                updateRoleCardSelectionBorders();
            }
        };
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        Component[] clickableParts = {
            card, title, body, avatarPreview, infoBox, skillBadge, desc, radioBtn
        };
        for (Component part : clickableParts) {
            part.setCursor(handCursor);
            part.addMouseListener(selectCardListener);
        }

        return card;
    }

    private void updateRoleCardSelectionBorders() {
        applyRoleCardBorder(warriorRoleCard, ACCENT_BLUE, rbWarrior != null && rbWarrior.isSelected());
        applyRoleCardBorder(mageRoleCard, ACCENT_PURPLE, rbMage != null && rbMage.isSelected());
    }

    private void applyRoleCardBorder(JPanel card, Color accentColor, boolean selected) {
        if (card == null) {
            return;
        }
        if (selected) {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 4),
                new EmptyBorder(12, 14, 12, 14)
            ));
        } else {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(74, 85, 104), 2),
                new EmptyBorder(14, 16, 14, 16)
            ));
        }
    }

    private void applyMaxLengthFilter(JTextField field, int maxLength) {
        if (field.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                    if (string == null) {
                        return;
                    }
                    int available = maxLength - fb.getDocument().getLength();
                    if (available <= 0) {
                        return;
                    }
                    String text = string.length() > available ? string.substring(0, available) : string;
                    super.insertString(fb, offset, text, attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                    if (text == null) {
                        super.replace(fb, offset, length, null, attrs);
                        return;
                    }
                    int available = maxLength - (fb.getDocument().getLength() - length);
                    if (available <= 0) {
                        return;
                    }
                    String truncated = text.length() > available ? text.substring(0, available) : text;
                    super.replace(fb, offset, length, truncated, attrs);
                }
            });
        }
    }

    // =========================================================================
    // 4. PANEL ARENA PERTARUNGAN DENGAN GRAFIK AVATAR & EFEK SKILL (KF-03 s.d. KF-13)
    // =========================================================================

    private JPanel buildBattlePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(16, 26, 16, 26));

        // Bagian Atas: Arena Visual Pahlawan vs Monster (AvatarPanel + Status + JProgressBar HP)
        JPanel combatantsPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        combatantsPanel.setBackground(BG_DARK);

        // Kartu Pahlawan Pemain
        JPanel playerCard = new JPanel(new BorderLayout(12, 8));
        playerCard.setBackground(BG_PANEL);
        playerCard.setBorder(createTitledBorder("PAHLAWAN PEMAIN", ACCENT_BLUE));

        playerAvatarPanel = new AvatarPanel(AvatarType.WARRIOR, 155, 138);

        JPanel playerInfoBox = new JPanel(new GridLayout(3, 1, 4, 4));
        playerInfoBox.setBackground(BG_PANEL);

        playerNameLabel = new JLabel("Nama Pahlawan [Role]");
        playerNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        playerNameLabel.setForeground(TEXT_LIGHT);

        playerStatsLabel = new JLabel("Attack: - | Skill: -");
        playerStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        playerStatsLabel.setForeground(TEXT_MUTED);

        playerHpBar = new JProgressBar(0, 100);
        playerHpBar.setStringPainted(true);
        playerHpBar.setFont(new Font("SansSerif", Font.BOLD, 13));
        playerHpBar.setForeground(ACCENT_GREEN);

        playerInfoBox.add(playerNameLabel);
        playerInfoBox.add(playerStatsLabel);
        playerInfoBox.add(playerHpBar);

        playerCard.add(playerAvatarPanel, BorderLayout.WEST);
        playerCard.add(playerInfoBox, BorderLayout.CENTER);

        // Kartu Monster Lawan
        JPanel enemyCard = new JPanel(new BorderLayout(12, 8));
        enemyCard.setBackground(BG_PANEL);
        enemyCard.setBorder(createTitledBorder("MONSTER PENJAGA KASTIL", ACCENT_RED));

        enemyAvatarPanel = new AvatarPanel(AvatarType.MONSTER, 155, 138);

        JPanel enemyInfoBox = new JPanel(new GridLayout(3, 1, 4, 4));
        enemyInfoBox.setBackground(BG_PANEL);

        enemyNameLabel = new JLabel("Nama Monster [Monster]");
        enemyNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        enemyNameLabel.setForeground(TEXT_LIGHT);

        enemyStatsLabel = new JLabel("Attack: - | Skill: -");
        enemyStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        enemyStatsLabel.setForeground(TEXT_MUTED);

        enemyHpBar = new JProgressBar(0, 100);
        enemyHpBar.setStringPainted(true);
        enemyHpBar.setFont(new Font("SansSerif", Font.BOLD, 13));
        enemyHpBar.setForeground(ACCENT_GREEN);

        enemyInfoBox.add(enemyNameLabel);
        enemyInfoBox.add(enemyStatsLabel);
        enemyInfoBox.add(enemyHpBar);

        enemyCard.add(enemyInfoBox, BorderLayout.CENTER);
        enemyCard.add(enemyAvatarPanel, BorderLayout.EAST);

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
        battleLogArea.setBorder(new EmptyBorder(10, 14, 10, 14));

        JScrollPane scrollPane = new JScrollPane(battleLogArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(74, 85, 104), 1));

        logContainer.add(turnStatusBanner, BorderLayout.NORTH);
        logContainer.add(scrollPane, BorderLayout.CENTER);

        // Bagian Bawah: Tombol Aksi Giliran dengan SkillIcon & Tombol Main Ulang / Keluar
        southButtonsLayout = new CardLayout();
        southButtonsContainer = new JPanel(southButtonsLayout);
        southButtonsContainer.setBackground(BG_DARK);

        JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        actionButtonsPanel.setBackground(BG_DARK);

        btnBasicAttack = createStyledButton("1. Serangan Biasa", ACCENT_BLUE, Color.WHITE);
        btnBasicAttack.setIcon(new SkillIcon(IconType.BASIC_SWORD, 26));
        btnBasicAttack.setIconTextGap(10);
        btnBasicAttack.setToolTipText("Serangan standar sebesar nilai Attack dasar karakter.");
        btnBasicAttack.addActionListener(e -> handlePlayerTurnAction(1));

        btnSpecialSkill = createStyledButton("2. Gunakan Skill Spesial", ACCENT_PURPLE, Color.WHITE);
        btnSpecialSkill.setIcon(new SkillIcon(IconType.POWER_STRIKE, 26));
        btnSpecialSkill.setIconTextGap(10);
        btnSpecialSkill.setToolTipText("Jurus spesial unik sesuai kelas karakter.");
        btnSpecialSkill.addActionListener(e -> handlePlayerTurnAction(2));

        actionButtonsPanel.add(btnBasicAttack);
        actionButtonsPanel.add(btnSpecialSkill);

        postBattlePanel = new JPanel(new GridLayout(1, 3, 14, 0));
        postBattlePanel.setBackground(BG_DARK);

        btnPlayAgain = createStyledButton("Main Ulang [KF-13]", ACCENT_GREEN, Color.WHITE);
        btnPlayAgain.addActionListener(e -> cardLayout.show(mainContainer, CARD_CHARACTER_SELECT));

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

        southButtonsContainer.add(actionButtonsPanel, SOUTH_CARD_ACTIONS);
        southButtonsContainer.add(postBattlePanel, SOUTH_CARD_POST_BATTLE);
        southButtonsLayout.show(southButtonsContainer, SOUTH_CARD_ACTIONS);

        panel.add(combatantsPanel, BorderLayout.NORTH);
        panel.add(logContainer, BorderLayout.CENTER);
        panel.add(southButtonsContainer, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // LOGIKA INTERAKSI GUI <-> SERVICE & EFEK GRAFIK
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
            JOptionPane.showMessageDialog(
                frame,
                ex.getMessage() + "\nSilakan masukkan nama pahlawan terlebih dahulu.",
                "Input Tidak Valid (Enkapsulasi Character)",
                JOptionPane.WARNING_MESSAGE
            );
            heroNameField.requestFocusInWindow();
            return;
        }

        stopPendingTimers();

        Difficulty diff = settingsService.getCurrentDifficulty();
        currentEnemy = Monster.createRandomMonster(diff, random);
        battleService.resetBattleStats();

        // Set tipe grafik Avatar sesuai karakter yang dipilih (Warrior / Mage vs Monster)
        playerAvatarPanel.setAvatarType(AvatarPanel.fromRoleName(currentPlayer.getRole()));
        playerAvatarPanel.clearEffect();
        playerAvatarPanel.setDefeated(false);
        playerAvatarPanel.setCaptionText(currentPlayer.getRole().toUpperCase());

        enemyAvatarPanel.setAvatarType(AvatarType.MONSTER);
        enemyAvatarPanel.clearEffect();
        enemyAvatarPanel.setDefeated(false);
        enemyAvatarPanel.setCaptionText("MONSTER (" + diff.name() + ")");

        // Setup label dan bar HP awal
        playerNameLabel.setText(currentPlayer.getName() + " [" + currentPlayer.getRole() + "]");
        playerStatsLabel.setText(
            "Attack: " + currentPlayer.getAttack() + " | Skill: " + currentPlayer.getSkillName()
        );
        enemyNameLabel.setText(currentEnemy.getName());
        enemyStatsLabel.setText(
            "Attack: " + currentEnemy.getAttack() + " | Skill: " + currentEnemy.getSkillName()
        );

        showHpBar(currentPlayer);
        showHpBar(currentEnemy);

        btnBasicAttack.setText("1. Serangan Biasa (" + currentPlayer.getAttack() + " Damage)");
        btnBasicAttack.setIcon(new SkillIcon(IconType.BASIC_SWORD, 26));
        btnBasicAttack.setToolTipText(
            "Serangan standar sebesar Attack (" + currentPlayer.getAttack() + " Damage)"
        );

        btnSpecialSkill.setText("2. Skill Spesial: " + currentPlayer.getSkillName());
        btnSpecialSkill.setIcon(SkillIcon.forSkillName(currentPlayer.getSkillName(), 26));
        btnSpecialSkill.setToolTipText(buildSpecialSkillTooltip(currentPlayer));

        btnBasicAttack.setEnabled(true);
        btnSpecialSkill.setEnabled(true);
        southButtonsLayout.show(southButtonsContainer, SOUTH_CARD_ACTIONS);

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

        stopPendingTimers();
        btnBasicAttack.setEnabled(false);
        btnSpecialSkill.setEnabled(false);
        playerAvatarPanel.clearEffect();

        // 1. Giliran Pemain & Efek Grafik pada Avatar Monster
        int playerDmg = battleService.executePlayerAction(currentPlayer, currentEnemy, choice);
        ActionEffect playerVisualEffect;
        if (choice == 1) {
            playerVisualEffect = ActionEffect.BASIC_SLASH;
        } else if ("Mage".equalsIgnoreCase(currentPlayer.getRole())) {
            playerVisualEffect = ActionEffect.FIREBALL;
        } else {
            playerVisualEffect = ActionEffect.POWER_STRIKE;
        }
        enemyAvatarPanel.triggerEffect(playerVisualEffect, playerDmg);

        if (battleService.isBattleOver(currentPlayer, currentEnemy)) {
            enemyAvatarPanel.setDefeated(true);
            battleService.concludeBattle(currentPlayer, currentEnemy);
            scheduleEffectClear(enemyAvatarPanel, 450);
            return;
        }

        turnStatusBanner.setText(currentEnemy.getName() + " bersiap membalas serangan...");

        // 2. Jeda 450 ms sebelum Monster membalas agar efek serangan pemain terlihat jelas
        turnDelayTimer = new Timer(450, e -> {
            if (currentPlayer == null || currentEnemy == null || battleService.isBattleOver(currentPlayer, currentEnemy)) {
                return;
            }
            enemyAvatarPanel.clearEffect();

            int enemyDmg = battleService.executeEnemyTurn(currentEnemy, currentPlayer);
            ActionEffect enemyVisualEffect = (enemyDmg > currentEnemy.getAttack())
                ? ActionEffect.MONSTER_SMASH
                : ActionEffect.BASIC_SLASH;
            playerAvatarPanel.triggerEffect(enemyVisualEffect, enemyDmg);
            showMessage("----------------------------------------------------------------------");

            if (battleService.isBattleOver(currentPlayer, currentEnemy)) {
                playerAvatarPanel.setDefeated(true);
                battleService.concludeBattle(currentPlayer, currentEnemy);
            } else {
                btnBasicAttack.setEnabled(true);
                btnSpecialSkill.setEnabled(true);
                turnStatusBanner.setText(
                    "Giliran ke-" + (battleService.getTurns() + 1) + ": Pilih aksi untuk " + currentPlayer.getName() + "!"
                );
            }
            scheduleEffectClear(playerAvatarPanel, 450);
        });
        turnDelayTimer.setRepeats(false);
        turnDelayTimer.start();
    }

    private void scheduleEffectClear(AvatarPanel panel, int delayMs) {
        if (effectClearTimer != null && effectClearTimer.isRunning()) {
            effectClearTimer.stop();
        }
        effectClearTimer = new Timer(delayMs, e -> panel.clearEffect());
        effectClearTimer.setRepeats(false);
        effectClearTimer.start();
    }

    private void stopPendingTimers() {
        if (turnDelayTimer != null && turnDelayTimer.isRunning()) {
            turnDelayTimer.stop();
        }
        if (effectClearTimer != null && effectClearTimer.isRunning()) {
            effectClearTimer.stop();
        }
    }

    private String buildSpecialSkillTooltip(Character hero) {
        if ("Mage".equalsIgnoreCase(hero.getRole())) {
            int dmg = Math.max(0, hero.getAttack() * 2 - 6);
            return hero.getSkillName() + ": (2 x Attack) - 6 = " + dmg + " Damage";
        }
        if ("Warrior".equalsIgnoreCase(hero.getRole())) {
            int dmg = hero.getAttack() * 2;
            return hero.getSkillName() + ": 2 x Attack = " + dmg + " Damage";
        }
        return hero.getSkillName() + " (Skill Spesial)";
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
        southButtonsLayout.show(southButtonsContainer, SOUTH_CARD_POST_BATTLE);

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
            new EmptyBorder(10, 14, 10, 14)
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
