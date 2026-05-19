# AdoptaPet — Árbol Completo de Archivos del Proyecto

```
AdoptaPet/
│
├── build.gradle                              ← Proyecto raíz (classpath, plugins)
├── settings.gradle                           ← Módulos del proyecto
├── gradle.properties                         ← Propiedades globales de Gradle
├── firestore.rules                           ← Reglas de seguridad Firestore
├── storage.rules                             ← Reglas de seguridad Firebase Storage
├── SETUP_GUIDE.md                            ← Guía de configuración Firebase
│
└── app/
    ├── build.gradle                          ← Dependencias y configuración del módulo
    ├── proguard-rules.pro                    ← Reglas ProGuard
    │
    ├── google-services.json                  ← ⚠️ DESCARGAR desde Firebase Console
    │
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   │
        │   ├── java/com/adoptapet/app/
        │   │   │
        │   │   ├── AdoptaPetApplication.kt   ← Inicialización Firebase
        │   │   │
        │   │   ├── data/
        │   │   │   ├── local/
        │   │   │   │   ├── AppDatabase.kt    ← Singleton Room Database
        │   │   │   │   └── PetDao.kt         ← DAO con CRUD + Flow
        │   │   │   │
        │   │   │   ├── model/
        │   │   │   │   ├── Pet.kt            ← Entidad Room + modelo Firestore
        │   │   │   │   └── User.kt           ← Modelo de usuario
        │   │   │   │
        │   │   │   └── repository/
        │   │   │       ├── AuthRepository.kt ← Firebase Auth + Firestore users
        │   │   │       └── PetRepository.kt  ← Cache-first: Room + Firestore + Storage
        │   │   │
        │   │   ├── ui/
        │   │   │   ├── adapter/
        │   │   │   │   ├── PetAdapter.kt     ← ListAdapter para Home (con DiffUtil)
        │   │   │   │   └── MyPetAdapter.kt   ← ListAdapter para Perfil
        │   │   │   │
        │   │   │   ├── auth/
        │   │   │   │   └── AuthActivity.kt   ← Login + Registro (alterna modo)
        │   │   │   │
        │   │   │   ├── contracts/
        │   │   │   │   ├── AuthContract.kt   ← Interfaz MVP Auth
        │   │   │   │   ├── HomeContract.kt   ← Interfaz MVP Home
        │   │   │   │   └── OtherContracts.kt ← Detail + Publish + Profile
        │   │   │   │
        │   │   │   ├── detail/
        │   │   │   │   └── DetailFragment.kt ← Detalle con Safe Args
        │   │   │   │
        │   │   │   ├── home/
        │   │   │   │   └── HomeFragment.kt   ← Lista + búsqueda en tiempo real
        │   │   │   │
        │   │   │   ├── main/
        │   │   │   │   └── MainActivity.kt   ← BottomNavigation + NavController
        │   │   │   │
        │   │   │   ├── presenter/
        │   │   │   │   ├── AuthPresenter.kt     ← Lógica login/registro + validación
        │   │   │   │   ├── DetailPresenter.kt   ← Carga detalle + contacto
        │   │   │   │   ├── HomePresenter.kt     ← Cache-first + sync + búsqueda
        │   │   │   │   ├── ProfilePresenter.kt  ← Perfil + mascotas + eliminar
        │   │   │   │   └── PublishPresenter.kt  ← Validación + subida foto + guardado
        │   │   │   │
        │   │   │   ├── profile/
        │   │   │   │   └── ProfileFragment.kt   ← Perfil + lista mascotas propias
        │   │   │   │
        │   │   │   └── publish/
        │   │   │       └── PublishFragment.kt   ← Formulario + galería + publish
        │   │   │
        │   │   └── util/
        │   │       ├── Extensions.kt         ← Extensiones generales (Toast, etc.)
        │   │       ├── ViewExtensions.kt     ← Extensiones de visibilidad de Views
        │   │       └── RepositoryProvider.kt ← Factory de repositorios
        │   │
        │   └── res/
        │       ├── anim/
        │       │   ├── slide_in_right.xml
        │       │   ├── slide_in_left.xml
        │       │   ├── slide_out_left.xml
        │       │   └── slide_out_right.xml
        │       │
        │       ├── color/
        │       │   └── bottom_nav_selector.xml
        │       │
        │       ├── drawable/
        │       │   ├── bg_badge.xml          ← Fondo para badges de tipo
        │       │   ├── bg_image_rounded.xml  ← Fondo redondeado para fotos
        │       │   ├── ic_paw.xml            ← Ícono huella (logo)
        │       │   ├── ic_paw_placeholder.xml← Placeholder con huella gris
        │       │   ├── ic_home.xml
        │       │   ├── ic_add_circle.xml
        │       │   ├── ic_person.xml
        │       │   ├── ic_email.xml
        │       │   ├── ic_lock.xml
        │       │   ├── ic_search.xml
        │       │   ├── ic_camera.xml
        │       │   ├── ic_calendar.xml
        │       │   └── ic_phone.xml
        │       │
        │       ├── layout/
        │       │   ├── activity_auth.xml     ← Pantalla login/registro
        │       │   ├── activity_main.xml     ← Contenedor principal + BottomNav
        │       │   ├── fragment_home.xml     ← AppBar + SearchBar + RecyclerView
        │       │   ├── fragment_detail.xml   ← Detalle completo + botón adoptar
        │       │   ├── fragment_publish.xml  ← Formulario de publicación
        │       │   ├── fragment_profile.xml  ← Header usuario + lista mascotas
        │       │   ├── item_pet.xml          ← Tarjeta mascota (lista Home)
        │       │   └── item_my_pet.xml       ← Tarjeta mascota (lista Perfil)
        │       │
        │       ├── menu/
        │       │   └── bottom_nav_menu.xml   ← 3 ítems: Inicio, Publicar, Perfil
        │       │
        │       ├── navigation/
        │       │   └── nav_graph.xml         ← Grafo de navegación + Safe Args
        │       │
        │       └── values/
        │           ├── colors.xml            ← Paleta: #4CAF50, #FF914D, #FFFFFF
        │           ├── dimens.xml            ← Dimensiones reutilizables
        │           ├── shape_appearances.xml ← ShapeAppearanceOverlay (círculo)
        │           ├── strings.xml           ← Todos los textos de la app
        │           └── themes.xml            ← Tema + estilos de botones y campos
        │
        ├── test/java/com/adoptapet/app/
        │   ├── model/
        │   │   └── PetModelTest.kt           ← Tests serialización Pet ↔ Firestore
        │   ├── presenter/
        │   │   ├── AuthPresenterTest.kt      ← 6 tests: login, registro, validación
        │   │   ├── PublishPresenterTest.kt   ← 6 tests: publish, validación, auth
        │   │   └── ProfilePresenterTest.kt   ← 8 tests: perfil, mascotas, logout
        │   └── repository/
        │       └── PetRepositoryAndHomePresenterTest.kt ← 7 tests DAO + HomePresenter
        │
        └── androidTest/java/com/adoptapet/app/
            └── local/
                └── PetDaoTest.kt             ← 8 tests Room in-memory database
```

---

## Resumen de cobertura de tests

| Archivo                              | Tipo        | Tests |
|--------------------------------------|-------------|-------|
| AuthPresenterTest.kt                 | JUnit       | 6     |
| PublishPresenterTest.kt              | JUnit       | 6     |
| ProfilePresenterTest.kt              | JUnit       | 8     |
| PetRepositoryAndHomePresenterTest.kt | JUnit       | 7     |
| PetModelTest.kt                      | JUnit       | 5     |
| PetDaoTest.kt                        | Instrumented| 8     |
| **Total**                            |             | **40**|

---

## Flujo completo de la aplicación

```
Inicio de app
    │
    ▼
AuthActivity ──── (sesión activa?) ────► MainActivity
    │                                         │
    ├── Login                         BottomNavigationView
    └── Registro                             │
                                    ┌────────┼─────────┐
                                    ▼        ▼         ▼
                               HomeFragment  PublishFrag  ProfileFrag
                                    │             │           │
                               (tap tarjeta)  (formulario) (mis mascotas)
                                    │             │           │
                               DetailFragment  Firestore   Eliminar
                               (info contacto) + Room      publicación
```
