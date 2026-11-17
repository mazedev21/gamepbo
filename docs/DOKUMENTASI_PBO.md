# Dokumentasi Teknis & Penjelasan Penerapan Konsep PBO (Implementasi v3)
## Proyek: "Hero's Regret: Rescue Gone Wrong" — Tim Expert Izin Lewat

Dokumen ini membedah secara lengkap **di mana** dan **bagaimana** konsep Pemrograman Berorientasi Objek (PBO/OOP) diterapkan pada kode sumber game RPG *turn-based* berbasis Java Swing ini, sesuai dengan spesifikasi Proposal Proyek Bab 4 s.d. Bab 11 (Implementasi v3).

---

## 1. Struktur Arsitektur Berlapis (Layered Architecture)

Untuk menghindari *anti-pattern* "satu kelas `Main` berisi seluruh logika (*God Class*)" yang diidentifikasi pada **Bab 1 & Bab 2 (Analisis 5 Whys)** proposal, proyek ini dirancang dengan arsitektur berlapis:

```
src/rpg/
├── model/                 # Lapisan Domain / Entitas Data & Aturan Bisnis Dasar
│   ├── Character.java     # «abstract» class induk seluruh petarung
│   ├── Warrior.java       # Subkelas Character (HP tinggi, skill PowerStrike)
│   ├── Mage.java          # Subkelas Character (Attack tinggi, skill Fireball)
│   ├── Monster.java       # Subkelas Character (Statistik mengikuti Difficulty)
│   └── Difficulty.java    # «enum» tingkat kesulitan (MUDAH, NORMAL, SULIT)
├── skill/                 # Lapisan Kontrak & Implementasi Variasi Jurus
│   ├── Skill.java         # «interface» kontrak jurus spesial
│   ├── PowerStrike.java   # Implementasi Skill milik Warrior
│   ├── Fireball.java      # Implementasi Skill milik Mage
│   └── MonsterSmash.java  # Implementasi Skill milik Monster
├── ui/                    # Lapisan Kontrak & Implementasi Antarmuka Pengguna (GUI)
│   ├── BattleDisplay.java # «interface» kontrak tampilan pertarungan
│   ├── SwingDisplay.java  # Implementasi GUI menggunakan Java Swing (javax.swing)
│   ├── AvatarPanel.java   # Subkelas JPanel penggambaran grafik vektor karakter & efek skill (Graphics2D)
│   └── SkillIcon.java     # Implementasi javax.swing.Icon untuk grafik vektor ikon skill pada tombol & kartu
├── service/               # Lapisan Logika Aplikasi / Use Case (Service Layer)
│   ├── GameSettings.java  # Penyimpan state tingkat kesulitan
│   ├── SettingsService.java # Layanan pengaturan kesulitan (Aktor: Pengelola Permainan)
│   └── BattleService.java # Layanan pengatur alur giliran & pertarungan (Aktor: Pemain)
├── test/                  # Lapisan Pengujian Otomatis Tanpa Pustaka Eksternal
│   ├── BattleServiceTest.java   # Kode uji fitur pertarungan & pilar PBO
│   └── SettingsServiceTest.java # Kode uji fitur pengaturan kesulitan
└── App.java               # Titik masuk (Entry Point) & Perakitan Objek (Dependency Injection)
```

---

## 2. Penjelasan Mendalam Penerapan 4 Pilar PBO di Dalam Kode

### 2.1 Pilar 1: Class, Object, dan Encapsulation (Enkapsulasi & Validasi Aturan Bisnis)

**Pengertian & Tujuan:**
Enkapsulasi membungkus data (atribut) dan perilaku (method) ke dalam satu unit kelas, menyembunyikan detail keadaan internal (`private`), serta memastikan bahwa setiap perubahan state harus melewati validasi aturan bisnis (*business rules* / *class invariants*).

**Lokasi Penerapan di Kode:**

