# Decisiones técnicas

## ADR-001 — Aplicación Android nativa

Kotlin, Jetpack Compose, Material 3 y Navigation Compose. No WebView ni layouts basados en capturas.

## ADR-002 — Un módulo Gradle durante foundation

Se evita el coste de una modularización temprana. La organización por paquetes permite extraer módulos cuando los tiempos de build o límites de dominio lo justifiquen.

## ADR-003 — Versiones y compatibilidad

Se usa compile/target SDK 37. El requisito de Google Play vigente desde el 31 de agosto de 2026 exige API 36 o superior. AGP 9.4 soporta API 37; la foundation usa Gradle 9.7.1 y JDK 17.

Referencias: [requisito de API de Google Play](https://developer.android.com/google/play/requirements/target-sdk) y [compatibilidad de AGP 9.4](https://developer.android.com/build/releases/agp-9-4-0-release-notes).

## ADR-004 — Identificador definitivo

`com.kyvo.app` es el `applicationId` definitivo de producción. Debug usa `com.kyvo.app.debug` mediante `applicationIdSuffix = ".debug"`; el `namespace` compartido permanece en `com.kyvo.app`. Cada variante usa su propio callback y requiere un cliente OAuth Android asociado a su package y certificado.

## ADR-005 — Sin persistencia ni autenticación concreta en Fase 0

Se crean límites de repositorio, pero Room, DataStore y el proveedor de autenticación se incorporarán cuando exista una feature que los use. No se almacenan credenciales ni secrets.

## ADR-006 — Mockups con inconsistencias conocidas

Algunos indicadores del onboarding muestran 18 pasos aunque el flujo aprobado tiene 15. Además, las pantallas resumen de Perfil y Ajustes aún contienen accesos cuyos mockups individuales fueron eliminados. La arquitectura funcional aprobada prevalece hasta que se actualicen esas composiciones.

## ADR-007 — Login con dos estados visuales

El único mockup de Login representa la portada de autenticación, pero la fase exige correo y contraseña. Se conserva la portada con alta fidelidad y el botón “Iniciar sesión” abre un formulario nativo dentro de la misma feature. Esto evita inventar una pantalla de producto adicional y permite teclado, foco, validación y errores accesibles.

## ADR-008 — Autenticación externa no simulada

Sin configuración pública local completa, `ConfigurationRequiredAuthRepository` devuelve `ConfigurationRequired`. Con ella, `SupabaseAuthRepository` ejecuta email/password y Google reales; los tests usan fakes aislados y ninguna credencial se acepta localmente. Google utiliza Android Credential Manager, no `GoogleSignInClient`, que está deprecado.

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

## ADR-013 — Supabase Auth como autoridad de sesión

Email/password y Google terminan en una sesión de Supabase. El SDK conserva y renueva la sesión; la app deriva su navegación del estado observado y nunca persiste contraseñas ni tokens manualmente.

## ADR-014 — Google nativo mediante Credential Manager

El botón de Google usa `GetSignInWithGoogleOption`, un Web Client ID como `serverClientId` y un nonce aleatorio de 256 bits. El hash SHA-256 va a Google y el nonce crudo a Supabase. Se descartan GoogleSignInClient, WebView y Firebase Auth.

## ADR-015 — OAuth estándar queda fuera de gcloud IAM

Los comandos `gcloud iam oauth-clients` no corresponden a los clientes estándar de Google Auth Platform usados por Sign in with Google. Branding, Audience, Data Access y clientes Web/Android se documentan como configuración manual hasta que Google publique una API/CLI soportada.

## ADR-016 — Configuración pública local y sin secrets Android

URL, publishable key y Web Client ID se leen de `local.properties` hacia `BuildConfig`. El archivo está ignorado; el ejemplo versionado tiene valores vacíos. El Client Secret de Google se guarda únicamente en Supabase.

## ADR-017 — Onboarding local aislado por usuario

El borrador y la finalización del onboarding permanecen en Preferences DataStore, pero cada clave usa como namespace el `user.id` estable de Supabase. El repositorio solo se crea después de restaurar una sesión autenticada y al cerrar sesión deja de exponerse. Así, dos cuentas en el mismo dispositivo no comparten estado; una futura sincronización multidispositivo podrá mover la autoridad a un perfil remoto sin cambiar el contrato del repositorio.

## ADR-018 — Favoritos, frecuentes y detalle histórico

Los favoritos se almacenan por usuario y referencia de alimento en `user_food_favorites`; el constraint primario `(user_id, food_id, food_type)` hace idempotente el toggle. Frecuentes se derivan exclusivamente del historial real mediante `get_frequent_foods`, sin tabla manual ni gamificación. El ranking usa `usos * 10 + recencia`, donde recencia aporta como máximo un punto durante 30 días. Detalle de comida muestra los snapshots guardados en `meal_items` y reutiliza el editor de Fase 3.

Con este checkpoint FASE 4 — Registro de alimentos queda cerrada; FASE 5 permanece fuera de alcance.
