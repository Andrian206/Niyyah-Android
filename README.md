# 📱 Niyyah - Aplikasi Produktivitas Android

<p align="center">
  <img src="app/src/main/res/drawable/ic_niyyah_purple.png" alt="Niyyah Logo" width="120"/>
</p>

**Niyyah** (نية - Niat dalam bahasa Arab) adalah aplikasi produktivitas berbasis Android yang membantu pengguna mengelola tugas harian mereka dengan antarmuka yang bersih dan modern. Aplikasi ini dibangun menggunakan Kotlin dengan arsitektur berbasis Fragment dan terintegrasi penuh dengan Firebase untuk autentikasi dan penyimpanan data.

---

## 📋 Daftar Isi

- [Fitur Utama](#-fitur-utama)
- [Tech Stack](#-tech-stack)
- [Arsitektur Aplikasi](#-arsitektur-aplikasi)
- [Struktur Project](#-struktur-project)
- [Penjelasan Detail Setiap File](#-penjelasan-detail-setiap-file)
  - [Data Layer](#1-data-layer-data-models)
  - [UI Layer - Auth](#2-ui-layer---authentication)
  - [UI Layer - Main](#3-ui-layer---main)
  - [UI Layer - Task](#4-ui-layer---task-management)
  - [UI Layer - Profile](#5-ui-layer---profile)
- [Konfigurasi & Build](#-konfigurasi--build)
- [Resources (Res)](#-resources-res)
- [Navigasi & Flow Aplikasi](#-navigasi--flow-aplikasi)
- [Firebase Structure](#-firebase-structure)
- [Rencana Pengembangan](#-rencana-pengembangan)
- [Cara Menjalankan](#-cara-menjalankan)

---

## ✨ Fitur Utama

| Fitur | Status | Deskripsi |
|-------|--------|-----------|
| ✅ **Autentikasi** | Selesai | Login & Register dengan Firebase Auth |
| ✅ **Task Management** | Selesai | Buat, edit, hapus, dan tandai task selesai |
| ✅ **Progress Tracking** | Selesai | Visualisasi persentase task yang selesai |
| ✅ **Profile Management** | Selesai | Edit profil pengguna |
| ✅ **Real-time Sync** | Selesai | Data tersinkronisasi real-time dengan Firestore |
| ✅ **Splash Screen** | Selesai | Splash screen dengan auth check |
| 🔲 **Pomodoro Timer** | Rencana | Timer fokus untuk produktivitas |
| 🔲 **Score Counter** | Rencana | Gamifikasi produktivitas |
| 🔲 **Feed Sharing** | Rencana | Berbagi progress dengan komunitas |

---

## 🛠 Tech Stack

### **Bahasa & Framework**
| Teknologi | Versi | Deskripsi |
|-----------|-------|-----------|
| **Kotlin** | 2.0.21 | Bahasa pemrograman utama |
| **Android SDK** | 36 (Target & Compile) | SDK Android |
| **Min SDK** | 33 (Android 13+) | Minimum Android yang didukung |

### **Libraries & Dependencies**
| Library | Versi | Fungsi |
|---------|-------|--------|
| **Firebase Auth** | BOM 32.7.0 | Autentikasi pengguna |
| **Firebase Firestore** | BOM 32.7.0 | Database NoSQL real-time |
| **AndroidX Core KTX** | 1.17.0 | Ekstensi Kotlin untuk Android |
| **AndroidX AppCompat** | 1.7.1 | Backward compatibility |
| **Material Design 3** | 1.13.0 | Komponen UI Material |
| **Navigation Component** | 2.9.6 | Navigasi antar fragment |
| **Splash Screen API** | 1.0.1 | Modern splash screen |
| **ConstraintLayout** | 2.2.1 | Flexible layout system |
| **Fragment KTX** | 1.8.9 | Ekstensi Fragment |

### **Build System**
- **Gradle** dengan Kotlin DSL
- **Version Catalog** (`libs.versions.toml`) untuk dependency management

---

## 🏗 Arsitektur Aplikasi

```
┌─────────────────────────────────────────────────────────────┐
│                        USER INTERFACE                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ AuthActivity│  │MainActivity │  │  ProfileActivity    │  │
│  │  (Fragment) │  │  (Fragment) │  │    (Fragment)       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│         │                │                    │              │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌─────────▼──────────┐   │
│  │  Greeting   │  │    Home     │  │     Profile        │   │
│  │   Login     │  │ CreateTask  │  │   EditProfile      │   │
│  │  Register   │  │  EditTask   │  │                    │   │
│  └─────────────┘  └─────────────┘  └────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │  DATA LAYER   │
                    │  Task.kt      │
                    │  User.kt      │
                    └───────┬───────┘
                            │
               ┌────────────▼────────────┐
               │      FIREBASE           │
               │  ┌──────────────────┐   │
               │  │ Firebase Auth    │   │
               │  │ (Authentication) │   │
               │  └──────────────────┘   │
               │  ┌──────────────────┐   │
               │  │ Cloud Firestore  │   │
               │  │ (Database)       │   │
               │  └──────────────────┘   │
               └─────────────────────────┘
```

### **Pattern yang Digunakan:**
- **Single Activity per Flow** - AuthActivity untuk auth flow, MainActivity untuk main flow
- **Fragment-based Navigation** - Menggunakan Navigation Component
- **View Binding** - Type-safe view access
- **Data Class** - Kotlin data classes untuk model
- **Real-time Listeners** - Firestore snapshot listeners

---

## 📁 Struktur Project

```
Niyyah-Android/
│
├── 📄 build.gradle.kts              # Root build configuration
├── 📄 settings.gradle.kts           # Project settings
├── 📄 gradle.properties             # Gradle properties
├── 📄 local.properties              # Local SDK configuration
│
├── 📁 gradle/
│   ├── 📄 libs.versions.toml        # Centralized dependency versions
│   └── 📁 wrapper/
│       └── 📄 gradle-wrapper.properties
│
└── 📁 app/
    ├── 📄 build.gradle.kts          # App module build config
    ├── 📄 google-services.json      # Firebase configuration
    ├── 📄 proguard-rules.pro        # ProGuard rules
    │
    └── 📁 src/main/
        ├── 📄 AndroidManifest.xml   # App manifest
        │
        ├── 📁 java/com/pab/niyyah/
        │   │
        │   ├── 📁 data/             # Data Models
        │   │   ├── 📄 Task.kt       # Task entity
        │   │   └── 📄 User.kt       # User entity
        │   │
        │   ├── 📁 ui/               # User Interface
        │   │   ├── 📁 auth/         # Authentication screens
        │   │   │   ├── 📄 AuthActivity.kt
        │   │   │   ├── 📄 GreetingFragment.kt
        │   │   │   ├── 📄 LoginFragment.kt
        │   │   │   └── 📄 RegisterFragment.kt
        │   │   │
        │   │   ├── 📁 main/         # Main app screens
        │   │   │   ├── 📄 MainActivity.kt
        │   │   │   ├── 📄 HomeFragment.kt
        │   │   │   └── 📄 TaskAdapter.kt
        │   │   │
        │   │   ├── 📁 profile/      # Profile screens
        │   │   │   ├── 📄 ProfileActivity.kt
        │   │   │   ├── 📄 ProfileFragment.kt
        │   │   │   └── 📄 EditProfileFragment.kt
        │   │   │
        │   │   └── 📁 task/         # Task management
        │   │       ├── 📄 CreateTaskFragment.kt
        │   │       └── 📄 EditTaskFragment.kt
        │   │
        │   └── 📁 utils/            # Utility classes (kosong)
        │
        └── 📁 res/                  # Resources
            ├── 📁 drawable/         # Icons & shapes
            ├── 📁 font/             # Custom fonts
            ├── 📁 layout/           # XML layouts
            ├── 📁 mipmap-*/         # App icons
            ├── 📁 navigation/       # Navigation graphs
            ├── 📁 values/           # Colors, strings, themes
            ├── 📁 values-night/     # Dark theme (jika ada)
            └── 📁 xml/              # Other XML configs
```

---

## 📖 Penjelasan Detail Setiap File

### **1. Data Layer (Data Models)**

#### 📄 `data/Task.kt`
```kotlin
data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val details: String = "",
    val dueDate: String = "",
    val time: String = "",
    val repeat: String = "Never",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0
)
```

**Fungsi:** Model data untuk task/tugas pengguna.

| Property | Type | Deskripsi |
|----------|------|-----------|
| `id` | String | ID unik dari Firestore document |
| `userId` | String | ID pengguna pemilik task |
| `title` | String | Judul task |
| `details` | String | Deskripsi/detail task |
| `dueDate` | String | Tanggal deadline (format: dd/MM/yyyy) |
| `time` | String | Waktu deadline (format: hh:mm a) |
| `repeat` | String | Frekuensi pengulangan (Never/Daily/Weekly/Monthly) |
| `isCompleted` | Boolean | Status penyelesaian task |
| `createdAt` | Long | Timestamp pembuatan (untuk sorting) |

**Digunakan oleh:**
- `HomeFragment.kt` - Menampilkan daftar task
- `CreateTaskFragment.kt` - Membuat task baru
- `EditTaskFragment.kt` - Mengedit task
- `TaskAdapter.kt` - Render item task di RecyclerView

---

#### 📄 `data/User.kt`
```kotlin
data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val username: String = "",
    val gender: String = "",
    val nationality: String = "",
    val birthDate: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}
```

**Fungsi:** Model data untuk profil pengguna.

| Property | Type | Deskripsi |
|----------|------|-----------|
| `firstName` | String | Nama depan |
| `lastName` | String | Nama belakang |
| `email` | String | Alamat email (dari Firebase Auth) |
| `username` | String | Username tampilan |
| `gender` | String | Jenis kelamin |
| `nationality` | String | Kebangsaan |
| `birthDate` | String | Tanggal lahir |
| `phoneNumber` | String | Nomor telepon |
| `photoUrl` | String | URL foto profil |
| `fullName` | String (computed) | Gabungan nama depan + belakang |

**Digunakan oleh:**
- `RegisterFragment.kt` - Saat registrasi user baru
- `ProfileFragment.kt` - Menampilkan profil
- `EditProfileFragment.kt` - Mengedit profil
- `HomeFragment.kt` - Menampilkan greeting dengan nama

---

### **2. UI Layer - Authentication**

#### 📄 `ui/auth/AuthActivity.kt`
```kotlin
class AuthActivity : AppCompatActivity()
```

**Fungsi:** Activity container untuk semua fragment autentikasi.

**Fitur:**
- Menggunakan `EdgeToEdge` untuk tampilan layar penuh
- Host untuk Navigation Component (`nav_auth.xml`)
- Window soft input mode: `adjustResize`

**Fragment yang di-host:**
- `GreetingFragment`
- `LoginFragment`
- `RegisterFragment`

**Flow:**
```
AuthActivity
    └── nav_auth.xml
        ├── GreetingFragment (start)
        ├── LoginFragment
        └── RegisterFragment
```

---

#### 📄 `ui/auth/GreetingFragment.kt`
```kotlin
class GreetingFragment : Fragment()
```

**Fungsi:** Layar selamat datang (onboarding pertama).

**Fitur:**
- Menampilkan logo dan tagline aplikasi
- Tombol "Get Started" untuk navigasi ke login
- View Binding untuk akses view

**Layout:** `fragment_greeting.xml`

**Navigasi:**
- "Get Started" → `LoginFragment`

**Komponen UI:**
| Komponen | ID | Fungsi |
|----------|-----|--------|
| Button | `btnGetStarted` | Navigasi ke login |

---

#### 📄 `ui/auth/LoginFragment.kt`
```kotlin
class LoginFragment : Fragment()
```

**Fungsi:** Form login pengguna dengan Firebase Authentication.

**Fitur:**
- Input email dan password
- Toggle visibility password
- Validasi input (email format, password min 6 karakter)
- Loading state saat proses login
- Error handling dengan pesan spesifik
- Navigasi ke RegisterFragment jika belum punya akun

**Dependencies:**
- `FirebaseAuth` - Untuk autentikasi

**Validasi:**
| Field | Rule |
|-------|------|
| Email | Required, valid email format |
| Password | Required, min 6 karakter |

**Error Messages:**
| Firebase Error | User Message |
|----------------|--------------|
| `no user record` | "Email tidak terdaftar" |
| `password is invalid` | "Password salah" |
| `network` | "Tidak ada koneksi internet" |

**Layout:** `fragment_login.xml`

**Navigasi:**
- Success → `MainActivity`
- "Sign Up" → `RegisterFragment`

---

#### 📄 `ui/auth/RegisterFragment.kt`
```kotlin
class RegisterFragment : Fragment()
```

**Fungsi:** Form registrasi pengguna baru.

**Fitur:**
- Input nama depan, nama belakang, email, password, konfirmasi password
- Toggle visibility untuk kedua field password
- Validasi lengkap semua field
- Registrasi dengan Firebase Auth
- Simpan data profil ke Firestore
- Error handling komprehensif

**Dependencies:**
- `FirebaseAuth` - Untuk membuat akun
- `FirebaseFirestore` - Untuk menyimpan profil user

**Validasi:**
| Field | Rule |
|-------|------|
| First Name | Required |
| Last Name | Required |
| Email | Required, valid format |
| Password | Required, min 6 karakter |
| Confirm Password | Required, harus sama dengan password |

**Firestore Document Structure:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "username": "",
  "photoUrl": "",
  "gender": "",
  "nationality": "",
  "birthDate": "",
  "phoneNumber": ""
}
```

**Layout:** `fragment_register.xml`

**Navigasi:**
- Success → `MainActivity`
- "Sign In" → `LoginFragment`

---

### **3. UI Layer - Main**

#### 📄 `ui/main/MainActivity.kt`
```kotlin
class MainActivity : AppCompatActivity()
```

**Fungsi:** Activity utama aplikasi setelah login.

**Fitur:**
- **Splash Screen Integration** - Menggunakan AndroidX Splash Screen API
- **Auth Check** - Cek status login saat app dibuka
- Auto-redirect ke `AuthActivity` jika belum login
- Host untuk Navigation Component (`nav_main.xml`)

**Flow Logic:**
```
App Start
    │
    ├── Splash Screen ditampilkan
    │
    ├── Cek FirebaseAuth.currentUser
    │   │
    │   ├── null → Redirect ke AuthActivity
    │   │
    │   └── not null → Lanjut ke HomeFragment
    │
    └── Splash Screen hilang
```

**Dependencies:**
- `FirebaseAuth` - Untuk cek status login

**Layout:** `activity_main.xml`

---

#### 📄 `ui/main/HomeFragment.kt`
```kotlin
class HomeFragment : Fragment()
```

**Fungsi:** Layar utama yang menampilkan daftar task dan progress.

**Fitur:**
- Greeting dengan nama user
- Progress circle menampilkan persentase task selesai
- RecyclerView dengan daftar task
- Real-time listener untuk update otomatis
- FAB untuk menambah task baru
- Avatar clickable ke profil

**Dependencies:**
- `FirebaseAuth` - Mendapatkan user ID
- `FirebaseFirestore` - Mengambil dan mendengarkan data task
- `TaskAdapter` - Adapter untuk RecyclerView

**Real-time Data Flow:**
```
Firestore "tasks" Collection
         │
         │ addSnapshotListener()
         ▼
    onChange detected
         │
         ├── Map to Task objects
         ├── Sort: ongoing first, then by createdAt desc
         ├── Update RecyclerView
         └── Update progress percentage
```

**Layout:** `fragment_home.xml`

**Navigasi:**
- FAB (+) → `CreateTaskFragment`
- Task item click → `EditTaskFragment`
- Avatar click → `ProfileFragment`

**Komponen UI:**
| Komponen | ID | Fungsi |
|----------|-----|--------|
| ImageView | `ivAvatar` | Navigasi ke profil |
| TextView | `tvGreeting` | Menampilkan "Hello, {name}!" |
| ProgressBar | `progressBar` | Visual progress circle |
| TextView | `tvProgressPercent` | Persentase angka |
| RecyclerView | `rvOngoingTasks` | Daftar task |
| FAB | `fabAddTask` | Tambah task baru |

---

#### 📄 `ui/main/TaskAdapter.kt`
```kotlin
class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckboxClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback())
```

**Fungsi:** RecyclerView Adapter untuk menampilkan daftar task.

**Fitur:**
- Extends `ListAdapter` dengan `DiffUtil` untuk efficient updates
- Menampilkan judul, deskripsi, waktu task
- Checkbox untuk toggle status completed
- Click listener untuk edit task

**ViewHolder Binding:**
| View | Data |
|------|------|
| `tvTaskTitle` | `task.title` |
| `tvTaskTime` | `"{dueDate}, {time}"` |
| `tvTaskDescription` | `task.details` (hidden jika kosong) |
| `cbTask` | `task.isCompleted` |

**DiffUtil Implementation:**
- `areItemsTheSame`: Bandingkan `id`
- `areContentsTheSame`: Bandingkan seluruh object
- `getChangePayload`: Optimize untuk perubahan `isCompleted` saja

**Layout Item:** `item_task.xml`

---

### **4. UI Layer - Task Management**

#### 📄 `ui/task/CreateTaskFragment.kt`
```kotlin
class CreateTaskFragment : Fragment()
```

**Fungsi:** Form untuk membuat task baru.

**Fitur:**
- Input judul task (required)
- Input detail/deskripsi (optional)
- Date picker untuk due date
- Time picker untuk waktu
- Spinner untuk repeat options (Never/Daily/Weekly/Monthly)
- Loading state saat menyimpan

**Dependencies:**
- `FirebaseAuth` - Mendapatkan user ID
- `FirebaseFirestore` - Menyimpan task baru

**Date/Time Pickers:**
- `DatePickerDialog` - Format: dd/MM/yyyy
- `TimePickerDialog` - Format: hh:mm a (12 hour)

**Firestore Document Structure:**
```json
{
  "title": "Task Title",
  "details": "Task description",
  "dueDate": "14/01/2026",
  "time": "09:30 AM",
  "repeat": "Never",
  "isCompleted": false,
  "userId": "firebase-user-id",
  "createdAt": 1736841600000
}
```

**Layout:** `fragment_create_task.xml`

**Navigasi:**
- Back button / Success → `navigateUp()` ke HomeFragment

---

#### 📄 `ui/task/EditTaskFragment.kt`
```kotlin
class EditTaskFragment : Fragment()
```

**Fungsi:** Form untuk mengedit atau menghapus task yang sudah ada.

**Fitur:**
- Menerima `taskId` via navigation arguments
- Load data task existing dari Firestore
- Edit semua field task
- Date picker dan time picker
- Spinner repeat options
- Tombol Save untuk update
- Tombol Delete dengan konfirmasi dialog

**Dependencies:**
- `FirebaseAuth` - Validasi user
- `FirebaseFirestore` - CRUD operations

**Navigation Arguments:**
```xml
<argument
    android:name="taskId"
    app:argType="string" />
```

**Operations:**
| Operation | Firestore Method |
|-----------|-----------------|
| Load | `document(id).get()` |
| Update | `document(id).update(data)` |
| Delete | `document(id).delete()` |

**Layout:** `fragment_edit_task.xml`

**Navigasi:**
- Back / Save / Delete success → `navigateUp()`

---

### **5. UI Layer - Profile**

#### 📄 `ui/profile/ProfileActivity.kt`
```kotlin
class ProfileActivity : AppCompatActivity()
```

**Fungsi:** Activity container untuk profile flow.

**Catatan:** Activity ini tidak digunakan dalam navigasi utama karena profile fragments di-host di `MainActivity`. Mungkin untuk pengembangan terpisah di masa depan.

---

#### 📄 `ui/profile/ProfileFragment.kt`
```kotlin
class ProfileFragment : Fragment()
```

**Fungsi:** Menampilkan profil pengguna dan opsi logout.

**Fitur:**
- Load dan tampilkan data profil dari Firestore
- Menampilkan nama lengkap dan username
- Tombol Edit untuk navigasi ke EditProfileFragment
- Tombol Logout dengan konfirmasi dialog
- Clear session dan redirect ke AuthActivity saat logout

**Dependencies:**
- `FirebaseAuth` - Get user ID, sign out
- `FirebaseFirestore` - Ambil data profil

**Layout:** `fragment_profile.xml`

**Navigasi:**
- Back → `navigateUp()`
- Edit → `EditProfileFragment`
- Logout → `AuthActivity` (clear task)

---

#### 📄 `ui/profile/EditProfileFragment.kt`
```kotlin
class EditProfileFragment : Fragment()
```

**Fungsi:** Form untuk mengedit profil pengguna.

**Fitur:**
- Load data profil existing
- Edit fields: First Name, Last Name, Username, Gender, Nationality, Birth Date, Phone
- Date picker untuk tanggal lahir
- Validasi nama depan (required)
- Save dengan merge (tidak overwrite field yang tidak diedit)

**Dependencies:**
- `FirebaseAuth` - Get user ID
- `FirebaseFirestore` - Update profil

**Firestore Operation:**
```kotlin
db.collection("users").document(userId)
    .set(userData, SetOptions.merge())
```

**Layout:** `fragment_edit_profile.xml`

**Navigasi:**
- Back / Save success → `navigateUp()`

---

## ⚙️ Konfigurasi & Build

### 📄 `build.gradle.kts` (Root)
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}
```

**Fungsi:** Konfigurasi build level project.

**Plugins:**
- Android Gradle Plugin
- Kotlin Android Plugin
- Google Services (Firebase)

---

### 📄 `app/build.gradle.kts`
**Fungsi:** Konfigurasi build level app module.

**Konfigurasi Penting:**
```kotlin
android {
    namespace = "com.pab.niyyah"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.pab.niyyah"
        minSdk = 33          // Android 13+
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        viewBinding = true   // Type-safe view access
    }
}
```

**Dependencies Explained:**
| Dependency | Fungsi |
|------------|--------|
| `core-splashscreen` | Modern splash screen API |
| `fragment-ktx` | Kotlin extensions untuk Fragment |
| `firebase-auth-ktx` | Firebase Authentication |
| `firebase-firestore-ktx` | Cloud Firestore database |
| `navigation-*` | Jetpack Navigation Component |
| `material` | Material Design 3 components |

---

### 📄 `gradle/libs.versions.toml`
**Fungsi:** Version catalog untuk centralized dependency management.

**Sections:**
- `[versions]` - Versi library
- `[libraries]` - Deklarasi library
- `[plugins]` - Deklarasi plugin

---

### 📄 `AndroidManifest.xml`
**Fungsi:** Manifest aplikasi Android.

**Permissions:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Activities:**
| Activity | Exported | Theme | Launcher |
|----------|----------|-------|----------|
| `MainActivity` | true | Splash → Base | ✅ |
| `AuthActivity` | false | Base | - |
| `ProfileActivity` | false | Base | - |

---

## 🎨 Resources (Res)

### 📁 `res/layout/` - Layout XML Files

| File | Tipe | Fungsi |
|------|------|--------|
| `activity_auth.xml` | Activity | Container untuk auth fragments |
| `activity_main.xml` | Activity | Container untuk main fragments |
| `activity_profile.xml` | Activity | Container untuk profile (unused) |
| `fragment_greeting.xml` | Fragment | Welcome/onboarding screen |
| `fragment_login.xml` | Fragment | Form login |
| `fragment_register.xml` | Fragment | Form registrasi |
| `fragment_home.xml` | Fragment | Dashboard utama |
| `fragment_create_task.xml` | Fragment | Form buat task |
| `fragment_edit_task.xml` | Fragment | Form edit task |
| `fragment_profile.xml` | Fragment | View profil user |
| `fragment_edit_profile.xml` | Fragment | Form edit profil |
| `item_task.xml` | Item | RecyclerView item untuk task |

---

### 📁 `res/navigation/` - Navigation Graphs

#### `nav_auth.xml`
```
greetingFragment (start)
    │
    └──► loginFragment
            │
            └──► registerFragment
                    │
                    └──► loginFragment (back)
```

#### `nav_main.xml`
```
homeFragment (start)
    │
    ├──► createTaskFragment
    │
    ├──► editTaskFragment (with taskId arg)
    │
    └──► profileFragment
            │
            └──► editProfileFragment
```

---

### 📁 `res/drawable/` - Icons & Shapes

| File | Tipe | Fungsi |
|------|------|--------|
| `bg_button_*.xml` | Shape | Background untuk berbagai button |
| `bg_card_rounded.xml` | Shape | Background card dengan corner radius |
| `bg_edittext_border.xml` | Shape | Border untuk EditText |
| `bg_fab_purple.xml` | Shape | Background FAB |
| `bg_progress_circle.xml` | Shape | Progress bar circular |
| `bg_purple_rounded_*.xml` | Shape | Header backgrounds |
| `bg_white_rounded*.xml` | Shape | White card backgrounds |
| `ic_add.xml` | Vector | Icon tambah (+) |
| `ic_avatar_placeholder.xml` | Vector | Default avatar |
| `ic_back_arrow.xml` | Vector | Icon back |
| `ic_calendar.xml` | Vector | Icon kalender |
| `ic_checkbox_*.xml` | Vector | Checkbox states |
| `ic_check_white.xml` | Vector | Checkmark putih |
| `ic_clock.xml` | Vector | Icon jam |
| `ic_delete.xml` | Vector | Icon hapus |
| `ic_dropdown.xml` | Vector | Dropdown arrow |
| `ic_email.xml` | Vector | Icon email |
| `ic_lock.xml` | Vector | Icon password |
| `ic_niyyah_*.png` | PNG | Logo aplikasi |
| `ic_ring.xml` | Vector | Icon notifikasi |
| `ic_splash_logo.xml` | Vector | Logo untuk splash |
| `ic_visibility*.xml` | Vector | Toggle password visibility |

---

### 📁 `res/values/`

#### `colors.xml`
| Color | Hex | Penggunaan |
|-------|-----|------------|
| `purpleD` | #FF541D72 | Primary/brand color |
| `purpleL` | #FFF8ECFF | Light purple background |
| `purpleA` | #FF8B5FA3 | Accent purple |
| `greyD` | #FFA7A4A8 | Dark grey text |
| `greyL` | #FFDBDBDB | Light grey |
| `greyMD` | #FFA29DA4 | Medium grey |
| `red` | #FFCF2629 | Logout/delete button |
| `greenSuccess` | #FF4CAF50 | Success state |

#### `themes.xml`
- `Base.Theme.Niyyah` - Base app theme (Material3 NoActionBar)
- `Theme.App.Starting` - Splash screen theme
- `buttonA` - Primary button style
- `buttonLogout` - Red logout button style
- `buttonEdit` - Small edit button style

---

## 🔄 Navigasi & Flow Aplikasi

```
┌──────────────────────────────────────────────────────────────────┐
│                        APPLICATION FLOW                           │
└──────────────────────────────────────────────────────────────────┘

    ┌─────────┐
    │  Start  │
    └────┬────┘
         │
         ▼
    ┌─────────────┐     No      ┌───────────────────────────────────┐
    │ User logged │────────────►│           AUTH FLOW               │
    │    in?      │             │  ┌─────────────────────────────┐  │
    └──────┬──────┘             │  │      GreetingFragment       │  │
           │ Yes                │  │   "Let's build momentum"    │  │
           │                    │  └────────────┬────────────────┘  │
           │                    │               │                   │
           │                    │               ▼                   │
           │                    │  ┌─────────────────────────────┐  │
           │                    │  │       LoginFragment         │◄─┼──┐
           │                    │  │    Email + Password         │  │  │
           │                    │  └────────────┬────────────────┘  │  │
           │                    │               │                   │  │
           │                    │    ┌──────────┴──────────┐        │  │
           │                    │    │                     │        │  │
           │                    │    ▼                     ▼        │  │
           │                    │ [Success]         [Sign Up]       │  │
           │                    │    │                     │        │  │
           │                    │    │                     ▼        │  │
           │                    │    │    ┌───────────────────────┐ │  │
           │                    │    │    │   RegisterFragment    │ │  │
           │                    │    │    │  Name, Email, Pass    │ │  │
           │                    │    │    └───────────┬───────────┘ │  │
           │                    │    │                │             │  │
           │                    │    │     ┌──────────┴──────────┐  │  │
           │                    │    │     │                     │  │  │
           │                    │    │     ▼                     ▼  │  │
           │                    │    │  [Success]           [Sign In]──┘
           │                    │    │     │                        │
           │                    └────┼─────┼────────────────────────┘
           │                         │     │
           │◄────────────────────────┴─────┘
           │
           ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │                          MAIN FLOW                              │
    │                                                                 │
    │  ┌────────────────────────────────────────────────────────────┐ │
    │  │                    HomeFragment                             │ │
    │  │  ┌──────────┐  ┌────────────────┐  ┌────────────────────┐  │ │
    │  │  │ Avatar   │  │   Progress     │  │     Task List      │  │ │
    │  │  │ (click)  │  │    Circle      │  │  (RecyclerView)    │  │ │
    │  │  └────┬─────┘  └────────────────┘  └─────────┬──────────┘  │ │
    │  │       │                                      │              │ │
    │  │       │                              ┌───────┴───────┐      │ │
    │  │       │                              │               │      │ │
    │  │       │                          [Checkbox]     [Card]      │ │
    │  │       │                              │               │      │ │
    │  │       │                              ▼               ▼      │ │
    │  │       │                      [Toggle Status]  ┌───────────┐ │ │
    │  │       │                                       │EditTask   │ │ │
    │  │       │                                       │Fragment   │ │ │
    │  │       │                                       └───────────┘ │ │
    │  │       │                                                     │ │
    │  │       │     ┌────────────────────────────────────────────┐  │ │
    │  │       │     │                   [FAB +]                   │  │ │
    │  │       │     └─────────────────────┬──────────────────────┘  │ │
    │  │       │                           │                         │ │
    │  │       │                           ▼                         │ │
    │  │       │                   ┌────────────────┐                │ │
    │  │       │                   │ CreateTask     │                │ │
    │  │       │                   │ Fragment       │                │ │
    │  │       │                   └────────────────┘                │ │
    │  │       │                                                     │ │
    │  └───────┼─────────────────────────────────────────────────────┘ │
    │          │                                                       │
    │          ▼                                                       │
    │  ┌────────────────────────────────────────────────────────────┐  │
    │  │                    PROFILE FLOW                             │  │
    │  │                                                             │  │
    │  │  ┌─────────────────┐       ┌─────────────────┐             │  │
    │  │  │ ProfileFragment │──────►│EditProfileFrag  │             │  │
    │  │  │   Name, @user   │ Edit  │  All fields     │             │  │
    │  │  └────────┬────────┘       └─────────────────┘             │  │
    │  │           │                                                 │  │
    │  │           │ Logout                                          │  │
    │  │           ▼                                                 │  │
    │  │    ┌─────────────┐                                          │  │
    │  │    │  Confirm?   │                                          │  │
    │  │    └──────┬──────┘                                          │  │
    │  │           │ Yes                                             │  │
    │  └───────────┼─────────────────────────────────────────────────┘  │
    │              │                                                    │
    └──────────────┼────────────────────────────────────────────────────┘
                   │
                   ▼
            [Auth Flow] (Firebase signOut → AuthActivity)
```

---

## 🔥 Firebase Structure

### **Authentication**
```
Firebase Auth
    └── Users
        └── uid: "abc123xyz..."
            ├── email: "user@example.com"
            └── (password managed by Firebase)
```

### **Firestore Database**
```
Firestore
    │
    ├── users (Collection)
    │   └── {userId} (Document)
    │       ├── firstName: "John"
    │       ├── lastName: "Doe"
    │       ├── email: "john@example.com"
    │       ├── username: "johndoe"
    │       ├── gender: "Male"
    │       ├── nationality: "Indonesia"
    │       ├── birthDate: "01/01/1990"
    │       ├── phoneNumber: "+628123456789"
    │       └── photoUrl: ""
    │
    └── tasks (Collection)
        └── {taskId} (Document)
            ├── title: "Complete homework"
            ├── details: "Math chapter 5"
            ├── dueDate: "15/01/2026"
            ├── time: "09:00 AM"
            ├── repeat: "Never"
            ├── isCompleted: false
            ├── userId: "abc123xyz..."
            └── createdAt: 1736841600000
```

### **Security Rules (Recommended)**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only read/write their own profile
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Users can only access their own tasks
    match /tasks/{taskId} {
      allow read, write: if request.auth != null 
                         && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null 
                    && request.resource.data.userId == request.auth.uid;
    }
  }
}
```

---

## 🚀 Rencana Pengembangan

### **Fase 1 - Core Features** ✅ (Selesai)
- [x] Autentikasi (Login/Register)
- [x] CRUD Task
- [x] Progress tracking
- [x] Profile management
- [x] Splash screen
- [x] Real-time sync dengan Firestore

### **Fase 2 - Productivity Features** 🔲 (Planned)
- [ ] **Pomodoro Timer**
  - Timer fokus 25 menit
  - Break timer 5 menit
  - Long break 15 menit
  - Session tracking
  
- [ ] **Score/Points System**
  - Points untuk task selesai
  - Streak harian
  - Achievements/badges
  - Leaderboard

### **Fase 3 - Social Features** 🔲 (Future)
- [ ] **Feed Sharing**
  - Share progress ke feed
  - Like dan komentar
  - Follow pengguna lain
  - Motivational quotes

### **Fase 4 - Advanced Features** 🔲 (Future)
- [ ] Task categories/labels
- [ ] Task prioritas
- [ ] Notifications/reminders
- [ ] Calendar view
- [ ] Widget home screen
- [ ] Dark mode
- [ ] Export data
- [ ] Multi-language support

---

## 🏃 Cara Menjalankan

### **Prerequisites**
1. Android Studio (Ladybug | 2024.2.1+)
2. JDK 11
3. Android SDK 36
4. Device/Emulator dengan Android 13+ (API 33+)

### **Setup**
1. Clone repository
   ```bash
   git clone https://github.com/Andrian206/Niyyah-Android.git
   ```

2. Buka project di Android Studio

3. Sync Gradle

4. **Firebase Setup** (PENTING):
   - Buat project Firebase baru di [Firebase Console](https://console.firebase.google.com)
   - Aktifkan Authentication (Email/Password & Google Sign-In)
   - Aktifkan Cloud Firestore
   - Download `google-services.json` dari Firebase Console
   - Copy file ke folder `app/` (file ini TIDAK disertakan di repo untuk keamanan)
   - Gunakan `app/google-services.json.example` sebagai referensi struktur

5. Run aplikasi
   - Pilih device/emulator
   - Klik Run ▶️

### **Build APK**
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📝 Catatan Tambahan

### **Best Practices yang Digunakan**
- ✅ View Binding (bukan findViewById)
- ✅ Null safety dengan Kotlin
- ✅ Lifecycle-aware components
- ✅ Navigation Component untuk navigasi
- ✅ Data classes untuk model
- ✅ ListAdapter dengan DiffUtil
- ✅ Real-time listeners dengan cleanup
- ✅ Loading states untuk async operations
- ✅ Error handling dengan user-friendly messages

### **Potential Improvements**
- Implementasi ViewModel + LiveData/StateFlow
- Repository pattern untuk data layer
- Dependency Injection (Hilt/Koin)
- Unit tests dan UI tests
- Offline support dengan Room
- Image upload untuk avatar

---

## 👨‍💻 Author

**Andrian** - [GitHub](https://github.com/Andrian206)

---

## 📄 License

Project ini dibuat untuk keperluan pembelajaran mata kuliah PAB (Pengembangan Aplikasi Bergerak).

---

<p align="center">
  <b>Niyyah</b> - Build momentum, one small win at a time. 🚀
</p>