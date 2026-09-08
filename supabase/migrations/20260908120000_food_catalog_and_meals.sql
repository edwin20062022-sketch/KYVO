-- Public normalized catalogue. Provider credentials remain server-side only.
create table if not exists public.canonical_foods (
  id uuid primary key default gen_random_uuid(), name_es text not null, image_key text,
  category text, created_at timestamptz not null default now(), updated_at timestamptz not null default now()
);
create table if not exists public.food_aliases (
  id uuid primary key default gen_random_uuid(), canonical_food_id uuid not null references public.canonical_foods(id) on delete cascade,
  alias text not null, normalized_alias text not null, unique(canonical_food_id, normalized_alias)
);
create index if not exists food_aliases_normalized_alias_idx on public.food_aliases(normalized_alias);
create table if not exists public.food_variants (
  id uuid primary key default gen_random_uuid(), canonical_food_id uuid not null references public.canonical_foods(id), source text not null check(source in ('usda','kyvo')),
  source_id text not null, name_original text not null, variant_name_es text, preparation text,
  calories_100g numeric, protein_100g numeric, carbohydrates_100g numeric, fat_100g numeric, fiber_100g numeric, sugar_100g numeric, sodium_mg_100g numeric,
  serving_data jsonb not null default '[]', created_at timestamptz not null default now(), updated_at timestamptz not null default now(), unique(source, source_id)
);
create table if not exists public.commercial_products (
  id uuid primary key default gen_random_uuid(), source text not null check(source = 'open_food_facts'), source_id text not null, barcode text, brand text,
  name_es text not null, name_original text, image_url text, calories_100g numeric, protein_100g numeric, carbohydrates_100g numeric, fat_100g numeric,
  fiber_100g numeric, sugar_100g numeric, sodium_mg_100g numeric, serving_data jsonb not null default '[]', created_at timestamptz not null default now(), updated_at timestamptz not null default now(), unique(source, source_id)
);
create table if not exists public.user_food_favorites (
  user_id uuid not null references auth.users(id) on delete cascade, food_id uuid not null, food_type text not null check(food_type in ('variant','commercial')),
  created_at timestamptz not null default now(), primary key(user_id, food_id, food_type)
);
create table if not exists public.meals (
  id uuid primary key default gen_random_uuid(), user_id uuid not null references auth.users(id) on delete cascade, date date not null,
  type text not null check(type in ('breakfast','lunch','dinner','snack')), title text not null, time time, created_at timestamptz not null default now(), updated_at timestamptz not null default now()
);
create index if not exists meals_user_date_idx on public.meals(user_id, date);
create table if not exists public.meal_items (
  id uuid primary key default gen_random_uuid(), meal_id uuid not null references public.meals(id) on delete cascade,
  food_reference uuid, food_type text not null check(food_type in ('variant','commercial','custom')), name_snapshot text not null,
  quantity numeric not null check(quantity > 0), unit text not null, grams numeric,
  calories_snapshot numeric not null, protein_snapshot numeric not null, carbohydrates_snapshot numeric not null, fat_snapshot numeric not null,
  fiber_snapshot numeric, sugar_snapshot numeric, sodium_mg_snapshot numeric, created_at timestamptz not null default now(), updated_at timestamptz not null default now()
);
alter table public.canonical_foods enable row level security; alter table public.food_aliases enable row level security; alter table public.food_variants enable row level security; alter table public.commercial_products enable row level security; alter table public.user_food_favorites enable row level security; alter table public.meals enable row level security; alter table public.meal_items enable row level security;
create policy "authenticated read catalog" on public.canonical_foods for select to authenticated using (true);
create policy "authenticated read aliases" on public.food_aliases for select to authenticated using (true);
create policy "authenticated read variants" on public.food_variants for select to authenticated using (true);
create policy "authenticated read products" on public.commercial_products for select to authenticated using (true);
create policy "own favorites" on public.user_food_favorites for all to authenticated using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "own meals" on public.meals for all to authenticated using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "own meal items" on public.meal_items for all to authenticated using (exists(select 1 from public.meals m where m.id = meal_id and m.user_id = auth.uid())) with check (exists(select 1 from public.meals m where m.id = meal_id and m.user_id = auth.uid()));
