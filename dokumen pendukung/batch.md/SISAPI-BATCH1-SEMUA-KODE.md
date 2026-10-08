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


---

# Kode lengkap (salin per file)

Setiap blok diawali path file di dalam project. Buat file dengan nama dan folder itu, lalu tempel isinya.

## `app/src/main/java/com/latihan/si_sapi/helpers/DateHelper.kt`

```kotlin
package com.latihan.si_sapi.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Format yang diminta Laravel untuk rule `date`: yyyy-MM-dd */
fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
```

## `app/src/main/java/com/latihan/si_sapi/helpers/ErrorParser.kt`

```kotlin
package com.latihan.si_sapi.helpers

import org.json.JSONObject
import retrofit2.Response

/**
 * Ambil pesan error dari body JSON Laravel.
 * - Validasi gagal (422): {"message": "...", "errors": {"field": ["pesan"]}} -> pesan pertama di "errors"
 * - Error biasa: {"message": "Stok pakan tidak mencukupi"} -> isi "message"
 */
fun <T> Response<T>.errorMessage(default: String): String = try {
    val obj = JSONObject(errorBody()?.string().orEmpty())
    val errors = obj.optJSONObject("errors")
    val firstField = errors?.keys()?.asSequence()?.firstOrNull()
    val firstError = firstField?.let { errors.optJSONArray(it)?.optString(0) }
    when {
        !firstError.isNullOrBlank() -> firstError
        obj.optString("message").isNotBlank() -> obj.optString("message")
        else -> default
    }
} catch (e: Exception) {
    default
}
```

## `app/src/main/java/com/latihan/si_sapi/helpers/SessionManager.kt`

```kotlin
package com.latihan.si_sapi.helpers

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("si_sapi_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUser(name: String, role: String) =
        prefs.edit().putString(KEY_NAME, name).putString(KEY_ROLE, role).apply()

    fun getUserName(): String = prefs.getString(KEY_NAME, "") ?: ""
    fun getUserRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun clearSession() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_NAME = "user_name"
        const val KEY_ROLE = "user_role"
    }
}
```

## `app/src/main/java/com/latihan/si_sapi/helpers/UiState.kt`

```kotlin
package com.latihan.si_sapi.helpers

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## `app/src/main/java/com/latihan/si_sapi/models/User.kt`

```kotlin
package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

// Bentuk JSON asli dari POST /api/login:
// { "message": "...", "access_token": "...", "token_type": "Bearer",
//   "user": { "id": 1, "name": "...", "email": "...", "role": "manager", "phone": "..." } }
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String? = null,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String? = null,
    val user: User
)
```

## `app/src/main/java/com/latihan/si_sapi/network/ApiClient.kt`

```kotlin
package com.latihan.si_sapi.network

