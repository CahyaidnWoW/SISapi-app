# 📊 Pedoman & Spesifikasi Activity Diagram Si-Sapi
> **Dokumen Resmi Acuan Bab III Laporan Tugas Akhir / Ujikom**  
> **Proyek:** Si-Sapi (Smart Livestock Management System)  
> **Arsitektur:** Client-Server dengan pembagian alur proses spesifik untuk Desktop (WPF) dan Mobile (Android).

---

## 1. Ikhtisar Alur Aktivitas Berdasarkan Platform

Aplikasi Desktop berorientasi pada **manajemen data administratif, pencetakan QR, dan evaluasi analitik**, sedangkan aplikasi Mobile berorientasi pada **kecepatan operasional di lapangan, pemindaian sensor kamera (QR & foto), dan pencatatan riil**.

---

## 2. Activity Diagram: Platform Desktop (WPF)

### A. Alur 1: Pendaftaran Sapi Baru & Auto-Generate Nomor Tag (`SP-xxx`)
```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant Desktop as "Desktop WPF UI"
    participant API as "Laravel Backend API"
    participant DB as "MySQL Database"

    Manager->>Desktop: Buka Menu Sapi & Klik "Tambah Sapi"
    Desktop->>Desktop: Tampilkan Form Registrasi Sapi
    Manager->>Desktop: Isi data (kandang, ras, tgl lahir, bobot masuk, kelamin)
    Manager->>Desktop: Klik tombol "Simpan"
    Desktop->>API: HTTP POST /api/livestocks (JSON data)
    activate API
    API->>API: Validasi input data request
    API->>DB: Query nomor tag terakhir (MAX tag_number)
    DB-->>API: Nilai tag terakhir (misal: "SP-015")
    API->>API: Hitung tag berikutnya: "SP-016" (LivestockService)
    API->>DB: INSERT into livestocks (tag: SP-016, status: healthy)
    DB-->>API: Record ID baru tersimpan
    API-->>Desktop: HTTP 201 Created (Data sapi & tag: SP-016)
    deactivate API
    Desktop->>Desktop: Refresh DataGrid tabel sapi & Tampilkan notifikasi sukses
    Desktop-->>Manager: Sapi berhasil didaftarkan dengan ID tag otomatis
```

---

### B. Alur 2: Pencetakan Label QR Code Ear Tag
```mermaid
flowchart TD
    Start([Mulai: Manager di Menu Data Sapi]) --> SelectCattle[Pilih Baris Sapi pada DataGrid]
    SelectCattle --> ClickPrint[Klik Tombol 'Cetak Label QR']
    ClickPrint --> FetchData[Ambil tag_number, nama ras, tgl masuk]
    FetchData --> GenerateQR[Library QRCoder generate Matrix Bitmap dari tag_number]
    GenerateQR --> ComposeDoc[Buat Dokumen Cetak FlowDocument: Logo, Nomor Tag, QR Image]
    ComposeDoc --> OpenDialog[Tampilkan PrintPreview / PrintDialog Windows]
    OpenDialog --> Choice{Manager konfirmasi cetak?}
    Choice -- Tidak --> Cancel([Batal / Tutup Preview])
    Choice -- Ya --> SendToPrinter[Kirim PrintJob ke Printer Fisik]
    SendToPrinter --> Finish([Selesai: Label Fisik Siap Dipasang di Telinga Sapi])
```

---

### C. Alur 3: Evaluasi & Pemantauan Laporan FCR Kandang
```mermaid
flowchart TD
    Start([Mulai: Manager/Vet buka menu Laporan FCR]) --> SelectFilter[Pilih ID Kandang & Rentang Tanggal Mulai - Selesai]
    SelectFilter --> ClickGenerate[Klik Tombol 'Tampilkan Laporan']
    ClickGenerate --> SendRequest[Kirim HTTP GET /api/reports/fcr?cage_id=X&start=Y&end=Z]
    SendRequest --> BackendProcess[Backend FcrService memproses kalkulasi]
    
    subgraph Backend_Calculation["Kalkulasi Backend (FcrService)"]
        BackendProcess --> SumFeed[Total Pakan = SUM quantity feed_consumptions pada periode]
        SumFeed --> WeightDelta[Kenaikan Berat = Berat Akhir Periode - Berat Awal Periode]
        WeightDelta --> CalcFormula["FCR = Total Konsumsi Pakan (kg) / Total Kenaikan Berat (kg)"]
        CalcFormula --> ResultPack[Bungkus hasil FCR, status efisiensi, dan tren bobot ke JSON]
    end

    ResultPack --> ReceiveResp[Desktop menerima response HTTP 200]
    ReceiveResp --> RenderView[Tampilkan Nilai FCR, Kategori Efisiensi, & Grafik LiveCharts]
    RenderView --> Finish([Selesai: Data analitik tersaji])
```

