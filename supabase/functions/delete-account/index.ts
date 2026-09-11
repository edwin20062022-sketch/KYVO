import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const json = (body: unknown, status = 200) => new Response(JSON.stringify(body), {
  status,
  headers: { "content-type": "application/json; charset=utf-8" },
});

Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);

  const authorization = request.headers.get("Authorization");
  if (!authorization?.startsWith("Bearer ")) return json({ error: "Not authenticated" }, 401);

  try {
    const token = authorization.slice("Bearer ".length).trim();
    const url = Deno.env.get("SUPABASE_URL");
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    if (!url || !anonKey || !serviceRoleKey) return json({ error: "Server configuration unavailable" }, 500);

    const userClient = createClient(url, anonKey, {
      auth: { persistSession: false, autoRefreshToken: false },
      global: { headers: { Authorization: `Bearer ${token}` } },
    });
    const { data, error } = await userClient.auth.getUser(token);
    if (error || !data.user?.id) return json({ error: "Not authenticated" }, 401);

    const adminClient = createClient(url, serviceRoleKey, {
      auth: { persistSession: false, autoRefreshToken: false },
    });
    const deletion = await adminClient.auth.admin.deleteUser(data.user.id);
    if (deletion.error) {
      console.error("account deletion failed", deletion.error.message);
      return json({ error: "Account deletion failed" }, 500);
    }
    return json({ deleted: true });
  } catch (error) {
    console.error("delete-account failed", error instanceof Error ? error.message : "unknown error");
    return json({ error: "Account deletion failed" }, 500);
  }
});
