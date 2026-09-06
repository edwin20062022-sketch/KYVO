# Autenticación de KYVO

## Arquitectura implementada

KYVO usa Supabase Auth para email/password y para validar el Google ID Token obtenido de forma nativa con Android Credential Manager. No usa `GoogleSignInClient`, WebView, Firebase Authentication, service accounts ni claves secretas dentro de Android.

Flujo Google:

1. `SecureRandom` genera 32 bytes y los codifica como Base64 URL-safe sin padding (`raw nonce`).
2. SHA-256 transforma el nonce crudo a hexadecimal (`hashed nonce`).
3. Credential Manager recibe el hash junto con `GOOGLE_WEB_CLIENT_ID` como `serverClientId`.
4. Google devuelve un ID Token.
5. Supabase Auth recibe el ID Token y el nonce crudo mediante `signInWith(IDToken)`; Supabase valida token, audiencia y nonce.
6. La sesión Supabase se persiste y restaura mediante el almacenamiento del SDK (`autoLoadFromStorage` y `alwaysAutoRefresh`).

La navegación deriva de dos fuentes persistentes:

- sin sesión: Login;
- sesión + onboarding incompleto: Onboarding;
- sesión + onboarding completo: placeholder mínimo de Home.

## Configuración local

Copiar las claves públicas a `local.properties`, archivo ignorado por Git:

```properties
SUPABASE_URL=https://gnzbnbgogcnebphooykd.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_...
GOOGLE_WEB_CLIENT_ID=....apps.googleusercontent.com
```

No agregar `GOOGLE_CLIENT_SECRET`, `SUPABASE_SECRET_KEY`, `service_role` ni contraseñas. `local.properties.example` contiene únicamente nombres y valores vacíos.

## Auditoría Google Cloud por CLI

Clasificación: `CLI_OFICIAL`.

- Google Cloud SDK: 583.0.0.
- Cuenta activa: `edwin2006.2022@gmail.com`.
- Proyecto: `kyvo-507802` (`KYVO`, número `484213679040`).
- No se habilitó ninguna API ni se creó infraestructura.

`gcloud iam oauth-clients` no administra los clientes estándar de Sign in with Google: usa IAM v1, clientes públicos/confidenciales y un conjunto limitado de scopes para identidades federadas de Google Cloud. Por eso no se utilizó.

Google no publica actualmente una API/CLI soportada para leer o crear los clientes estándar Web/Android ni para gestionar Branding, Audience y Data Access de Google Auth Platform. La API interna `clientauthconfig.googleapis.com` no se utiliza.

## Acción manual mínima: Google Auth Platform

Clasificación: `MANUAL_REQUERIDO`.

En el proyecto `kyvo-507802`:

1. Abrir **Google Auth Platform → Branding**. Si aparece **Get started**, configurar:
   - App name: `KYVO`
   - User support email: `edwin2006.2022@gmail.com`
   - Developer contact: `edwin2006.2022@gmail.com`
2. Abrir **Audience**:
   - User type: `External`
   - Publishing status: `Testing` durante desarrollo
   - Test user: `edwin2006.2022@gmail.com`, si la pantalla lo solicita.
3. Abrir **Data Access** y conservar solo:
   - `openid`
   - `https://www.googleapis.com/auth/userinfo.email`
   - `https://www.googleapis.com/auth/userinfo.profile`
4. Abrir **Clients** y comprobar primero si existe un Web client con el mismo redirect URI. Si no existe, seleccionar **Create client → Web application**:
   - Name: `KYVO Web Client`
   - Authorized JavaScript origins: ninguno
   - Authorized redirect URI: `https://gnzbnbgogcnebphooykd.supabase.co/auth/v1/callback`
5. Copiar su Client ID a `GOOGLE_WEB_CLIENT_ID` en `local.properties`. Guardar el Client Secret solo para el paso de Supabase; nunca incorporarlo al proyecto Android.
6. En **Clients**, comprobar si ya existe el Android client equivalente. Si no existe, seleccionar **Create client → Android**:
   - Name: `KYVO Android Debug`
   - Package name: `com.kyvo.app`
   - SHA-1: `D4:60:E4:2C:55:37:2E:DF:60:61:5C:C3:99:BF:8B:F0:CD:FC:A4:0B`

## Acción manual mínima: Supabase

Clasificación: `MANUAL_REQUERIDO`.

1. En el proyecto `gnzbnbgogcnebphooykd`, abrir **Authentication → Sign In / Providers → Google**.
2. Habilitar Google e introducir el Client ID y Client Secret de `KYVO Web Client`.
3. Guardar. El secret permanece exclusivamente en Supabase.
4. Copiar la **publishable key** del proyecto a `SUPABASE_PUBLISHABLE_KEY` en `local.properties`. No usar `secret` ni `service_role`.
5. Verificar en **Authentication → URL Configuration → Redirect URLs** que existe `com.kyvo.app://auth-callback`.

Supabase CLI no estaba instalada ni autenticada. La configuración del proveedor se mantiene manual para no introducir el Client Secret en el historial del shell, scripts o Git.

## Firmas Android

- Debug SHA-1: `D4:60:E4:2C:55:37:2E:DF:60:61:5C:C3:99:BF:8B:F0:CD:FC:A4:0B`
- Debug SHA-256: `45:C5:A1:69:6C:23:34:D6:B5:B1:58:34:E1:43:79:1B:7A:E3:C9:AF:13:C1:73:D2:3A:56:A7:B6:8C:58:48:40`
- Release: no hay signing config local.
- Producción/Play: crear después `KYVO Android Production / Play` con el SHA-1 real de Google Play App Signing; no reutilizar ni inventar la huella debug.

## Deep link

`AndroidManifest.xml` declara un `intent-filter` BROWSABLE para `com.kyvo.app://auth-callback`. `MainActivity` entrega intents iniciales y nuevos a `supabase.handleDeeplinks`, cubriendo confirmación de email, recuperación y callbacks compatibles con Supabase Auth.