1. **Kelas [`Character.java`](../src/rpg/model/Character.java) (Baris 30–72)**
   - **Visibilitas Atribut:** Seluruh atribut dideklarasikan `private`:
     ```java
     private final String name;
     private final int maxHp;
     private final int attack;
     private final Skill skill;
     private int hp;
     ```
     Atribut `name`, `maxHp`, `attack`, dan `skill` bahkan dikunci dengan `final` sehingga tidak dapat dimodifikasi setelah objek dibuat (*immutability* parsial). Hanya `hp` yang dapat berubah selama pertarungan, dan **tidak ada method `setHp(int)` publik** agar kelas luar (bahkan subkelas sekalipun) tidak dapat mengubah HP secara sembarangan.
   - **Tiga Aturan Bisnis yang Dijaga (Sesuai Sub-bab 11.3.1):**
     - **Aturan Bisnis 1 (Validasi Identitas):** Konstruktor menolak nama karakter yang `null` atau kosong/spasi saja (`name.isBlank()`):
       ```java
       if (name == null || name.isBlank()) {
           throw new IllegalArgumentException("Nama karakter wajib diisi");
       }
       ```
     - **Aturan Bisnis 2 (Validasi Statistik & Komponen):** Konstruktor menolak `maxHp <= 0`, `attack <= 0`, atau `skill == null`:
       ```java
       if (maxHp <= 0 || attack <= 0) {
           throw new IllegalArgumentException("HP maksimum dan attack harus > 0");
       }
       if (skill == null) {
           throw new IllegalArgumentException("Skill tidak boleh null");
       }
       ```
     - **Aturan Bisnis 3 (Invarian HP Tidak Pernah Negatif — `KF-06`):** Method `takeDamage(int dmg)` menolak nilai damage negatif dan membatasi nilai minimum `hp` pada angka `0`:
       ```java
       public void takeDamage(int dmg) {
           if (dmg < 0) {
               throw new IllegalArgumentException("Damage tidak boleh negatif");
           }
           this.hp = Math.max(0, this.hp - dmg);
       }
       ```

2. **Kelas [`GameSettings.java`](../src/rpg/service/GameSettings.java) (Baris 14–31)**
   - Atribut `private Difficulty difficulty` diinisialisasi ke `Difficulty.NORMAL`. Method `setDifficulty(Difficulty)` menolak input `null`, dan `resetToDefault()` mengembalikan state ke `Difficulty.NORMAL` (`KF-12`).

3. **Kelas [`BattleService.java`](../src/rpg/service/BattleService.java) (Baris 27–31)**
   - Atribut penghitung `private int turns` dan `private int totalPlayerDamage` disembunyikan dari luar agar ringkasan pertarungan (`KF-10`) tidak dapat dimanipulasi dari luar alur giliran resmi.

---

### 2.2 Pilar 2: Inheritance (Pewarisan)

**Pengertian & Tujuan:**
Pewarisan memungkinkan pembuatan kelas baru (subkelas) dari kelas yang sudah ada (superkelas) apabila memenuhi hubungan **"adalah" (*is-a relationship*)**, sehingga kode umum cukup ditulis satu kali di kelas induk.

**Lokasi Penerapan di Kode:**

1. **Hierarki `Character` → [`Warrior.java`](../src/rpg/model/Warrior.java), [`Mage.java`](../src/rpg/model/Mage.java), dan [`Monster.java`](../src/rpg/model/Monster.java)**
   - **Relasi *Is-A*:**
     - `Warrior` **adalah** `Character` (`public class Warrior extends Character`)
     - `Mage` **adalah** `Character` (`public class Mage extends Character`)
     - `Monster` **adalah** `Character` (`public class Monster extends Character`)
   - **Pemanfaatan Konstruktor Induk (`super(...)`):**
     ```java
     // Di Warrior.java
     public Warrior(String name) {
         super(name, 140, 20, new PowerStrike());
     }

     // Di Mage.java
     public Mage(String name) {
         super(name, 100, 28, new Fireball());
     }

     // Di Monster.java
     public Monster(String name, Difficulty diff) {
         super(
             name,
             (int) (110 * requireDifficulty(diff).getMultiplier()),
             (int) (16 * requireDifficulty(diff).getMultiplier()),
             new MonsterSmash()
         );
     }
     ```
   - **Keuntungan Nyata:** `Warrior`, `Mage`, dan `Monster` otomatis mewarisi seluruh mekanisme `takeDamage()`, `basicAttack()`, `specialAttack()`, `isAlive()`, `getName()`, `getHp()`, `getMaxHp()`, `getAttack()`, dan `getSkillName()` dari `Character` tanpa menulis ulang satu baris pun logika tersebut.

---

### 2.3 Pilar 3: Abstraction (Abstraksi)

**Pengertian & Tujuan:**
Abstraksi memfokuskan rancangan pada **"apa"** yang dapat dilakukan oleh suatu objek dan menyembunyikan detail **"bagaimana"** cara kerjanya, menggunakan `abstract class` maupun `interface`.

**Lokasi Penerapan di Kode:**