---

### D. Alur 4: Monitoring Rekam Medis & Peninjauan Foto Bukti Darurat
```mermaid
flowchart TD
    Start([Mulai: Dokter Hewan buka menu Rekam Medis di Desktop]) --> LoadRecords[Kirim HTTP GET /api/health-records]
    LoadRecords --> DisplayGrid[Tampilkan daftar pemeriksaan & laporan darurat di DataGrid]
    DisplayGrid --> CheckPhoto{Apakah baris memiliki photo_url?}
    CheckPhoto -- Tidak --> NormalRow[Tombol 'Lihat Foto' dinonaktifkan/sembunyi]
    CheckPhoto -- Ya --> EnableButton[Tombol 'Lihat Foto' aktif]
    EnableButton --> ClickPhoto[User mengklik tombol 'Lihat Foto']
    ClickPhoto --> OpenPopup[Buka Window Pop-up Detail Foto]
    OpenPopup --> LoadImageHttp[Download & Tampilkan gambar via URL Storage Backend]
    LoadImageHttp --> Finish([Selesai: Dokter dapat mengevaluasi kondisi visual sapi])
```

---

### E. Alur 5: Login & Autentikasi Pengguna Desktop
```mermaid
flowchart TD
    Start([Mulai: Buka Aplikasi Desktop]) --> ShowLoginForm[Tampilkan Window LoginView]
    ShowLoginForm --> InputCreds[Manager/Vet memasukkan Email & Password]
    InputCreds --> ClickLogin[Klik Tombol 'Login']
    ClickLogin --> ValidateLocal{Format email valid & field terisi?}
    ValidateLocal -- Tidak --> ShowLocalErr[Tampilkan pesan error validasi input] --> ShowLoginForm
    ValidateLocal -- Ya --> SendAuthReq[Kirim HTTP POST /api/auth/login]
    
    SendAuthReq --> BackendAuth[Backend verifikasi password Hash::check & Role]
    BackendAuth --> AuthResult{Kredensial Cocok & Role Sah?}
    AuthResult -- Tidak --> Ret401[Kembalikan HTTP 401 Unauthorized]
    Ret401 --> ShowAuthErr[Desktop tampilkan alert: 'Email atau password salah'] --> ShowLoginForm
    AuthResult -- Ya --> GenToken[Sanctum generate Personal Access Token]
    GenToken --> RetToken[Kembalikan HTTP 200 OK + Token + Data User]
    RetToken --> SaveSession[Simpan token ke memory AppSession]
    SaveSession --> OpenShell[Buka MainShellView & Load Menu sesuai Hak Akses]
    OpenShell --> Finish([Selesai: Masuk ke Dashboard])
```

---

### F. Alur 6: Pengelolaan Data Master Kandang (CRUD)
```mermaid
flowchart TD
    Start([Mulai: Buka Menu Master Kandang]) --> FetchPens[Kirim HTTP GET /api/pens]
    FetchPens --> DisplayPenGrid[Tampilkan daftar kandang di DataGrid]
    DisplayPenGrid --> UserAction{Pilih Aksi?}
    
    UserAction -- Tambah --> OpenAddPen[Klik 'Tambah Kandang' & Isi Nama, Kapasitas, Catatan]
    OpenAddPen --> SubmitAddPen[Kirim HTTP POST /api/pens]
    SubmitAddPen --> SavePenDB[Backend INSERT ke tabel cages]
    SavePenDB --> RetPenSuccess[HTTP 201 Created]
    
    UserAction -- Edit --> OpenEditPen[Pilih baris kandang & Ubah data]
    OpenEditPen --> SubmitEditPen[Kirim HTTP PUT /api/pens/:id]
    SubmitEditPen --> UpdatePenDB[Backend UPDATE tabel cages]
    UpdatePenDB --> RetPenSuccess
    
    UserAction -- Hapus --> ConfirmDeletePen{Konfirmasi hapus?}
    ConfirmDeletePen -- Batal --> DisplayPenGrid
    ConfirmDeletePen -- Ya --> CheckCattleCount{Apakah kandang masih berisi sapi?}
    CheckCattleCount -- Ya --> ShowConstraintErr[Backend tolak: Kandang masih terisi sapi!] --> DisplayPenGrid
    CheckCattleCount -- Kosong --> SubmitDelPen[Kirim HTTP DELETE /api/pens/:id]
    SubmitDelPen --> RetPenSuccess
    
    RetPenSuccess --> RefreshGrid[Perbarui daftar DataGrid & Notifikasi sukses]
    RefreshGrid --> Finish([Selesai])
```

