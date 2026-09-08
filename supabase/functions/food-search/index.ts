import { cacheFood, searchCached } from "../_shared/catalog.ts";
import { dedupeFoods, json, normalizeQuery, serviceClient } from "../_shared/food.ts";
import { searchOpenFoodFacts, searchUsda } from "../_shared/providers.ts";

Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
  try {
    const { query } = await request.json() as { query?: string }; const normalized = normalizeQuery(query ?? "");
    if (normalized.length < 2 || normalized.length > 80) return json({ error: "La búsqueda debe tener entre 2 y 80 caracteres." }, 400);
    const client = serviceClient();
    let cached = [];
    try { cached = await searchCached(client, normalized); }
    catch (error) { console.error("catalog search failed", error instanceof Error ? error.message : "unknown error"); }
    if (cached.length >= 5) return json({ foods: cached, source: "catalog" });
    const [usda, off] = await Promise.allSettled([searchUsda(normalized), searchOpenFoodFacts(normalized)]);
    const upstream = [...(usda.status === "fulfilled" ? usda.value : []), ...(off.status === "fulfilled" ? off.value : [])];
    if (!upstream.length && !cached.length) return json({ error: "No pudimos consultar el catálogo de alimentos." }, 502);
    const stored = await Promise.allSettled(upstream.map((food) => cacheFood(client, food)));
    stored.forEach((result) => { if (result.status === "rejected") console.error("catalog cache write failed", result.reason instanceof Error ? result.reason.message : "unknown error"); });
    const fresh = stored.flatMap((result, index) => result.status === "fulfilled" ? [result.value] : [upstream[index]]);
    return json({ foods: dedupeFoods([...cached, ...fresh]).slice(0, 15), source: fresh.length ? "catalog_and_provider" : "catalog" });
  } catch (error) { console.error("food-search failed", error instanceof Error ? error.message : "unknown error"); return json({ error: "No pudimos realizar la búsqueda." }, 500); }
});
