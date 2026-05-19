# AdoptaPet — Guía de Configuración Firebase

## 1. Crear proyecto en Firebase Console

1. Ve a https://console.firebase.google.com/
2. Crea un nuevo proyecto: **"AdoptaPet"**
3. Habilita **Google Analytics** (opcional)

---

## 2. Registrar la aplicación Android

1. En la consola Firebase → **Agregar app → Android**
2. Ingresa el nombre del paquete: `com.adoptapet.app`
3. Descarga el archivo **`google-services.json`**
4. Colócalo en: `app/google-services.json` (mismo nivel que `app/build.gradle`)

> ⚠️ **El archivo `google-services.json` no se incluye en el repositorio por seguridad.**
> Cada desarrollador debe descargar el suyo desde Firebase Console.

---

## 3. Habilitar servicios en Firebase Console

### Authentication
1. Ve a **Authentication → Sign-in method**
2. Habilita **Email/Password**

### Firestore Database
1. Ve a **Firestore Database → Crear base de datos**
2. Selecciona modo **Producción** (las reglas de seguridad están en `firestore.rules`)
3. Elige la región más cercana (ej: `us-central1`)

### Storage
1. Ve a **Storage → Comenzar**
2. Acepta las reglas por defecto (serán reemplazadas con `storage.rules`)

---

## 4. Desplegar reglas de seguridad

Instala Firebase CLI y ejecuta:

```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Autenticarse
firebase login

# Inicializar proyecto (selecciona Firestore y Storage)
firebase init

# Desplegar reglas
firebase deploy --only firestore:rules,storage
```

---

## 5. Estructura del proyecto

```
AdoptaPet/
├── app/
│   ├── build.gradle
│   ├── google-services.json          ← DESCARGAR desde Firebase Console
│   └── src/main/java/com/adoptapet/app/
│       ├── AdoptaPetApplication.kt
│       ├── data/
│       │   ├── local/
│       │   │   ├── AppDatabase.kt
│       │   │   └── PetDao.kt
│       │   ├── model/
│       │   │   ├── Pet.kt
│       │   │   └── User.kt
│       │   └── repository/
│       │       ├── AuthRepository.kt
│       │       └── PetRepository.kt
│       └── ui/
│           ├── adapter/
│           │   ├── MyPetAdapter.kt
│           │   └── PetAdapter.kt
│           ├── auth/
│           │   └── AuthActivity.kt
│           ├── contracts/
│           │   ├── AuthContract.kt
│           │   ├── HomeContract.kt
│           │   └── OtherContracts.kt  (Detail, Publish, Profile)
│           ├── detail/
│           │   └── DetailFragment.kt
│           ├── home/
│           │   └── HomeFragment.kt
│           ├── main/
│           │   └── MainActivity.kt
│           ├── presenter/
│           │   ├── AuthPresenter.kt
│           │   ├── DetailPresenter.kt
│           │   ├── HomePresenter.kt
│           │   ├── ProfilePresenter.kt
│           │   └── PublishPresenter.kt
│           ├── profile/
│           │   └── ProfileFragment.kt
│           └── publish/
│               └── PublishFragment.kt
├── build.gradle
├── firestore.rules
└── storage.rules
```

---

## 6. Íconos requeridos (Vector Assets)

Crea los siguientes íconos en Android Studio:
**File → New → Vector Asset → Clip Art**

| Archivo             | Material Icon     |
|---------------------|-------------------|
| `ic_home.xml`       | `home`            |
| `ic_add_circle.xml` | `add_circle`      |
| `ic_person.xml`     | `person`          |
| `ic_email.xml`      | `email`           |
| `ic_lock.xml`       | `lock`            |
| `ic_search.xml`     | `search`          |
| `ic_camera.xml`     | `photo_camera`    |
| `ic_calendar.xml`   | `calendar_today`  |
| `ic_phone.xml`      | `phone`           |

---

## 7. Navegación Safe Args — Generar código

El proyecto usa **Safe Args** para pasar el `petId` al `DetailFragment`.
Después de sincronizar Gradle, Android Studio genera automáticamente:
- `HomeFragmentDirections`
- `DetailFragmentArgs`

Si hay errores de compilación relacionados con Safe Args, agrega el plugin al `app/build.gradle`:

```groovy
plugins {
    // ... otros plugins
    id 'androidx.navigation.safeargs.kotlin'
}
```

Y en el `build.gradle` raíz:
```groovy
classpath 'androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6'
```

---

## 8. Ejecutar pruebas unitarias

```bash
./gradlew test
```

Las pruebas están en:
- `app/src/test/java/com/adoptapet/app/presenter/AuthPresenterTest.kt`
- `app/src/test/java/com/adoptapet/app/repository/PetRepositoryAndHomePresenterTest.kt`
