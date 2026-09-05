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

Login es el destino inicial. Una autenticación correcta abre el wizard de Onboarding de 15 pasos. El `OnboardingViewModel` mantiene una única fuente de verdad y permite avanzar o regresar conservando respuestas. Al completar el resumen se marca el onboarding como finalizado y se navega a un placeholder mínimo de Home; Home no está implementado.

## Autenticación

`LoginViewModel` depende únicamente de `AuthRepository`. La implementación actual, `PendingAuthRepository`, rechaza de forma segura cualquier intento porque aún no existe un proveedor configurado. Nunca acepta credenciales locales como autenticación válida. Un adaptador futuro podrá integrar Firebase Authentication o un backend propio sin modificar la pantalla ni el ViewModel.

## Persistencia

- DataStore: configuración pequeña y preferencias.
- Room: alimentos, comidas, platillos e historial.
- Estado temporal: `SavedStateHandle` o estado de ViewModel.
- Información sensible: almacenamiento cifrado o credenciales administradas por el proveedor de autenticación.

Fase 2 incorpora Preferences DataStore detrás de `OnboardingRepository`. Guarda únicamente el borrador, paso actual, respuestas necesarias, plan calculado y bandera de finalización. Room sigue reservado para datos relacionales futuros como alimentos e historial.
