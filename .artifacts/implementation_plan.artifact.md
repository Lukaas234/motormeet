# Migración a Kotlin Multiplatform (Android + iOS)

## Contexto

La aplicación **MotorMeet Uruguay** está actualmente desarrollada 100% en Android usando Jetpack Compose, Hilt, Appwrite SDK para Android, Google Maps y Coil. El objetivo es convertirla en un proyecto **Kotlin Multiplatform (KMP)** con **Compose Multiplatform** para compartir lógica de negocio y UI entre Android e iOS.

## Cambios Propuestos por Componente

### 1. Configuración de Build (Gradle)

#### [MODIFY] `build.gradle` (raíz)
- Agregar plugins: `org.jetbrains.kotlin.multiplatform`, `org.jetbrains.compose`
- Mantener `com.android.tools.build:gradle` y `org.jetbrains.kotlin.android` para el target Android

#### [MODIFY] `settings.gradle`
- Agregar include para módulo iOS (`:iosApp`) si se crea módulo separado, o configurar target iOS en el mismo módulo `:app`

#### [MODIFY] `app/build.gradle` → `app/build.gradle.kts`
- Reemplazar plugins Android puros por configuración multiplatform: `androidTarget()`, `iosTarget()`
- Reemplazar dependencias Android-specific por equivalentes multiplatform o condicionales por fuente de código

### 2. Inyección de Dependencias

#### [MODIFY] `app/src/main/java/com/motormeet/uruguay/di/RepositoryModule.kt`
- Reemplazar **Hilt** (`@HiltAndroidApp`, `@HiltViewModel`, `@Binds`, `@Inject`) por **Koin** (`module { single { ... } }`, `get<>()`)
- Eliminar dependencia `hilt-android` y `hilt-navigation-compose`

#### [MODIFY] Todos los ViewModels (`@HiltViewModel`)
- Quitar anotación `@HiltViewModel`
- Heredar de `ViewModel()` normal y recibir dependencias por constructor
- Registrarse en módulo Koin

### 3. Modelos de Dominio (Common)

#### [MODIFY] `domain/model/*.kt`
- Mover a `commonMain` (ya son puros Kotlin, solo cambio de carpeta)
- Agregar `@Serializable` donde se necesite serialización JSON para Ktor

### 4. Repositorios y Datos (Common + Platform)

#### [MODIFY] `domain/repository/*.kt`
- Interfaces se mueven a `commonMain` (ya son interfaces puras)

#### [MODIFY] `data/repository/*.kt`
- **Appwrite Android SDK** (`io.appwrite.services.*`) → se reemplaza por **Ktor Client** + `kotlinx.serialization` en `commonMain`
- Implementaciones específicas por plataforma en `androidMain` y `iosMain`
- `SharedPreferences` → **Multiplatform Settings** (`commonMain`)

### 5. UI (Compose Multiplatform)

#### [MODIFY] `ui/theme/`, `ui/components/`
- Mover a `commonMain` (Compose Multiplatform es compatible)
- Reemplazar imports de `androidx.compose.*` por `androidx.compose.ui:*` (multiplatform)

#### [MODIFY] `presentation/**/*.kt`
- Pantallas y ViewModels se mueven a `commonMain`
- Navegación: `androidx.navigation:navigation-compose` → se evalúa reemplazo por navegación multiplatform (Voyager o similar)

### 6. Mapas

#### [MODIFY] `presentation/events/EventsMapScreen.kt`, `util/MapUtils.kt`
- Google Maps SDK es solo Android. Se debe reemplazar por:
  - Opción A: Mapas web dentro de `AndroidView` / `UIViewControllerRepresentable`
  - Opción B: Librería multiplatform de mapas (si existe estable)
  - Opción C: Pantalla de mapas nativa por plataforma (`androidMain` y `iosMain`)

### 7. Imágenes (Coil)

#### [MODIFY] `ui/components/Avatar.kt`, pantallas con `AsyncImage`
- Coil Compose es principalmente Android. Evaluar reemplazo por:
  - `coil-compose` multiplatform (experimental)
  - Carga de imágenes por plataforma con `rememberImagePainter` equivalente

### 8. Módulo iOS (Nuevo)

#### [NEW] `iosApp/`
- `iosApp/iosApp.swift` - Punto de entrada SwiftUI que hostea Compose UI
- `iosApp/Info.plist`
- `iosApp/iosApp.xcodeproj` (generado por Xcode/Gradle)

## Verificación Plan

- [ ] `./gradlew :app:compileDebugKotlin` (Android)
- [ ] `./gradlew :iosApp:build` (iOS, requiere Mac con Xcode)
- [ ] Ejecutar en emulador Android y simulador iOS

## Preguntas Abiertas

1. ¿Se crea módulo separado `:iosApp` o se usa un solo módulo `:app` con targets `androidTarget()` e `iosTarget()`?
2. ¿Qué librería de mapas se usará para iOS (Apple Maps nativo o web view)?
3. ¿Se reemplaza Hilt completamente por Koin o se mantiene Hilt solo para Android y Koin para iOS?
