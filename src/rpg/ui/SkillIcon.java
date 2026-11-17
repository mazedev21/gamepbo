package rpg.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * Kelas {@code SkillIcon} mengimplementasikan interface standar {@link Icon}
 * menggunakan {@link Graphics2D} prosedural untuk menggambar ikon grafik
 * sederhana pada tombol aksi dan kartu karakter tanpa berkas gambar eksternal.
 *
 * <p><b>Penerapan Konsep PBO:</b></p>
 * <ul>
 *   <li><b>Interface Realization (Polymorphism):</b> Mengimplementasikan kontrak
 *       {@link Icon} ({@code paintIcon}, {@code getIconWidth}, {@code getIconHeight})
 *       sehingga objek {@code SkillIcon} dapat dipasang secara polimorfik pada
 *       komponen {@code JButton} maupun {@code JLabel} mana pun di Java Swing.</li>
 *   <li><b>Encapsulation:</b> Menyembunyikan koordinat gambar vektor dan warna
 *       di balik tipe {@link IconType}.</li>
 * </ul>
 */
public class SkillIcon implements Icon {

    public enum IconType {
        BASIC_SWORD,
        POWER_STRIKE,
        FIREBALL,
        MONSTER_SMASH
    }

    private final IconType type;
    private final int size;

    public SkillIcon(IconType type, int size) {
        this.type = (type != null) ? type : IconType.BASIC_SWORD;
        this.size = Math.max(16, size);
    }

    public static SkillIcon forSkillName(String skillName, int size) {
        if (skillName == null) {
            return new SkillIcon(IconType.BASIC_SWORD, size);
        }
        String lower = skillName.toLowerCase();
        if (lower.contains("power")) {
            return new SkillIcon(IconType.POWER_STRIKE, size);
        } else if (lower.contains("fire")) {
            return new SkillIcon(IconType.FIREBALL, size);
        } else if (lower.contains("smash")) {
            return new SkillIcon(IconType.MONSTER_SMASH, size);
        }
        return new SkillIcon(IconType.BASIC_SWORD, size);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);

        switch (type) {
            case BASIC_SWORD -> drawBasicSword(g2, size);
            case POWER_STRIKE -> drawPowerStrike(g2, size);
            case FIREBALL -> drawFireball(g2, size);
            case MONSTER_SMASH -> drawMonsterSmash(g2, size);
        }

        g2.dispose();
    }

    private void drawBasicSword(Graphics2D g2, int s) {
        // Lingkaran latar belakang
        g2.setColor(new Color(43, 108, 176));
        g2.fillOval(1, 1, s - 2, s - 2);
        g2.setColor(new Color(144, 205, 244));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(1, 1, s - 2, s - 2);

        // Bilah pedang diagonal
        g2.setColor(new Color(237, 242, 247));
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(s / 4 + 2, s * 3 / 4 - 2, s * 3 / 4, s / 4);

        // Pelindung gagang (crossguard) & gagang
        g2.setColor(new Color(246, 224, 94));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(s / 4 - 2, s / 2 + 2, s / 2 - 2, s * 3 / 4 + 2);
        g2.setColor(new Color(183, 121, 31));
        g2.drawLine(s / 5, s * 4 / 5, s / 3, s * 2 / 3);
    }

    private void drawPowerStrike(Graphics2D g2, int s) {
        // Lingkaran latar emas-oranye
        GradientPaint gp = new GradientPaint(0, 0, new Color(221, 107, 32), s, s, new Color(246, 173, 85));
        g2.setPaint(gp);
        g2.fillOval(1, 1, s - 2, s - 2);

        // Efek kilatan tebasan ganda
        g2.setColor(new Color(255, 255, 240));
        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(3, 3, s - 6, s - 6, 35, 160);

        // Pedang petir emas di tengah
        Polygon bolt = new Polygon(
            new int[]{s / 2 + 2, s / 3, s / 2, s / 3 + 2, s * 2 / 3 + 2, s / 2 + 1},
            new int[]{4, s / 2, s / 2, s - 4, s / 2 - 2, s / 2 - 2},
            6
        );
        g2.setColor(new Color(254, 252, 191));
        g2.fillPolygon(bolt);
    }

    private void drawFireball(Graphics2D g2, int s) {
        // Lingkaran latar ungu-merah
        g2.setColor(new Color(116, 42, 42));
        g2.fillOval(1, 1, s - 2, s - 2);

        // Lidah api luar (merah-oranye)
        Polygon outerFlame = new Polygon(
            new int[]{s / 2, s * 4 / 5, s * 3 / 4, s / 2, s / 4, s / 5},
            new int[]{3, s / 2 - 1, s - 4, s - 2, s - 4, s / 2 - 1},
            6
        );
        g2.setColor(new Color(237, 137, 54));
        g2.fillPolygon(outerFlame);
        g2.fillOval(s / 4, s / 3, s / 2, s / 2 + 2);

        // Inti bola api kuning terang
        g2.setColor(new Color(254, 240, 138));
        g2.fillOval(s / 3, s / 2 - 1, s / 3 + 1, s / 3 + 1);
    }

    private void drawMonsterSmash(Graphics2D g2, int s) {
        g2.setColor(new Color(155, 44, 44));
        g2.fillOval(1, 1, s - 2, s - 2);

        // Gada berduri / gelombang kejut
        g2.setColor(new Color(254, 178, 178));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(4, s / 2, s - 4, s / 2);
        g2.drawLine(s / 2, 4, s / 2, s - 4);
        g2.drawLine(6, 6, s - 6, s - 6);
        g2.drawLine(s - 6, 6, 6, s - 6);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
