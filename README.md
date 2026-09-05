# KYVO Android

Aplicación Android nativa de nutrición para personas que entrenan en gimnasio. El proyecto se desarrolla por fases y cada módulo debe quedar compilable, probado y documentado antes de avanzar.

## Estado

Fase 2 — Onboarding Wizard. El flujo progresivo de 15 pasos, validaciones, cálculo nutricional, reveals y persistencia local están implementados. Home permanece como placeholder hasta la Fase 3.

## Stack

- Kotlin 2.4.10
- Jetpack Compose + Material 3
- Navigation Compose
- Preferences DataStore
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
