# Hero's Regret: Rescue Gone Wrong
**Game RPG Turn-Based dengan Antarmuka GUI Java Swing sebagai Media Penerapan Konsep PBO (Implementasi v3)**

Disusun oleh **Tim Expert Izin Lewat** — Program Studi S1 Teknologi Informasi, Fakultas Teknik, Universitas Tidar (2026):
1. **Emannuel Henji Pratama Putra** (2505060012) — Ketua / Manajer Proyek (`rpg.model`)
2. **Albani Hafiz Innopha** (2505060001) — Analis Kebutuhan (`rpg.ui`)
3. **Florania Ayodya Firdyaziz** (2505060010) — Perancang Kelas (`rpg.skill`)
4. **Angger Naufal Abror** (2505060020) — Pengembang (`rpg.service` & `rpg.test`)
5. **Ziffy Hilya** (2505060008) — Penguji & Dokumentasi (`rpg.App`, integrasi & dokumentasi)

Dosen Pengampu: **Imam Adi Nata, M.Kom.**

---

## Ringkasan Cerita & Permainan
Pemain berperan sebagai pahlawan (**Warrior** atau **Mage**) yang bertarung satu lawan satu secara bergiliran (*turn-based*) melawan monster penjaga kastil demi menyelamatkan seorang Putri. Namun, ketika berhasil menang, cara penyelamatan sang pahlawan yang kacau justru membuat sang Putri kesal karena merusak estetika, lalu memblokir seluruh akun media sosial sang pahlawan!

Selain aktor **Pemain**, terdapat aktor **Pengelola Permainan** yang dapat menyesuaikan tingkat kesulitan (**Mudah**, **Normal**, **Sulit**) atau mengembalikannya ke pengaturan bawaan (**Normal**) tanpa mengubah kode program.

---

## Struktur Direktori Repositori

```
.
├── README.md
├── docs/
│   ├── DOKUMENTASI_PBO.md          # Penjelasan lengkap penerapan konsep PBO & pemetaan KF/KNF
│   ├── class-diagram-v3.puml       # Sumber PlantUML Class Diagram v3
│   └── use-case-diagram.puml       # Sumber PlantUML Use Case Diagram
└── src/
    └── rpg/
        ├── App.java                # Entry point aplikasi & perakitan Dependency Injection
        ├── model/
        │   ├── Character.java      # Abstract class induk semua karakter
        │   ├── Warrior.java        # Subkelas Character (HP: 140, Attack: 20, Power Strike)
        │   ├── Mage.java           # Subkelas Character (HP: 100, Attack: 28, Fireball)
        │   ├── Monster.java        # Subkelas Character (Statistik mengikuti Difficulty)
        │   └── Difficulty.java     # Enum tingkat kesulitan (MUDAH, NORMAL, SULIT)
        ├── skill/
        │   ├── Skill.java          # Interface kontrak jurus spesial
        │   ├── PowerStrike.java    # Implementasi Skill milik Warrior (2 x Attack)
        │   ├── Fireball.java       # Implementasi Skill milik Mage (2 x Attack - 6)
        │   └── MonsterSmash.java   # Implementasi Skill milik Monster (1.5 x Attack + 6)
        ├── service/
        │   ├── GameSettings.java   # State pengaturan tingkat kesulitan
        │   ├── SettingsService.java# Service pengaturan tingkat kesulitan (KF-11, KF-12)
        │   └── BattleService.java  # Service logika pertarungan bergiliran (KF-04 s.d. KF-10)
        ├── ui/
        │   ├── BattleDisplay.java  # Interface kontrak tampilan pertarungan
        │   ├── SwingDisplay.java   # Antarmuka GUI berbasis Java Swing (javax.swing)
        │   ├── AvatarPanel.java    # Grafik vektor 2D karakter (Warrior, Mage, Monster) & efek skill
        │   └── SkillIcon.java      # Ikon vektor 2D jurus spesial (implements javax.swing.Icon)
        └── test/
            ├── BattleServiceTest.java   # Pengujian otomatis BattleService & pilar PBO
            └── SettingsServiceTest.java # Pengujian otomatis SettingsService
```

---

## Cara Kompilasi dan Menjalankan Program (Linux Ubuntu)

Pastikan JDK (Java Development Kit) sudah terpasang di sistem Anda. Tidak diperlukan pustaka eksternal apa pun.

### 1. Kompilasi Seluruh Kode Sumber
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
```

### 2. Menjalankan Aplikasi GUI (Java Swing)
```bash
java -cp out rpg.App
```

### 3. Menjalankan Pengujian Otomatis (Unit Tests)
```bash
java -cp out rpg.test.BattleServiceTest
java -cp out rpg.test.SettingsServiceTest
```

### 4. Menjalankan Simulasi Pertarungan di Konsol (Headless Demo)
```bash
java -cp out rpg.App --headless-demo
```

---

## Dokumentasi Penerapan Konsep PBO
Penjelasan lengkap mengenai penerapan **Class & Object**, **Encapsulation**, **Inheritance**, **Abstraction**, **Polymorphism**, dan **Interface** beserta pemetaan kode baris demi baris dapat dibaca pada berkas:
👉 **[docs/DOKUMENTASI_PBO.md](docs/DOKUMENTASI_PBO.md)**
