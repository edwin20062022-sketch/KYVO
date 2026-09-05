# KYVO Android

Aplicación Android nativa de nutrición para personas que entrenan en gimnasio. El proyecto se desarrolla por fases y cada módulo debe quedar compilable, probado y documentado antes de avanzar.

## Estado

Fase 1 — Login. La portada de acceso, el formulario de correo/contraseña, sus estados y la navegación mínima posterior están implementados. El proveedor real de identidad permanece pendiente de configuración externa.

## Stack

- Kotlin 2.4.10
- Jetpack Compose + Material 3
- Navigation Compose
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