import android.content.Context
import com.latihan.si_sapi.helpers.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Emulator Android -> laptop. Untuk HP asli ganti ke IP LAN laptop, mis. "http://192.168.1.10:8000/api/"
    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    // BODY menampilkan token dan password di Logcat. Ubah ke false sebelum dikumpulkan.
    private const val LOG_BODY = true

    @Volatile
    private var api: ApiService? = null

    fun getApiService(context: Context): ApiService =
        api ?: synchronized(this) {
            api ?: build(context.applicationContext).also { api = it }
        }

    private fun build(context: Context): ApiService {
        val session = SessionManager(context)

        val logging = HttpLoggingInterceptor().apply {
            level = if (LOG_BODY) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder().addHeader("Accept", "application/json")
                session.getToken()?.takeIf { it.isNotEmpty() }?.let {
                    builder.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

## `app/src/main/java/com/latihan/si_sapi/network/ApiService.kt`

```kotlin
package com.latihan.si_sapi.network

import com.latihan.si_sapi.models.LoginRequest
import com.latihan.si_sapi.models.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Backend mengembalikan JSON mentah (tidak dibungkus success/data).
// Endpoint lain ditambahkan satu per satu di batch berikutnya.
interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<ResponseBody>
}
```

## `app/src/main/java/com/latihan/si_sapi/services/AuthService.kt`

```kotlin
package com.latihan.si_sapi.services

import android.content.Context
import com.latihan.si_sapi.helpers.SessionManager
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.helpers.errorMessage
import com.latihan.si_sapi.models.LoginRequest
import com.latihan.si_sapi.models.User
import com.latihan.si_sapi.network.ApiClient
import java.io.IOException

class AuthService(context: Context) {
    private val api = ApiClient.getApiService(context)
    private val session = SessionManager(context)

    suspend fun login(email: String, password: String): UiState<User> = try {
        val res = api.login(LoginRequest(email, password))
        val body = res.body()
        if (res.isSuccessful && body != null) {
            session.saveAuthToken(body.accessToken)
            session.saveUser(body.user.name, body.user.role)
            UiState.Success(body.user)
        } else {
            UiState.Error(res.errorMessage("Email atau password salah."))
        }
    } catch (e: IOException) {
        UiState.Error("Tidak bisa terhubung ke server. Periksa koneksi dan alamat server.")
    } catch (e: Exception) {
        UiState.Error(e.localizedMessage ?: "Terjadi kesalahan.")
    }

    // Sesi lokal SELALU dihapus, walau server tidak terjangkau
    suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
            // diabaikan: yang penting token lokal hilang
        }
        session.clearSession()
    }
}
```

## `app/src/main/java/com/latihan/si_sapi/ui/LoginActivity.kt`

```kotlin
package com.latihan.si_sapi.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapi.databinding.ActivityLoginBinding
import com.latihan.si_sapi.helpers.SessionManager
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.viewmodels.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login: sudah punya token -> langsung ke menu utama
        if (!SessionManager(this).getToken().isNullOrEmpty()) {
            goToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(binding.etEmail.text.toString(), binding.etPassword.text.toString())
        }
        binding.tvTogglePassword.setOnClickListener { togglePassword() }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.tvError.visibility = View.GONE
                    setBusy(true)
                }
                is UiState.Success -> {
                    setBusy(false)
                    goToMain()
                }
                is UiState.Error -> {
                    setBusy(false)
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        binding.progressBar.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !busy
        binding.btnLogin.text = if (busy) "Memproses..." else "Masuk"
    }

    private fun togglePassword() {
        passwordVisible = !passwordVisible
        binding.etPassword.transformationMethod =
            if (passwordVisible) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
        binding.etPassword.setSelection(binding.etPassword.text.length)
        binding.tvTogglePassword.text = if (passwordVisible) "Sembunyikan" else "Tampilkan"
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
```

## `app/src/main/java/com/latihan/si_sapi/ui/MainActivity.kt`

```kotlin
package com.latihan.si_sapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.latihan.si_sapi.databinding.ActivityMainBinding
import com.latihan.si_sapi.helpers.SessionManager
import com.latihan.si_sapi.viewmodels.AuthViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        binding.tvGreeting.text = "Halo, ${session.getUserName()}"
        binding.tvRole.text = roleLabel(session.getUserRole())

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Keluar dari Si-Sapi?")
                .setMessage("Kamu perlu masuk lagi untuk memakai aplikasi.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Keluar") { _, _ -> viewModel.logout() }
                .show()
        }

        viewModel.logoutDone.observe(this) { done ->
            if (done) {
                startActivity(
                    Intent(this, LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        }
    }

    // Nilai role asli dari backend (huruf kecil). Cek di Logcat/DB lalu sesuaikan kalau beda.
    private fun roleLabel(role: String) = when (role.lowercase()) {
        "manager" -> "Manager"
        "vet" -> "Dokter hewan"
        "worker", "pekerja" -> "Pekerja"
        else -> role
    }
}
```

## `app/src/main/java/com/latihan/si_sapi/viewmodels/AuthViewModel.kt`

```kotlin
package com.latihan.si_sapi.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.latihan.si_sapi.helpers.UiState
import com.latihan.si_sapi.models.User
import com.latihan.si_sapi.services.AuthService
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val service = AuthService(app)

    private val _loginState = MutableLiveData<UiState<User>>()
    val loginState: LiveData<UiState<User>> = _loginState

    private val _logoutDone = MutableLiveData<Boolean>()
    val logoutDone: LiveData<Boolean> = _logoutDone

    fun login(email: String, password: String) {
        val e = email.trim()
        if (e.isEmpty() || password.isEmpty()) {
            _loginState.value = UiState.Error("Email dan password wajib diisi.")
            return
        }
        _loginState.value = UiState.Loading
        viewModelScope.launch { _loginState.postValue(service.login(e, password)) }
    }

    fun logout() {
        viewModelScope.launch {
            service.logout()
            _logoutDone.postValue(true)
        }
    }
}
```

## `app/src/main/res/drawable/bg_error.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FDECEC" />
    <corners android:radius="10dp" />
</shape>
```

## `app/src/main/res/drawable/bg_input.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FFFFFF" />
    <stroke android:width="1.5dp" android:color="@color/line" />
    <corners android:radius="12dp" />
</shape>
```

## `app/src/main/res/layout/activity_login.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/mist"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:minHeight="640dp"
        android:orientation="vertical"
        android:padding="24dp">

        <!-- Ganti dengan logo asli: taruh res/drawable/logo_sisapi.png lalu pakai ImageView -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Si-Sapi"
            android:textColor="@color/navy"
            android:textSize="36sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:text="Masuk untuk memakai aplikasi Si-Sapi"
            android:textColor="@color/muted"
            android:textSize="14sp" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="32dp"
            android:text="Email anggota"
            android:textColor="@color/navy"
            android:textSize="14sp"
            android:textStyle="bold" />

        <EditText
            android:id="@+id/etEmail"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:layout_marginTop="6dp"
            android:background="@drawable/bg_input"
            android:hint="nama@peternakan.com"
            android:importantForAutofill="no"
            android:inputType="textEmailAddress"
            android:paddingStart="14dp"
            android:paddingEnd="14dp"
            android:textColor="@color/ink"
            android:textColorHint="#6B7799"
            android:textSize="16sp" />

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Password"
                android:textColor="@color/navy"
                android:textSize="14sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvTogglePassword"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentEnd="true"
                android:padding="4dp"
                android:text="Tampilkan"
                android:textColor="@color/royal"
                android:textSize="14sp"
                android:textStyle="bold" />
        </RelativeLayout>

        <EditText
            android:id="@+id/etPassword"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:layout_marginTop="6dp"
            android:background="@drawable/bg_input"
            android:hint="Masukkan password"
            android:importantForAutofill="no"
            android:inputType="textPassword"
            android:paddingStart="14dp"
            android:paddingEnd="14dp"
            android:textColor="@color/ink"
            android:textColorHint="#6B7799"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/tvError"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:background="@drawable/bg_error"
            android:padding="10dp"
            android:textColor="@color/danger"
            android:textSize="14sp"
            android:visibility="gone" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnLogin"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:layout_marginTop="24dp"
            android:text="Masuk"
            android:textAllCaps="false"
            android:textSize="16sp"
            android:textStyle="bold"
            app:backgroundTint="@color/royal"
            app:cornerRadius="12dp" />

        <ProgressBar
            android:id="@+id/progressBar"
            style="?android:attr/progressBarStyleSmall"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:layout_marginTop="12dp"
            android:visibility="gone" />
    </LinearLayout>
</ScrollView>
```

## `app/src/main/res/layout/activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/mist"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/sky"
        android:orientation="vertical"
        android:paddingStart="20dp"
        android:paddingTop="40dp"
        android:paddingEnd="20dp"
        android:paddingBottom="24dp">

        <TextView
            android:id="@+id/tvGreeting"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#FFFFFF"
            android:textSize="24sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvRole"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:textColor="#FFFFFF"
            android:textSize="14sp" />
    </LinearLayout>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:gravity="center"
        android:padding="24dp"
        android:text="Login berhasil.\nMenu Alat ditambahkan di Batch 2."
        android:textColor="@color/muted"
        android:textSize="16sp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnLogout"
        style="@style/Widget.Material3.Button.OutlinedButton"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:layout_margin="16dp"
        android:text="Keluar"
        android:textAllCaps="false"
        android:textColor="@color/danger"
        android:textSize="16sp"
        android:textStyle="bold"
        app:cornerRadius="12dp"
        app:strokeColor="@color/danger" />
</LinearLayout>
```

## `app/src/main/res/values/colors_sisapi.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="navy">#1B1464</color>
    <color name="royal">#2D5FAD</color>
    <color name="sky">#1890DB</color>
    <color name="mist">#EEF4FD</color>
    <color name="ink">#10183A</color>
    <color name="muted">#566384</color>
    <color name="line">#8FA3CC</color>
    <color name="danger">#C62828</color>
    <color name="ok">#17784F</color>
</resources>
```
