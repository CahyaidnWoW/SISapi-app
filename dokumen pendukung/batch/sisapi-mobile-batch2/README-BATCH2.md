# Si-Sapi Mobile, Batch 2: Menu Alat + Scan QR + Profil sapi

Package: `com.latihan.si_sapimobile`. Syarat: Batch 1 sudah jalan (login berhasil).

## 1. Salin file
Isi `app/src/main/` disalin ke project. **Timpa** 3 file lama (pilih Replace):
`network/ApiService.kt`, `ui/MainActivity.kt`, `res/layout/activity_main.xml`.
File baru: `models/Livestock.kt`, `services/LivestockService.kt`, `viewmodels/ScanViewModel.kt`, `ui/ScanActivity.kt`, `res/layout/activity_scan.xml`, `res/layout/item_tool.xml`, `res/drawable/bg_card.xml`.

## 2. Daftarkan ScanActivity di AndroidManifest.xml
Di dalam `<application>`, di bawah MainActivity:
```xml
<activity android:name=".ui.ScanActivity" android:exported="false" />
```
Izin `INTERNET` dan `CAMERA` sudah ada dari Batch 1. Kalau belum, tambahkan:
```xml
<uses-permission android:name="android.permission.CAMERA" />
```
Pastikan dependency `com.journeyapps:zxing-android-embedded:4.3.0` ada di `build.gradle.kts` (sudah di Batch 1). Sync, lalu Run.

## 3. Yang berubah
- **Layar utama** menampilkan tile Alat sesuai role: Pekerja (Scan QR, Timbang, Pakan, Lapor sakit), Dokter hewan (Scan QR, Vaksin, Tindakan, Rekam medis), role lain (hanya Scan QR + catatan).
- Tile yang fiturnya belum dikerjakan memunculkan Toast "dikerjakan di Batch N". Ini disengaja.
- **ScanActivity** memindai QR terus-menerus, ada input ketik nomor tag, tombol Scan ulang, dan kartu profil sapi.
- Satu baris log sementara di MainActivity: `Log.d("ROLE", ...)`. Dipakai untuk memastikan nilai role asli dari backend (hapus setelah dicek).

## 4. Uji (di HP asli, urut)
1. **Role:** login, lalu buka Logcat, filter `ROLE`. Catat isinya (`worker`? `pekerja`? `vet`?). Kalau layar utama hanya menampilkan "Scan QR" padahal akunmu pekerja, berarti nilai role-nya belum dikenali. Kirim nilainya, nanti disesuaikan.
2. **Tile:** akun pekerja → 4 tile. Tile Pakan menampilkan Toast Batch 4.
3. **Scan QR:** tekan Scan QR → izin kamera muncul → izinkan → arahkan ke QR ear tag dari aplikasi desktop. Profil sapi muncul (tag, ras, kandang, status, berat terakhir, vaksin berikutnya).
4. **Ketik manual:** ketik `SP-001` (atau tag yang ada di databasemu) → Cari → profil sama.
5. **Tag tidak ada:** ketik `SP-9999` → kotak merah "Sapi dengan tag SP-9999 tidak ditemukan." + tombol Scan ulang.
6. **Izin ditolak:** cabut izin kamera di Pengaturan app, buka Scan → muncul Toast, ketik manual masih bisa.
7. **Scan ulang:** setelah profil muncul, tekan Scan ulang → kamera aktif lagi, kartu hilang.
8. **Putar layar** saat profil tampil: kartu tetap ada, scanner tidak jalan sendiri.

## 5. Kalau ada yang tidak sesuai
| Gejala | Penyebab / tindakan |
|---|---|
| Kamera hitam | Izin kamera belum diberikan. Buka Pengaturan app → Izin → Kamera |
| QR terbaca tapi "tidak ditemukan" | Isi QR bukan persis `tag_number`. Lihat isi QR: ketik hasil scan di kolom tag (atau lihat di Logcat `OkHttp` path `livestocks/...`) |
| Kandang atau ras tampil `-` | Nama field `Cage.name` / `Breed.breed_name` beda. Lihat JSON di Logcat (filter `OkHttp`, cari `"cage"` dan `"breed"`), sesuaikan `@SerializedName` di `Livestock.kt` |
| Berat terakhir "Belum ada" padahal ada | Field `latest_weight` null di respons. Cek JSON di Logcat |
| `Unresolved reference ItemToolBinding` / `ActivityScanBinding` | Belum Sync / file layout belum disalin. Sync lalu Rebuild |
| Crash saat buka Scan | Kirim baris merah pertama di Logcat (filter `AndroidRuntime`) |

## 6. Setelah lolos uji
Kirim: nilai `ROLE`, hasil uji 3 sampai 5, dan isi QR yang terbaca (cukup teksnya). Setelah itu Batch 3 (Timbangan).
