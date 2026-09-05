# Desarrollo

## Requisitos

- JDK 17
- Android SDK 37
- Android SDK Build Tools compatibles con AGP 9.3
- Gradle Wrapper 9.7.1 incluido

`local.properties` debe contener `sdk.dir` y nunca se versiona.

## Verificación local

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Las pruebas instrumentadas requieren emulador o dispositivo autorizado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

## Flujo por fase

1. Auditar mockups de la feature.
2. Definir estado, eventos, dominio y navegación.
3. Implementar UI responsive.
4. Agregar pruebas.
5. Ejecutar build, unit tests y lint.
6. Validar tamaños compactos y grandes.
7. Actualizar documentación.
8. Crear checkpoint Git y detenerse para revisión.