---

### G. Alur 7: Pengelolaan Data Master Pakan & Penerimaan Batch (FIFO)
```mermaid
flowchart TD
    Start([Mulai: Buka Menu Master Pakan]) --> LoadFeeds[Load Daftar Pakan & Sisa Stok]
    LoadFeeds --> ActionFeed{Pilih Operasi?}
    
    ActionFeed -- Tambah Jenis Pakan --> InputFeedInfo[Input Nama Pakan, Batas Min Stok, Satuan kg/zak]
    InputFeedInfo --> PostFeed[HTTP POST /api/feeds] --> SaveFeed[Simpan ke feed_stocks]
    
    ActionFeed -- Restock Batch Baru --> InputBatchInfo[Pilih Pakan, No Batch, Jumlah kg, Tgl Masuk, Tgl Kedaluwarsa]
    InputBatchInfo --> PostBatch[HTTP POST /api/feeds/:id/batches]
    PostBatch --> SaveBatch[Backend INSERT ke feed_stock_batches & Tambah current_stock]
    
    SaveFeed --> RefreshFeedView[Perbarui Tampilan Stok & Notifikasi Sukses]
    SaveBatch --> RefreshFeedView
    RefreshFeedView --> Finish([Selesai: Stok Terdaftar untuk Logika FIFO])
```

---

### H. Alur 8: Melihat Dashboard & Monitoring Ringkasan KPI
```mermaid
flowchart TD
    Start([Mulai: Navigasi ke Halaman Dashboard]) --> ReqSummary[Kirim HTTP GET /api/dashboard]
    ReqSummary --> ProcessKPI[Backend agregasi data: total sapi, kandang terisi, peringatan stok minim, dan sapi sakit]
    ProcessKPI --> RespKPI[Kembalikan JSON ringkasan metrik]
    RespKPI --> RenderCards[Tampilkan kartu statistik: Total Sapi, Sapi Sehat/Sakit, Stok Pakan]
    RenderCards --> RenderNotif[Tampilkan tabel pengingat jadwal vaksinasi mendekati jatuh tempo]
    RenderNotif --> Finish([Selesai: Ringkasan siap dipantau])
```

---

## 3. Activity Diagram: Platform Mobile (Android)


### A. Alur 5: Scan QR Code Ear Tag & Buka Profil Sapi
```mermaid
flowchart TD
    Start([Mulai: Worker/Vet buka aplikasi Mobile]) --> TapScan[Tekan Tombol 'Scan QR' di Bottom Navigation]
    TapScan --> CheckPerm{Izin Kamera diberikan?}
    CheckPerm -- Belum --> RequestPerm[Tampilkan dialog izin CAMERA]
    RequestPerm --> PermGranted{Disetujui?}
    PermGranted -- Tidak --> DenyMsg[Tampilkan pesan peringatan izin ditolak] --> TapScan
    PermGranted -- Ya --> OpenCam[Aktifkan Kamera ZXing Scanner View]
    CheckPerm -- Sudah --> OpenCam
    
    OpenCam --> PointToTag[Arahkan lensa kamera ke QR Code Ear Tag fisik sapi]
    PointToTag --> DetectQR[ZXing mendeteksi & decode payload string: 'SP-xxx']
    DetectQR --> TriggerBeep[Bunyikan beep getar & jeda kamera]
    TriggerBeep --> FetchProfile[Kirim HTTP GET /api/livestocks/tag/{tag_number}]
    
    FetchProfile --> APIResponse{Sapi ditemukan di database?}
    APIResponse -- Tidak --> Show404[Tampilkan toast 'Data Sapi Tidak Ditemukan'] --> OpenCam
    APIResponse -- Ya --> ShowProfile[Pindah ke Activity Detail Profil Sapi: ras, umur, bobot terakhir, kandang]
    ShowProfile --> Finish([Selesai: Profil siap untuk aksi lanjutan timbang / lapor])
```

