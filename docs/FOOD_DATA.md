# Datos de alimentos

KYVO presenta un modelo unificado a Android y mantiene las fuentes separadas en backend. Los alimentos genéricos se normalizan desde USDA FoodData Central en `canonical_foods` y `food_variants`; los productos comerciales proceden de Open Food Facts en `commercial_products`. Android nunca consulta esas fuentes directamente.

Los aliases se normalizan sin acentos, mayúsculas ni espacios duplicados. Las imágenes genéricas se resuelven desde la clave `image_key` de Storage (`food-images/`); USDA no es fuente visual. Los productos comerciales pueden incluir imagen de su fuente si existe.

Cada `meal_item` guarda snapshots nutricionales al registrarse, por lo que cambios posteriores en proveedores no modifican el historial. Favoritos, comidas e items están protegidos por RLS con `auth.uid()`; no se utilizan `service_role` ni claves de proveedores en Android.

La Edge Function `food-search` deberá consultar, normalizar y cachear las fuentes usando secretos server-side. Falta configurar `USDA_API_KEY` en el entorno de Supabase y revisar antes de producción términos, atribución y licencias vigentes de USDA, Open Food Facts y las imágenes propias de KYVO.
# Food catalog architecture

KYVO keeps provider credentials and provider HTTP calls in Supabase Edge Functions only. Android first searches the normalized Supabase catalog and calls the authenticated `food-search` function only when the local catalog has fewer than five matches.

- `food-search`: normalized search across the KYVO cache, USDA FoodData Central for generic foods, and Open Food Facts for commercial products.
- `food-detail`: cached normalized detail by UUID and type.
- USDA generic results are cached only when a controlled Spanish canonical mapping exists. Unknown provider descriptions are returned without inventing a translation or creating a generic canonical record.
- Open Food Facts commercial products retain the provider image URL when available. Generic foods have no provider photo and may use a future KYVO `image_key`.
- Cache writes use `source + source_id`; canonical Spanish names are unique to prevent duplicates under concurrent requests.
- Nutrients are normalized per 100 g. Missing provider values remain `null`; they are never treated as zero.

The Edge Functions require a Supabase JWT. `USDA_API_KEY` and the service-role credential exist only in Supabase's server environment and are not Android configuration.
