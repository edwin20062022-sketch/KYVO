import { getCachedDetail } from "../_shared/catalog.ts";
import { json, serviceClient } from "../_shared/food.ts";

Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
  try {
    const { id, type } = await request.json() as { id?: string; type?: "generic" | "commercial" };
    if (!id || (type !== "generic" && type !== "commercial")) return json({ error: "Alimento inválido." }, 400);
    const food = await getCachedDetail(serviceClient(), id, type);
    return food ? json({ food }) : json({ error: "Alimento no encontrado." }, 404);
  } catch (error) { console.error("food-detail failed", error instanceof Error ? error.message : "unknown error"); return json({ error: "No pudimos cargar el alimento." }, 500); }
});
