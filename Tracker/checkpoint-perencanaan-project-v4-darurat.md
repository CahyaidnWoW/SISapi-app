# Checkpoint Perencanaan Project (v4 — RENCANA DARURAT)
## Si-Sapi — Tugas Akhir SMK

**Update:** 3 September 2026 — Terjadi keterlambatan ±2 minggu karena kesibukan panitia HUT Sekolah, HUT RI, dan kegiatan Pramuka. Deadline sidang **tetap akhir September 2026** → sisa waktu efektif **~4 minggu**. Dokumen ini menggantikan roadmap 8 minggu sebelumnya dengan rencana pemulihan yang lebih ketat.

---

## 1. Informasi Umum
| Item | Keterangan |
|---|---|
| Nama Aplikasi | **Si-Sapi** |
| Judul Laporan TA | *"Si-Sapi: Implementasi Arsitektur Client-Server pada Aplikasi Pemantauan dan Pelacakan Ternak Sapi Potong Berbasis QR Code"* |
| Sisa Waktu | ~4 minggu (3-30 September 2026) |
| Status Ketersediaan Waktu | Belum pasti — masih mungkin ada kesibukan lain |

---

## 2. Prinsip Rencana Darurat
1. **Semua fitur 🟡 dan 🟢 (lihat checkpoint v3) otomatis di-skip**, kecuali ada waktu sisa di minggu 4
2. **Tidak boleh mulai fitur baru sebelum fitur wajib sebelumnya tervalidasi jalan** — disiplin ini penentu utama keberhasilan
3. Kalau ada keterlambatan lagi di tengah jalan → **potong salah satu platform (Desktop/Mobile) jadi versi minimal**, jangan korbankan waktu laporan & latihan sidang
4. Minimal **3-4 hari terakhir wajib dikhususkan untuk laporan TA & latihan sidang**, tidak boleh dipakai untuk coding

---

## 3. Fitur WAJIB Final (Sudah Dipangkas, Tidak Bisa Dikurangi Lagi)

### Backend
- [x] Auth + role-based access
- [x] CRUD API: livestocks, cages, breeds, feed_stocks
- [x] Scan by tag_number
- [x] POST weight log
- [x] POST feed consumption (FIFO)
- [ ] POST health report — **versi simpel, tanpa upload foto**
- [ ] FCR — rumus dasar per kandang, tanpa perbandingan ideal_fcr_range

### Desktop (WPF)
- [ ] Login
- [ ] CRUD Sapi, Kandang, Pakan
- [ ] QR Code generator
- [ ] 1 grafik dashboard (populasi ATAU tren berat — pilih satu paling mudah)

### Mobile (Kotlin)
- [ ] Login
- [ ] Scanner QR → profil sapi
- [ ] Input timbangan
- [ ] Input pakan harian
- [ ] Laporan darurat — **versi teks saja, tanpa foto**

### ❌ Resmi di-drop dari scope (dicatat sebagai saran pengembangan BAB 5)
- Cron job reminder vaksinasi otomatis
- Upload foto di laporan darurat
- Riwayat medis detail per individu (cukup list sederhana)
- Manajemen user lengkap di desktop (hardcode 3 akun dari seeder cukup)
- Perbandingan FCR dengan ideal_fcr_range per breed

---

## 4. Roadmap Pemulihan (4 Minggu)

| Minggu | Periode | Fokus | Target Selesai |
|---|---|---|---|
| 1 | 3-9 Sep | **Backend — tuntaskan semua** | Test FeedConsumption, `HealthRecordController` (simpel), `WeightLogController`, `FcrService` (dasar) |
| 2 | 10-16 Sep | **Desktop — WAJIB saja** | Login, CRUD Sapi/Kandang/Pakan, QR generator, 1 grafik dashboard |
| 3 | 17-23 Sep | **Mobile — WAJIB saja** | Login, Scanner QR, Input timbangan, Input pakan, Laporan darurat (teks) |
| 4 | 24-30 Sep | **Integrasi + Laporan** | End-to-end test, bug fixing, deploy, build .exe & .apk. **3-4 hari terakhir: laporan & latihan sidang, tanpa coding** |

**Checkpoint mingguan wajib dicek di akhir tiap minggu** — kalau di akhir Minggu 1 backend belum 100% tuntas, langsung potong lagi scope Desktop atau Mobile di minggu berikutnya, jangan tunda keputusan sampai minggu 3-4.

---

## 5. Progres Saat Ini (per 3 September 2026)
- Sprint 1 (Backend Foundation): ✅ Selesai
- `LivestockController`: ✅ Selesai & lulus test
- `FeedConsumptionController` (FIFO): ✅ Dibuat, **belum ditest**
- `HealthRecordController`: ⏳ Belum dimulai
- `WeightLogController`: ⏳ Belum dimulai
- `FcrService`: ⏳ Belum dimulai
- Desktop: ⏳ Belum dimulai sama sekali
- Mobile: ⏳ Belum dimulai sama sekali

**Dokumentasi pendukung sudah siap:** ERD, Sequence Diagram (8 fitur, format .docx) — ini bisa langsung dipakai di laporan tanpa perlu dikerjakan ulang.

---

## 6. Agenda Langsung (Minggu Ini)
1. Testing Postman `FeedConsumptionController` — prioritas hari ini/besok
2. `HealthRecordController` versi simpel (tanpa foto)
3. `WeightLogController`
4. `FcrService` versi dasar
5. Kalau Minggu 1 ini selesai tepat waktu → lanjut Desktop di Minggu 2 sesuai jadwal

---

## 7. Status Checkpoint
- [x] Minggu 1 (Backend Foundation) selesai
- [ ] Minggu 1 Pemulihan (3-9 Sep) — Backend tuntas
- [ ] Minggu 2 Pemulihan (10-16 Sep) — Desktop
- [ ] Minggu 3 Pemulihan (17-23 Sep) — Mobile
- [ ] Minggu 4 Pemulihan (24-30 Sep) — Integrasi & Laporan