---

### B. Alur 6: Input Penimbangan Cepat (Quick Weight Logging)
```mermaid
flowchart TD
    Start([Mulai: Dari Halaman Detail Sapi hasil Scan QR]) --> ClickWeigh[Tekan Tombol 'Input Timbangan']
    ClickWeigh --> ShowForm[Tampilkan dialog form input berat badan]
    ShowForm --> InputValue[Pekerja memasukkan angka bobot dalam kg & catatan opsional]
    InputValue --> SubmitWeigh[Tekan tombol 'Simpan Timbangan']
    SubmitWeigh --> ValidateVal{Angka > 0 dan realistis?}
    ValidateVal -- Tidak --> ErrorInput[Tampilkan error validasi input] --> ShowForm
    ValidateVal -- Ya --> SendWeightAPI[Kirim HTTP POST /api/weight-logs dengan payload JSON]
    
    SendWeightAPI --> BackendSave[Backend validasi token & simpan ke weight_logs]
    BackendSave --> Return201[Backend mengembalikan HTTP 201 Created]
    Return201 --> UpdateUI[Mobile memperbarui teks bobot terakhir & tampilkan toast sukses]
    UpdateUI --> Finish([Selesai: Riwayat timbangan tercatat])
```

---

### C. Alur 7: Input Konsumsi Pakan Harian Kandang (Auto FIFO Deduction)
```mermaid
flowchart TD
    Start([Mulai: Pekerja membuka menu 'Pemberian Pakan' di Mobile]) --> LoadOptions[Aplikasi memuat daftar kandang & jenis pakan]
    LoadOptions --> SelectForm[Pilih ID Kandang, Jenis Pakan, dan Jumlah Pakan kg]
    SelectForm --> ClickSubmit[Tekan tombol 'Kirim Pencatatan']
    ClickSubmit --> SendFeedAPI[Kirim HTTP POST /api/feed-consumptions]
    
    subgraph Backend_FIFO_Deduction["Logika Backend FIFO (FeedConsumptionService)"]
        SendFeedAPI --> FetchBatches[Ambil daftar feed_stock_batches ORDER BY entry_date ASC sisa > 0]
        FetchBatches --> CheckTotalStock{Total sisa stok >= jumlah yang diminta?}
        CheckTotalStock -- Tidak --> Return422[Throw Exception 422: Stok Pakan Tidak Mencukupi]
        CheckTotalStock -- Ya --> LoopBatches[Loop potong stok dari batch tertua dahulu]
        LoopBatches --> SaveBatchUpdates[Update remaining_quantity batch terkait]
        SaveBatchUpdates --> InsertConsumption[Insert ke tabel feed_consumptions]
        InsertConsumption --> CommitDB[Commit transaksi database]
    end

    Return422 --> ShowError[Mobile tampilkan alert: 'Stok tidak mencukupi di gudang'] --> FinishErr([Gagal])
    CommitDB --> ReturnSuccess[Kembalikan HTTP 201 Created]
    ReturnSuccess --> ShowSuccessToast[Mobile tampilkan: 'Pakan harian berhasil dicatat!']
    ShowSuccessToast --> Finish([Selesai: Stok pakan gudang terpotong rapi secara FIFO])
```

---

