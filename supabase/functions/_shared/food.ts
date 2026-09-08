import { createClient, type SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2";

export type FoodType = "generic" | "commercial";
export type Nutrients = { caloriesPer100g: number | null; proteinPer100g: number | null; carbohydratesPer100g: number | null; fatPer100g: number | null; fiberPer100g: number | null; sugarPer100g: number | null; sodiumMgPer100g: number | null };
export type Serving = { label: string; gramEquivalent: number; source: "provider" | "estimated" };
export type NormalizedFood = { id?: string; type: FoodType; source: "usda" | "open_food_facts"; sourceId: string; name: string; nameOriginal: string | null; canonicalNameEs: string | null; variant: string | null; brand: string | null; imageUrl: string | null; aliases: string[]; nutrients: Nutrients; servings: Serving[] };

const knownFoods: Record<string, { canonical: string; aliases: string[]; usdaQuery: string }> = {
  tomate: { canonical: "Tomate", aliases: ["tomate", "jitomate", "tomate rojo"], usdaQuery: "tomato" },
  pollo: { canonical: "Pechuga de pollo", aliases: ["pollo", "pechuga de pollo"], usdaQuery: "chicken breast" },
  arroz: { canonical: "Arroz blanco", aliases: ["arroz", "arroz blanco"], usdaQuery: "white rice" },
  aguacate: { canonical: "Aguacate", aliases: ["aguacate", "avocado"], usdaQuery: "avocado" },
  huevo: { canonical: "Huevo", aliases: ["huevo", "egg"], usdaQuery: "egg" },
  avena: { canonical: "Avena", aliases: ["avena", "oats", "oatmeal"], usdaQuery: "oats" },
};

export function normalizeQuery(value: string): string { return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim().replace(/\s+/g, " "); }
export function canonicalFor(query: string): { canonical: string; aliases: string[]; usdaQuery: string } | null { const normalized = normalizeQuery(query); return Object.entries(knownFoods).find(([key, value]) => normalized.includes(key) || value.aliases.includes(normalized))?.[1] ?? null; }
export function numberOrNull(value: unknown): number | null { const number = typeof value === "number" ? value : Number(value); return Number.isFinite(number) ? number : null; }
export function gramsServing(value: unknown): Serving[] { const match = String(value ?? "").match(/(\d+(?:[.,]\d+)?)\s*g\b/i); if (!match) return []; const grams = Number(match[1].replace(",", ".")); return Number.isFinite(grams) && grams > 0 ? [{ label: String(value), gramEquivalent: grams, source: "provider" }] : []; }
export function serviceClient(): SupabaseClient { return createClient(requiredEnv("SUPABASE_URL"), requiredEnv("SUPABASE_SERVICE_ROLE_KEY"), { auth: { persistSession: false, autoRefreshToken: false } }); }
function requiredEnv(name: string): string { const value = Deno.env.get(name); if (!value) throw new Error(`Missing required server environment variable: ${name}`); return value; }
export function json(body: unknown, status = 200): Response { return new Response(JSON.stringify(body), { status, headers: { "content-type": "application/json; charset=utf-8" } }); }
export function dedupeFoods(foods: NormalizedFood[]): NormalizedFood[] { const seen = new Set<string>(); return foods.filter((food) => { const key = `${food.type}:${food.source}:${food.sourceId}`; if (seen.has(key)) return false; seen.add(key); return true; }); }
