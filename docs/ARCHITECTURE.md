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
│   └── <feature>
│       ├── presentation
│       ├── domain
│       └── data
├── domain
└── data
```

Las abstracciones compartidas viven en `domain`; los adaptadores en `data`; los Composables y ViewModels en `presentation`. La UI observa estados inmutables mediante `StateFlow` y emite eventos, sin ejecutar reglas de negocio.

## Navegación

La navegación aprobada es Inicio, Comidas, Meal Share, Progreso y Perfil. Login es el destino inicial. Al autenticar correctamente se navega a un destino mínimo de Onboarding; la pantalla real del wizard no se implementará hasta aprobar la Fase 2. Las rutas restantes se incorporarán únicamente al aprobar su fase.

## Autenticación

`LoginViewModel` depende únicamente de `AuthRepository`. La implementación actual, `PendingAuthRepository`, rechaza de forma segura cualquier intento porque aún no existe un proveedor configurado. Nunca acepta credenciales locales como autenticación válida. Un adaptador futuro podrá integrar Firebase Authentication o un backend propio sin modificar la pantalla ni el ViewModel.

## Persistencia

- DataStore: configuración pequeña y preferencias.
- Room: alimentos, comidas, platillos e historial.
- Estado temporal: `SavedStateHandle` o estado de ViewModel.
- Información sensible: almacenamiento cifrado o credenciales administradas por el proveedor de autenticación.

Room y DataStore no se agregan aún: se incorporarán cuando exista el primer caso de uso real para evitar dependencias y esquemas vacíos.