1. **Abstract Class [`Character.java`](../src/rpg/model/Character.java)**
   - Di dunia permainan ini, tidak ada karakter generik tanpa peran; pemain harus memilih `Warrior` atau `Mage`, dan lawan adalah `Monster`. Oleh karena itu, `Character` dideklarasikan `public abstract class Character` sehingga tidak bisa diinstansiasi secara langsung (`new Character(...)` dilarang kecuali dibuat subkelasnya).
   - Mendeklarasikan method abstrak `public abstract String getRole();` yang memaksa setiap subkelas mendefinisikan identitas perannya masing-masing dengan anotasi `@Override`.

2. **Interface Pertama: [`Skill.java`](../src/rpg/skill/Skill.java)**
   - Menetapkan kontrak bahwa setiap jurus spesial wajib memiliki nama (`String getName()`) dan dapat digunakan dari pengguna ke target (`int use(Character user, Character target)`).
   - `Character` tidak perlu tahu bagaimana rumus matematika masing-masing jurus dihitung; `Character` cukup memanggil `skill.use(this, target)`.

3. **Interface Kedua: [`BattleDisplay.java`](../src/rpg/ui/BattleDisplay.java)**
   - Menetapkan kontrak komunikasi antara logika pertarungan (`BattleService`) dan layar tampilan:
     - `void showMessage(String message)`
     - `void showHpBar(Character character)`
     - `void showSummary(int turns, int totalDamage, boolean playerWon)`
   - `BattleService` sama sekali tidak mengimpor `javax.swing.*` karena hanya berkomunikasi melalui abstraksi `BattleDisplay`.

---

### 2.4 Pilar 4: Polymorphism (Polimorfisme) & Penggunaan Interface

**Pengertian & Tujuan:**
Polimorfisme memungkinkan satu pemanggilan method yang sama melalui referensi bertipe induk/interface menghasilkan perilaku yang berbeda-beda saat runtime (*dynamic method dispatch*) bergantung pada objek konkret yang sebenarnya terpasang.

**Lokasi Penerapan di Kode:**

1. **Polimorfisme Jurus Spesial melalui Interface `Skill` ([`PowerStrike.java`](../src/rpg/skill/PowerStrike.java), [`Fireball.java`](../src/rpg/skill/Fireball.java), [`MonsterSmash.java`](../src/rpg/skill/MonsterSmash.java))**
   - Di dalam [`Character.java`](../src/rpg/model/Character.java) baris 92:
     ```java
     public int specialAttack(Character target) {
         return skill.use(this, target); // Pemanggilan polimorfik!
     }
     ```
   - Ketika `player.specialAttack(enemy)` dipanggil di `BattleService`:
     - Jika `player` adalah objek `Warrior`, `skill` merujuk ke `PowerStrike` → menghitung `user.getAttack() * 2` (`40` damage).
     - Jika `player` adalah objek `Mage`, `skill` merujuk ke `Fireball` → menghitung `(user.getAttack() * 2) - 6` (`50` damage).
     - Jika yang memanggil adalah `Monster`, `skill` merujuk ke `MonsterSmash` → menghitung `round(user.getAttack() * 1.5) + 6`.
   - **Pembuktian `KNF-04` (Maintainability / Open-Closed Principle):** Tidak ada blok `if (character instanceof Warrior) ... else if (character instanceof Mage)` di dalam `BattleService` maupun `Character`. Saat kita menambahkan skill baru atau subkelas karakter baru (seperti `ThunderBolt` dan `Paladin` pada `BattleServiceTest`), `BattleService` sama sekali tidak perlu diubah!

2. **Polimorfisme Karakter pada [`BattleService.java`](../src/rpg/service/BattleService.java)**
   - Method `executePlayerAction(Character player, Character enemy, int choice)` dan `executeEnemyTurn(Character enemy, Character player)` menerima parameter bertipe induk `Character`. Baik `Warrior`, `Mage`, maupun `Monster` diperlakukan secara seragam melalui tipe `Character`.

3. **Polimorfisme Tampilan melalui Interface `BattleDisplay` ([`SwingDisplay.java`](../src/rpg/ui/SwingDisplay.java) vs Anonymous Stub di [`BattleServiceTest.java`](../src/rpg/test/BattleServiceTest.java) vs Console Display di [`App.java`](../src/rpg/App.java))**
   - `BattleService` menyimpan atribut `private final BattleDisplay display;`.
   - Saat aplikasi utama dijalankan, `display` berisi objek `SwingDisplay` yang memperbarui `JProgressBar` dan `JTextArea` pada jendela GUI Java Swing.
   - Saat pengujian otomatis (`BattleServiceTest`) dijalankan, `display` berisi objek `silentDisplay` (implementasi ringan `BattleDisplay`) sehingga pengujian berjalan dalam hitungan milidetik tanpa harus membuka jendela GUI.

