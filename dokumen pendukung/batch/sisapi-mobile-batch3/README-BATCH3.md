# Si-Sapi Mobile, Batch 3: Input Timbangan

Package: `com.latihan.si_sapimobile`. Syarat: Batch 2 sudah jalan (scan QR berhasil).

## 1. Salin file
Isi `app/src/main/` disalin ke project.
**Timpa (Replace)** 3 file lama: `network/ApiService.kt`, `ui/ScanActivity.kt`, `res/layout/activity_scan.xml`.
**File baru:** `models/WeightLogs.kt`, `services/WeightLogService.kt`, `viewmodels/WeightLogViewModel.kt`, `ui/WeightInputActivity.kt`, `res/layout/activity_weight_input.xml`.

## 2. Daftarkan di AndroidManifest.xml
Di dalam `<application>`:
```xml
<activity android:name=".ui.WeightInputActivity" android:exported="false" />
```

## 3. Yang berubah
- **Mode Timbang:** scan (atau ketik tag) → setelah sapi ditemukan, form timbangan langsung terbuka. Setelah disimpan, kembali ke kamera yang siap memindai sapi berikutnya.
- **Mode Scan QR biasa:** tombol "Catat timbangan" di profil membuka form yang sama. Setelah disimpan, profil dimuat ulang sehingga "Berat terakhir" ikut berubah.
- **Form timbangan:** menampilkan tag dan timbangan terakhir, selisih langsung muncul saat mengetik ("Naik 16,5 kg ..."), dan ada konfirmasi kalau berat berubah lebih dari 25% (mencegah salah ketik, mis. 3855 padahal 385,5).
- **Profil sapi:** vaksin yang tanggalnya sudah lewat ditandai merah "(terlambat)". Status "Healthy" ditampilkan "Sehat".
- **Teks tombol** "Cari" dan "Catat timbangan" dipaksa putih.
- Kalau teks tombol **Masuk** di layar Login juga terlihat gelap, tambahkan `android:textColor="#FFFFFF"` pada `btnLogin` di `activity_login.xml`.

## 4. Uji (HP asli, urut)
1. Beranda → **Timbang** → scan QR `SP-001` → form timbangan terbuka sendiri, tag dan "Timbangan terakhir: 385,5 kg (2026-09-27)" tampil.
2. Ketik `390` → muncul "Naik 4,5 kg dari timbangan terakhir" (hijau). Ketik `380` → "Turun 5,5 kg" (merah).
3. Ketik `3855` → tekan Simpan → dialog "Berat berubah banyak" → pilih **Periksa lagi**. Perbaiki ke `385,5` atau `390`, lalu Simpan.
4. Setelah simpan: Toast "Timbangan SP-001 tersimpan.", kembali ke kamera yang siap scan.
5. **Cek database:** tabel `weight_logs` punya baris baru (berat, tanggal hari ini, `livestock_id` benar, `worker_id` terisi akun yang login).
6. Beranda → **Scan QR** → scan `SP-001` → "Berat terakhir" sekarang berisi angka yang baru kamu simpan. Tekan "Catat timbangan" dari profil → simpan lagi → profil ter-refresh.
7. Kolom kosong → Simpan → "Berat badan wajib diisi." Isi `0` → "Berat harus lebih dari 0 kg." (tanpa menembak server).
8. Matikan `php artisan serve`, Simpan → kotak merah "Tidak bisa terhubung ke server...". Nyalakan lagi, Simpan → berhasil.
9. Putar layar saat form timbangan terbuka di mode Timbang: form tidak terbuka dobel.

## 5. Kalau ada yang tidak sesuai
| Gejala | Penyebab / tindakan |
|---|---|
| Crash saat buka form timbangan | Lupa daftarkan `WeightInputActivity` di manifest |
| Kotak merah dengan pesan Laravel (mis. "The weight field must be ...") | Aturan validasi backend. Kirim pesannya, kita sesuaikan |
| 404 / "tidak ditemukan" padahal sapi ada | Path salah. Pastikan di `ApiService`: `livestocks/{tagNumber}/weight-logs` |
| Berhasil tapi "Berat terakhir" tidak berubah | Cek `weight_logs` di database. Kalau barisnya ada tapi profil lama, lihat JSON `GET livestocks/SP-001` di Logcat (filter `OkHttp`) |
| `Unresolved reference ActivityWeightInputBinding` | Belum Sync / file layout belum disalin. Sync lalu Rebuild |

## 6. Setelah lolos uji
Kirim hasil uji 5 dan 6 (baris di database dan profil yang ter-refresh), dan pesan error kalau ada. Setelah itu Batch 4 (Pakan).
