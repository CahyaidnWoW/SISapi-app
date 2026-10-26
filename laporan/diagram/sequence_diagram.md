# ⏱️ Pedoman & Spesifikasi Sequence Diagram Si-Sapi
> **Dokumen Resmi Acuan Bab III Laporan Tugas Akhir / Ujikom**  
> **Proyek:** Si-Sapi (Smart Livestock Management System)  
> **Arsitektur:** Multi-tier Client-Server (Aktor $\rightarrow$ UI Layer $\rightarrow$ ViewModel/API Client $\rightarrow$ REST Controller $\rightarrow$ Service Layer $\rightarrow$ Eloquent Model $\rightarrow$ MySQL Database).

---

## 1. Ikhtisar Sequence Diagram

Sequence Diagram menggambarkan interaksi antar-objek dan komponen secara berurutan berdasarkan dimensi waktu (*chronological messages*). Pada Si-Sapi, terdapat pembagian alur yang tegas antara:
- **Client Desktop (C# WPF):** Berfokus pada interaksi antarmuka XAML, View-Logic code-behind, deserialisasi JSON (`Newtonsoft.Json`), dan manipulasi data analitik/laporan.
- **Client Mobile (Kotlin Android):** Berfokus pada arsitektur MVVM, pemanggilan asynchronous coroutine/Retrofit2, penanganan kamera perangkat, dan transmisi payload multipart.
- **Backend API (Laravel 11):** Menggunakan pola *Service Layer Pattern* di mana Controller hanya bertindak sebagai pintu gerbang HTTP, sedangkan kalkulasi bisnis kompleks (FCR, FIFO, auto-numbering) dipusatkan di `app/Services/`.

---

## 2. Sequence Diagram: Platform Desktop (WPF)

### A. SD-D01: Login & Autentikasi Pengguna Desktop
```mermaid
sequenceDiagram
    autonumber
    actor User as "Manager / Vet"
    participant View as "LoginView.xaml (.cs)"
    participant Service as "AuthService.cs"
    participant API as "AuthController.php"
    participant Sanctum as "Laravel Sanctum"
    participant DB as "MySQL (users)"
    participant Shell as "MainShellView.xaml"

    User->>View: Input email & password, klik "Login"
    View->>View: Validasi format email & kelengkapan form
    View->>Service: LoginAsync(email, password)
    Service->>API: HTTP POST /api/auth/login {email, password}
    activate API
    API->>DB: Query User where email = email
    DB-->>API: Data record User & hashed password
    API->>API: Hash::check(password, user->password)
    alt Password Salah / User Tidak Ada
        API-->>Service: HTTP 401 Unauthorized {message: "Kredensial tidak valid"}
        Service-->>View: Kembalikan status gagal
        View-->>User: Tampilkan dialog error merah "Email atau password salah"
    else Kredensial Valid
        API->>Sanctum: user->createToken('desktop-token')->plainTextToken
        Sanctum-->>API: Plain Token String (misal: "1|abCxyZ...")
        API-->>Service: HTTP 200 OK {token, user: {id, name, role}}
        deactivate API
        Service->>Service: Simpan token & role ke AppSession memori
        Service-->>View: Kembalikan status berhasil
        View->>Shell: Inisialisasi MainShellView(user) & Show()
        View->>View: Close() login window
        Shell-->>User: Tampilkan Dashboard Utama sesuai Role
    end
```

---

### B. SD-D02: Input Sapi Baru & Auto-Generate Tag `SP-xxx`
```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant View as "LivestockView.xaml"
    participant Service as "LivestockService.cs"
    participant Controller as "LivestockController.php"
    participant BizLogic as "LivestockService.php"
    participant Model as "Livestock Eloquent Model"
    participant DB as "MySQL Database"

    Manager->>View: Isi form (breed_id, cage_id, birth_date, entry_weight, gender)
    Manager->>View: Klik tombol "Simpan"
    View->>Service: CreateLivestockAsync(payload)
    Service->>Controller: HTTP POST /api/livestocks (Bearer Token)
    activate Controller
    Controller->>Controller: Validasi data request (rules)
    Controller->>BizLogic: registerNewLivestock(validatedData)
    activate BizLogic
    BizLogic->>DB: SELECT MAX(tag_number) FROM livestocks WHERE tag_number LIKE 'SP-%'
    DB-->>BizLogic: "SP-015"
    BizLogic->>BizLogic: Ekstrak nomor 15 + 1 = 16 -> Format "SP-016"
    BizLogic->>Model: Create([tag_number => 'SP-016', status => 'healthy', ...])
    Model->>DB: INSERT INTO livestocks VALUES (...)
    DB-->>Model: ID baru (misal: 16)
    BizLogic-->>Controller: Return Objek Livestock Baru
    deactivate BizLogic
    Controller-->>Service: HTTP 201 Created {success: true, data: {...}}
    deactivate Controller
    Service-->>View: Deserialize JSON ke Objek C# Livestock
    View->>View: Tambahkan record ke ObservableCollection DataGrid
    View-->>Manager: Tampilkan popup hijau: "Sapi SP-016 berhasil didaftarkan!"
```

---

### C. SD-D03: Evaluasi & Perhitungan Laporan FCR Kandang
```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant View as "FcrReportView.xaml"
    participant Controller as "FcrController.php"
    participant FcrService as "FcrService.php"
    participant DB as "MySQL Database"

    Manager->>View: Pilih Kandang (cage_id=1), Tanggal Mulai, Tanggal Akhir
    Manager->>View: Klik "Hitung FCR"
    View->>Controller: HTTP GET /api/reports/fcr?cage_id=1&start_date=...&end_date=...
    activate Controller
    Controller->>FcrService: calculateFcr(cageId, startDate, endDate)
    activate FcrService
    FcrService->>DB: SUM(quantity) FROM feed_consumptions WHERE cage_id=1 AND date BETWEEN...
    DB-->>FcrService: totalFeed = 1200.5 kg
    FcrService->>DB: Query bobot awal & bobot akhir sapi-sapi di kandang 1
    DB-->>FcrService: totalWeightGain = 185.0 kg
    FcrService->>FcrService: Hitung FCR = 1200.5 / 185.0 = 6.48
    FcrService->>FcrService: Evaluasi kategori: Baik / Efisien / Boros
    FcrService-->>Controller: Return Array Rekapitulasi & Tren
    deactivate FcrService
    Controller-->>View: HTTP 200 OK {fcr: 6.48, total_feed: 1200.5, weight_gain: 185.0, status: "Normal"}
    deactivate Controller
    View->>View: Render angka KPI FCR & gambar chart batang performa
    View-->>Manager: Tampilan grafik dan metrik efisiensi siap dianalisis
```

---

### D. SD-D04: Menampilkan Pop-up Foto Bukti Kondisi Darurat Sapi Sakit
```mermaid
sequenceDiagram
    autonumber
    actor Vet as "Dokter Hewan"
    participant View as "HealthRecordView.xaml (.cs)"
    participant Popup as "PhotoDetailWindow.xaml"
    participant Storage as "Laravel Web Server (/storage/...)"

    Vet->>View: Meninjau daftar rekam medis di DataGrid
    Note over View: Baris memiliki photo_url != null
    Vet->>View: Klik tombol "Lihat Foto" pada baris sapi sakit
    View->>Popup: Inisialisasi PhotoDetailWindow(record.PhotoUrl, record.TagNumber)
    Popup->>Popup: Tampilkan loader / progress bar
    Popup->>Storage: HTTP GET request ke URL foto (misal: http://.../storage/health-reports/sapi1.jpg)
    Storage-->>Popup: Response Binary Stream Image
    Popup->>Popup: BitmapImage.SetSource(stream) & sembunyikan loader
    Popup-->>Vet: Muncul jendela pop-up memperlihatkan foto kondisi visual luka/sakit sapi
```

---

### E. SD-D05: Pengelolaan Data Master Kandang (CRUD)
```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant View as "CageView.xaml (.cs)"
    participant Service as "CageService.cs"
    participant Controller as "CageController.php"
    participant Model as "Cage Model"
    participant DB as "MySQL (cages)"

    Manager->>View: Buka menu Kandang & Klik "Tambah"
    Manager->>View: Isi nama kandang, kapasitas, catatan & Klik "Simpan"
    View->>Service: CreateCageAsync(payload)
    Service->>Controller: HTTP POST /api/pens (Bearer Token)
    activate Controller
    Controller->>Controller: Validasi data request
    Controller->>Model: Create([name, capacity, notes])
    Model->>DB: INSERT INTO cages VALUES (...)
    DB-->>Model: ID baru
    Controller-->>Service: HTTP 201 Created {success: true, data: {...}}
    deactivate Controller
    Service-->>View: Kembalikan objek CageModel
    View->>View: Tambahkan record ke DataGrid & Tampilkan alert sukses
    View-->>Manager: Data kandang baru berhasil disimpan
```

---

### F. SD-D06: Pengelolaan Master Pakan & Penambahan Batch Baru (Restock)
```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant View as "FeedView.xaml (.cs)"
    participant Service as "FeedService.cs"
    participant Controller as "FeedStockController.php"
    participant BatchModel as "FeedStockBatch Model"
    participant FeedModel as "FeedStock Model"
    participant DB as "MySQL Database"

    Manager->>View: Pilih jenis pakan & Klik "Tambah Batch Masuk"
    Manager->>View: Input batch_number, quantity=500kg, entry_date, expired_date
    Manager->>View: Klik tombol "Simpan Batch"
    View->>Service: AddBatchAsync(feedId, payload)
    Service->>Controller: HTTP POST /api/feeds/:id/batches (Bearer Token)
    activate Controller
    Controller->>BatchModel: Create([feed_stock_id, batch_number, initial_qty, remaining_qty=500, entry_date])
    BatchModel->>DB: INSERT INTO feed_stock_batches
    Controller->>FeedModel: Increment current_stock + 500
    FeedModel->>DB: UPDATE feed_stocks SET current_stock = current_stock + 500
    DB-->>Controller: Commit Sukses
    Controller-->>Service: HTTP 201 Created {message: "Batch stok berhasil ditambahkan"}
    deactivate Controller
    Service-->>View: Update View data stok
    View-->>Manager: Tampilkan popup hijau: "500 kg pakan berhasil dimasukkan ke stok FIFO"
```

---

### G. SD-D07: Update Data Sapi & Normalisasi (Method PUT/PATCH)
```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant View as "LivestockView.xaml (.cs)"
    participant Service as "LivestockService.cs"
    participant Controller as "LivestockController.php"
    participant DB as "MySQL Database"

    Manager->>View: Pilih sapi, ubah status jadi "terjual", gender "jantan"
    Manager->>View: Klik tombol "Perbarui"
    View->>Service: UpdateLivestockAsync(id, payload)
    Service->>Controller: HTTP PUT /api/livestocks/:id {gender: "jantan", status: "terjual"}
    activate Controller
    Note over Controller: Normalisasi Backend:<br/>$genderMap: "jantan" -> "jantan"<br/>$statusMap: "terjual" -> "sold"<br/>Lookup breed_id jika kirim nama ras string
    Controller->>DB: UPDATE livestocks SET status = 'sold', gender = 'jantan' WHERE id = :id
    DB-->>Controller: Update 1 Row Affected
    Controller-->>Service: HTTP 200 OK {success: true, message: "Data sapi berhasil diperbarui"}
    deactivate Controller
    Service-->>View: Success
    View->>View: Perbarui DataGrid row & tampilkan badge status
    View-->>Manager: "Perubahan data sapi berhasil disimpan!"
```

---

### H. SD-D08: Pencetakan Label QR Code Ear Tag
```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant View as "QrGeneratorView.xaml (.cs)"
    participant QRCoder as "QRCoder QRCodeGenerator"
    participant PrintEngine as "Windows PrintDialog / FlowDocument"
    participant Printer as "Printer Fisik"

    Manager->>View: Pilih sapi "SP-001" & Klik "Generate QR"
    View->>QRCoder: CreateQrCode("SP-001", QRCodeGenerator.ECCLevel.Q)
    QRCoder-->>View: Return QRCodeData & BitmapImage
    View->>View: Render pratinjau label (Logo Si-Sapi, Tag: SP-001, QR Image)
    Manager->>View: Klik tombol "Cetak Label"
    View->>PrintEngine: ShowDialog() & Send Document
    PrintEngine-->>Manager: Muncul dialog pemilihan printer
    Manager->>PrintEngine: Pilih printer & tekan "Print"
    PrintEngine->>Printer: Kirim halaman cetak fisik
    Printer-->>Manager: Label tercetak siap dipasang pada sapi
```

---

## 3. Sequence Diagram: Platform Mobile (Android)


### A. SD-M01: Scan QR Code Ear Tag & Buka Profil Sapi
```mermaid
sequenceDiagram
    autonumber
    actor Worker as "Pekerja Lapangan"
    participant ScanAct as "ScanActivity.kt"
    participant ZXing as "ZXing BarcodeScannerView"
    participant VM as "ScanViewModel.kt"
    participant Retrofit as "ApiService.kt (Retrofit2)"
    participant API as "LivestockController.php"
    participant DB as "MySQL Database"
    participant DetailAct as "LivestockDetailActivity.kt"

    Worker->>ScanAct: Buka tab Scanner
    ScanAct->>ZXing: Start camera preview & continuous decode
    Worker->>ZXing: Arahkan kamera ke QR Ear Tag fisik di telinga sapi
    ZXing->>ZXing: Deteksi matriks QR & ekstrak payload teks: "SP-002"
    ZXing-->>ScanAct: onScanResult("SP-002")
    ScanAct->>ScanAct: Play sound beep & trigger haptic feedback
    ScanAct->>VM: getLivestockByTag("SP-002")
    VM->>Retrofit: getLivestockByTag("SP-002")
    Retrofit->>API: HTTP GET /api/livestocks/tag/SP-002 (Header: Bearer Token)
    activate API
    API->>DB: SELECT * FROM livestocks JOIN breeds JOIN pens WHERE tag_number = 'SP-002'
    DB-->>API: Data record Sapi ditemukan
    API-->>Retrofit: HTTP 200 OK {success: true, data: {id: 2, tag: "SP-002", weight: 340, ...}}
    deactivate API
    Retrofit-->>VM: Response Body
    VM-->>ScanAct: LiveData / StateFlow emits Success(Livestock)
    ScanAct->>DetailAct: Intent to DetailActivity with Parcelable Data
    DetailAct-->>Worker: Tampilkan layar informasi lengkap sapi (ras, usia, kandang, riwayat)
```

---

### B. SD-M02: Pencatatan Penimbangan Cepat (Quick Weight Logging)
```mermaid
sequenceDiagram
    autonumber
    actor Worker
    participant View as "WeightInputActivity.kt"
    participant VM as "WeightLogViewModel.kt"
    participant ApiClient as "ApiService (Retrofit)"
    participant Controller as "WeightLogController.php"
    participant Service as "WeightLogService.php"
    participant DB as "MySQL Database"

    Worker->>View: Ketik angka berat: "348.5" kg & catatan opsional
    Worker->>View: Tekan tombol "Simpan Timbangan"
    View->>VM: saveWeightLog(livestockId, weight=348.5, notes)
    VM->>ApiClient: postWeightLog(requestBody)
    ApiClient->>Controller: HTTP POST /api/weight-logs {livestock_id: 2, weight: 348.5, weigh_date: NOW()}
    activate Controller
    Controller->>Service: recordWeight(data, userId)
    activate Service
    Service->>DB: INSERT INTO weight_logs (livestock_id, recorded_by, weight, weigh_date)
    DB-->>Service: Insert Success
    Service-->>Controller: Return WeightLog Model
    deactivate Service
    Controller-->>ApiClient: HTTP 201 Created {success: true, data: {...}}
    deactivate Controller
    ApiClient-->>VM: Response success
    VM-->>View: UiState.Success
    View-->>Worker: Tampilkan Toast sukses: "Timbangan 348.5 kg berhasil dicatat!"
    View->>View: Finish() & kembali ke profil sapi
```

---

### C. SD-M03: Pencatatan Konsumsi Pakan Berbasis FIFO (Auto Deduction)
```mermaid
sequenceDiagram
    autonumber
    actor Worker
    participant UI as "FeedInputActivity.kt"
    participant VM as "FeedViewModel.kt"
    participant Retrofit as "ApiService (Retrofit)"
    participant Controller as "FeedConsumptionController.php"
    participant Service as "FeedConsumptionService.php"
    participant DB as "MySQL Database"

    Worker->>UI: Pilih Kandang A, Pakan Konsentrat, Jumlah: 50 kg
    Worker->>UI: Tekan tombol "Kirim Konsumsi Pakan"
    UI->>VM: submitFeedConsumption(cageId, feedStockId, quantity=50)
    VM->>Retrofit: postFeedConsumption(...)
    Retrofit->>Controller: HTTP POST /api/feed-consumptions {cage_id: 1, feed_stock_id: 3, quantity: 50}
    activate Controller
    Controller->>Service: recordConsumption(cage_id, feed_stock_id, quantity, user_id)
    activate Service
    
    Service->>DB: DB::beginTransaction()
    Service->>DB: SELECT * FROM feed_stock_batches WHERE feed_stock_id = 3 AND remaining_quantity > 0 ORDER BY entry_date ASC
    DB-->>Service: List Batch: [Batch-A sisa 30 kg, Batch-B sisa 100 kg]
    
    Note over Service: Logika FIFO:<br/>1. Ambil 30 kg dari Batch-A (sisa jadi 0 kg)<br/>2. Ambil 20 kg dari Batch-B (sisa jadi 80 kg)
    
    Service->>DB: UPDATE feed_stock_batches SET remaining_quantity = 0 WHERE id = Batch-A
    Service->>DB: UPDATE feed_stock_batches SET remaining_quantity = 80 WHERE id = Batch-B
    Service->>DB: INSERT INTO feed_consumptions (cage_id, feed_stock_id, quantity=50, recorded_by=...)
    Service->>DB: DB::commit()
    DB-->>Service: Commit Berhasil
    Service-->>Controller: Return Model FeedConsumption
    deactivate Service
    
    Controller-->>Retrofit: HTTP 201 Created {message: "Pakan berhasil dicatat secara FIFO"}
    deactivate Controller
    Retrofit-->>VM: Success Response
    VM-->>UI: UiState.Success
    UI-->>Worker: Dialog notifikasi sukses: "50 kg pakan berhasil dikeluarkan dari gudang"
```

---

### D. SD-M04: Pelaporan Darurat Sapi Sakit dengan Unggah Foto Kamera
```mermaid
sequenceDiagram
    autonumber
    actor Worker
    participant UI as "EmergencyReportActivity.kt"
    participant Intent as "Android Camera Intent"
    participant VM as "HealthRecordViewModel.kt"
    participant Retrofit as "ApiService (Retrofit Multipart)"
    participant Controller as "HealthRecordController.php"
    participant Storage as "Laravel Storage (public)"
    participant DB as "MySQL Database"

    Worker->>UI: Buka form darurat, pilih sapi, ketik "Sapi lemas nafsu makan turun"
    Worker->>UI: Klik tombol "Ambil Foto"
    UI->>Intent: startActivityForResult(MediaStore.ACTION_IMAGE_CAPTURE)
    Intent-->>Worker: Buka antarmuka kamera
    Worker->>Intent: Ambil foto & tekan centang OK
    Intent-->>UI: Return Image URI / Bitmap
    UI->>UI: Tampilkan preview foto di ImageView
    Worker->>UI: Klik tombol "Kirim Laporan"
    
    UI->>VM: reportEmergency(livestockId, diagnosis, photoFile)
    VM->>Retrofit: uploadEmergencyReport(livestock_id_part, diagnosis_part, photo_multipart)
    Retrofit->>Controller: HTTP POST /api/health-records (Content-Type: multipart/form-data)
    activate Controller
    Controller->>Controller: Validasi file ('photo' => 'required|image|max:5120')
    Controller->>Storage: photo->store('health-reports', 'public')
    Storage-->>Controller: "health-reports/rand_name.jpg"
    Controller->>DB: INSERT INTO health_records (livestock_id, diagnosis, photo_path, status='sick')
    Controller->>DB: UPDATE livestocks SET status = 'sick' WHERE id = livestock_id
    DB-->>Controller: Insert & Update Sukses
    Controller-->>Retrofit: HTTP 201 Created {success: true, data: {id: 7, photo_url: "http://.../storage/health-reports/rand_name.jpg"}}
    deactivate Controller
    Retrofit-->>VM: Success
    VM-->>UI: Emit Status Sukses
    UI-->>Worker: Tampilkan alert dialog hijau: "Laporan darurat & foto terkirim ke Dokter Hewan!"
```

---

### E. SD-M05: Login & Autentikasi Pengguna Mobile
```mermaid
sequenceDiagram
    autonumber
    actor Worker as "Pekerja / Dokter Hewan"
    participant View as "LoginActivity.kt"
    participant VM as "AuthViewModel.kt"
    participant ApiClient as "ApiService (Retrofit)"
    participant Session as "SessionManager.kt (SharedPreferences)"
    participant Controller as "AuthController.php"
    participant DB as "MySQL (users)"
    participant Dash as "DashboardActivity.kt"

    Worker->>View: Input email & password, tekan tombol "Masuk"
    View->>VM: login(email, password)
    VM->>ApiClient: login(LoginRequest(email, password))
    ApiClient->>Controller: HTTP POST /api/auth/login {email, password}
    activate Controller
    Controller->>DB: Query User & verifikasi Hash
    DB-->>Controller: User valid (role: worker)
    Controller->>Controller: Generate Sanctum Token
    Controller-->>ApiClient: HTTP 200 OK {token: "2|xyz...", user: {id: 3, name: "Budi", role: "worker"}}
    deactivate Controller
    ApiClient-->>VM: Response Success
    VM->>Session: saveAuthToken(token) & saveUserData(user)
    Session-->>VM: Token tersimpan aman
    VM-->>View: UiState.Success(user)
    View->>Dash: Intent to DashboardActivity
    View->>View: finish()
    Dash-->>Worker: Tampilkan dashboard mobile & tugas harian
```

---

### F. SD-M06: Input Tindakan Medis Lapangan oleh Dokter Hewan (TreatmentActivity)
```mermaid
sequenceDiagram
    autonumber
    actor Vet as "Dokter Hewan"
    participant View as "TreatmentActivity.kt"
    participant VM as "HealthRecordViewModel.kt"
    participant ApiClient as "ApiService (Retrofit)"
    participant Controller as "HealthRecordController.php"
    participant DB as "MySQL Database"

    Vet->>View: Tinjau riwayat keluhan sapi, ketik resep/tindakan medis
    Vet->>View: Pilih status hasil: "healthy / recovered" & tekan "Simpan Tindakan"
    View->>VM: submitTreatment(recordId, treatment, newStatus="healthy")
    VM->>ApiClient: updateTreatment(recordId, requestBody)
    ApiClient->>Controller: HTTP POST /api/health-records/:id/treatment (Bearer Token)
    activate Controller
    Controller->>DB: UPDATE health_records SET treatment = :treatment, status = 'healthy'
    Controller->>DB: UPDATE livestocks SET status = 'healthy' WHERE id = record.livestock_id
    DB-->>Controller: Update Berhasil
    Controller-->>ApiClient: HTTP 200 OK {success: true, message: "Tindakan medis berhasil disimpan"}
    deactivate Controller
    ApiClient-->>VM: Success Response
    VM-->>View: UiState.Success
    View-->>Vet: Tampilkan Toast: "Tindakan berhasil! Status sapi kini SEHAT."
    View->>View: finish()
```

---

### G. SD-M07: Pengambilan Pengingat / Reminder Jadwal Vaksinasi
```mermaid
sequenceDiagram
    autonumber
    actor Worker as "Pekerja / Dokter Hewan"
    participant View as "ReminderListActivity.kt"
    participant VM as "ReminderViewModel.kt"
    participant ApiClient as "ApiService (Retrofit)"
    participant Controller as "HealthRecordController.php"
    participant DB as "MySQL Database"

    Worker->>View: Buka menu Pengingat Vaksinasi
    View->>VM: loadUpcomingReminders()
    VM->>ApiClient: getUpcomingVaccinations()
    ApiClient->>Controller: HTTP GET /api/reminders/vaccinations
    activate Controller
    Controller->>DB: SELECT * FROM health_records WHERE check_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY)
    DB-->>Controller: Daftar sapi jatuh tempo vaksinasi
    Controller-->>ApiClient: HTTP 200 OK {success: true, data: [...]}
    deactivate Controller
    ApiClient-->>VM: List<ReminderItem>
    VM-->>View: Render RecyclerView Card
    View-->>Worker: Tampilkan daftar kartu pengingat (Tag Sapi, Vaksin, Tanggal)
```

---

## 4. Panduan Prompt untuk AI Pembuat Diagram (PlantUML / Draw.io)


### Prompt Sequence Diagram PlantUML:
```text
Buatkan Sequence Diagram PlantUML modern yang elegan untuk alur:
"Pencatatan Konsumsi Pakan Harian berbasis Algoritma FIFO pada Sistem Si-Sapi".

Partisipan yang terlibat:
- actor "Pekerja Kandang" as Worker
- boundary "FeedInputActivity" as UI
- control "FeedViewModel" as VM
- control "ApiService (Retrofit)" as Retrofit
- boundary "FeedConsumptionController" as Controller
- control "FeedConsumptionService" as Service
- database "MySQL Database" as DB

Skenario:
1. Worker menginput konsumsi 50 kg untuk pakan konsentrat di kandang A.
2. Request dikirim hingga ke Service Layer.
3. Service memulai transaksi DB (DB::beginTransaction).
4. Service mencari batch pakan dengan tanggal masuk tertua (ORDER BY entry_date ASC).
5. Service memotong stok batch tertua lebih dulu hingga total 50 kg tercukupi.
6. Service menyimpan record konsumsi pakan dan melakukan commit transaksi.
7. Mengembalikan response HTTP 201 Created hingga worker melihat dialog sukses.
Gunakan styling PlantUML modern dengan warna background putih, lifeline jelas, dan autonumber.
```
