# Checkpoint Project & Development Reference (v5)
## Si-Sapi — Tugas Akhir SMK

**Tanggal Update:** 4 September 2026
**Status Backend:** ⚠️ 95% Selesai & Terverifikasi — tersisa 1 modul (`FcrService`) sebelum Sprint 2 benar-benar tuntas
**Fokus Selanjutnya:** Selesaikan `FcrService`, lalu lanjut Sprint 3 — Desktop Client (C# WPF)

---

## 1. Status Checkpoint & Roadmap Pemulihan

| Minggu | Periode | Fokus | Status |
|---|---|---|---|
| 1 | 3–9 Sep | Backend: Tuntaskan seluruh API & Transaksi | 🔄 Hampir selesai — sisa `FcrService` |
| 2 | 10–16 Sep | Desktop (C# WPF): Login, CRUD Master, QR Generator, Grafik | ⏳ Belum Mulai |
| 3 | 17–23 Sep | Mobile (Kotlin): Login, Scanner QR, Input Timbangan/Pakan/Laporan | ⏳ Belum Mulai |
| 4 | 24–30 Sep | Integrasi & Polishing (3–4 hari terakhir murni laporan TA & sidang) | ⏳ Belum Mulai |

> **Catatan:** scope tetap dipertahankan penuh sesuai keputusan — tidak ada fitur yang dipotong. Konsekuensinya, disiplin jadwal di Minggu 2 dan 3 jadi krusial karena tidak ada ruang buffer tambahan.

---

## 2. Environment & Kredensial Pengujian

### Base URL API
- **Local / Desktop (WPF):** `http://127.0.0.1:8000/api`
- **Android Emulator (Kotlin):** `http://10.0.2.2:8000/api`
- **Storage / Gambar:** `http://127.0.0.1:8000/storage/`

### Default Headers
```http
Accept: application/json
Authorization: Bearer <TOKEN_SANCTUM>
```

### Akun Dummy (dari Seeder)
| Role | Email | Password |
|---|---|---|
| Manager | manager@farm.com | password |
| Vet | vet@farm.com | password |
| Worker | worker@farm.com | password |

### Data Dummy dari Seeder
- **Ras Sapi:** Simental, Limousin, Brahman
- **Kandang:** Kandang A, Kandang B
- **Sapi:** SP-001, SP-002 (lengkap dengan histori timbangan awal)
- **Pakan & Batch FIFO awal:** sudah tersedia untuk testing logika FIFO

---

## 3. Tech Stack & Arsitektur
| Layer | Teknologi | Keterangan |
|---|---|---|
| Backend/API | Laravel + MySQL | Sanctum (token auth), pola **Service Layer** |
| Desktop | C# WPF | Konsumsi REST API (bukan akses DB langsung) |
| Mobile | Kotlin (Android Native) | Retrofit/OkHttp |

**Pola Service Layer:** Controller hanya menangani request/response — semua logic bisnis (kalkulasi, FIFO, validasi kompleks) berada di `app/Services/`. Diterapkan konsisten mulai `LivestockController` dan seterusnya.

---

## 4. Daftar Endpoint API — Status Final

| Method | Endpoint | Modul | Status |
|---|---|---|---|
| `POST` | `/api/login` | Auth | ✅ PASSED |
| `GET` | `/api/me` | Auth | ✅ PASSED |
| `POST` | `/api/logout` | Auth | ✅ PASSED |
| `GET` | `/api/cages` | Master Data | ✅ PASSED |
| `POST` | `/api/cages` | Master Data | ✅ PASSED |
| `GET` | `/api/cages/{id}` | Master Data | ✅ PASSED |
| `GET` | `/api/breeds` | Master Data | ✅ PASSED |
| `POST` | `/api/breeds` | Master Data | ✅ PASSED |
| `GET` | `/api/feeds` | Master Data | ✅ PASSED |
| `POST` | `/api/feeds/batches` | Master Data | ✅ PASSED |
| `GET` | `/api/livestocks` | Livestock | ✅ PASSED |
| `POST` | `/api/livestocks` | Livestock | ✅ PASSED |
| `GET` | `/api/livestocks/{tagNumber}` | Livestock | ✅ PASSED |
| `POST` | `/api/cages/{id}/feed` | Feed Consumption (FIFO) | ✅ PASSED |
| `POST` | `/api/livestocks/{tagNumber}/health-report` | Health Record | ✅ PASSED |
| `GET` | `/api/livestocks/{tagNumber}/health-records` | Health Record | ✅ PASSED |
| `PATCH` | `/api/health-records/{id}` | Health Record | ✅ PASSED |
| `GET` | `/api/health-records/due` | Health Record | ✅ PASSED |
| `POST` | `/api/livestocks/{tagNumber}/weight` | Weight Log | ✅ PASSED |
| `GET` | `/api/cages/{id}/fcr` | FCR Report | ⏳ **BELUM DIBUAT** |

---

## 5. Bug & Perbaikan yang Sudah Ditangani (Log Debugging)
Dicatat untuk referensi & bahan lampiran BAB implementasi (bukti proses debugging):

1. **`FeedStockController@index`** — `BadMethodCallException`, nama relasi di controller (`feedStockBatches`) tidak cocok dengan model (`batches`) → disesuaikan.
2. **`FeedConsumptionController@store`** — validasi `cage_id required` gagal karena parameter `{id}` dari URL tidak otomatis masuk ke body request → diperbaiki dengan `$request->merge(['cage_id' => $id])`.
3. **`WeightLogService@recordWeightByTag`** — kolom `worker_id` di database tidak diisi karena key array yang dipakai salah nama (`recorded_by` bukan `worker_id`) → disesuaikan.

---

## 6. Dokumentasi Pendukung yang Sudah Selesai
- [x] ERD lengkap (9 tabel + penjelasan kolom) — format `.docx`
- [x] Sequence Diagram — 8 fitur utama, format `.docx`
- [x] Use Case Diagram — 3 aktor (Manager, Vet, Worker) *(sedang difinalisasi)*
- [ ] Flowchart per modul — belum dibuat, opsional untuk lampiran

---

## 7. Agenda Selanjutnya (Urutan Prioritas)
1. **`FcrService` + endpoint `GET /api/cages/{id}/fcr`** — modul terakhir Sprint 2, wajib selesai sebelum pindah ke Desktop
   - Formula: `FCR = SUM(feed_consumptions.amount_used) / SUM(weight_logs terbaru - initial_weight)` per kandang
2. Setelah `FcrService` lulus test → **Sprint 2 resmi tuntas**, update status jadi 100%
3. Mulai Sprint 3: setup project WPF, service layer HTTP client untuk konsumsi API, login screen

---

## 8. Pengingat Disiplin (dari kesepakatan sebelumnya)
- Jangan mulai fitur Desktop sebelum `FcrService` tuntas dan tervalidasi
- Checkpoint mingguan wajib dicek di akhir tiap minggu — kalau ada tanda keterlambatan, ambil keputusan cepat, jangan ditunda
- Minimal 3-4 hari terakhir (27-30 Sep) dikhususkan untuk laporan TA & latihan sidang, bukan coding
