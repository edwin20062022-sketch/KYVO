import type { SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2";
import { dedupeFoods, normalizeQuery, type NormalizedFood, type Nutrients, type Serving } from "./food.ts";

export async function cacheFood(client: SupabaseClient, food: NormalizedFood): Promise<NormalizedFood> {
  if (food.type === "commercial") return cacheCommercial(client, food);
  if (!food.canonicalNameEs) return food;
  const { data: existing, error: findError } = await client.from("canonical_foods").select("id").eq("name_es", food.canonicalNameEs).maybeSingle();
  if (findError) throw findError;
  let canonicalId = existing?.id;
  if (!canonicalId) {
    const proposedCanonicalId = crypto.randomUUID();
    const { error: insertError } = await client.from("canonical_foods").insert({ id: proposedCanonicalId, name_es: food.canonicalNameEs });
    if (insertError) {
      const { data: concurrent, error: concurrentError } = await client.from("canonical_foods").select("id").eq("name_es", food.canonicalNameEs).single();
      if (concurrentError) throw insertError;
      canonicalId = concurrent.id;
    } else canonicalId = proposedCanonicalId;
  }
  const aliases = [...new Map([food.canonicalNameEs, ...food.aliases].map((alias) => [normalizeQuery(alias), alias])).values()];
  if (aliases.length) {
    const { error } = await client.from("food_aliases").upsert(aliases.map((alias) => ({ canonical_food_id: canonicalId, alias, normalized_alias: normalizeQuery(alias) })), { onConflict: "canonical_food_id,normalized_alias" });
    if (error) throw error;
  }
  const { data: existingVariant, error: existingVariantError } = await client.from("food_variants").select("id").eq("source", "usda").eq("source_id", food.sourceId).maybeSingle();
  if (existingVariantError) throw existingVariantError;
  if (existingVariant) return { ...food, id: existingVariant.id };
  const variantId = crypto.randomUUID();
  const row = { id: variantId, canonical_food_id: canonicalId, source: "usda", source_id: food.sourceId, name_original: food.nameOriginal ?? food.name, variant_name_es: food.variant, calories_100g: food.nutrients.caloriesPer100g, protein_100g: food.nutrients.proteinPer100g, carbohydrates_100g: food.nutrients.carbohydratesPer100g, fat_100g: food.nutrients.fatPer100g, fiber_100g: food.nutrients.fiberPer100g, sugar_100g: food.nutrients.sugarPer100g, sodium_mg_100g: food.nutrients.sodiumMgPer100g, serving_data: food.servings };
  const { error } = await client.from("food_variants").insert(row);
  if (!error) return { ...food, id: variantId };
  const { data: concurrent, error: concurrentError } = await client.from("food_variants").select("id").eq("source", "usda").eq("source_id", food.sourceId).single();
  if (concurrentError) throw error;
  return { ...food, id: concurrent.id };
}

async function cacheCommercial(client: SupabaseClient, food: NormalizedFood): Promise<NormalizedFood> {
  const { data, error } = await client.from("commercial_products").upsert({ source: "open_food_facts", source_id: food.sourceId, barcode: food.sourceId, brand: food.brand, name_es: food.name, name_original: food.nameOriginal, image_url: food.imageUrl, calories_100g: food.nutrients.caloriesPer100g, protein_100g: food.nutrients.proteinPer100g, carbohydrates_100g: food.nutrients.carbohydratesPer100g, fat_100g: food.nutrients.fatPer100g, fiber_100g: food.nutrients.fiberPer100g, sugar_100g: food.nutrients.sugarPer100g, sodium_mg_100g: food.nutrients.sodiumMgPer100g, serving_data: food.servings }, { onConflict: "source,source_id" }).select("id").single();
  if (error) throw error;
  return { ...food, id: data.id };
}

export async function searchCached(client: SupabaseClient, query: string): Promise<NormalizedFood[]> {
  const term = normalizeQuery(query);
  const [{ data: variants, error: variantsError }, { data: products, error: productsError }] = await Promise.all([
    client.from("food_variants").select("id,source,source_id,name_original,variant_name_es,calories_100g,protein_100g,carbohydrates_100g,fat_100g,fiber_100g,sugar_100g,sodium_mg_100g,serving_data,canonical_foods(name_es,food_aliases(alias,normalized_alias))"),
    client.from("commercial_products").select("id,source,source_id,name_es,name_original,brand,image_url,calories_100g,protein_100g,carbohydrates_100g,fat_100g,fiber_100g,sugar_100g,sodium_mg_100g,serving_data"),
  ]);
  if (variantsError) throw variantsError; if (productsError) throw productsError;
  const generic = (variants ?? []).flatMap((row: any) => { const canonical = Array.isArray(row.canonical_foods) ? row.canonical_foods[0] : row.canonical_foods; const aliases = [canonical?.name_es, ...(canonical?.food_aliases ?? []).map((alias: any) => alias.alias)].filter(Boolean); return aliases.some((alias: string) => normalizeQuery(alias).includes(term) || term.includes(normalizeQuery(alias))) ? [fromGenericRow(row, canonical?.name_es ?? row.name_original, aliases)] : []; });
  const commercial = (products ?? []).flatMap((row: any) => normalizeQuery(row.name_es).includes(term) || term.includes(normalizeQuery(row.name_es)) ? [fromCommercialRow(row)] : []);
  return dedupeFoods([...generic, ...commercial]).slice(0, 15);
}

export async function getCachedDetail(client: SupabaseClient, id: string, type: string): Promise<NormalizedFood | null> {
  if (type === "commercial") { const { data, error } = await client.from("commercial_products").select("*").eq("id", id).maybeSingle(); if (error) throw error; return data ? fromCommercialRow(data) : null; }
  const { data, error } = await client.from("food_variants").select("*,canonical_foods(name_es,food_aliases(alias))").eq("id", id).maybeSingle(); if (error) throw error; const canonical = Array.isArray(data?.canonical_foods) ? data.canonical_foods[0] : data?.canonical_foods; return data ? fromGenericRow(data, canonical?.name_es ?? data.name_original, (canonical?.food_aliases ?? []).map((alias: any) => alias.alias)) : null;
}

function nutrients(row: any): Nutrients { return { caloriesPer100g: row.calories_100g, proteinPer100g: row.protein_100g, carbohydratesPer100g: row.carbohydrates_100g, fatPer100g: row.fat_100g, fiberPer100g: row.fiber_100g, sugarPer100g: row.sugar_100g, sodiumMgPer100g: row.sodium_mg_100g }; }
function servings(row: any): Serving[] { return Array.isArray(row.serving_data) ? row.serving_data : []; }
function fromGenericRow(row: any, name: string, aliases: string[]): NormalizedFood { return { id: row.id, type: "generic", source: "usda", sourceId: row.source_id, name, nameOriginal: row.name_original, canonicalNameEs: name, variant: row.variant_name_es, brand: null, imageUrl: null, aliases, nutrients: nutrients(row), servings: servings(row) }; }
function fromCommercialRow(row: any): NormalizedFood { return { id: row.id, type: "commercial", source: "open_food_facts", sourceId: row.source_id, name: row.name_es, nameOriginal: row.name_original, canonicalNameEs: null, variant: null, brand: row.brand, imageUrl: row.image_url, aliases: [], nutrients: nutrients(row), servings: servings(row) }; }
