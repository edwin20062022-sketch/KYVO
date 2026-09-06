# Arquitectura

## Enfoque

KYVO empieza como una aplicación de un solo módulo Gradle para mantener builds rápidos y evitar modularización prematura. El código se organiza por feature y cada feature separa presentación, dominio y datos cuando esas capas aportan valor real.

```text
com.kyvo.app
├── core
│   ├── designsystem
│   └── navigation
├── feature
│   ├── auth
│   │   ├── presentation
│   │   └── domain
│   └── onboarding
│       ├── presentation
│       │   └── components
│       ├── domain
│       │   ├── model
│       │   ├── validation
│       │   ├── calculator
│       │   └── repository
│       └── data
├── domain
└── data
```

Las abstracciones compartidas viven en `domain`; los adaptadores en `data`; los Composables y ViewModels en `presentation`. La UI observa estados inmutables mediante `StateFlow` y emite eventos, sin ejecutar reglas de negocio.

## Navegación

El destino se deriva de sesión y onboarding: sin sesión abre Login; con sesión y onboarding incompleto abre el wizard; con ambos completos abre el placeholder mínimo de Home. El `OnboardingViewModel` mantiene una única fuente de verdad y permite avanzar o regresar conservando respuestas. Home no está implementado.

## Autenticación

`LoginViewModel` depende únicamente de `AuthRepository`. `SupabaseAuthRepository` coordina `SupabaseSdkAuthDataSource` y `CredentialManagerGoogleGateway`; la UI no conoce tokens, clientes OAuth ni detalles del SDK. `AuthState` distingue inicialización, sesión cerrada y sesión restaurada para evitar mostrar Login mientras Supabase carga su almacenamiento. Cuando faltan claves públicas locales se inyecta `ConfigurationRequiredAuthRepository`, que rechaza de forma segura los intentos sin simular autenticación.

## Persistencia

- DataStore: configuración pequeña y preferencias.
- Room: alimentos, comidas, platillos e historial.
- Estado temporal: `SavedStateHandle` o estado de ViewModel.
- Información sensible: almacenamiento cifrado o credenciales administradas por el proveedor de autenticación.

Fase 2 incorpora Preferences DataStore detrás de `OnboardingRepository`. Guarda únicamente el borrador, paso actual, respuestas necesarias, plan calculado y bandera de finalización. Room sigue reservado para datos relacionales futuros como alimentos e historial.
