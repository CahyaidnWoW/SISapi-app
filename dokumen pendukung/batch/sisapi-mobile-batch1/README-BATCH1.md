# Si-Sapi Mobile, mulai dari awal: Batch 1 (Login + Menu Utama)

Target Batch 1: **login ke Laravel berhasil, nama dan role muncul di layar utama, logout jalan.**
Hanya 11 file Kotlin/XML. Jangan lanjut ke batch berikutnya sebelum ini jalan.

## 1. Buat project baru
Android Studio -> New Project -> **Empty Views Activity**
- Name: Si-Sapi, Package: `com.latihan.si_sapi`, Language: Kotlin, Minimum SDK: API 26
- Hapus `MainActivity.kt` dan `res/layout/activity_main.xml` bawaan (kita pakai versi di `ui/`).

## 2. Gradle (module app)
Di blok `android { ... }` tambahkan:
```kotlin
buildFeatures { viewBinding = true }
```
Di blok `dependencies { ... }` tambahkan (kalau file kamu `build.gradle` Groovy, tulis tanpa kurung: `implementation '...'`):
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("androidx.activity:activity-ktx:1.9.0")
implementation("com.journeyapps:zxing-android-embedded:4.3.0")
```
Klik **Sync Now**.

## 3. AndroidManifest.xml
Di atas `<application>`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```
Di tag `<application ...>` tambahkan atribut: `android:usesCleartextTraffic="true"` (server masih http).
Ganti isi daftar Activity menjadi:
```xml
<activity android:name=".ui.LoginActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name=".ui.MainActivity" android:exported="false" />
```

## 4. Salin file
Isi folder `app/src/main/` di zip ini disalin ke project (java/... dan res/...). Kalau ada file yang bentrok, timpa.

## 5. Jalankan
Di laptop: `php artisan serve` (cukup untuk emulator). Jalankan app di emulator, login dengan akun seeder, misalnya `manager@farm.com`.

## Hasil yang benar
- Login berhasil -> layar biru "Halo, Budi Manager" + role.
- Tutup app lalu buka lagi -> langsung masuk (auto-login).
- Tombol Keluar -> dialog konfirmasi -> kembali ke Login.
- Password salah -> pesan merah di bawah kolom password.

## Kalau gagal
| Gejala | Penyebab paling mungkin |
|---|---|
| "Tidak bisa terhubung ke server" | `php artisan serve` belum jalan, atau pakai HP asli (ganti `BASE_URL` di `ApiClient.kt` ke IP LAN laptop dan jalankan `php artisan serve --host=0.0.0.0`) |
| `Unresolved reference: ActivityLoginBinding` | `viewBinding = true` belum ditambahkan atau belum Sync |
| Crash saat buka Login: `MaterialButton` | Theme project bukan Material. Pakai template "Empty Views Activity" terbaru |
| Role tampil huruf kecil apa adanya | Nilai role dari backend belum dikenali di `roleLabel()` (MainActivity). Lihat nilai aslinya di Logcat (tag OkHttp) lalu tambahkan ke `when` |

## Rencana batch berikutnya
| Batch | Isi |
|---|---|
| 2 | Menu Alat di layar utama + Scan QR + Profil sapi (dengan `LivestockProfile` yang sudah cocok dengan backend) |
| 3 | Timbangan |
| 4 | Pakan |
| 5 | Lapor sakit (foto) |

Tiga file layanan, ViewModel, dan Activity untuk Batch 3 sampai 5 sudah saya tulis di folder `mobile/` sebelumnya. Nama helper, package, dan `ApiClient` di batch ini sengaja disamakan, jadi file itu bisa dipakai tanpa ditulis ulang. Yang berubah hanya cara menghubungkannya, dan itu saya kerjakan per batch.
