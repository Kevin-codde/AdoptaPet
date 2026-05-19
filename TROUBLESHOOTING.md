# AdoptaPet — Guía de Integración y Resolución de Problemas

## Pasos para compilar desde cero en Android Studio

### 1. Crear el proyecto en Android Studio
- File → New → New Project → "Empty Views Activity"
- Package name: `com.adoptapet.app`
- Language: Kotlin | Min SDK: API 26

### 2. Reemplazar archivos generados
Sobreescribe los archivos con los del proyecto AdoptaPet en este orden:
1. `settings.gradle`
2. `build.gradle` (raíz)
3. `app/build.gradle`
4. `app/src/main/AndroidManifest.xml`
5. Todos los archivos de `res/values/`
6. Todos los archivos Kotlin en `java/com/adoptapet/app/`
7. Todos los layouts XML
8. Archivos de navegación, menú y animación

### 3. Configurar Firebase
```
1. Crear proyecto en https://console.firebase.google.com/
2. Agregar app Android con package: com.adoptapet.app
3. Descargar google-services.json → colocar en app/
4. Habilitar Email/Password en Authentication
5. Crear Firestore Database en modo producción
6. Crear Storage bucket
7. Desplegar reglas: firebase deploy --only firestore:rules,storage
```

### 4. Sincronizar Gradle
- Toolbar → "Sync Project with Gradle Files" (ícono de elefante)
- Esperar a que descargue todas las dependencias

---

## Resolución de errores comunes

### ❌ "Unresolved reference: HomeFragmentDirections"
**Causa:** Safe Args no ha generado las clases aún.
**Solución:**
1. Verificar que `id 'androidx.navigation.safeargs.kotlin'` está en `app/build.gradle`
2. Verificar que el classpath está en el `build.gradle` raíz
3. Build → Clean Project → Rebuild Project

---

### ❌ "Cannot find google-services.json"
**Causa:** El archivo no está en `app/`
**Solución:** Descargar de Firebase Console y colocarlo en `app/google-services.json`

---

### ❌ "FIRESTORE_INSTANCE not initialized"
**Causa:** `AdoptaPetApplication` no está registrado en el Manifest.
**Solución:** Verificar que el Manifest tiene:
```xml
android:name=".AdoptaPetApplication"
```

---

### ❌ "Room schema export directory is not provided"
**Causa:** Falta la configuración kapt en `app/build.gradle`.
**Solución:** Agregar dentro de `android { defaultConfig { ... } }`:
```groovy
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
```

---

### ❌ Tests fallan con "Mockito cannot mock final class"
**Causa:** Por defecto Mockito no puede mockear clases `final` de Kotlin.
**Solución:** Crear el archivo:
`app/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
Con el contenido:
```
mock-maker-inline
```

---

### ❌ "ViewBinding not found for fragment_xxx"
**Causa:** `buildFeatures { viewBinding true }` no está activo o Gradle no sincronizó.
**Solución:**
1. Verificar `buildFeatures { viewBinding true }` en `app/build.gradle`
2. Build → Clean Project → Make Project

---

### ❌ "Permission denied" al subir imagen a Storage
**Causa:** Las reglas de Storage no están desplegadas o el usuario no está autenticado.
**Solución:**
1. Verificar que el usuario está autenticado antes de publicar
2. Desplegar reglas: `firebase deploy --only storage`
3. En desarrollo: cambiar temporalmente las reglas a `allow write: if true;`

---

## Permisos de imagen en Android 13+

El `AndroidManifest.xml` ya incluye el permiso correcto para cada versión:
```xml
<!-- Android 12 y anterior -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

En `PublishFragment.kt`, para Android 13+ puede ser necesario solicitar el permiso
en runtime antes de abrir la galería. Agregar:
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_PERMISSION)
} else {
    openGallery()
}
```

---

## Ejecutar las pruebas

```bash
# Pruebas unitarias (JVM)
./gradlew test

# Pruebas de instrumentación (requiere emulador/dispositivo)
./gradlew connectedAndroidTest

# Reporte HTML de cobertura
./gradlew test --info
# El reporte se genera en: app/build/reports/tests/
```

---

## Variables de entorno opcionales (CI/CD)

Para pipelines de CI, el `google-services.json` puede inyectarse como variable:
```bash
# Decodificar desde Base64 en CI
echo $GOOGLE_SERVICES_JSON | base64 --decode > app/google-services.json
```
