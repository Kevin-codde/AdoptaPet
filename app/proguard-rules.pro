# app/proguard-rules.pro
# Reglas ProGuard para AdoptaPet

# Mantener modelos de datos (Room + Firestore)
-keep class com.adoptapet.app.data.model.** { *; }

# Mantener DAOs de Room
-keep interface com.adoptapet.app.data.local.** { *; }

# Firebase / Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Coil
-keep class coil.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin data classes utilizadas como modelos Firestore (serialización)
-keepclassmembers class com.adoptapet.app.data.model.** {
    public <init>(...);
    public ** get*();
    public void set*(...);
}
