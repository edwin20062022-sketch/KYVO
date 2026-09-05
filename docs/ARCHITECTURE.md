# Arquitectura

## Enfoque

KYVO empieza como una aplicación de un solo módulo Gradle para mantener builds rápidos y evitar modularización prematura. El código se organiza por feature y cada feature separa presentación, dominio y datos cuando esas capas aportan valor real.

```text
com.kyvo.app
├── core
│   ├── designsystem
│   └── navigation
├── feature
│   └── <feature>
│       ├── presentation
│       ├── domain
│       └── data
├── domain
└── data
```

Las abstracciones compartidas viven en `domain`; los adaptadores en `data`; los Composables y ViewModels en `presentation`. La UI observa estados inmutables mediante `StateFlow` y emite eventos, sin ejecutar reglas de negocio.

## Navegación

La navegación aprobada es Inicio, Comidas, Meal Share, Progreso y Perfil. Durante la Fase 0 solo se registra una ruta técnica de foundation. Login será el primer destino real en la Fase 1 y las rutas restantes se incorporarán únicamente al aprobar su fase.

## Persistencia

- DataStore: configuración pequeña y preferencias.
- Room: alimentos, comidas, platillos e historial.
- Estado temporal: `SavedStateHandle` o estado de ViewModel.
- Información sensible: almacenamiento cifrado o credenciales administradas por el proveedor de autenticación.

Room y DataStore no se agregan aún: se incorporarán cuando exista el primer caso de uso real para evitar dependencias y esquemas vacíos.

