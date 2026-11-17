package rpg.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Kelas {@code AvatarPanel} merupakan komponen visual kustom berbasis
 * {@link JPanel} yang menggambar ilustrasi karakter (Warrior, Mage, Monster)
 * serta efek visual serangan/skill menggunakan {@link Graphics2D} prosedural.
 *
 * <p><b>Penerapan Konsep PBO:</b></p>
 * <ul>
 *   <li><b>Inheritance & Method Overriding:</b> Mewarisi {@link JPanel} dan
 *       meng-override {@link #paintComponent(Graphics)} untuk merender grafik
 *       vektor 2D kustom sesuai peran karakter.</li>
 *   <li><b>Encapsulation:</b> Menyimpan state efek aksi aktif ({@link ActionEffect}),
 *       label damage terakhir, serta status hidup/kalah karakter secara {@code private}
 *       dan memperbarui tampilan melalui method publik {@link #triggerEffect(ActionEffect, int)}.</li>
 * </ul>
 */
public class AvatarPanel extends JPanel {

    /**
     * Jenis peran visual karakter yang digambar pada panel avatar.
     */
    public enum AvatarType {
        WARRIOR,
        MAGE,
        MONSTER
    }

    /**
     * Jenis efek grafik skill/serangan yang ditampilkan di atas karakter.
     */
    public enum ActionEffect {
        NONE,
        BASIC_SLASH,
        POWER_STRIKE,
        FIREBALL,
        MONSTER_SMASH
    }

    private AvatarType avatarType;
    private ActionEffect activeEffect = ActionEffect.NONE;
    private int lastDamageTaken = 0;
    private boolean defeated = false;
    private String captionText = "";

    public AvatarPanel(AvatarType avatarType, int width, int height) {
        this.avatarType = (avatarType != null) ? avatarType : AvatarType.WARRIOR;
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setOpaque(false);
    }

    public void setAvatarType(AvatarType avatarType) {
        if (avatarType != null) {
            this.avatarType = avatarType;
            repaint();
        }
    }

    public static AvatarType fromRoleName(String role) {
        if (role == null) {
            return AvatarType.WARRIOR;
        }
        return switch (role.trim().toLowerCase()) {
            case "mage" -> AvatarType.MAGE;
            case "monster" -> AvatarType.MONSTER;
            default -> AvatarType.WARRIOR;
        };
    }

    /**
     * Menampilkan efek grafik serangan/skill dan teks damage pada panel avatar.
     *
     * @param effect jenis efek visual yang ditampilkan
     * @param damage jumlah damage yang diterima (0 jika tidak menampilkan badge damage)
     */
    public void triggerEffect(ActionEffect effect, int damage) {
        this.activeEffect = (effect != null) ? effect : ActionEffect.NONE;
        this.lastDamageTaken = Math.max(0, damage);
        repaint();
    }

    public void clearEffect() {
        this.activeEffect = ActionEffect.NONE;
        this.lastDamageTaken = 0;
        repaint();
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
        repaint();
    }

    public void setCaptionText(String captionText) {
        this.captionText = (captionText != null) ? captionText : "";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Latar panggung (stage) dengan gradasi lembut
        Color topColor = switch (avatarType) {
            case WARRIOR -> new Color(26, 54, 93);
            case MAGE -> new Color(50, 38, 89);
            case MONSTER -> new Color(74, 28, 36);
        };
        Color bottomColor = new Color(20, 24, 34);
        g2.setPaint(new GradientPaint(0, 0, topColor, 0, h, bottomColor));
        g2.fillRoundRect(2, 2, w - 4, h - 4, 16, 16);

        // Garis bingkai panggung
        Color borderColor = switch (avatarType) {
            case WARRIOR -> new Color(66, 153, 225);
            case MAGE -> new Color(159, 122, 234);
            case MONSTER -> new Color(229, 62, 62);
        };
        if (activeEffect != ActionEffect.NONE) {
            borderColor = new Color(246, 224, 94);
        }
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(2, 2, w - 4, h - 4, 16, 16);

        // Bayangan pijakan lantai di bawah karakter
        int cx = w / 2;
        int cy = h / 2 + 4;
        g2.setColor(new Color(10, 12, 18, 160));
        g2.fillOval(cx - 46, cy + 36, 92, 18);

        // 2. Gambar karakter sesuai AvatarType
        switch (avatarType) {
            case WARRIOR -> drawWarriorGraphic(g2, cx, cy);
            case MAGE -> drawMageGraphic(g2, cx, cy);
            case MONSTER -> drawMonsterGraphic(g2, cx, cy);
        }

        // 3. Gambar efek visual serangan/skill jika sedang aktif
        if (activeEffect != ActionEffect.NONE) {
            drawSkillEffectOverlay(g2, cx, cy, w, h);
        }

        // 4. Gambar overlay "K.O." jika karakter kalah (HP == 0)
        if (defeated) {
            drawDefeatedOverlay(g2, w, h);
        }

        // 5. Teks caption kecil di bagian bawah jika ada
        if (!captionText.isBlank()) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(captionText);
            g2.setColor(new Color(203, 213, 224));
            g2.drawString(captionText, (w - tw) / 2, h - 7);
        }

        g2.dispose();
    }

    // =========================================================================
    // GRAFIK VEKTOR KARAKTER: WARRIOR, MAGE, MONSTER
    // =========================================================================

    private void drawWarriorGraphic(Graphics2D g2, int cx, int cy) {
        // Jubah belakang merah ksatria
        Polygon cape = new Polygon(
            new int[]{cx - 22, cx + 22, cx + 34, cx - 34},
            new int[]{cy - 10, cy - 10, cy + 40, cy + 40},
            4
        );
        g2.setColor(new Color(197, 48, 48));
        g2.fillPolygon(cape);

        // Tubuh zirah baja (Chestplate)
        g2.setColor(new Color(160, 174, 192));
        g2.fillRoundRect(cx - 22, cy - 8, 44, 46, 12, 12);
        g2.setColor(new Color(113, 128, 150));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(cx - 22, cy - 8, 44, 46, 12, 12);

        // Sabuk emas
        g2.setColor(new Color(236, 201, 75));
        g2.fillRect(cx - 21, cy + 20, 42, 7);

        // Jambul merah di atas helm
        g2.setColor(new Color(229, 62, 62));
        g2.fillArc(cx - 20, cy - 54, 40, 28, 0, 180);

        // Helm Ksatria Baja
        g2.setColor(new Color(203, 213, 224));
        g2.fillRoundRect(cx - 20, cy - 42, 40, 36, 16, 16);
        g2.setColor(new Color(74, 85, 104));
        g2.drawRoundRect(cx - 20, cy - 42, 40, 36, 16, 16);

        // Visor Helm & Mata Menyala Biru
        g2.setColor(new Color(26, 32, 44));
        g2.fillRoundRect(cx - 14, cy - 28, 28, 10, 4, 4);
        g2.setColor(new Color(99, 179, 237));
        g2.fillOval(cx - 10, cy - 26, 6, 6);
        g2.fillOval(cx + 4, cy - 26, 6, 6);

        // Pedang Besar (Broadsword) di tangan kanan
        g2.setColor(new Color(237, 242, 247));
        Polygon blade = new Polygon(
            new int[]{cx + 30, cx + 38, cx + 44, cx + 38, cx + 30},
            new int[]{cy + 14, cy + 14, cy - 42, cy - 52, cy - 42},
            5
        );
        g2.fillPolygon(blade);
        g2.setColor(new Color(144, 205, 244));
        g2.drawPolygon(blade);

        // Crossguard & gagang pedang
        g2.setColor(new Color(236, 201, 75));
        g2.fillRoundRect(cx + 24, cy + 12, 20, 6, 3, 3);
        g2.setColor(new Color(151, 90, 22));
        g2.fillRect(cx + 31, cy + 18, 6, 11);

        // Perisai Biru-Emas di tangan kiri
        Polygon shield = new Polygon(
            new int[]{cx - 42, cx - 14, cx - 14, cx - 28, cx - 42},
            new int[]{cy - 6, cy - 6, cy + 18, cy + 34, cy + 18},
            5
        );
        g2.setColor(new Color(49, 130, 206));
        g2.fillPolygon(shield);
        g2.setColor(new Color(246, 224, 94));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawPolygon(shield);
        // Lambang salib emas di perisai
        g2.drawLine(cx - 28, cy, cx - 28, cy + 26);
        g2.drawLine(cx - 36, cy + 10, cx - 20, cy + 10);
    }

    private void drawMageGraphic(Graphics2D g2, int cx, int cy) {
        // Aura lingkaran sihir di belakang Mage
        g2.setColor(new Color(159, 122, 234, 55));
        g2.fillOval(cx - 46, cy - 44, 92, 86);
        g2.setColor(new Color(183, 148, 244, 110));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - 46, cy - 44, 92, 86);

        // Jubah Penyihir Ungu
        Polygon robe = new Polygon(
            new int[]{cx - 18, cx + 18, cx + 30, cx - 30},
            new int[]{cy - 8, cy - 8, cy + 40, cy + 40},
            4
        );
        g2.setColor(new Color(107, 70, 193));
        g2.fillPolygon(robe);
        g2.setColor(new Color(236, 201, 75));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(robe);
        // Ornamen vertikal emas di jubah
        g2.drawLine(cx, cy - 6, cx, cy + 39);

        // Wajah / Kepala Mage
        g2.setColor(new Color(254, 215, 170));
        g2.fillOval(cx - 16, cy - 34, 32, 28);
        // Mata fokus bercahaya ungu-biru
        g2.setColor(new Color(85, 60, 154));
        g2.fillOval(cx - 9, cy - 22, 5, 6);
        g2.fillOval(cx + 4, cy - 22, 5, 6);

        // Topi Kerucut Penyihir (Wizard Hat)
        Polygon hatCone = new Polygon(
            new int[]{cx - 24, cx + 24, cx + 6},
            new int[]{cy - 28, cy - 28, cy - 60},
            3
        );
        g2.setColor(new Color(68, 51, 122));
        g2.fillPolygon(hatCone);
        g2.fillOval(cx - 32, cy - 33, 64, 11);
        // Pita emas pada topi
        g2.setColor(new Color(246, 224, 94));
        g2.fillRect(cx - 18, cy - 33, 36, 4);

        // Tongkat Sihir (Arcane Staff) & Kristal Api di sebelah kanan
        g2.setColor(new Color(183, 121, 31));
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx + 34, cy - 28, cx + 28, cy + 40);

        // Bola Kristal Api di ujung tongkat
        g2.setColor(new Color(237, 137, 54, 140));
        g2.fillOval(cx + 22, cy - 48, 24, 24);
        g2.setColor(new Color(254, 240, 138));
        g2.fillOval(cx + 27, cy - 43, 14, 14);

        // Bola api kecil melayang di tangan kiri
        g2.setColor(new Color(245, 101, 101));
        g2.fillOval(cx - 42, cy - 8, 16, 16);
        g2.setColor(new Color(254, 240, 138));
        g2.fillOval(cx - 38, cy - 4, 8, 8);
    }

    private void drawMonsterGraphic(Graphics2D g2, int cx, int cy) {
        // Tubuh kekar Monster (Hijau Gelap / Kekar)
        g2.setColor(new Color(47, 133, 90));
        g2.fillRoundRect(cx - 30, cy - 10, 60, 50, 20, 20);
        g2.setColor(new Color(34, 84, 61));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(cx - 30, cy - 10, 60, 50, 20, 20);

        // Sabuk tengkorak / pelindung dada monster
        g2.setColor(new Color(116, 66, 16));
        g2.fillRect(cx - 28, cy + 16, 56, 9);
        g2.setColor(new Color(237, 242, 247));
        g2.fillOval(cx - 6, cy + 14, 12, 12);

        // Tanduk Kiri & Kanan
        Polygon leftHorn = new Polygon(
            new int[]{cx - 18, cx - 26, cx - 38},
            new int[]{cy - 36, cy - 26, cy - 52},
            3
        );
        Polygon rightHorn = new Polygon(
            new int[]{cx + 18, cx + 26, cx + 38},
            new int[]{cy - 36, cy - 26, cy - 52},
            3
        );
        g2.setColor(new Color(226, 232, 240));
        g2.fillPolygon(leftHorn);
        g2.fillPolygon(rightHorn);

        // Kepala Monster
        g2.setColor(new Color(56, 161, 105));
        g2.fillRoundRect(cx - 24, cy - 42, 48, 36, 16, 16);
        g2.setColor(new Color(34, 84, 61));
        g2.drawRoundRect(cx - 24, cy - 42, 48, 36, 16, 16);

        // Mata Merah Menyala & Alis Marah
        g2.setColor(new Color(229, 62, 62));
        g2.fillOval(cx - 15, cy - 30, 10, 8);
        g2.fillOval(cx + 5, cy - 30, 10, 8);
        g2.setColor(new Color(254, 240, 138));
        g2.fillOval(cx - 12, cy - 28, 4, 4);
        g2.fillOval(cx + 8, cy - 28, 4, 4);

        g2.setColor(new Color(26, 32, 44));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(cx - 17, cy - 33, cx - 5, cy - 29);
        g2.drawLine(cx + 17, cy - 33, cx + 5, cy - 29);

        // Mulut menyeringai & Taring tajam
        g2.fillRect(cx - 14, cy - 16, 28, 6);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[]{cx - 12, cx - 7, cx - 9}, new int[]{cy - 10, cy - 10, cy - 17}, 3);
        g2.fillPolygon(new int[]{cx + 12, cx + 7, cx + 9}, new int[]{cy - 10, cy - 10, cy - 17}, 3);

        // Gada Berduri Raksasa (Spiked Club) di tangan kiri monster
        g2.setColor(new Color(120, 72, 32));
        g2.fillRoundRect(cx - 48, cy - 38, 16, 52, 8, 8);
        g2.setColor(new Color(203, 213, 224));
        // Duri-duri gada
        g2.fillPolygon(new int[]{cx - 48, cx - 55, cx - 48}, new int[]{cy - 30, cy - 26, cy - 22}, 3);
        g2.fillPolygon(new int[]{cx - 48, cx - 55, cx - 48}, new int[]{cy - 16, cy - 12, cy - 8}, 3);
        g2.fillPolygon(new int[]{cx - 32, cx - 25, cx - 32}, new int[]{cy - 24, cy - 20, cy - 16}, 3);
    }

    // =========================================================================
    // EFEK GRAFIK SKILL & SERANGAN DI ATAS AVATAR TARGET
    // =========================================================================

    private void drawSkillEffectOverlay(Graphics2D g2, int cx, int cy, int w, int h) {
        switch (activeEffect) {
            case BASIC_SLASH -> {
                // Kilatan tebasan pedang perak diagonal
                g2.setColor(new Color(226, 232, 240, 210));
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 38, cy - 32, cx + 38, cy + 28);
                g2.setColor(new Color(99, 179, 237, 180));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(cx - 32, cy - 36, cx + 42, cy + 22);
            }
            case POWER_STRIKE -> {
                // Kilatan tebasan silang X emas berkilau (Power Strike)
                g2.setColor(new Color(246, 173, 85, 190));
                g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 42, cy - 36, cx + 42, cy + 32);
                g2.drawLine(cx + 42, cy - 36, cx - 42, cy + 32);

                g2.setColor(new Color(254, 252, 191, 240));
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 42, cy - 36, cx + 42, cy + 32);
                g2.drawLine(cx + 42, cy - 36, cx - 42, cy + 32);

                drawEffectLabel(g2, "POWER STRIKE!", 8, 20, new Color(246, 224, 94));
            }
            case FIREBALL -> {
                // Ledakan bola api berlapis (Fireball)
                g2.setColor(new Color(229, 62, 62, 150));
                g2.fillOval(cx - 44, cy - 38, 88, 76);
                g2.setColor(new Color(237, 137, 54, 190));
                g2.fillOval(cx - 30, cy - 26, 60, 52);
                g2.setColor(new Color(254, 240, 138, 220));
                g2.fillOval(cx - 16, cy - 14, 32, 28);

                drawEffectLabel(g2, "FIREBALL!", 8, 20, new Color(251, 211, 141));
            }
            case MONSTER_SMASH -> {
                // Gelombang kejut hantaman Monster Smash
                g2.setColor(new Color(229, 62, 62, 200));
                g2.setStroke(new BasicStroke(4f));
                g2.drawOval(cx - 42, cy - 34, 84, 68);
                g2.drawLine(cx - 45, cy, cx + 45, cy);
                g2.drawLine(cx, cy - 40, cx, cy + 40);

                drawEffectLabel(g2, "SMASH!", 8, 20, new Color(254, 178, 178));
            }
            case NONE -> {
            }
        }

        // Badge angka damage (-DMG) di pojok kanan atas
        if (lastDamageTaken > 0) {
            String dmgText = "-" + lastDamageTaken + " HP";
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(dmgText) + 12;
            int bh = 20;
            int bx = w - bw - 6;
            int by = 6;

            g2.setColor(new Color(229, 62, 62));
            g2.fillRoundRect(bx, by, bw, bh, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(bx, by, bw, bh, 8, 8);
            g2.drawString(dmgText, bx + 6, by + 14);
        }
    }

    private void drawEffectLabel(Graphics2D g2, String text, int x, int y, Color color) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        g2.setColor(new Color(15, 18, 26, 215));
        g2.fillRoundRect(x, y - 12, tw + 10, 16, 6, 6);
        g2.setColor(color);
        g2.drawString(text, x + 5, y);
    }

    private void drawDefeatedOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(15, 18, 25, 175));
        g2.fillRoundRect(2, 2, w - 4, h - 4, 16, 16);

        String ko = "TUMBANG (0 HP)";
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(ko);
        int bx = (w - tw) / 2 - 8;
        int by = h / 2 - 12;

        g2.setColor(new Color(197, 48, 48));
        g2.fillRoundRect(bx, by, tw + 16, 24, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(bx, by, tw + 16, 24, 8, 8);
        g2.drawString(ko, (w - tw) / 2, h / 2 + 5);
    }
}
