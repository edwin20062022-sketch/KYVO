create table if not exists public.saved_dishes (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null check (char_length(btrim(name)) between 1 and 80),
  portions integer not null default 1 check (portions between 1 and 99),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists saved_dishes_user_updated_idx
  on public.saved_dishes (user_id, updated_at desc);

create table if not exists public.saved_dish_items (
  id uuid primary key default gen_random_uuid(),
  saved_dish_id uuid not null references public.saved_dishes(id) on delete cascade,
  food_reference uuid not null,
  food_type text not null check (food_type in ('variant', 'commercial')),
  name_snapshot text not null,
  quantity numeric not null check (quantity > 0),
  unit text not null,
  grams numeric not null check (grams > 0),
  calories_snapshot numeric not null check (calories_snapshot >= 0),
  protein_snapshot numeric not null check (protein_snapshot >= 0),
  carbohydrates_snapshot numeric not null check (carbohydrates_snapshot >= 0),
  fat_snapshot numeric not null check (fat_snapshot >= 0),
  fiber_snapshot numeric,
  image_reference text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists saved_dish_items_dish_idx
  on public.saved_dish_items (saved_dish_id, created_at);

alter table public.saved_dishes enable row level security;
alter table public.saved_dish_items enable row level security;

create policy "own saved dishes" on public.saved_dishes
  for all to authenticated
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

create policy "own saved dish items" on public.saved_dish_items
  for all to authenticated
  using (exists(select 1 from public.saved_dishes d where d.id = saved_dish_id and d.user_id = auth.uid()))
  with check (exists(select 1 from public.saved_dishes d where d.id = saved_dish_id and d.user_id = auth.uid()));

create or replace function public.save_saved_dish(
  p_dish_id uuid,
  p_name text,
  p_portions integer,
  p_items jsonb
)
returns uuid
language plpgsql
security invoker
set search_path = public
as $$
declare
  v_user_id uuid := auth.uid();
  v_dish_id uuid;
begin
  if v_user_id is null then raise exception 'Authentication required'; end if;
  if char_length(btrim(coalesce(p_name, ''))) not between 1 and 80 then raise exception 'Invalid dish name'; end if;
  if coalesce(p_portions, 0) not between 1 and 99 then raise exception 'Invalid portions'; end if;
  if jsonb_typeof(p_items) <> 'array' or jsonb_array_length(p_items) = 0 then raise exception 'A dish needs at least one item'; end if;

  if p_dish_id is null then
    insert into public.saved_dishes(user_id, name, portions)
    values (v_user_id, btrim(p_name), p_portions)
    returning id into v_dish_id;
  else
    update public.saved_dishes
    set name = btrim(p_name), portions = p_portions, updated_at = now()
    where id = p_dish_id and user_id = v_user_id
    returning id into v_dish_id;
    if v_dish_id is null then raise exception 'Saved dish not found'; end if;
    delete from public.saved_dish_items where saved_dish_id = v_dish_id;
  end if;

  if exists(
    select 1
    from jsonb_to_recordset(p_items) as i(
      food_reference uuid, food_type text, name_snapshot text, quantity numeric, unit text, grams numeric,
      calories_snapshot numeric, protein_snapshot numeric, carbohydrates_snapshot numeric, fat_snapshot numeric,
      fiber_snapshot numeric, image_reference text
    )
    where i.food_reference is null or i.food_type not in ('variant', 'commercial') or coalesce(i.name_snapshot, '') = ''
      or coalesce(i.quantity, 0) <= 0 or coalesce(i.grams, 0) <= 0 or coalesce(i.unit, '') = ''
      or coalesce(i.calories_snapshot, -1) < 0 or coalesce(i.protein_snapshot, -1) < 0
      or coalesce(i.carbohydrates_snapshot, -1) < 0 or coalesce(i.fat_snapshot, -1) < 0
  ) then raise exception 'Invalid saved dish item'; end if;

  insert into public.saved_dish_items(
    saved_dish_id, food_reference, food_type, name_snapshot, quantity, unit, grams,
    calories_snapshot, protein_snapshot, carbohydrates_snapshot, fat_snapshot, fiber_snapshot, image_reference
  )
  select v_dish_id, i.food_reference, i.food_type, i.name_snapshot, i.quantity, i.unit, i.grams,
    i.calories_snapshot, i.protein_snapshot, i.carbohydrates_snapshot, i.fat_snapshot, i.fiber_snapshot, i.image_reference
  from jsonb_to_recordset(p_items) as i(
    food_reference uuid, food_type text, name_snapshot text, quantity numeric, unit text, grams numeric,
    calories_snapshot numeric, protein_snapshot numeric, carbohydrates_snapshot numeric, fat_snapshot numeric,
    fiber_snapshot numeric, image_reference text
  );
  return v_dish_id;
end;
$$;

create or replace function public.add_saved_dish_to_day(
  p_dish_id uuid,
  p_date date,
  p_meal_type text,
  p_portions numeric
)
returns uuid
language plpgsql
security invoker
set search_path = public
as $$
declare
  v_user_id uuid := auth.uid();
  v_meal_id uuid;
  v_title text;
  v_recipe_portions integer;
  v_scale numeric;
begin
  if v_user_id is null then raise exception 'Authentication required'; end if;
  if p_date is null then raise exception 'A date is required'; end if;
  if p_meal_type not in ('breakfast', 'lunch', 'dinner', 'snack') then raise exception 'Invalid meal type'; end if;
  if coalesce(p_portions, 0) <= 0 or p_portions > 99 then raise exception 'Invalid portions'; end if;
  select name, portions into v_title, v_recipe_portions
  from public.saved_dishes where id = p_dish_id and user_id = v_user_id;
  if v_title is null then raise exception 'Saved dish not found'; end if;
  v_scale := p_portions / v_recipe_portions;
  insert into public.meals(user_id, date, type, title, time)
  values (v_user_id, p_date, p_meal_type, v_title, localtime)
  returning id into v_meal_id;
  insert into public.meal_items(
    meal_id, food_reference, food_type, name_snapshot, quantity, unit, grams,
    calories_snapshot, protein_snapshot, carbohydrates_snapshot, fat_snapshot, fiber_snapshot
  )
  select v_meal_id, food_reference, food_type, name_snapshot, quantity * v_scale, unit, grams * v_scale,
    calories_snapshot * v_scale, protein_snapshot * v_scale, carbohydrates_snapshot * v_scale,
    fat_snapshot * v_scale, fiber_snapshot * v_scale
  from public.saved_dish_items where saved_dish_id = p_dish_id;
  return v_meal_id;
end;
$$;

revoke all on function public.save_saved_dish(uuid, text, integer, jsonb) from public, anon, service_role;
grant execute on function public.save_saved_dish(uuid, text, integer, jsonb) to authenticated;
revoke all on function public.add_saved_dish_to_day(uuid, date, text, numeric) from public, anon, service_role;
grant execute on function public.add_saved_dish_to_day(uuid, date, text, numeric) to authenticated;
