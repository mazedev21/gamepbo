package rpg.skill;

import rpg.model.Character;

/**
 * Interface Skill merepresentasikan kontrak jurus spesial yang dapat dimiliki
 * oleh setiap Character.
 *
 * <p><b>Penerapan Konsep PBO:</b></p>
 * <ul>
 *   <li><b>Abstraction (Abstraksi):</b> Menetapkan "apa" yang bisa dilakukan oleh
 *       sebuah skill ({@link #getName()} dan {@link #use(Character, Character)}),
 *       tanpa mendikte "bagaimana" rumus perhitungan damage-nya.</li>
 *   <li><b>Polymorphism (Polimorfisme via Interface):</b> Kelas {@link Character}
 *       dan {@link rpg.service.BattleService} memanggil skill melalui tipe
 *       interface {@code Skill} sehingga jurus baru dapat ditambahkan tanpa
 *       mengubah kode kelas pemanggil (memenuhi KNF-04 / Open-Closed Principle).</li>
 * </ul>
 */
public interface Skill {

    /**
     * Mengembalikan nama skill spesial untuk ditampilkan pada UI dan log pertarungan.
     *
     * @return nama skill
     */
    String getName();

    /**
     * Menggunakan skill spesial dari pengguna ({@code user}) terhadap sasaran ({@code target}).
     *
     * @param user   karakter yang melancarkan skill
     * @param target karakter lawan yang menerima efek/damage skill
     * @return jumlah damage yang diberikan kepada target
     */
    int use(Character user, Character target);
}
