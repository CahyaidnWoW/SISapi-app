# 🏛️ Pedoman & Spesifikasi Class Diagram Si-Sapi
> **Dokumen Resmi Acuan Bab III Laporan Tugas Akhir / Ujikom**  
> **Proyek:** Si-Sapi (Smart Livestock Management System)  
> **Arsitektur:** Client-Server Multi-tier (Backend Laravel Eloquent & Service Layer, Client Desktop C# WPF MVVM/Code-behind, Client Mobile Kotlin Android MVVM).

---

## 1. Ikhtisar Arsitektur Kelas

Diagram kelas (*Class Diagram*) menggambarkan struktur statis sistem dengan memodelkan kelas-kelas, atribut, metode, serta hubungan ketergantungan dan pewarisan (*inheritance/association*).

Sistem Si-Sapi memiliki pembagian layer berorientasi objek yang rapi:
1. **Backend Laravel (PHP):** Model Eloquent ORM (mewarisi `Illuminate\Database\Eloquent\Model`), Service Layer untuk enkapsulasi logika bisnis, dan API Controllers untuk penanganan HTTP.
2. **Client Desktop (C# .NET WPF):** Model data DTO C# (dengan deserialisasi `Newtonsoft.Json`), Service Client (`HttpClient`), serta Window/Page View.
3. **Client Mobile (Kotlin Android Native):** Data Class (`@SerializedName`), Network Service (Retrofit2 Interface), Android Jetpack `ViewModel`, dan `AppCompatActivity`.

---

## 2. Class Diagram: Backend API (Laravel 11)

### Diagram Mermaid (Backend Models & Services)
```mermaid
classDiagram
    class Model {
        <<Eloquent>>
        +timestamps
        +save()
        +find()
        +create()
    }

    class User {
        +int id
        +string name
        +string email
        +string password
        +string role
        +string phone
        +createToken(name)
        +healthRecords() HasMany
        +weightLogs() HasMany
        +feedConsumptions() HasMany
    }

    class Cage {
        +int id
        +string name
        +int capacity
        +string notes
        +livestocks() HasMany
        +feedConsumptions() HasMany
    }

    class LivestockBreed {
        +int id
        +string name
        +string description
        +livestocks() HasMany
    }

    class Livestock {
        +int id
        +string tag_number
        +int breed_id
        +int cage_id
        +date birth_date
        +date entry_date
        +float entry_weight
        +string gender
        +string status
        +string qr_code_path
        +breed() BelongsTo
        +cage() BelongsTo
        +weightLogs() HasMany
        +healthRecords() HasMany
    }

    class WeightLog {
        +int id
        +int livestock_id
        +int recorded_by
        +float weight
        +date weigh_date
        +string notes
        +livestock() BelongsTo
        +recordedBy() BelongsTo
    }

    class HealthRecord {
        +int id
        +int livestock_id
        +int recorded_by
        +date check_date
        +string diagnosis
        +string treatment
        +string status
        +string photo_path
        +string photo_url
        +livestock() BelongsTo
        +doctor() BelongsTo
        +getPhotoUrlAttribute()
    }

    class FeedStock {
        +int id
        +string name
        +float current_stock
        +float min_stock
        +string unit
        +batches() HasMany
        +consumptions() HasMany
    }

    class FeedStockBatch {
        +int id
        +int feed_stock_id
        +string batch_number
        +float initial_quantity
        +float remaining_quantity
        +date entry_date
        +date expired_date
        +feedStock() BelongsTo
    }

    class FeedConsumption {
        +int id
        +int cage_id
        +int feed_stock_id
        +int recorded_by
        +float quantity
        +date consumption_date
        +cage() BelongsTo
        +feedStock() BelongsTo
        +recordedBy() BelongsTo
    }

    %% Service Layer
    class LivestockService {
        +registerNewLivestock(data) Livestock
        +generateTagNumber() string
        +updateLivestock(id, data) Livestock
    }

    class FeedConsumptionService {
        +recordConsumption(cageId, feedStockId, quantity, userId) FeedConsumption
        -deductStockFifo(feedStockId, quantity)
    }

    class FcrService {
        +calculateFcr(cageId, startDate, endDate) array
        -getTotalFeed(cageId, startDate, endDate) float
        -getWeightGain(cageId, startDate, endDate) float
    }

    %% Inheritance
    Model <|-- User
    Model <|-- Cage
    Model <|-- LivestockBreed
    Model <|-- Livestock
    Model <|-- WeightLog
    Model <|-- HealthRecord
    Model <|-- FeedStock
    Model <|-- FeedStockBatch
    Model <|-- FeedConsumption

    %% Relasi Antar Model
    Cage "1" --> "*" Livestock : contains
    LivestockBreed "1" --> "*" Livestock : classifies
    Livestock "1" --> "*" WeightLog : has
    Livestock "1" --> "*" HealthRecord : has
    User "1" --> "*" WeightLog : records
    User "1" --> "*" HealthRecord : examines
    User "1" --> "*" FeedConsumption : inputs
    Cage "1" --> "*" FeedConsumption : consumes
    FeedStock "1" --> "*" FeedStockBatch : batches
    FeedStock "1" --> "*" FeedConsumption : supplied_to

    %% Relasi Services
    LivestockService ..> Livestock : creates/updates
    FeedConsumptionService ..> FeedConsumption : creates
    FeedConsumptionService ..> FeedStockBatch : updates (FIFO)
    FcrService ..> FeedConsumption : queries
    FcrService ..> WeightLog : queries
```

---

## 3. Class Diagram: Client Desktop (C# .NET WPF)

### Diagram Mermaid (Desktop Architecture)
```mermaid
classDiagram
    %% Desktop Models
    class LivestockModel {
        +int Id
        +string TagNumber
        +int BreedId
        +string BreedName
        +int CageId
        +string CageName
        +DateTime BirthDate
        +DateTime EntryDate
        +double EntryWeight
        +string Gender
        +string Status
        +string QrCodePath
    }

    class HealthRecordModel {
        +int Id
        +int LivestockId
        +string TagNumber
        +DateTime CheckDate
        +string Diagnosis
        +string Treatment
        +string Status
        +string PhotoPath
        +string PhotoUrl
        +bool HasPhoto
    }

    class FcrReportModel {
        +int CageId
        +string CageName
        +double TotalFeedKg
        +double TotalWeightGainKg
        +double FcrValue
        +string EvaluationStatus
        +DateTime StartDate
        +DateTime EndDate
    }

    %% Desktop Services
    class ApiClient {
        -HttpClient _httpClient
        -string _baseUrl
        -string _bearerToken
        +SetToken(token: string)
        +GetAsync~T~(endpoint: string) Task~T~
        +PostAsync~T~(endpoint: string, payload: object) Task~T~
        +PutAsync~T~(endpoint: string, payload: object) Task~T~
        +DeleteAsync(endpoint: string) Task~bool~
    }

    class LivestockService {
        -ApiClient _apiClient
        +GetAllLivestocksAsync() Task~List~LivestockModel~~
        +CreateLivestockAsync(payload) Task~LivestockModel~
        +UpdateLivestockAsync(id, payload) Task~LivestockModel~
        +DeleteLivestockAsync(id) Task~bool~
    }

    class FcrService {
        -ApiClient _apiClient
        +GetFcrReportAsync(cageId, start, end) Task~FcrReportModel~
    }

    %% Desktop Views
    class LivestockView {
        -LivestockService _service
        -ObservableCollection~LivestockModel~ _livestocks
        +BtnTambah_Click()
        +BtnEdit_Click()
        +BtnCetakQR_Click()
        +FilterDataGrid()
    }

    class HealthRecordView {
        -HealthRecordService _service
        +BtnTambah_Click()
        +BtnViewPhoto_Click(sender, e)
    }

    class FcrReportView {
        -FcrService _service
        +BtnHitungFcr_Click()
        +RenderChart()
    }

    %% Relasi Desktop
    LivestockView --> LivestockService : uses
    HealthRecordView --> HealthRecordModel : binds
    FcrReportView --> FcrService : uses
    LivestockService --> ApiClient : uses
    FcrService --> ApiClient : uses
    LivestockService ..> LivestockModel : produces
    FcrService ..> FcrReportModel : produces
```

---

## 4. Class Diagram: Client Mobile (Kotlin Android Native)

### Diagram Mermaid (Mobile Architecture)
```mermaid
classDiagram
    %% Data Models
    class Livestock {
        +int id
        +String tagNumber
        +int breedId
        +String breedName
        +int cageId
        +String cageName
        +String gender
        +String status
        +Double lastWeight
    }

    class HealthRecord {
        +int id
        +int livestockId
        +String checkDate
        +String diagnosis
        +String treatment
        +String status
        +String photoPath
        +String photoUrl
    }

    class WeightLog {
        +int id
        +int livestockId
        +Double weight
        +String weighDate
        +String notes
    }

    %% Retrofit Network Layer
    class ApiService {
        <<Interface>>
        +login(body) Call~LoginResponse~
        +getLivestockByTag(tag) Call~ApiResponse~Livestock~~
        +postWeightLog(body) Call~ApiResponse~WeightLog~~
        +postFeedConsumption(body) Call~ApiResponse~Any~~
        +uploadHealthRecord(fields, photoPart) Call~ApiResponse~HealthRecord~~
    }

    %% ViewModels
    class ScanViewModel {
        -ApiService apiService
        +LiveData~UiState~Livestock~~ livestockState
        +fetchByTag(tagNumber: String)
    }

    class WeightLogViewModel {
        -ApiService apiService
        +LiveData~UiState~WeightLog~~ submitState
        +saveWeight(livestockId, weight, notes)
    }

    class HealthRecordViewModel {
        -ApiService apiService
        +LiveData~UiState~HealthRecord~~ uploadState
        +submitEmergencyReport(livestockId, diagnosis, photoFile)
    }

    %% Activities
    class ScanActivity {
        -ScanViewModel viewModel
        -CompoundBarcodeView barcodeView
        +onBarcodeScanned(result)
        +navigateToDetail(livestock)
    }

    class WeightInputActivity {
        -WeightLogViewModel viewModel
        +submitWeight()
    }

    class EmergencyReportActivity {
        -HealthRecordViewModel viewModel
        -File capturedPhotoFile
        +openCamera()
        +onPhotoCaptured(file)
        +sendReport()
    }

    %% Relasi Mobile
    ScanActivity --> ScanViewModel : observes
    WeightInputActivity --> WeightLogViewModel : observes
    EmergencyReportActivity --> HealthRecordViewModel : observes
    ScanViewModel --> ApiService : calls
    WeightLogViewModel --> ApiService : calls
    HealthRecordViewModel --> ApiService : calls
    ApiService ..> Livestock : returns
    ApiService ..> WeightLog : returns
    ApiService ..> HealthRecord : returns
```

---

## 5. Panduan Prompt untuk AI Pembuat Diagram (PlantUML / Draw.io)

### Prompt Class Diagram PlantUML:
```text
Buatkan Class Diagram UML standar berorientasi objek menggunakan PlantUML untuk arsitektur Backend Laravel 11 pada sistem "Si-Sapi (Sistem Informasi Peternakan Sapi Potong)".

Model Utama yang harus dimodelkan (semua mewarisi Model):
- User (id, name, email, role, phone)
- Cage (id, name, capacity, notes)
- LivestockBreed (id, name, description)
- Livestock (id, tag_number, breed_id, cage_id, birth_date, entry_date, entry_weight, gender, status, qr_code_path)
- WeightLog (id, livestock_id, recorded_by, weight, weigh_date, notes)
- HealthRecord (id, livestock_id, recorded_by, check_date, diagnosis, treatment, status, photo_path, photo_url)
- FeedStock (id, name, current_stock, min_stock, unit)
- FeedStockBatch (id, feed_stock_id, batch_number, initial_quantity, remaining_quantity, entry_date, expired_date)
- FeedConsumption (id, cage_id, feed_stock_id, recorded_by, quantity, consumption_date)

Service Layer (Business Logic):
- LivestockService (registerNewLivestock, generateTagNumber, updateLivestock)
- FeedConsumptionService (recordConsumption, deductStockFifo)
- FcrService (calculateFcr, getTotalFeed, getWeightGain)

Tampilkan relasi kardinalitas (1 ke banyak, belongsTo/hasMany) dan dependency panah putus-putus dari Service ke Model. Gunakan warna lembut, skinparam classFontSize 11, dan layout vertikal yang rapi.
```