### D. Alur 8: Pelaporan Darurat Sapi Sakit dengan Foto Kamera (Multipart Upload)
```mermaid
sequenceDiagram
    autonumber
    actor Worker as "Pekerja Lapangan"
    participant Mobile as "EmergencyReportActivity (Android)"
    participant Camera as "Kamera Hardware / Android Intent"
    participant API as "Laravel Backend (HealthRecordController)"
    participant Storage as "Local File Storage (public/storage)"
    participant DB as "MySQL Database"

    Worker->>Mobile: Buka Form "Lapor Sapi Sakit" & Pilih Sapi
    Worker->>Mobile: Ketik deskripsi gejala/kondisi darurat
    Worker->>Mobile: Tekan tombol "Ambil Foto Kondisi"
    Mobile->>Camera: Launch MediaStore.ACTION_IMAGE_CAPTURE
    Camera-->>Worker: Tampilkan Viewfinder Kamera
    Worker->>Camera: Tekan tombol shutter jepret foto
    Camera-->>Mobile: Return Intent Extra Bitmap / URI Gambar
    Mobile->>Mobile: Kompresi gambar & tampilkan preview di ImageView
    Worker->>Mobile: Tekan tombol "Kirim Laporan Darurat"
    
    Mobile->>API: HTTP POST /api/health-records (Multipart/form-data: photo, livestock_id, diagnosis, status="sick")
    activate API
    API->>API: Validasi mimetype (image/jpeg, max: 5MB)
    API->>Storage: Simpan berkas fisik foto ke direktori "health-reports/"
    Storage-->>API: Dapatkan path file (contoh: "health-reports/abc.jpg")
    API->>DB: INSERT into health_records (livestock_id, photo_path, status="sick", diagnosis)
    API->>DB: UPDATE livestocks SET status = "sick" WHERE id = livestock_id
    DB-->>API: Query Berhasil
    API-->>Mobile: HTTP 201 Created (JSON respons berisi health_record & photo_url)
    deactivate API
    
    Mobile-->>Worker: Tampilkan alert: "Laporan darurat terkirim ke sistem!"
```

---

### E. Alur 13: Input Tindakan Medis Lapangan oleh Dokter Hewan (TreatmentActivity)
```mermaid
flowchart TD
    Start([Mulai: Dokter Hewan di Lokasi Kandang]) --> ScanOrSelect[Scan QR atau Pilih Sapi yang Sakit]
    ScanOrSelect --> OpenTreatment[Buka TreatmentActivity]
    OpenTreatment --> ViewDiag[Tinjau riwayat laporan gejala pekerja & foto sapi sakit]
    ViewDiag --> FillTreatment[Input diagnosis dokter, obat/tindakan medis, dan status baru: 'recovered' / 'healthy']
    FillTreatment --> SubmitTreat[Tekan tombol 'Simpan Tindakan Medis']
    SubmitTreat --> SendTreatAPI[Kirim HTTP POST /api/health-records/treatment]
    SendTreatAPI --> UpdateRecord[Backend update rekam medis & kembalikan status sapi jadi 'healthy']
    UpdateRecord --> TreatSuccess[Mobile tampilkan alert: 'Tindakan medis berhasil disimpan!']
    TreatSuccess --> Finish([Selesai: Status sapi pulih])
```

---

### F. Alur 14: Cek Pengingat / Reminder Jadwal Vaksinasi di Mobile
```mermaid
flowchart TD
    Start([Mulai: Buka ReminderListActivity di Mobile]) --> ReqReminders[Kirim HTTP GET /api/reminders/vaccinations]
    ReqReminders --> QueryReminders[Backend cari sapi yang memiliki jadwal vaksinasi rentang 7 hari ke depan]
    QueryReminders --> RetReminders[Kembalikan daftar pengingat JSON]
    RetReminders --> DisplayList[Tampilkan daftar Card Reminder: Tag Sapi, Jenis Vaksin, Tanggal Jatuh Tempo]
    DisplayList --> TapItem{Pengguna klik salah satu reminder?}
    TapItem -- Tidak --> Finish([Selesai])
    TapItem -- Ya --> OpenCattleDetail[Buka Profil Sapi untuk persiapan tindakan] --> Finish
```

---

## 4. Panduan Prompt untuk AI Pembuat Diagram (PlantUML / Draw.io)


### Prompt Activity Diagram PlantUML:
```text
Buatkan Activity Diagram PlantUML modern dengan swimlane partisi (User, Mobile App, Laravel Backend, MySQL Database) untuk alur:
"Pelaporan Darurat Sapi Sakit Lengkap dengan Pengambilan Foto Kamera pada Aplikasi Si-Sapi".

Langkah yang harus ada:
1. User mengisi gejala di Mobile Form.
2. User membuka kamera dan mengambil foto fisik sapi.
3. Mobile memvalidasi dan mengompres foto.
4. Mobile mengirim HTTP POST multipart form data ke /api/health-records.
5. Backend Laravel memvalidasi file gambar dan menyimpannya di file storage.
6. Backend menyimpan record ke tabel health_records dan mengupdate status sapi menjadi 'sick'.
7. Backend mengembalikan status HTTP 201 Created.
8. Mobile menampilkan pesan sukses.
Gunakan styling yang estetik, warna pastel, dan panah vertikal yang rapi.
```
