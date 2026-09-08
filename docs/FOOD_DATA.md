# Datos de alimentos

KYVO presenta un modelo unificado a Android y mantiene las fuentes separadas en backend. Los alimentos genéricos se normalizan desde USDA FoodData Central en `canonical_foods` y `food_variants`; los productos comerciales proceden de Open Food Facts en `commercial_products`. Android nunca consulta esas fuentes directamente.

Los aliases se normalizan sin acentos, mayúsculas ni espacios duplicados. Las imágenes genéricas se resuelven desde la clave `image_key` de Storage (`food-images/`); USDA no es fuente visual. Los productos comerciales pueden incluir imagen de su fuente si existe.

Cada `meal_item` guarda snapshots nutricionales al registrarse, por lo que cambios posteriores en proveedores no modifican el historial. Favoritos, comidas e items están protegidos por RLS con `auth.uid()`; no se utilizan `service_role` ni claves de proveedores en Android.

La Edge Function `food-search` deberá consultar, normalizar y cachear las fuentes usando secretos server-side. Falta configurar `USDA_API_KEY` en el entorno de Supabase y revisar antes de producción términos, atribución y licencias vigentes de USDA, Open Food Facts y las imágenes propias de KYVO.
