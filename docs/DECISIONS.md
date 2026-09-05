# Decisiones técnicas

## ADR-001 — Aplicación Android nativa

Kotlin, Jetpack Compose, Material 3 y Navigation Compose. No WebView ni layouts basados en capturas.

## ADR-002 — Un módulo Gradle durante foundation

Se evita el coste de una modularización temprana. La organización por paquetes permite extraer módulos cuando los tiempos de build o límites de dominio lo justifiquen.

## ADR-003 — Versiones y compatibilidad

Se usa compile/target SDK 37. El requisito de Google Play vigente desde el 31 de agosto de 2026 exige API 36 o superior. AGP 9.4 soporta API 37; la foundation usa Gradle 9.7.1 y JDK 17.

Referencias: [requisito de API de Google Play](https://developer.android.com/google/play/requirements/target-sdk) y [compatibilidad de AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes).

## ADR-004 — Identificador provisional

`com.kyvo.app` es provisional. El identificador definitivo debe confirmarse antes de publicar porque el `applicationId` no debe cambiar después de distribuir la app.

## ADR-005 — Sin persistencia ni autenticación concreta en Fase 0

Se crean límites de repositorio, pero Room, DataStore y el proveedor de autenticación se incorporarán cuando exista una feature que los use. No se almacenan credenciales ni secrets.

## ADR-006 — Mockups con inconsistencias conocidas

Algunos indicadores del onboarding muestran 18 pasos aunque el flujo aprobado tiene 15. Además, las pantallas resumen de Perfil y Ajustes aún contienen accesos cuyos mockups individuales fueron eliminados. La arquitectura funcional aprobada prevalece hasta que se actualicen esas composiciones.

## ADR-007 — Login con dos estados visuales

El único mockup de Login representa la portada de autenticación, pero la fase exige correo y contraseña. Se conserva la portada con alta fidelidad y el botón “Iniciar sesión” abre un formulario nativo dentro de la misma feature. Esto evita inventar una pantalla de producto adicional y permite teclado, foco, validación y errores accesibles.

## ADR-008 — Proveedor de autenticación no simulado

Sin backend, configuración OAuth o proyecto Firebase, `PendingAuthRepository` devuelve `ConfigurationRequired`. Los flujos de éxito se prueban con un fake aislado; no existe ninguna credencial aceptada localmente. La futura integración de Google utilizará Android Credential Manager, no `GoogleSignInClient`, que está deprecado.

Referencia: [migración oficial hacia Credential Manager](https://developer.android.com/identity/sign-in/legacy-gsi-migration).

## ADR-009 — Secuencia canónica de onboarding

Los mockups mezclan indicadores de 18, 11 y 15 pasos; Objetivo y Experiencia también muestran numeración desplazada. La secuencia funcional aprobada de 15 pasos es la fuente de verdad. “Otro” en entrenamiento y “Muy avanzado/Experto” en experiencia se omiten porque las listas funcionales definitivas establecen cuatro tipos de entrenamiento y tres niveles.

## ADR-010 — Género y Mifflin-St Jeor

El mockup pregunta género, mientras Mifflin-St Jeor utiliza una constante asociada al sexo del modelo original. El dominio conserva las tres respuestas visuales sin convertirlas a cadenas arbitrarias. Masculino y Femenino aplican sus constantes explícitas; “Prefiero no decirlo” utiliza el punto medio de ambas constantes (`-78`) como estimación neutral y muestra una nota visible en el reveal. Es una decisión auditable, no una equivalencia médica oculta.

## ADR-011 — Persistencia del onboarding

Preferences DataStore es suficiente porque el onboarding es un conjunto pequeño de valores escalares sin relaciones ni consultas complejas. Se guarda de forma asíncrona y transaccional detrás de un repositorio. Room no aporta valor en esta fase.

Referencia: [DataStore en la arquitectura Android](https://developer.android.com/topic/libraries/architecture/datastore).

## ADR-012 — Opciones de preferencias

El mockup utiliza controles de selección exclusiva y presenta “Sin restricciones”, “Vegetariano”, “Vegano”, “Sin gluten”, “Sin lácteos” y “Otra”. La implementación respeta ese comportamiento. Si producto decide admitir combinaciones de restricciones, el enum puede migrarse a un conjunto sin alterar el resto del wizard.
