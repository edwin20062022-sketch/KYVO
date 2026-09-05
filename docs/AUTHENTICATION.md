# Autenticación de KYVO

## Estado actual

La UI, validación, estados, ViewModel, navegación y contrato `AuthRepository` están implementados. `PendingAuthRepository` bloquea la autenticación porque todavía no existe una configuración externa segura.

## Configuración necesaria para correo y contraseña

Antes de activar acceso real se debe decidir y configurar uno de estos proveedores:

- Firebase Authentication; o
- backend propio con sesiones/token de corta duración y renovación segura.

El adaptador deberá mapear respuestas a `InvalidCredentials`, `Network` y `Unknown`. Las contraseñas no deben persistirse ni registrarse. Los tokens sensibles deberán mantenerse con almacenamiento seguro administrado por Android y el proveedor.

## Configuración necesaria para Google

La implementación futura utilizará Android Credential Manager y Sign in with Google. Faltan:

1. Proyecto en Google Auth Platform o Firebase.
2. Identificador de cliente web/server.
3. Clientes Android para los package names de release y debug.
4. Huellas SHA-1 y SHA-256 de las claves correspondientes.
5. Verificación de marca y pantalla de consentimiento.
6. Endpoint backend que valide el Google ID token y su nonce.
7. Identificador definitivo de la aplicación; `com.kyvo.app` sigue siendo provisional.

No se añadieron todavía las dependencias de Credential Manager porque no pueden completar un flujo verificable sin estas decisiones y credenciales. Al configurar el proveedor se usarán `androidx.credentials`, el adaptador de Play Services y `googleid`, siguiendo la [guía oficial](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation).
