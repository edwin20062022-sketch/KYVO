create or replace function public.get_frequent_foods(limit_count integer default 50)
returns table(food_reference uuid, food_type text, usage_count bigint, last_used_at timestamptz)
language sql
stable
security invoker
set search_path = public
as $$
  select
    mi.food_reference,
    mi.food_type,
    count(*)::bigint as usage_count,
    max((m.date + coalesce(m.time, time '00:00:00')) at time zone 'UTC') as last_used_at
  from public.meal_items mi
  join public.meals m on m.id = mi.meal_id
  where m.user_id = auth.uid()
    and mi.food_reference is not null
    and mi.food_type in ('variant', 'commercial')
  group by mi.food_reference, mi.food_type
  order by count(*) desc, max((m.date + coalesce(m.time, time '00:00:00')) at time zone 'UTC') desc, mi.food_reference
  limit least(greatest(coalesce(limit_count, 50), 1), 100);
$$;

revoke all on function public.get_frequent_foods(integer) from public;
grant execute on function public.get_frequent_foods(integer) to authenticated;
