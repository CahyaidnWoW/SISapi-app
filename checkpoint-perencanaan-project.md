# Checkpoint Perencanaan Project
## Smart Livestock Management System — Tugas Akhir SMK

---

## 1. Informasi Umum
| Item | Keterangan |
|---|---|
| Nama Project | Smart Livestock Management System |
| Studi Kasus | Peternakan Sapi Potong |
| Tujuan | Tugas Akhir/Skripsi SMK |
| Model Pengerjaan | Solo |
| Durasi | 8 minggu |
| Arsitektur | Client-Server (1 backend, 2 client independen) |

---

## 2. Tech Stack Final
| Layer | Teknologi | Keterangan |
|---|---|---|
| Backend/API | Laravel + MySQL | Autentikasi via Sanctum, REST API |
| Desktop | C# WPF + MySQL (via API) | Konsumsi REST API, bukan akses DB langsung |
| Mobile | Kotlin (Android Native) | Retrofit/OkHttp untuk konsumsi API |
| Autentikasi | Laravel Sanctum (token-based) | Dipakai bersama oleh Desktop & Mobile |

**Catatan Arsitektur:** Desktop WPF dan Mobile Kotlin sama-sama client independen yang mengonsumsi REST API yang sama dari Laravel — bukan dua sistem terpisah. Ini membuktikan arsitektur client-server yang benar.

---

## 3. Role & Hak Akses
| Role | Platform | Akses |
|---|---|---|
| Manager/Pemilik | Desktop | Full akses: dashboard, stok, analitik, manajemen user |
| Dokter Hewan (Vet) | Desktop & Mobile | Jadwal vaksin, diagnosis, resep tindakan medis |
| Pekerja Kandang | Mobile | Input pakan harian, timbang sapi, lapor gejala sakit |

---

## 4. Struktur Database (Core ERD)
| Tabel | Deskripsi | Kolom Kunci |
|---|---|---|
| `users` | Akun pengguna sistem | id, name, role, email, password |
| `cages` | Data lokasi/blok kandang | id, name, capacity, current_population |
| `livestocks` | Data profil individu sapi | id, tag_number, cage_id, gender, birth_date, status |
| `weight_logs` | Histori penimbangan berkala | id, livestock_id, weight, date, worker_id |
| `health_records` | Catatan rekam medis & vaksin | id, livestock_id, diagnosis, treatment, handled_by |
| `feed_stocks` | Gudang pakan utama | id, feed_name, stock_quantity, unit |
| `feed_consumptions` | Pemakaian pakan harian | id, cage_id, feed_id, amount_used, date |

---

## 5. List Fitur per Platform

### A. Web/Server (Backend Laravel)
- [ ] Autentikasi & manajemen token (login, logout, Sanctum)
- [ ] Role-based access control (Manager, Vet, Pekerja)
- [ ] REST API penuh: sapi, kandang, pakan, rekam medis, timbangan
- [ ] Algoritma Feed Conversion Ratio (FCR)
- [ ] Inventory control stok pakan (metode FIFO)
- [ ] Task scheduler (cron job) — reminder jadwal vaksinasi
- [ ] Endpoint upload foto (laporan darurat)

### B. Desktop (C# WPF — Manager & Vet)
- [ ] Login screen + simpan token/session
- [ ] Dashboard interaktif: grafik populasi, mortality rate, tren berat badan (LiveCharts/OxyPlot)
- [ ] Master data management: CRUD Sapi, Kandang, Pakan, Pegawai
- [ ] QR Code generator: cetak label Ear Tag massal (QRCoder)
- [ ] Laporan medis: riwayat rekam medis per individu sapi
- [ ] Laporan logistik: buku besar stok pakan
- [ ] Manajemen jadwal vaksinasi & tindakan medis (khusus Vet)
- [ ] Manajemen user/pegawai (khusus Manager)

### C. Mobile (Kotlin Android — Pekerja Kandang & Vet)
- [ ] Login screen
- [ ] Smart scanner QR (ML Kit/ZXing) → tampil profil sapi instan
- [ ] Quick input penimbangan
- [ ] Pelaporan darurat: foto + form gejala klinis
- [ ] Pencatatan pakan harian per kandang

---

## 6. Roadmap 8 Minggu

| Minggu | Fokus | Output |
|---|---|---|
| 1 | Backend: Fondasi | Setup Laravel, migration, seeder, auth API |
| 2 | Backend: Core API | CRUD lengkap, FCR, FIFO, upload foto, dokumentasi Postman |
| 3-4 | Desktop (WPF) | Login, CRUD UI, QR generator, dashboard grafik, rekam medis |
| 5-6 | Mobile (Kotlin) | Login, scanner QR, input timbangan, input pakan |
| 7 | Mobile lanjutan + polish | Laporan darurat, polish UI, cron reminder (jika sempat) |
| 8 | Integrasi & Deployment | End-to-end test, bug fixing, deploy backend, build .exe & .apk, bahan sidang |

---

## 7. Status Checkpoint
> Update bagian ini tiap minggu untuk tracking progres.

- [done] Minggu 1 selesai
- [ ] Minggu 2 selesai
- [ ] Minggu 3-4 selesai
- [ ] Minggu 5-6 selesai
- [ ] Minggu 7 selesai
- [ ] Minggu 8 selesai
