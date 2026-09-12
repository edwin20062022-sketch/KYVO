insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('avatars', 'avatars', true, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do nothing;

-- Upload: each authenticated user can write only to their own folder
create policy "Avatar upload ownership"
on storage.objects
for insert to authenticated
with check (
    bucket_id = 'avatars'
    and (string_to_array(name, '/'))[1] = auth.uid()::text
);

-- Update: each authenticated user can update only their own avatar
create policy "Avatar update ownership"
on storage.objects
for update to authenticated
using (
    bucket_id = 'avatars'
    and (string_to_array(name, '/'))[1] = auth.uid()::text
)
with check (
    bucket_id = 'avatars'
    and (string_to_array(name, '/'))[1] = auth.uid()::text
);

-- Delete: each authenticated user can delete only their own avatar
create policy "Avatar delete ownership"
on storage.objects
for delete to authenticated
using (
    bucket_id = 'avatars'
    and (string_to_array(name, '/'))[1] = auth.uid()::text
);

-- Read: public bucket, anyone can read
create policy "Avatar public read"
on storage.objects
for select to public
using (bucket_id = 'avatars');
