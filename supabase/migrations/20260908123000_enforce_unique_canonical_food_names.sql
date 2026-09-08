-- A normalized Spanish food name must resolve to one canonical record even when
-- simultaneous provider requests attempt to warm the cache.
create unique index if not exists canonical_foods_name_es_unique_idx
  on public.canonical_foods (name_es);
