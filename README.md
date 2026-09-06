# KYVO Android

Aplicación Android nativa de nutrición para personas que entrenan en gimnasio. El proyecto se desarrolla por fases y cada módulo debe quedar compilable, probado y documentado antes de avanzar.

## Estado

Fase 2 — Onboarding Wizard completada. Autenticación real con Supabase y Google Credential Manager implementada; queda completar la configuración manual externa de OAuth/Provider y colocar las claves públicas locales. Home permanece como placeholder hasta la Fase 3.

## Stack

- Kotlin 2.4.10
- Jetpack Compose + Material 3
- Navigation Compose
- Preferences DataStore
- Supabase Auth Kotlin 3.8.0
- Android Credential Manager 1.6.0 + Google ID 1.2.0
- Gradle Kotlin DSL
- AGP 9.4.0 y Gradle 9.7.1
- minSdk 26, targetSdk/compileSdk 37
- Java 17

## Comandos

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Consulta [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) para preparar el entorno local.

La configuración pendiente del proveedor de acceso se documenta en [docs/AUTHENTICATION.md](docs/AUTHENTICATION.md).