---

### 2.5 Penerapan PBO pada Grafik Vektor Karakter & Skill (`AvatarPanel` & `SkillIcon`)

Untuk menampilkan visual karakter (**Warrior**, **Mage**, **Monster**) serta efek grafis jurus (**Serangan Biasa**, **Power Strike**, **Fireball**, **Monster Smash**) tanpa bergantung pada berkas gambar eksternal (`.png`/`.jpg`) dan tanpa mengotori kelas domain `rpg.model` dengan pustaka GUI, proyek ini menerapkan **Procedural Vector Graphics (`java.awt.Graphics2D`)** di dalam paket `rpg.ui`:

1. **Inheritance & Method Overriding pada [`AvatarPanel.java`](../src/rpg/ui/AvatarPanel.java)**
   - `AvatarPanel` mewarisi kelas komponen standar Swing (`public class AvatarPanel extends JPanel`) dan meng-override method siklus hidup penggambaran Swing:
     ```java
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g.create();
         // Menggambar panggung, avatar karakter (Warrior/Mage/Monster),
         // efek visual jurus (Slash / Power Strike / Fireball / Smash),
         // serta badge damage (-DMG HP) dan status TUMBANG (0 HP).
     }
     ```
   - **Apa yang digambar secara prosedural?**
     - **`WARRIOR` (`drawWarriorGraphic`)**: Jubah merah ksatria, zirah baja, helm baja berjambul merah, pedang besar (*broadsword*), dan perisai biru berlambang salib emas.
     - **`MAGE` (`drawMageGraphic`)**: Aura lingkaran sihir ungu, jubah penyihir berornamen emas, topi kerucut (*wizard hat*), tongkat sihir kristal api, dan bola api melayang.
     - **`MONSTER` (`drawMonsterGraphic`)**: Tubuh hijau kekar bersabuk tengkorak, tanduk runcing, mata merah menyala, taring tajam, dan gada berduri raksasa (*spiked club*).
     - **Efek Serangan & Skill (`drawSkillEffectOverlay`)**: Menampilkan tebasan perak diagonal (`BASIC_SLASH`), tebasan silang X emas berkilau (`POWER_STRIKE`), ledakan bola api berlapis (`FIREBALL`), gelombang kejut merah (`MONSTER_SMASH`), serta badge `-DMG HP` di atas karakter target.

2. **Interface Realization (Polymorphism) pada [`SkillIcon.java`](../src/rpg/ui/SkillIcon.java)**
   - `SkillIcon` mengimplementasikan interface bawaan Java Swing `javax.swing.Icon` (`public class SkillIcon implements Icon`):
     ```java
     @Override
     public void paintIcon(Component c, Graphics g, int x, int y) { ... }
     @Override
     public int getIconWidth() { return size; }
     @Override
     public int getIconHeight() { return size; }
     ```
   - Karena komponen Swing seperti `JButton` dan `JLabel` menerima parameter bertipe interface `Icon` (`button.setIcon(Icon)`), objek `SkillIcon` dapat langsung dipasang secara polimorfik pada tombol **1. Serangan Biasa**, tombol **2. Skill Spesial**, maupun badge informasi di kartu pemilihan karakter.

3. **Separation of Concerns (SRP) tetap Terjaga**
   - Kelas model (`Character`, `Warrior`, `Mage`, `Monster`) dan kelas skill (`Skill`, `PowerStrike`, `Fireball`, `MonsterSmash`) **sama sekali tidak mengimpor `java.awt.*` atau `javax.swing.*`**.
   - Pemetaan dari data model ke representasi grafik dilakukan di lapisan UI (`AvatarPanel.fromRoleName(character.getRole())` dan `SkillIcon.forSkillName(character.getSkillName(), 26)`). Dengan demikian, pemisahan antara logika bisnis dan tampilan GUI tetap 100% murni sesuai arsitektur berlapis Bab 11 proposal.

---

## 3. Pemetaan Kebutuhan Fungsional (`KF-01` s.d. `KF-13`) ke Kode Sumber

| Kode | Deskripsi Kebutuhan Fungsional | Kelas & Method Pelaksana | Status |
| :--- | :--- | :--- | :---: |
| **KF-01** | Menampilkan judul game, narasi pembukaan, dan menu utama (Mulai, Pengaturan, Keluar). | `SwingDisplay.buildMainMenuPanel()` | Terpenuhi |
| **KF-02** | Memungkinkan pemain memilih karakter Warrior atau Mage beserta menampilkan statistiknya (HP, serangan, nama skill). | `SwingDisplay.buildCharacterSelectPanel()`, `Warrior`, `Mage` | Terpenuhi |
| **KF-03** | Membuat monster dengan statistik acak dalam rentang yang disesuaikan dengan tingkat kesulitan. | `Monster.createRandomMonster(Difficulty, Random)` & `Monster(String, Difficulty)` | Terpenuhi |
| **KF-04** | Mengatur giliran pemain dan monster secara bergantian sampai HP salah satu karakter habis. | `SwingDisplay.handlePlayerTurnAction(int)`, `BattleService.isBattleOver(...)` | Terpenuhi |
| **KF-05** | Memungkinkan pemain memilih aksi pada gilirannya: (1) serangan biasa atau (2) skill spesial. | `BattleService.executePlayerAction(Character, Character, int)` | Terpenuhi |
| **KF-06** | Menghitung damage, mengurangi HP target, dan memastikan HP tidak bernilai kurang dari nol. | `Character.takeDamage(int)` (`Math.max(0, this.hp - dmg)`) | Terpenuhi |
| **KF-07** | Menampilkan bar HP kedua karakter serta pesan aksi pada setiap giliran. | `SwingDisplay.showHpBar(Character)` (`JProgressBar`) & `SwingDisplay.showMessage(String)` | Terpenuhi |
| **KF-08** | Menjalankan aksi monster secara otomatis, dengan peluang 20% memakai skill spesial. | `BattleService.executeEnemyTurn(Character, Character)` (`MONSTER_SKILL_CHANCE = 0.20`) | Terpenuhi |
| **KF-09** | Menentukan pemenang lalu menampilkan narasi akhir yang sesuai (menang atau kalah). | `BattleService.concludeBattle(...)` & `SwingDisplay.showSummary(...)` | Terpenuhi |
| **KF-10** | Menampilkan ringkasan pertarungan berisi jumlah giliran dan total damage yang diberikan pemain. | `BattleService.getTurns()`, `BattleService.getTotalPlayerDamage()`, `SwingDisplay.showSummary(...)` | Terpenuhi |
| **KF-11** | Memungkinkan pengelola mengatur tingkat kesulitan Mudah, Normal, atau Sulit. | `SettingsService.changeDifficulty(Difficulty)` & `SwingDisplay.buildSettingsPanel()` | Terpenuhi |
| **KF-12** | Memungkinkan pengelola mengembalikan pengaturan ke nilai bawaan (Normal). | `SettingsService.resetDifficulty()`, `GameSettings.resetToDefault()` | Terpenuhi |
| **KF-13** | Menawarkan pilihan main ulang atau keluar setelah pertarungan selesai. | `SwingDisplay.buildBattlePanel()` (`btnPlayAgain`, `btnBackToMenu`, `btnExitAfterBattle`) | Terpenuhi |

---

## 4. Pemetaan Kebutuhan Nonfungsional (`KNF-01` s.d. `KNF-05`)

| Kode | Kategori | Bukti Pemenuhan di Kode |
| :--- | :--- | :--- |
| **KNF-01** | **Kinerja** (Respons ≤ 1 detik) | Seluruh kalkulasi pertarungan berjalan di memori $O(1)$ dan pembaruan GUI dieksekusi langsung pada *Event Dispatch Thread* Swing (< 5 milidetik per aksi). |
| **KNF-02** | **Kemudahan Penggunaan** | Navigasi antar-layar menggunakan `CardLayout` satu jendela utuh dengan tombol berlabel jelas, deskripsi statistik lengkap, bar HP berwarna dinamis, dan narasi komedi yang langsung tampil. |
| **KNF-03** | **Keandalan** | Tombol aksi otomatis dinonaktifkan (`setEnabled(false)`) saat giliran monster atau saat pertarungan usai; nama karakter kosong ditangani dengan `JOptionPane` peringatan tanpa menghentikan aplikasi. |
| **KNF-04** | **Pemeliharaan** | Dibuktikan langsung pada `BattleServiceTest` (Skenario 4): penambahan skill baru `Thunder Bolt` dan karakter `Paladin` berjalan mulus tanpa mengubah satu baris pun di `BattleService`. |
| **KNF-05** | **Portabilitas** | Menggunakan 100% pustaka standar Java SE (`javax.swing`, `java.awt`, `java.util`) dengan `CrossPlatformLookAndFeel` sehingga tampil konsisten di Linux Ubuntu maupun OS lain. |
